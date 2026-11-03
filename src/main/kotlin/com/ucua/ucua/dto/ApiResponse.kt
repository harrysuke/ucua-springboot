package com.ucua.ucua.dto

 data class ApiErrorResponse(
    val error: String = "",
    val detail: String? = null
)

 data class HealthResponse(
    val status: String = "ok",
    val time: String = ""
)
