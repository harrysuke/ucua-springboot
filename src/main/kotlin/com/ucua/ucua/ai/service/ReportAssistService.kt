package com.ucua.ucua.ai.service

import com.ucua.ucua.ai.client.EmbeddingClient
import com.ucua.ucua.ai.dto.*
import com.ucua.ucua.ai.ingestion.CorpusIndexer
import com.ucua.ucua.config.AiProperties
import com.ucua.ucua.entity.UcuaForm
import com.ucua.ucua.repository.OffenceRepository
import com.ucua.ucua.repository.UcuaFormRepository
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.Executors

@Service
class ReportAssistService(
    private val rag: RagService,
    private val embeddingClient: EmbeddingClient,
    private val offenceRepository: OffenceRepository,
    private val ucuaFormRepository: UcuaFormRepository,
    private val corpusIndexer: CorpusIndexer,
    private val props: AiProperties
) {
    private val executor = Executors.newCachedThreadPool()

    /** UC-1: given a description, return the top-N matching offence codes. */
    fun suggestOffence(text: String): SuggestResponse {
        if (!props.enabled) return SuggestResponse(disabled = true, message = "AI layer is disabled")
        if (text.isBlank()) return SuggestResponse(message = "No description provided")
        return try {
            val hits = rag.query(embeddingClient.embed(text), "OFFENCE")
            val suggestions = hits.mapNotNull { h ->
                val off = offenceRepository.findById(h.sourceRef).orElse(null) ?: return@mapNotNull null
                OffenceSuggestion(off.offenceCode, off.category, off.offKeyword, off.offence, h.score)
            }
            SuggestResponse(suggestions = suggestions)
        } catch (e: Exception) {
            SuggestResponse(message = "Offence suggestion unavailable: ${e.message}")
        }
    }

    /** UC-2: find semantically similar past reports. */
    fun findSimilar(reportId: Int, limit: Int): SimilarResponse {
        if (!props.enabled) return SimilarResponse(disabled = true, message = "AI layer is disabled")
        val form = ucuaFormRepository.findById(reportId).orElse(null)
            ?: return SimilarResponse(message = "Report not found")
        return try {
            val vec = embeddingClient.embed(buildReportText(form))
            val hits = rag.query(vec, "REPORT")
                .filter { it.sourceRef != reportId.toString() }
                .take(if (limit < 1) 5 else limit)
            val results = hits.mapNotNull { h ->
                val id = h.sourceRef.toIntOrNull() ?: return@mapNotNull null
                val f = ucuaFormRepository.findById(id).orElse(null) ?: return@mapNotNull null
                SimilarReport(id, f.formType, (f.conditionDetails ?: "").take(160), h.score)
            }
            SimilarResponse(results = results)
        } catch (e: Exception) {
            SimilarResponse(message = "Similarity search unavailable: ${e.message}")
        }
    }

    /** UC-3: RAG Q&A assistant, streamed as Server-Sent Events. */
    fun streamAnswer(question: String, sourceType: String?): SseEmitter {
        val emitter = SseEmitter(120_000L)
        if (!props.enabled) {
            emitter.send(SseEmitter.event().data("AI layer is disabled."))
            emitter.complete()
            return emitter
        }
        executor.submit {
            try {
                val (answer, citations) = rag.answer(question, sourceType)
                emitter.send(SseEmitter.event().name("citations").data("Sources: ${citations.joinToString(", ")}"))
                answer.split(Regex("(?<=[.!?])\\s+")).forEach { seg ->
                    if (seg.isNotBlank()) {
                        emitter.send(SseEmitter.event().data(seg))
                        Thread.sleep(35)
                    }
                }
                emitter.complete()
            } catch (e: Exception) {
                try {
                    emitter.send(SseEmitter.event().data("Assistant error: ${e.message}"))
                } catch (_: Exception) {
                }
                emitter.completeWithError(e)
            }
        }
        return emitter
    }

    /** Admin: rebuild the entire vector corpus. */
    fun reindexAll(): ReindexResponse {
        return try {
            val o = corpusIndexer.indexAllOffences()
            val r = corpusIndexer.indexAllReports()
            ReindexResponse(indexedOffences = o, indexedReports = r, message = "Reindex complete")
        } catch (e: Exception) {
            ReindexResponse(message = "Reindex failed: ${e.message}")
        }
    }

    private fun buildReportText(f: UcuaForm): String = listOf(
        f.formType, f.conditionDetails, f.actionTaken, f.followUp,
        f.observerConditionDetails, f.violatorName, f.violatorCompany, f.violatorDept
    ).filter { !it.isNullOrBlank() }.joinToString(" | ")
}
