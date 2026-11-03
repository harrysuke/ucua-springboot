package com.ucua.ucua.controller

import com.ucua.ucua.dto.WorkflowRequest
import com.ucua.ucua.dto.WorkflowResponse
import com.ucua.ucua.entity.User
import com.ucua.ucua.service.WorkflowService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workflow")
class WorkflowController(private val workflowService: WorkflowService) {

    @PostMapping
    fun executeWorkflow(
        @AuthenticationPrincipal user: User,
        @RequestBody request: WorkflowRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<WorkflowResponse> {
        return ResponseEntity.ok(workflowService.executeWorkflow(user, request, httpRequest))
    }
}
