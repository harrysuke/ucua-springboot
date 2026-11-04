package com.ucua.ucua.ai.client

interface EmbeddingClient {
    fun embed(text: String): FloatArray
    fun embedBatch(texts: List<String>): List<FloatArray> = texts.map { embed(it) }
}
