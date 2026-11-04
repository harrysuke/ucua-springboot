package com.ucua.ucua.ai.store

data class ScoredChunk(
    val id: Long,
    val sourceType: String,
    val sourceId: Int,
    val sourceRef: String,
    val chunk: String,
    val score: Float
)

interface VectorStore {
    fun upsert(sourceType: String, sourceId: Int, sourceRef: String, chunk: String, vector: FloatArray, model: String)
    fun delete(sourceType: String, sourceRef: String)

    /** Remove every embedding of a given [sourceType] (used to rebuild the corpus). */
    fun deleteByType(sourceType: String)
    fun query(vector: FloatArray, topK: Int, sourceType: String? = null): List<ScoredChunk>
    fun count(sourceType: String? = null): Long
}
