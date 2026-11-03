package com.ucua.ucua.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user")
data class User(
    @Id
    @Column(name = "staffId", length = 30, nullable = false)
    val staffId: String = "",

    @Column(name = "companyCode", length = 3, nullable = false)
    val companyCode: String = "",

    @Column(name = "branchCode", length = 3, nullable = false)
    val branchCode: String = "",

    @Column(name = "Password", length = 50, nullable = false)
    val password: String = "",

    @Column(name = "Password1", length = 50, nullable = false)
    val password1: String = "",

    @Column(name = "Name", length = 200, nullable = false)
    val name: String = "",

    @Column(name = "PhoneNo", length = 12, nullable = false)
    val phoneNo: String = "",

    @Column(name = "CompanyID", nullable = false)
    val companyId: Int = 0,

    @Column(name = "Department", length = 300, nullable = false)
    val department: String = "",

    @Column(name = "Designation", length = 50, nullable = false)
    val designation: String = "",

    @Column(name = "AccessLevel", nullable = false)
    val accessLevel: Int = 1,

    @Column(name = "Email", length = 200, nullable = false)
    val email: String = "",

    @Column(name = "Status", nullable = false)
    val status: Int = 1,

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
) {
    fun isActive(): Boolean = status == 1 && isDelete == 0
    fun isPendingApproval(): Boolean = status == 3 && isDelete == 0
    fun isDisabled(): Boolean = status == 2 && isDelete == 0
}
