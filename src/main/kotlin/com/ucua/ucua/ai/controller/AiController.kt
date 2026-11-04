package com.ucua.ucua.ai.controller

import com.ucua.ucua.ai.dto.*
import com.ucua.ucua.ai.service.ReindexJobService
import com.ucua.ucua.ai.service.ReportAssistService
import com.ucua.ucua.entity.User
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/ai")
class AiController(
    private val assist: ReportAssistService,
    private val reindexJob: ReindexJobService
) {

    @PostMapping("/suggest-offence")
    fun suggestOffence(
        @AuthenticationPrincipal user: User,
        @RequestBody r: SuggestRequest
    ) = assist.suggestOffence(r.conditionDetails)

    @PostMapping("/similar")
    fun similar(
        @AuthenticationPrincipal user: User,
        @RequestBody r: SimilarRequest
    ) = assist.findSimilar(r.reportId, r.limit)

    @PostMapping(value = ["/ask"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun ask(
        @AuthenticationPrincipal user: User,
        @RequestBody r: AskRequest
    ): SseEmitter = assist.streamAnswer(r.question, r.sourceType)

    /** Start a background reindex. Returns 202 Accepted with the job status. */
    @PostMapping("/reindex")
    fun reindex(@AuthenticationPrincipal user: User): ResponseEntity<ReindexStatus> {
        if (user.accessLevel < 3) {
            throw AccessDeniedException("Only HSE administrators can reindex the AI corpus")
        }
        return ResponseEntity.accepted().body(reindexJob.start())
    }

    /** Poll the progress of the current/last reindex job. */
    @GetMapping("/reindex/status")
    fun reindexStatus(@AuthenticationPrincipal user: User): ReindexStatus {
        if (user.accessLevel < 3) {
            throw AccessDeniedException("Only HSE administrators can view reindex status")
        }
        return reindexJob.current()
    }
}
