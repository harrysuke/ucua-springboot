package com.ucua.ucua.controller

import com.ucua.ucua.dto.*
import com.ucua.ucua.entity.User
import com.ucua.ucua.service.ReportService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/reports")
class ReportController(private val reportService: ReportService) {

    @GetMapping
    fun listReports(
        @AuthenticationPrincipal user: User,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) mine: String?
    ): ResponseEntity<Map<String, List<ReportListDto>>> {
        val reports = reportService.getReports(user, type, status, q, mine == "1")
        return ResponseEntity.ok(mapOf("reports" to reports))
    }

    @GetMapping("/{id}")
    fun getReport(@AuthenticationPrincipal user: User, @PathVariable id: Int): ResponseEntity<Map<String, ReportDetailDto>> {
        return ResponseEntity.ok(mapOf("report" to reportService.getReport(user, id)))
    }

    @PostMapping
    fun createReport(
        @AuthenticationPrincipal user: User,
        @RequestBody request: ReportRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ReportResponse> {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.createReport(user, request, httpRequest))
    }

    @PutMapping("/{id}")
    fun updateReport(
        @AuthenticationPrincipal user: User,
        @PathVariable id: Int,
        @RequestBody request: ReportRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ReportResponse> {
        return ResponseEntity.ok(reportService.updateReport(user, id, request, httpRequest))
    }

    @PatchMapping("/{id}")
    fun patchReport(
        @AuthenticationPrincipal user: User,
        @PathVariable id: Int,
        @RequestBody request: ReportRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ReportResponse> {
        return ResponseEntity.ok(reportService.updateReport(user, id, request, httpRequest))
    }

    @DeleteMapping("/{id}")
    fun deleteReport(
        @AuthenticationPrincipal user: User,
        @PathVariable id: Int,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ReportResponse> {
        return ResponseEntity.ok(reportService.deleteReport(user, id, httpRequest))
    }

    @GetMapping("/{id}/photos")
    fun getPhotos(@AuthenticationPrincipal user: User, @PathVariable id: Int): ResponseEntity<PhotoListResponse> {
        return ResponseEntity.ok(reportService.getPhotos(user, id))
    }

    @PostMapping("/photos")
    fun uploadPhoto(
        @AuthenticationPrincipal user: User,
        @RequestParam reportId: Int,
        @RequestParam(defaultValue = "evidence") photoType: String,
        @RequestParam file: MultipartFile,
        httpRequest: HttpServletRequest
    ): ResponseEntity<PhotoUploadResponse> {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.uploadPhoto(user, reportId, photoType, file, httpRequest))
    }
}
