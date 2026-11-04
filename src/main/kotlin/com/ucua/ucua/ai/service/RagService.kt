package com.ucua.ucua.ai.service

import com.ucua.ucua.ai.client.EmbeddingClient
import com.ucua.ucua.ai.client.LlmClient
import com.ucua.ucua.ai.store.ScoredChunk
import com.ucua.ucua.ai.store.VectorStore
import com.ucua.ucua.config.AiProperties
import org.springframework.stereotype.Service

@Service
class RagService(
    private val embeddingClient: EmbeddingClient,
    private val vectorStore: VectorStore,
    private val llmClient: LlmClient,
    private val props: AiProperties
) {
    fun query(vector: FloatArray, sourceType: String? = null): List<ScoredChunk> =
        vectorStore.query(vector, props.rag.topK, sourceType)

    fun generate(system: String, user: String): String = llmClient.generate(system, user)

    /**
     * Retrieve relevant chunks for [question], build a grounded prompt, and call the LLM.
     * Returns the answer plus the source references used (for citation display).
     */
    fun answer(question: String, sourceType: String? = null): Pair<String, List<String>> {
        val hits = vectorStore.query(embeddingClient.embed(question), props.rag.topK, sourceType)
        val context = hits.joinToString("\n---\n") { "[${it.sourceType}:${it.sourceRef}] ${it.chunk}" }
        val system = """
            You are the UCUA HSE (Health, Safety & Environment) assistant for the "U-SEE U-ACT" programme.
            Answer the user's question using ONLY the context below. Do not use outside knowledge.
            If the context does not contain the answer, say you don't have that information.
            When you use a fact, cite its source reference exactly as shown (e.g. OFFENCE:UA012).
            Keep the answer concise and factual.

            CONTEXT:
            $context
        """.trimIndent()
        val answer = llmClient.generate(system, question)
        return answer to hits.map { "${it.sourceType}:${it.sourceRef}" }
    }
}
