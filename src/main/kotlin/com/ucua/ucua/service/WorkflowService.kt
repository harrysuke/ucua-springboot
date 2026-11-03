package com.ucua.ucua.service

import com.ucua.ucua.dto.WorkflowRequest
import com.ucua.ucua.dto.WorkflowResponse
import com.ucua.ucua.entity.History
import com.ucua.ucua.entity.MobileReportHistory
import com.ucua.ucua.entity.User
import com.ucua.ucua.repository.HistoryRepository
import com.ucua.ucua.repository.MobileReportHistoryRepository
import com.ucua.ucua.repository.UcuaFormRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class WorkflowService(
    private val ucuaFormRepository: UcuaFormRepository,
    private val historyRepository: HistoryRepository,
    private val mobileReportHistoryRepository: MobileReportHistoryRepository
) {

    @Transactional
    fun executeWorkflow(
        user: User,
        request: WorkflowRequest,
        httpRequest: HttpServletRequest
    ): WorkflowResponse {
        val staffId = user.staffId.trim()
        val level = user.accessLevel

        val report = ucuaFormRepository.findById(request.id)
            .orElseThrow { IllegalArgumentException("Report not found") }

        val status = report.status
        val stage = ReportService.stageForStatus(status)

        when (request.action.lowercase()) {
            "resubmit" -> {
                if (report.staffId.trim() != staffId) {
                    throw org.springframework.security.access.AccessDeniedException("Only the submitter can resubmit this report")
                }
                if (status != ReportService.STATUS_DRAFT && !ReportService.isRejectedStatus(status)) {
                    throw IllegalStateException("Only rejected or draft reports can be resubmitted")
                }

                val updated = report.copy(
                    status = ReportService.STATUS_HSE,
                    submitDate = LocalDateTime.now()
                )
                ucuaFormRepository.save(updated)
                addReportHistory(request.id, staffId, "RESUBMIT", request.remarks ?: "Resubmitted for review")
                logHistory(staffId, "RESUBMIT", "Resubmitted report ID ${request.id} via mobile app", request.id, httpRequest)
                return WorkflowResponse(id = request.id, status = ReportService.STATUS_HSE)
            }

            "reject" -> {
                if (stage == null) {
                    throw IllegalStateException("This report is not awaiting approval")
                }
                if (!ReportService.canActOnStage(level, stage)) {
                    throw org.springframework.security.access.AccessDeniedException("Your role is not authorised for this approval stage")
                }
                if (request.remarks.isNullOrBlank()) {
                    throw IllegalArgumentException("A rejection reason is required")
                }

                val newStatus = ReportService.rejectedStatus(stage)
                val updated = report.copy(status = newStatus)
                ucuaFormRepository.save(updated)
                addReportHistory(request.id, staffId, "REJECT", request.remarks)
                logHistory(staffId, "REJECT", "Rejected report ID ${request.id} via mobile app", request.id, httpRequest)
                return WorkflowResponse(id = request.id, status = newStatus)
            }

            "close" -> {
                if (!ReportService.canActOnStage(level, "CLOSE") || stage != "HOD") {
                    throw org.springframework.security.access.AccessDeniedException("Only HOD or an administrator can close this report")
                }

                val updated = report.copy(status = ReportService.STATUS_CLOSED)
                ucuaFormRepository.save(updated)
                addReportHistory(request.id, staffId, "CLOSE", request.remarks ?: "Report closed with evidence")
                logHistory(staffId, "CLOSE", "Closed report ID ${request.id} via mobile app", request.id, httpRequest)
                return WorkflowResponse(id = request.id, status = ReportService.STATUS_CLOSED)
            }

            "approve" -> {
                if (stage == null) {
                    throw IllegalStateException("This report is not awaiting approval")
                }
                if (!ReportService.canActOnStage(level, stage)) {
                    throw org.springframework.security.access.AccessDeniedException("Your role is not authorised for this approval stage")
                }

                val next = ReportService.nextStatus(stage)
                    ?: throw IllegalStateException("Report cannot be advanced further")

                val updated = report.copy(status = next)
                ucuaFormRepository.save(updated)
                addReportHistory(request.id, staffId, "APPROVE", request.remarks ?: "Approved at $stage stage")

                // Handle closing evidence photos
                request.evidence?.forEach { photoId ->
                    // In a real implementation, update photo type in repository
                }

                logHistory(staffId, "APPROVE", "Approved report ID ${request.id} at $stage stage via mobile app", request.id, httpRequest)
                return WorkflowResponse(id = request.id, status = next)
            }

            else -> throw IllegalArgumentException("action must be approve, reject, close or resubmit")
        }
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
