package com.ucua.ucua.dto

import com.ucua.ucua.entity.MobileReportHistory
import com.ucua.ucua.entity.UcuaPhoto
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

// Report creation/update
 data class ReportRequest(
    val formType: String = "",
    val locationId: String = "",
    val conditionDetails: String? = null,
    val offenceCode: String? = null,
    val violatorName: String? = null,
    val violatorCompany: String? = null,
    val violatorDept: String? = null,
    val actionTaken: String? = null,
    val draft: Boolean = false,
    val submit: Boolean = false
)

 data class ReportResponse(
    val created: Boolean? = null,
    val updated: Boolean? = null,
    val deleted: Boolean? = null,
    val id: Int? = null,
    val status: String? = null,
    val error: String? = null
)

// Report detail
 data class ReportDetailDto(
    val id: Int = 0,
    val staffId: String = "",
    val formType: String = "",
    val locationId: String = "",
    val conditionDetails: String? = null,
    val offenceCode: String? = null,
    val violatorName: String? = null,
    val violatorCompany: String? = null,
    val violatorDept: String? = null,
    val actionTaken: String? = null,
    val submitDate: LocalDateTime? = null,
    val status: String = "",
    val observerName: String = "",
    val observerDepartment: String = "",
    val photos: List<PhotoDto> = emptyList(),
    val history: List<HistoryTimelineDto> = emptyList(),
    val rejectionReason: String? = null,
    val canApprove: Boolean = false,
    val isOwner: Boolean = false
)

 data class ReportListDto(
    val id: Int = 0,
    val staffId: String = "",
    val formType: String = "",
    val locationId: String = "",
    val conditionDetails: String? = null,
    val offenceCode: String? = null,
    val violatorName: String? = null,
    val violatorCompany: String? = null,
    val violatorDept: String? = null,
    val actionTaken: String? = null,
    val submitDate: LocalDateTime? = null,
    val status: String = "",
    val observerName: String = "",
    val observerDepartment: String = ""
)

 data class PhotoDto(
    val id: Int = 0,
    val filename: String = "",
    val photoType: String = "",
    val url: String = ""
)

 data class HistoryTimelineDto(
    val id: Int = 0,
    val action: String = "",
    val remarks: String? = null,
    val createdAt: LocalDateTime? = null,
    val staffId: String = "",
    val staffName: String? = null
)

// Photos
 data class PhotoListResponse(
    val photos: List<PhotoDto> = emptyList()
)

 data class PhotoUploadResponse(
    val uploaded: Boolean = true,
    val photo: PhotoDto? = null
)
