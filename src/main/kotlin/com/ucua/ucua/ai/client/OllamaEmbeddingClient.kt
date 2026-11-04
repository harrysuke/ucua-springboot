package com.ucua.ucua.ai.client

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.client.RestClient

data class OllamaEmbedRequest(val model: String, val prompt: String)
data class OllamaEmbedResponse(@JsonProperty("embedding") val embedding: List<Double> = emptyList())

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
}
