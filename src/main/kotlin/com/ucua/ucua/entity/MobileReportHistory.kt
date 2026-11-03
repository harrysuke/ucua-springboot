package com.ucua.ucua.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "mobile_report_history")
data class MobileReportHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int? = null,

    @Column(name = "report_id", nullable = false)
    val reportId: Int = 0,

    @Column(name = "staffId", length = 50, nullable = false)
    val staffId: String = "",

    @Column(name = "action", length = 30, nullable = false)
    val action: String = "",

    @Column(name = "remarks", columnDefinition = "TEXT")
    val remarks: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
