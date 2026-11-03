package com.ucua.ucua.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "userroles")
data class UserRole(
    @Id
    @Column(name = "id", nullable = false)
    val id: Int = 0,

    @Column(name = "companyCode", length = 3, nullable = false)
    val companyCode: String = "",

    @Column(name = "branchCode", length = 3, nullable = false)
    val branchCode: String = "",

    @Column(name = "rolename", length = 100, nullable = false)
    val roleName: String = "",

    @Column(name = "roleabbreviation", length = 50, nullable = false)
    val roleAbbreviation: String = "",

    @Column(name = "roledescription", nullable = false, columnDefinition = "TEXT")
    val roleDescription: String = "",

    @Column(name = "createby", length = 20, nullable = false)
    val createBy: String = "",

    @Column(name = "createdate", nullable = false)
    val createDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "editby", length = 20, nullable = false)
    val editBy: String = "",

    @Column(name = "editdate", nullable = false)
    val editDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "isdelete", nullable = false)
    val isDelete: Int = 0,

    @Column(name = "deleteby", length = 20, nullable = false)
    val deleteBy: String = "",

    @Column(name = "deletedate", nullable = false)
    val deleteDate: LocalDateTime = LocalDateTime.now()
)
