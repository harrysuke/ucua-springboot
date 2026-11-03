package com.ucua.ucua.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "offence")
data class Offence(
    @Id
    @Column(name = "OffenceCode", length = 5, nullable = false)
    val offenceCode: String = "",

    @Column(name = "companyCode", length = 3, nullable = false)
    val companyCode: String = "",

    @Column(name = "branchCode", length = 3, nullable = false)
    val branchCode: String = "",

    @Column(name = "Category", length = 10, nullable = false)
    val category: String = "",

    @Column(name = "offKeyword", nullable = false, columnDefinition = "TEXT")
    val offKeyword: String = "",

    @Column(name = "Offence", nullable = false, columnDefinition = "TEXT")
    val offence: String = "",

    @Column(name = "createby", length = 20, nullable = false)
    val createBy: String = "",

    @Column(name = "createdate", nullable = false)
    val createDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updateby", length = 20, nullable = false)
    val updateBy: String = "",

    @Column(name = "updatedate", nullable = false)
    val updateDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "isdelete", nullable = false)
    val isDelete: Int = 0,

    @Column(name = "deleteby", length = 20, nullable = false)
    val deleteBy: String = "",

    @Column(name = "deletedate", nullable = false)
    val deleteDate: LocalDateTime = LocalDateTime.now()
)
