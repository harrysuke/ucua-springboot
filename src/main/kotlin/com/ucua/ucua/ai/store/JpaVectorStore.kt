package com.ucua.ucua.ai.store

import kotlin.math.sqrt
import org.springframework.stereotype.Component

/**
 * Phase-1 vector store: embeddings are stored as comma-separated floats in SQLite
 * (matching the project's embedded, no-server philosophy). Cosine similarity is
 * computed in Kotlin. Swap for Qdrant / pgvector (Phase 2) by providing another
 * [VectorStore] implementation.
 */
@Component
class JpaVectorStore(private val repo: EmbeddingRepository) : VectorStore {

    override fun upsert(
        sourceType: String,
        sourceId: Int,
        sourceRef: String,
        chunk: String,
        vector: FloatArray,
        model: String
    ) {
        repo.deleteBySourceTypeAndSourceRef(sourceType, sourceRef)
        repo.save(
            EmbeddingEntity(
                sourceType = sourceType,
                sourceId = sourceId,
                sourceRef = sourceRef,
                chunkText = chunk,
                model = model,
                embedding = vector.joinToString(",") { "%.6f".format(it) }
            )
        )
    }

    override fun delete(sourceType: String, sourceRef: String) {
        repo.deleteBySourceTypeAndSourceRef(sourceType, sourceRef)
    }

    override fun query(vector: FloatArray, topK: Int, sourceType: String?): List<ScoredChunk> {
        val rows = if (sourceType != null) repo.findBySourceType(sourceType) else repo.findAll()
        val scored = rows.mapNotNull { e ->
            val vec = e.embedding.split(",").mapNotNull { it.toFloatOrNull() }.toFloatArray()
            if (vec.isEmpty() || vec.size != vector.size) null
            else ScoredChunk(e.id ?: 0L, e.sourceType, e.sourceId, e.sourceRef, e.chunkText, cosine(vector, vec))
        }
        return scored.sortedByDescending(ScoredChunk::score).take(topK)
    }

    override fun count(sourceType: String?): Long =
        if (sourceType != null) repo.countBySourceType(sourceType) else repo.count()

    private fun cosine(a: FloatArray, b: FloatArray): Float {
        if (a.isEmpty()) return 0f
        var dot = 0f
        var na = 0f
        var nb = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
            na += a[i] * a[i]
            nb += b[i] * b[i]
        }
        val denom = sqrt(na.toDouble()) * sqrt(nb.toDouble())
        return if (denom == 0.0) 0f else (dot / denom).toFloat()
    }
}
