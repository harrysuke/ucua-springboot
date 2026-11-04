# RAG Intelligence Layer — Proposal for `ucua-springboot`

> Audience: maintainers of the UCUA ("U-SEE U-ACT") Spring Boot backend.
> Goal: add a Retrieval-Augmented Generation (RAG) capability that turns the
> existing HSE data into an AI assistant without breaking the current
> embedded, on-prem-friendly architecture.

---

## 1. What the project already gives us (RAG corpus)

| Asset | Table / Field | RAG role |
|-------|---------------|----------|
| Offence catalogue | `offence` (`OffenceCode`, `Category`, `offKeyword`, `Offence`) | **Static knowledge base** — ideal for "which offence fits this situation?" |
| Report narratives | `ucuaform.conditionDetails`, `actionTaken`, `followUp`, `observerConditionDetails`, `violator*` | **Unstructured corpus** for similarity + Q&A |
| Review remarks | `mobile_report_history.remarks` | Past decisions / rejection reasons |
| Master data | `company`, `dept`, `location` | Filtering, routing, grounding |
| Workflow states | `ReportService` status constants | "What happens next?" knowledge |

The single biggest win: today search is `LIKE '%term%'` only. A supervisor typing
*"worker on scaffold without harness"* will never match an offence stored as
*"Working at height without fall protection"*. **Embeddings fix exactly this.**

---

## 2. Recommended target use cases (priority order)

1. **Offence auto-suggestion (highest value, lowest risk).** Given the free-text
   `conditionDetails`, return the top-N matching `OffenceCode`s with confidence.
   This directly reduces mis-classified UA reports at submission time.
2. **"Similar past reports"** panel on the report detail screen.
3. **RAG Q&A assistant** grounded in the offence catalogue + past reports
   (e.g. *"What's the typical fine for UA-012?"*, *"Explain the UA approval flow"*).
4. **Natural-language dashboard/analytics** (*"open UA reports at location X this month"*).
5. **Auto-draft** of `conditionDetails` / `actionTaken` from a short observer note.
6. *(Optional, later)* **Photo evidence understanding** via a multimodal model.

---

## 3. Architecture

```
 Mobile App ──HTTPS/JWT──> /api/ai/**  (Spring MVC controllers, same JWT filter)
                              │
                              ▼
                   ┌──────────────────────┐
                   │   AiController        │  (suggest / similar / ask / draft / reindex)
                   └──────────┬───────────┘
                              ▼
                   ┌──────────────────────┐
                   │   RagService          │  retrieve(query, filters) + generate(prompt, ctx)
                   │   ReportAssistService│  (offence suggestion, drafting, NL analytics)
                   └───┬──────────────┬───┘
                       │              │
            ┌──────────▼───┐    ┌─────▼──────────┐
            │ EmbeddingClient│   │  LlmClient     │  (abstraction; Ollama default, OpenAI/Azure pluggable)
            └──────────┬───┘    └─────┬──────────┘
                       │              │
                       ▼              ▼
              ┌────────────────────────────┐
              │   VectorStore (port)        │  Phase 1: SQLite + float[] + cosine in Kotlin
              │   (impl: JpaVectorStore)   │  Phase 2: Qdrant / pgvector (Docker service)
              └────────────────────────────┘

 Ingestion (async, no request blocking):
   • Offence catalogue  → embedded on change / nightly cron
   • New/updated report → @TransactionalEvent / @Async after save in ReportService
   • Historical backfill → POST /api/ai/reindex (admin, accessLevel >= 3)
```

Design principle: **all AI access goes through two interfaces** — `EmbeddingClient`
and `LlmClient`. The rest of the app never talks to a vendor SDK directly, so you
can switch from local Ollama to OpenAI/Azure by changing `application.yml` only.

---

## 4. Technology recommendation (data-sovereignty first)

This is HSE / manpower data — normally sensitive and often subject to on-prem
or in-country hosting rules. **Default to a local model so no report text or
embeddings ever leave the server.**

| Concern | Recommendation |
|---------|----------------|
| Embeddings | **Ollama `nomic-embed-text`** (or `bge-m3`) locally; OpenAI `text-embedding-3-small` as fallback |
| LLM (chat) | **Ollama `llama3.1:8b` / `qwen2.5`** locally; Azure OpenAI as enterprise fallback |
| Vector store | **Phase 1:** SQLite table storing `float[]` vectors, cosine similarity computed in Kotlin (zero new infra, matches the embedded-SQLite philosophy). **Phase 2:** add `qdrant` (or `pgvector`) as a Docker Compose service when volume grows |
| Orchestration | Use **Spring AI** (`spring-ai-…`) for the chat/embedding/vector-store abstractions if you want batteries included. If you prefer to stay minimal, hand-roll the two client interfaces above with `RestClient` (already auto-configured by `spring-boot-starter-web` on Boot 3.4 — **no new dependency** for HTTP calls to Ollama) |

> If you adopt Spring AI: it already ships `OllamaChatModel`, `OllamaEmbeddingModel`,
> and vector-store bindings for pgvector/Qdrant/Redis/Chroma — this collapses most of
> the boilerplate in section 6.

---

## 5. New package layout (fits `com.ucua.ucua`)

```
src/main/kotlin/com/ucua/ucua/ai/
  config/
    AiConfig.kt            # beans: EmbeddingClient, LlmClient, VectorStore, RestClient wiring
  client/
    EmbeddingClient.kt     # interface
    OllamaEmbeddingClient.kt
    LlmClient.kt           # interface
    OllamaLlmClient.kt
  store/
    VectorStore.kt         # interface (upsert, query)
    JpaVectorStore.kt      # Phase-1 SQLite impl
    EmbeddingEntity.kt     # new @Entity for vectors (id, sourceType, sourceId, chunk, vector)
  service/
    RagService.kt          # retrieve() + generate() with citation grounding
    ReportAssistService.kt # suggestOffence(), findSimilar(), draftText(), nlAnalytics()
  controller/
    AiController.kt        # /api/ai/**
  dto/
    AiDtos.kt              # request/response data classes
  ingestion/
    CorpusIndexer.kt       # offence + report ingestion, @Async + cron
```

---

## 6. Integration points with existing code

**a) Secure the endpoints** — `/api/ai/**` is already covered by
`SecurityConfig.anyRequest().authenticated()` (the JWT filter runs before everything),
so no security change is needed. Gate the `reindex` action with `accessLevel >= 3`
(consistent with `ReportService.isAdmin`).

**b) Index new reports without blocking the request** — extend `ReportService`
(save points already exist at lines ~156, ~217, ~317). Emit a Spring event or call
an `@Async` method so embedding happens off the request thread:

```kotlin
// in ReportService.createReport / updateReport, after ucuaFormRepository.save(saved):
applicationEventPublisher.publishEvent(ReportIndexedEvent(saved.id!!))
```
```kotlin
@Async
@TransactionalEventListener
fun onReportSaved(e: ReportIndexedEvent) = corpusIndexer.indexReport(e.reportId)
```

**c) Offence catalogue indexing** — `Offence` rows rarely change; re-index on save
or via a `@Scheduled(cron = "0 0 3 * * *")` nightly job. This becomes the assistant's
"rule book".

**d) Streaming chat** — for the `ask` endpoint, return `SseEmitter` with
`MediaType.TEXT_EVENT_STREAM_VALUE`. `SseEmitter` works on the existing Tomcat/WAR
runtime with **no extra dependency**; token chunks are pushed as they arrive from
the LLM.

**e) Exclude deleted data** — every ingestion query must filter `isDelete = 0`
(exactly like the existing repositories) so embeddings never include soft-deleted
records.

---

## 7. `build.gradle.kts` additions

```kotlin
// If using Spring AI (recommended for faster delivery):
implementation("org.springframework.ai:spring-ai-ollama-spring-boot-starter:1.0.0")
implementation("org.springframework.ai:spring-ai-pgvector-store-spring-boot-starter:1.0.0") // Phase 2

// Minimal hand-rolled path (no Spring AI): restClient already available;
// only add if you want a typed vector math helper:
implementation("org.kotlinx:kotlinx-coroutines-core:1.9.0") // optional, for @Async-free async
```

## 8. `application.yml` additions

```yaml
ai:
  enabled: true
  provider: ollama            # ollama | openai | azure
  ollama:
    base-url: ${OLLAMA_URL:http://localhost:11434}
    embedding-model: nomic-embed-text
    chat-model: llama3.1:8b
  openai:                     # used only when provider=openai/azure
    api-key: ${OPENAI_API_KEY:}
    embedding-model: text-embedding-3-small
    chat-model: gpt-4o-mini
  rag:
    top-k: 5
    similarity-threshold: 0.78
    max-context-chars: 4000
  vector-store: sqlite        # sqlite | qdrant | pgvector
```

---

## 9. Minimal code skeletons (actionable)

```kotlin
// client/EmbeddingClient.kt
interface EmbeddingClient {
    fun embed(text: String): FloatArray
    fun embedBatch(texts: List<String>): List<FloatArray>
}

// store/VectorStore.kt
interface VectorStore {
    fun upsert(id: String, sourceType: String, sourceId: Int, chunk: String, vector: FloatArray)
    fun query(vector: FloatArray, topK: Int, sourceType: String? = null): List<ScoredChunk>
    data class ScoredChunk(val id: String, val sourceType: String, val sourceId: Int,
                           val chunk: String, val score: Float)
}

// service/RagService.kt
@Service
class RagService(private val embed: EmbeddingClient, private val store: VectorStore,
                 private val llm: LlmClient, @Value("\${ai.rag.top-k:5}") private val topK: Int) {

    fun answer(question: String, filterSource: String? = null): RagAnswer {
        val ctx = store.query(embed.embed(question), topK, filterSource)
        val prompt = buildPrompt(question, ctx)          // include citations to sourceId
        val reply = llm.generate(prompt, stream = false)
        return RagAnswer(answer = reply, citations = ctx.map { it.sourceId })
    }
}

// controller/AiController.kt
@RestController
@RequestMapping("/api/ai")
class AiController(private val assist: ReportAssistService) {
    @PostMapping("/suggest-offence")
    fun suggest(@AuthenticationPrincipal user: User, @RequestBody r: SuggestRequest) =
        assist.suggestOffence(r.conditionDetails)

    @PostMapping("/similar")
    fun similar(@AuthenticationPrincipal user: User, @RequestBody r: SimilarRequest) =
        assist.findSimilar(r.reportId, r.limit)

    @PostMapping(value = ["/ask"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun ask(@AuthenticationPrincipal user: User, @RequestBody r: AskRequest): SseEmitter =
        assist.streamAnswer(r.question)
}
```

---

## 10. Phased roadmap

- **Phase 0 — Spike (3–5 days):** Run Ollama locally, embed the `offence` table
  into a SQLite vector table, build `suggest-offence`. Demo to HSE officers.
- **Phase 1 — MVP:** `suggest-offence` + `similar-reports` + async ingestion on
  report save. Ship behind a feature flag (`ai.enabled`).
- **Phase 2 — Assistant:** `ask` (RAG Q&A with citations) + `draft`; add Qdrant or
  pgvector if the corpus outgrows SQLite.
- **Phase 3 — Hardening:** PII redaction before embedding, evaluation harness
  (golden-question accuracy), audit logging into the existing `history` table,
  multimodal photo understanding.

---

## 11. Risks & guardrails

- **Hallucination:** Always ground the LLM with retrieved chunks and return
  `citations` (source IDs) so a user can verify. Never let the model invent an
  `OffenceCode` that isn't in the catalogue — constrain suggestion output to the
  retrieved set.
- **Data privacy:** Default provider is **local Ollama**; report text and embeddings
  stay on the server. Only opt into OpenAI/Azure via config, and consider redacting
  `violator*` PII before embedding.
- **Latency:** Embedding + retrieve + generate can be 1–3 s locally. Use streaming
  for chat and cache offence embeddings (they're static).
- **Soft-deletes:** Filter `isDelete = 0` everywhere; add a reindex job so deleted
  records are purged from the vector store.
- **Auditability:** Log every AI call into the existing `history` table
  (reuse `ReportService.logHistory`) for compliance traceability.
- **Graded rollout:** Gate behind `ai.enabled` and `accessLevel` so you can pilot
  with HSE admins before opening to all mobile users.

---

## 12. Why this fits the current codebase

- No new mandatory infrastructure in Phase 1 (reuses SQLite + Docker volumes).
- Follows existing patterns: `@AuthenticationPrincipal User`, `data class` DTOs,
  `@Service` + `@Repository`, `application.yml` config, `history` audit logging.
- Keeps HSE data on-prem by default (local Ollama), satisfying typical
  manpower/safety data-residency requirements.
- Every endpoint is JWT-protected for free via the existing security filter chain.
```
