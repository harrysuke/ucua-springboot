package com.ucua.ucua.ai.client

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.client.RestClient

data class OllamaEmbedRequest(val model: String, val prompt: String)
data class OllamaEmbedResponse(@JsonProperty("embedding") val embedding: List<Double> = emptyList())

data class OllamaEmbedBatchRequest(val model: String, val input: List<String>)
data class OllamaEmbedBatchResponse(@JsonProperty("embeddings") val embeddings: List<List<Double>> = emptyList())

/**
 * Talks to a local/remote Ollama instance. Created by [com.ucua.ucua.config.AiConfig];
 * no network call happens at construction, so the app still boots if Ollama is down.
 */
class OllamaEmbeddingClient(
    builder: RestClient.Builder,
    baseUrl: String,
    private val model: String
) : EmbeddingClient {

    private val client = builder.baseUrl(baseUrl).build()

    override fun embed(text: String): FloatArray {
        val resp = client.post()
            .uri("/api/embeddings")
            .body(OllamaEmbedRequest(model, text))
            .retrieve()
            .body(OllamaEmbedResponse::class.java)
        return resp?.embedding?.map { it.toFloat() }?.toFloatArray() ?: FloatArray(0)
    }

    /**
     * Uses Ollama's batch endpoint (`/api/embed`) in sub-batches to avoid one HTTP
     * round-trip per chunk. Falls back to single embeddings if the endpoint is
     * unavailable (older Ollama) or returns an unexpected shape.
     */
    override fun embedBatch(texts: List<String>): List<FloatArray> {
        if (texts.isEmpty()) return emptyList()
        return try {
            texts.chunked(BATCH_SIZE).flatMap { batch ->
                val resp = client.post()
                    .uri("/api/embed")
                    .body(OllamaEmbedBatchRequest(model, batch))
                    .retrieve()
                    .body(OllamaEmbedBatchResponse::class.java)
                val embeddings = resp?.embeddings ?: emptyList()
                if (embeddings.size == batch.size) {
                    embeddings.map { it.map(Double::toFloat).toFloatArray() }
                } else {
                    batch.map { embed(it) }
                }
            }
        } catch (_: Exception) {
            texts.map { embed(it) }
        }
    }

    private companion object {
        const val BATCH_SIZE = 32
    }
}
