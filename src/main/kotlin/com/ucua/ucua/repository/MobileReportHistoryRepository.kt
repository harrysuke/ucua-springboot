package com.ucua.ucua.repository

import com.ucua.ucua.entity.MobileReportHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MobileReportHistoryRepository : JpaRepository<MobileReportHistory, Int> {
    fun findByReportIdOrderByIdAsc(reportId: Int): List<MobileReportHistory>
    fun findTop1ByReportIdAndActionOrderByIdDesc(reportId: Int, action: String): MobileReportHistory?
}
