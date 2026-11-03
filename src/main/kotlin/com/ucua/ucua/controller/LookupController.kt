package com.ucua.ucua.controller

import com.ucua.ucua.dto.LookupResponse
import com.ucua.ucua.service.LookupService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/lookups")
class LookupController(private val lookupService: LookupService) {

    @GetMapping
    fun getLookups(): ResponseEntity<LookupResponse> {
        return ResponseEntity.ok(lookupService.getLookups())
    }
}
