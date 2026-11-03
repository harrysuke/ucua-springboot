package com.ucua.ucua.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "companies")
data class Company(
    @Id
    @Column(name = "id", nullable = false)
    val id: Int = 0,

    @Column(name = "companyCode", length = 3, nullable = false)
    val companyCode: String = "",

    @Column(name = "branchCode", length = 3, nullable = false)
    val branchCode: String = "",

    @Column(name = "companyName", length = 100, nullable = false)
    val companyName: String = "",

    @Column(name = "companyRegNo", length = 15, nullable = false)
    val companyRegNo: String = "",

    @Column(name = "groupType", length = 20, nullable = false)
    val groupType: String = "",

    @Column(name = "description", length = 255, nullable = false)
    val description: String = "",

    @Column(name = "deptInCharge", length = 20, nullable = false)
    val deptInCharge: String = "",

    @Column(name = "sectInCharge", length = 20, nullable = false)
    val sectInCharge: String = "",

    @Column(name = "createBy", length = 50, nullable = false)
    val createBy: String = "",

    @Column(name = "createDate", nullable = false)
    val createDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "editBy", length = 50)
    val editBy: String? = null,

    @Column(name = "editDate")
    val editDate: LocalDateTime? = null,

    @Column(name = "isApproved", nullable = false)
    val isApproved: Int = 0,

    @Column(name = "approvedBy", length = 10)
    val approvedBy: String? = null,

    @Column(name = "approvedDate")
    val approvedDate: LocalDateTime? = null,

    @Column(name = "isDeleted", nullable = false)
    val isDeleted: Int = 0,

    @Column(name = "deleteBy", length = 50)
    val deleteBy: String? = null,

    @Column(name = "DeleteDate")
    val deleteDate: LocalDateTime? = null
)
