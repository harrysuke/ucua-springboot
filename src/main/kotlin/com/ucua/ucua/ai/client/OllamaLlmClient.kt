package com.ucua.ucua.ai.client

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.client.RestClient

data class OllamaMsg(val role: String, val content: String)
data class OllamaChatRequest(
    val model: String,
    val messages: List<OllamaMsg>,
    val stream: Boolean = false
)
data class OllamaChatResponse(@JsonProperty("message") val message: OllamaMsg? = null)

/**
 * Chat completions against Ollama. Non-streaming for the spike; the controller
 * chunks the returned text into an SSE stream for a streaming feel.
 */
class OllamaLlmClient(
    builder: RestClient.Builder,
    baseUrl: String,
    private val model: String
) : LlmClient {

    private val client = builder.baseUrl(baseUrl).build()

    override fun generate(system: String, user: String): String {
        val resp = client.post()
            .uri("/api/chat")
            .body(OllamaChatRequest(model, listOf(OllamaMsg("system", system), OllamaMsg("user", user)), false))
            .retrieve()
            .body(OllamaChatResponse::class.java)
        return resp?.message?.content ?: ""
    }
}
