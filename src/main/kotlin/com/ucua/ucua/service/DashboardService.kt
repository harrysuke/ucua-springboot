package com.ucua.ucua.service

import com.ucua.ucua.dto.*
import com.ucua.ucua.entity.User
import com.ucua.ucua.repository.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class DashboardService(
    private val ucuaFormRepository: UcuaFormRepository,
    private val historyRepository: HistoryRepository,
    private val userRepository: UserRepository
) {

    fun getDashboard(user: User): DashboardResponse {
        val staffId = user.staffId.trim()
        val level = user.accessLevel
        val isAdmin = level >= 3

        // Personal counters
        val counts = CountsDto()
        val rows = ucuaFormRepository.countByStaffIdGrouped(staffId)
        for (row in rows) {
            val formType = row[0] as? String ?: continue
            val status = row[1] as? String ?: continue
            val total = (row[2] as? Number)?.toInt() ?: 0

            if (formType == "Unsafe Condition") counts.uc += total
            if (formType == "Unsafe Action") counts.ua += total
            if (status == ReportService.STATUS_DRAFT) counts.drafts += total
            else if (ReportService.isClosedStatus(status)) counts.closed += total
            else if (ReportService.isRejectedStatus(status)) counts.rejected += total
            else if (status.isNotBlank()) counts.pending += total
        }

        // Approval queues
        val allReports = ucuaFormRepository.findReports(null, null, null, null)
        val hseStatuses = listOf(ReportService.STATUS_HSE, "Pending HSE Reps", "Updated by HSE Reps")
        val hosStatuses = listOf(ReportService.STATUS_HOS, "Approved by PSSD Officer")
        val hodStatuses = listOf(ReportService.STATUS_HOD)

        val hseCount = allReports.count { it.status in hseStatuses }
        val hosCount = allReports.count { it.status in hosStatuses }
        val hodCount = allReports.count { it.status in hodStatuses }

        val queues = QueuesDto(
            hse = if (ReportService.canActOnStage(level, "HSE")) hseCount else 0,
            hos = if (ReportService.canActOnStage(level, "HOS")) hosCount else 0,
            hod = if (ReportService.canActOnStage(level, "HOD")) hodCount else 0
        )

        // Six month trend
        val since = LocalDateTime.now().minusMonths(6)
        val trendData = mutableMapOf<String, Pair<Int, Int>>()
        val userReports = ucuaFormRepository.findReports(if (isAdmin) null else staffId, null, null, null)
            .filter { it.submitDate != null && it.submitDate.isAfter(since) && it.status != "Draft" }

        for (report in userReports) {
            val ym = report.submitDate!!.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val current = trendData.getOrDefault(ym, 0 to 0)
            if (report.formType == "Unsafe Condition") {
                trendData[ym] = current.first + 1 to current.second
            } else if (report.formType == "Unsafe Action") {
                trendData[ym] = current.first to current.second + 1
            }
        }

        val trend = mutableListOf<TrendDto>()
        for (i in 5 downTo 0) {
            val date = LocalDateTime.now().minusMonths(i.toLong())
            val ym = date.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val label = date.format(DateTimeFormatter.ofPattern("MMM"))
            val data = trendData[ym] ?: (0 to 0)
            trend.add(TrendDto(month = ym, label = label, uc = data.first, ua = data.second))
        }

        // Activity feed
        val activities = historyRepository.findTop8ByStaffIdOrderByIdDesc(staffId)
            .map {
                ActivityDto(
                    id = it.id ?: 0,
                    action = it.action,
                    activity = it.activity,
                    date = it.dateTimestamp.toString()
                )
            }

        // Leaderboard
        val leaderboard = userReports
            .groupBy { it.staffId.trim() }
            .map { (sid, reps) ->
                val u = userRepository.findByStaffIdAndIsDelete(sid, 0)
                LeaderboardDto(
                    staffId = sid,
                    name = u?.name ?: sid,
                    total = reps.size
                )
            }
            .sortedByDescending { it.total }
            .take(5)

        return DashboardResponse(
            counts = counts,
            queues = queues,
            trend = trend,
            activity = activities,
            leaderboard = leaderboard,
            accessLevel = level
        )
    }
}
