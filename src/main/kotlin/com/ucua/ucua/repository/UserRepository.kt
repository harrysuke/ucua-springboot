package com.ucua.ucua.repository

import com.ucua.ucua.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, String> {
    fun findByStaffIdAndIsDelete(staffId: String, isDelete: Int): User?
    fun existsByStaffId(staffId: String): Boolean
}
