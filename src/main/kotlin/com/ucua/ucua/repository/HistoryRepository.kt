package com.ucua.ucua.repository

import com.ucua.ucua.entity.History
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HistoryRepository : JpaRepository<History, Int> {
    fun findByStaffIdOrderByIdDesc(staffId: String): List<History>
    fun findTop8ByStaffIdOrderByIdDesc(staffId: String): List<History>
}
