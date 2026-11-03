package com.ucua.ucua.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "mobile_tokens")
data class MobileToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int? = null,

    @Column(name = "token", length = 255, nullable = false, unique = true)
    val token: String = "",

    @Column(name = "staffId", length = 50, nullable = false)
    val staffId: String = "",

    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
