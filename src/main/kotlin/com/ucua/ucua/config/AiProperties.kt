package com.ucua.ucua.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ai")
data class AiProperties(
    var enabled: Boolean = true,
    var provider: String = "ollama",
    var ollama: Ollama = Ollama(),
    var rag: Rag = Rag()
) {
    data class Ollama(
        var baseUrl: String = "http://localhost:11434",
        var embeddingModel: String = "nomic-embed-text",
        var chatModel: String = "llama3.1:8b"
    )

    data class Rag(
        var topK: Int = 5,
        var similarityThreshold: Double = 0.0,
        var maxContextChars: Int = 4000
    )
}
