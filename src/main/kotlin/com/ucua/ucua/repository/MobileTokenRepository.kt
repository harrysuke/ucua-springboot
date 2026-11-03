package com.ucua.ucua.repository

import com.ucua.ucua.entity.MobileToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface MobileTokenRepository : JpaRepository<MobileToken, Int> {
    fun findByTokenAndExpiresAtAfter(token: String, now: LocalDateTime): MobileToken?
    fun deleteByExpiresAtBefore(now: LocalDateTime)
}
