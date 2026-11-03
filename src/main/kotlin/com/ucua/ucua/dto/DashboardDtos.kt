package com.ucua.ucua.dto

// Dashboard
 data class DashboardResponse(
    val counts: CountsDto = CountsDto(),
    val queues: QueuesDto = QueuesDto(),
    val trend: List<TrendDto> = emptyList(),
    val activity: List<ActivityDto> = emptyList(),
    val leaderboard: List<LeaderboardDto> = emptyList(),
    val accessLevel: Int = 1
)

 data class CountsDto(
    val uc: Int = 0,
    val ua: Int = 0,
    val drafts: Int = 0,
    val pending: Int = 0,
    val closed: Int = 0,
    val rejected: Int = 0
)

 data class QueuesDto(
    val hse: Int = 0,
    val hos: Int = 0,
    val hod: Int = 0
)

 data class TrendDto(
    val month: String = "",
    val label: String = "",
    val uc: Int = 0,
    val ua: Int = 0
)

 data class ActivityDto(
    val id: Int = 0,
    val action: String = "",
    val activity: String = "",
    val date: String = ""
)

 data class LeaderboardDto(
    val staffId: String = "",
    val name: String = "",
    val total: Int = 0
)
