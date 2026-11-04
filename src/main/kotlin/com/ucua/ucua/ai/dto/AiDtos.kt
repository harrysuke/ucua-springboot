package com.ucua.ucua.ai.dto

data class SuggestRequest(val conditionDetails: String = "")

data class OffenceSuggestion(
    val offenceCode: String,
    val category: String,
    val keyword: String,
    val offence: String,
    val score: Float
)

data class SuggestResponse(
    val suggestions: List<OffenceSuggestion> = emptyList(),
    val disabled: Boolean = false,
    val message: String? = null
)

data class SimilarRequest(val reportId: Int = 0, val limit: Int = 5)

data class SimilarReport(
    val id: Int,
    val formType: String,
    val snippet: String,
    val score: Float
)

data class SimilarResponse(
    val results: List<SimilarReport> = emptyList(),
    val disabled: Boolean = false,
    val message: String? = null
)

data class AskRequest(val question: String = "", val sourceType: String? = null)

data class ReindexResponse(
    val indexedOffences: Int = 0,
    val indexedReports: Int = 0,
    val message: String? = null
)
