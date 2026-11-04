package com.ucua.ucua.ai.store

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface EmbeddingRepository : JpaRepository<EmbeddingEntity, Long> {
    fun findBySourceType(sourceType: String): List<EmbeddingEntity>
    fun findBySourceTypeAndSourceRef(sourceType: String, sourceRef: String): List<EmbeddingEntity>

    @Transactional
    fun deleteBySourceTypeAndSourceRef(sourceType: String, sourceRef: String)

    /** Bulk delete of every embedding of a source type (used to rebuild the DOC corpus). */
    @Modifying
    @Transactional
    @Query("delete from EmbeddingEntity e where e.sourceType = :sourceType")
    fun deleteBySourceType(sourceType: String)

    fun countBySourceType(sourceType: String): Long
}
