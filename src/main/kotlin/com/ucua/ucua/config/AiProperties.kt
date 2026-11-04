package com.ucua.ucua.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ai")
data class AiProperties(
    var enabled: Boolean = true,
    var provider: String = "ollama",
    var ollama: Ollama = Ollama(),
    var rag: Rag = Rag(),
    var corpus: Corpus = Corpus(),
    var ocr: Ocr = Ocr()
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

    /** File-based documentation ingested into the vector store by DocumentIndexer. */
    data class Corpus(
        var dir: String = "./rag-docs",
        var chunkSize: Int = 1200,
        var chunkOverlap: Int = 200,
        var maxFileSizeBytes: Long = 20_000_000
    )

    /**
     * OCR fallback for PDFs whose text layer is empty (scanned documents).
     * Requires Tesseract native libraries (bundled with Tess4J) and `tessdata`
     * language files (e.g. `eng.traineddata`) in [tessdataPath].
     */
    data class Ocr(
        var enabled: Boolean = true,
        var tessdataPath: String = "./tessdata",
        var language: String = "eng",
        var dpi: Int = 300,
        var minCharsPerPage: Int = 40
    )
}
