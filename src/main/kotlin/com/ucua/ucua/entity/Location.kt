package com.ucua.ucua.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "location")
data class Location(
    @Id
    @Column(name = "LocationId", length = 20, nullable = false)
    val locationId: String = "",

    @Column(name = "companyCode", length = 3, nullable = false)
    val companyCode: String = "",

    @Column(name = "branchCode", length = 3, nullable = false)
    val branchCode: String = "",

    @Column(name = "LocationName", length = 200, nullable = false)
    val locationName: String = "",

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
