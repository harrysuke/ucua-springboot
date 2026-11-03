package com.ucua.ucua.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "dept")
data class Department(
    @Id
    @Column(name = "deptId", nullable = false)
    val deptId: Int = 0,

    @Column(name = "companyCode", length = 3, nullable = false)
    val companyCode: String = "",

    @Column(name = "branchCode", length = 3, nullable = false)
    val branchCode: String = "",

    @Column(name = "deptName", length = 100, nullable = false)
    val deptName: String = "",

    @Column(name = "dept", length = 100, nullable = false)
    val dept: String = "",

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
