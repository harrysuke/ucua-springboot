package com.ucua.ucua.ai.store

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EmbeddingRepository : JpaRepository<EmbeddingEntity, Long> {
    fun findBySourceType(sourceType: String): List<EmbeddingEntity>
    fun findBySourceTypeAndSourceRef(sourceType: String, sourceRef: String): List<EmbeddingEntity>
    fun deleteBySourceTypeAndSourceRef(sourceType: String, sourceRef: String)
    fun countBySourceType(sourceType: String): Long
}
