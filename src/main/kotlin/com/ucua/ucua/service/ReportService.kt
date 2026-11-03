package com.ucua.ucua.service

import com.ucua.ucua.dto.*
import com.ucua.ucua.entity.History
import com.ucua.ucua.entity.MobileReportHistory
import com.ucua.ucua.entity.UcuaForm
import com.ucua.ucua.entity.UcuaPhoto
import com.ucua.ucua.entity.User
import com.ucua.ucua.repository.*
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class ReportService(
    private val ucuaFormRepository: UcuaFormRepository,
    private val ucuaPhotoRepository: UcuaPhotoRepository,
    private val historyRepository: HistoryRepository,
    private val mobileReportHistoryRepository: MobileReportHistoryRepository,
    private val userRepository: UserRepository,
    @Value("\${storage.upload-dir:./uploads}") private val uploadDir: String
) {

    companion object {
        const val STATUS_DRAFT = "Draft"
        const val STATUS_HSE = "Pending PSSD HSE Officer"
        const val STATUS_HOS = "Approved by PSSD HSE Officer"
        const val STATUS_HOD = "Approved by HOS"
        const val STATUS_CLOSED = "Approved by HOD"

        fun isRejectedStatus(status: String?): Boolean =
            status?.startsWith("Rejected", ignoreCase = true) == true

        fun isClosedStatus(status: String?): Boolean =
            status == STATUS_CLOSED || status == "Approved by HODs"

        fun stageForStatus(status: String?): String? = when (status) {
            STATUS_HSE, "Pending HSE Reps", "Updated by HSE Reps" -> "HSE"
            STATUS_HOS, "Approved by PSSD Officer" -> "HOS"
            STATUS_HOD -> "HOD"
            STATUS_CLOSED, "Approved by HODs" -> "CLOSE"
            else -> null
        }

        fun nextStatus(stage: String?): String? = when (stage) {
            "HSE" -> STATUS_HOS
            "HOS" -> STATUS_HOD
            "HOD" -> STATUS_CLOSED
            else -> null
        }

        fun rejectedStatus(stage: String?): String = when (stage) {
            "HOS" -> "Rejected by HOS"
            "HOD" -> "Rejected by HOD"
            else -> STATUS_REJECTED
        }

        fun stageApprovers(stage: String): List<Int> = when (stage) {
            "HSE" -> listOf(3, 4, 8, 9)
            "HOS" -> listOf(3, 6, 9)
            "HOD" -> listOf(3, 7, 9)
            "CLOSE" -> listOf(3, 7, 9)
            else -> emptyList()
        }

        fun canActOnStage(accessLevel: Int, stage: String?): Boolean =
            stage != null && accessLevel in stageApprovers(stage)
    }

    fun getReports(
        user: User,
        type: String?,
        status: String?,
        search: String?,
        mineOnly: Boolean?
    ): List<ReportListDto> {
        val isAdmin = user.accessLevel >= 3
        val formType = when (type) {
            "UC", "Unsafe Condition" -> "Unsafe Condition"
            "UA", "Unsafe Action" -> "Unsafe Action"
            else -> null
        }

        val staffIdFilter = if (!isAdmin || mineOnly == true) user.staffId.trim() else null

        val reports = ucuaFormRepository.findReports(staffIdFilter, formType, status, search)
        return reports.map { it.toListDto() }
    }

    fun getReport(user: User, id: Int): ReportDetailDto {
        val report = ucuaFormRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Report not found") }

        val isAdmin = user.accessLevel >= 3
        if (!isAdmin && report.staffId.trim() != user.staffId.trim()) {
            throw org.springframework.security.access.AccessDeniedException("Access denied")
        }

        return report.toDetailDto(user)
    }

    @Transactional
    fun createReport(user: User, request: ReportRequest, httpRequest: HttpServletRequest): ReportResponse {
        if (request.formType != "Unsafe Condition" && request.formType != "Unsafe Action") {
            throw IllegalArgumentException("formType must be 'Unsafe Condition' or 'Unsafe Action'")
        }

        if (request.locationId.isBlank()) {
            throw IllegalArgumentException("locationId is required")
        }

        val isDraft = request.draft
        val now = LocalDateTime.now()
        val status = if (isDraft) STATUS_DRAFT else STATUS_HSE

        if (!isDraft) {
            if (request.formType == "Unsafe Condition" && request.conditionDetails.isNullOrBlank()) {
                throw IllegalArgumentException("locationId and conditionDetails are required")
            }
            if (request.formType == "Unsafe Action" && (request.offenceCode.isNullOrBlank() || request.violatorCompany.isNullOrBlank())) {
                throw IllegalArgumentException("locationId, offenceCode and violatorCompany are required")
            }
        }

        val report = UcuaForm(
            companyCode = "JPB",
            branchCode = "PG",
            locationId = request.locationId,
            offenceCode = request.offenceCode ?: "",
            staffId = user.staffId.trim(),
            evidenceImage = "",
            conditionDetails = request.conditionDetails ?: "",
            formType = request.formType,
            observerName = user.name,
            observerDepartment = user.department,
            observerEmail = user.email,
            observerDateTime = now,
            violatorName = request.violatorName,
            violatorCompany = request.violatorCompany ?: "",
            violatorDept = request.violatorDept,
            actionTaken = request.actionTaken,
            status = status,
            submitDate = now,
            createBy = user.staffId.trim(),
            createDate = now
        )

        val saved = ucuaFormRepository.save(report)
        val action = if (isDraft) "DRAFT" else "SUBMIT"
        val remark = if (isDraft) "Saved as draft" else "Submitted for PSSD HSE review"
        addReportHistory(saved.id!!, user.staffId.trim(), action, remark)
        logHistory(user.staffId.trim(), action, "${if (isDraft) "Saved draft" else "Created"} report ID ${saved.id} via mobile app", saved.id, httpRequest)

        return ReportResponse(created = true, id = saved.id, status = status)
    }

    @Transactional
    fun updateReport(
        user: User,
        id: Int,
        request: ReportRequest,
        httpRequest: HttpServletRequest
    ): ReportResponse {
        val report = ucuaFormRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Report not found") }

        val isAdmin = user.accessLevel >= 3
        val isOwner = report.staffId.trim() == user.staffId.trim()

        if (!isAdmin && !isOwner) {
            throw org.springframework.security.access.AccessDeniedException("Access denied")
        }

        val editable = report.status == STATUS_DRAFT || isRejectedStatus(report.status)
        if (!editable && !isAdmin) {
            throw IllegalStateException("Only draft or rejected reports can be edited")
        }

        val submit = request.submit
        var newStatus = report.status

        if (submit) {
            val locationId = request.locationId.ifBlank { report.locationId }
            if (locationId.isBlank()) throw IllegalArgumentException("locationId is required")

            val formType = request.formType.ifBlank { report.formType }
            if (formType == "Unsafe Condition" && (request.conditionDetails ?: report.conditionDetails).isNullOrBlank()) {
                throw IllegalArgumentException("conditionDetails is required for Unsafe Condition")
            }
            if (formType == "Unsafe Action" && ((request.offenceCode ?: report.offenceCode).isNullOrBlank() || (request.violatorCompany ?: report.violatorCompany).isNullOrBlank())) {
                throw IllegalArgumentException("offenceCode and violatorCompany are required for Unsafe Action")
            }
            newStatus = STATUS_HSE
        }

        val updated = report.copy(
            formType = request.formType.ifBlank { report.formType },
            locationId = request.locationId.ifBlank { report.locationId },
            conditionDetails = request.conditionDetails ?: report.conditionDetails,
            offenceCode = request.offenceCode ?: report.offenceCode,
            violatorName = request.violatorName ?: report.violatorName,
            violatorCompany = request.violatorCompany ?: report.violatorCompany,
            violatorDept = request.violatorDept ?: report.violatorDept,
            actionTaken = request.actionTaken ?: report.actionTaken,
            status = newStatus,
            submitDate = if (submit) LocalDateTime.now() else report.submitDate
        )

        ucuaFormRepository.save(updated)
        val action = if (submit) "RESUBMIT" else "EDIT"
        val remark = if (submit) "Submitted for review" else "Report details updated"
        addReportHistory(id, user.staffId.trim(), action, remark)
        logHistory(user.staffId.trim(), "EDIT", "Updated report ID $id via mobile app", id, httpRequest)

        return ReportResponse(updated = true, id = id, status = newStatus)
    }

    @Transactional
    fun deleteReport(user: User, id: Int, httpRequest: HttpServletRequest): ReportResponse {
        val report = ucuaFormRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Report not found") }

        val isAdmin = user.accessLevel >= 3
        val isOwner = report.staffId.trim() == user.staffId.trim()

        if (!isAdmin && !isOwner) {
            throw org.springframework.security.access.AccessDeniedException("Access denied")
        }

        if (!isAdmin && report.status != STATUS_DRAFT && !isRejectedStatus(report.status)) {
            throw IllegalStateException("Only drafts or rejected reports can be deleted")
        }

        try { ucuaPhotoRepository.deleteByUcuaFormId(id) } catch (_: Exception) { }
        try { mobileReportHistoryRepository.findByReportIdOrderByIdAsc(id).forEach { mobileReportHistoryRepository.delete(it) } } catch (_: Exception) { }
        ucuaFormRepository.delete(report)

        logHistory(user.staffId.trim(), "DELETE", "Deleted report ID $id via mobile app", id, httpRequest)
        return ReportResponse(deleted = true, id = id)
    }

    fun getPhotos(user: User, reportId: Int): PhotoListResponse {
        val report = ucuaFormRepository.findById(reportId)
            .orElseThrow { IllegalArgumentException("Report not found") }

        if (user.accessLevel < 3 && report.staffId.trim() != user.staffId.trim()) {
            throw org.springframework.security.access.AccessDeniedException("Access denied")
        }

        val photos = ucuaPhotoRepository.findByUcuaFormIdAndIsDelete(reportId, 0)
        return PhotoListResponse(photos = photos.map { it.toDto() })
    }

    @Transactional
    fun uploadPhoto(
        user: User,
        reportId: Int,
        photoType: String,
        file: MultipartFile,
        httpRequest: HttpServletRequest
    ): PhotoUploadResponse {
        val report = ucuaFormRepository.findById(reportId)
            .orElseThrow { IllegalArgumentException("Report not found") }

        if (user.accessLevel < 3 && report.staffId.trim() != user.staffId.trim()) {
            throw org.springframework.security.access.AccessDeniedException("Access denied")
        }

        val original = file.originalFilename ?: "unknown"
        val ext = original.substringAfterLast('.', "").lowercase()
        val allowed = listOf("jpg", "jpeg", "png", "webp", "gif")
        if (ext !in allowed) {
            throw IllegalArgumentException("File type .$ext is not allowed")
        }
        if (file.size > 8 * 1024 * 1024) {
            throw IllegalArgumentException("File exceeds the 8 MB limit")
        }

        val dir = File(uploadDir)
        if (!dir.exists()) dir.mkdirs()

        val filename = "${DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())}_${reportId}_${UUID.randomUUID().toString().take(6)}.$ext"
        val target = File(dir, filename)
        file.transferTo(target)

        val now = LocalDateTime.now()
        val label = when (photoType.lowercase()) {
            "closed" -> "Closed Evidence"
            "violator" -> "Violator Evidence"
            else -> "Photo Evidence"
        }

        val photo = UcuaPhoto(
            companyCode = "JPB",
            branchCode = "PG",
            ucuaFormId = reportId,
            evidenceImage = "uploads/$filename",
            type = label,
            createBy = user.staffId.trim(),
            createDate = now,
            editBy = user.staffId.trim(),
            editDate = now
        )

        val saved = ucuaPhotoRepository.save(photo)

        if (photoType.lowercase() != "closed") {
            try {
                val updated = report.copy(evidenceImage = "uploads/$filename")
                ucuaFormRepository.save(updated)
            } catch (_: Exception) { }
        }

        addReportHistory(reportId, user.staffId.trim(), "PHOTO", "Attached $label")

        return PhotoUploadResponse(
            uploaded = true,
            photo = PhotoDto(
                id = saved.id ?: 0,
                filename = filename,
                photoType = label,
                url = "/uploads/$filename"
            )
        )
    }

    private fun UcuaForm.toListDto(): ReportListDto = ReportListDto(
        id = this.id ?: 0,
        staffId = this.staffId.trim(),
        formType = this.formType,
        locationId = this.locationId,
        conditionDetails = this.conditionDetails,
        offenceCode = this.offenceCode,
        violatorName = this.violatorName,
        violatorCompany = this.violatorCompany,
        violatorDept = this.violatorDept,
        actionTaken = this.actionTaken,
        submitDate = this.submitDate,
        status = this.status,
        observerName = this.observerName,
        observerDepartment = this.observerDepartment
    )

    private fun UcuaForm.toDetailDto(user: User): ReportDetailDto {
        val photos = ucuaPhotoRepository.findByUcuaFormIdAndIsDelete(this.id ?: 0, 0)
        val history = mobileReportHistoryRepository.findByReportIdOrderByIdAsc(this.id ?: 0)
        val latestReject = mobileReportHistoryRepository.findTop1ByReportIdAndActionOrderByIdDesc(this.id ?: 0, "REJECT")

        return ReportDetailDto(
            id = this.id ?: 0,
            staffId = this.staffId.trim(),
            formType = this.formType,
            locationId = this.locationId,
            conditionDetails = this.conditionDetails,
            offenceCode = this.offenceCode,
            violatorName = this.violatorName,
            violatorCompany = this.violatorCompany,
            violatorDept = this.violatorDept,
            actionTaken = this.actionTaken,
            submitDate = this.submitDate,
            status = this.status,
            observerName = this.observerName,
            observerDepartment = this.observerDepartment,
            photos = photos.map { it.toDto() },
            history = history.map { it.toDto() },
            rejectionReason = latestReject?.remarks,
            canApprove = canActOnStage(user.accessLevel, stageForStatus(this.status)),
            isOwner = this.staffId.trim() == user.staffId.trim()
        )
    }

    private fun UcuaPhoto.toDto(): PhotoDto = PhotoDto(
        id = this.id ?: 0,
        filename = File(this.evidenceImage).name,
        photoType = this.type,
        url = if (this.evidenceImage.startsWith("http")) this.evidenceImage else "/${this.evidenceImage}"
    )

    private fun MobileReportHistory.toDto(): HistoryTimelineDto {
        val user = userRepository.findByStaffIdAndIsDelete(this.staffId, 0)
        return HistoryTimelineDto(
            id = this.id ?: 0,
            action = this.action,
            remarks = this.remarks,
            createdAt = this.createdAt,
            staffId = this.staffId,
            staffName = user?.name
        )
    }

    private fun addReportHistory(reportId: Int, staffId: String, action: String, remarks: String?) {
        try {
            mobileReportHistoryRepository.save(
                MobileReportHistory(
                    reportId = reportId,
                    staffId = staffId,
                    action = action,
                    remarks = remarks,
                    createdAt = LocalDateTime.now()
                )
            )
        } catch (_: Exception) { }
    }

    private fun logHistory(staffId: String, action: String, activity: String, reportId: Int, request: HttpServletRequest) {
        try {
            historyRepository.save(
                History(
                    companyCode = "JPB",
                    branchCode = "PG",
                    ucuaId = reportId,
                    staffId = staffId,
                    accessLevel = 0,
                    action = action,
                    activity = activity,
                    status = "SUCCESS",
                    sql = "",
                    ipAddress = request.remoteAddr ?: "",
                    userAgent = request.getHeader("User-Agent") ?: "",
                    dateTimestamp = LocalDateTime.now()
                )
            )
        } catch (_: Exception) { }
    }
}
