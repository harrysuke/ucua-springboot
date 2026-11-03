package com.ucua.ucua.controller

import com.ucua.ucua.dto.DashboardResponse
import com.ucua.ucua.entity.User
import com.ucua.ucua.service.DashboardService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dashboard")
class DashboardController(private val dashboardService: DashboardService) {

    @GetMapping
    fun getDashboard(@AuthenticationPrincipal user: User): ResponseEntity<DashboardResponse> {
        return ResponseEntity.ok(dashboardService.getDashboard(user))
    }
}
