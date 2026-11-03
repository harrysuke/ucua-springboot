package com.ucua.ucua.dto

// Workflow
 data class WorkflowRequest(
    val id: Int = 0,
    val action: String = "",
    val remarks: String? = null,
    val evidence: List<Int>? = null
)

 data class WorkflowResponse(
    val updated: Boolean = true,
    val id: Int = 0,
    val status: String = ""
)
