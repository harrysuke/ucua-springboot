package com.ucua.ucua.ai.store

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "ai_embedding")
data class EmbeddingEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") val id: Long? = null,

    @Column(name = "source_type", length = 20, nullable = false)
    val sourceType: String = "",

    @Column(name = "source_id", nullable = false)
    val sourceId: Int = 0,

    @Column(name = "source_ref", length = 80, nullable = false)
    val sourceRef: String = "",

    @Column(name = "chunk_text", columnDefinition = "TEXT", nullable = false)
    val chunkText: String = "",

    @Column(name = "model", length = 50, nullable = false)
    val model: String = "",

    @Column(name = "embedding", columnDefinition = "TEXT", nullable = false)
    val embedding: String = "",

    @Column(name = "create_date", nullable = false)
    val createDate: LocalDateTime = LocalDateTime.now()
)
