package com.ucua.ucua.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "history")
data class History(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int? = null,

    @Column(name = "companyCode", length = 3, nullable = false)
    val companyCode: String = "",

    @Column(name = "branchCode", length = 3, nullable = false)
    val branchCode: String = "",

    @Column(name = "ucuaid", nullable = false)
    val ucuaId: Int = 0,

    @Column(name = "staffid", length = 30, nullable = false)
    val staffId: String = "",

    @Column(name = "accessLevel", nullable = false)
    val accessLevel: Int = 0,

    @Column(name = "action", length = 20, nullable = false)
    val action: String = "",

    @Column(name = "activity", nullable = false, columnDefinition = "TEXT")
    val activity: String = "",

    @Column(name = "status", length = 50, nullable = false)
    val status: String = "",

    @Column(name = "sql", nullable = false, columnDefinition = "TEXT")
    val sql: String = "",

    @Column(name = "ipaddress", length = 20)
    val ipAddress: String? = null,

    @Column(name = "useragent", columnDefinition = "TEXT")
    val userAgent: String? = null,

    @Column(name = "datetimestamp", nullable = false)
    val dateTimestamp: LocalDateTime = LocalDateTime.now()
)
