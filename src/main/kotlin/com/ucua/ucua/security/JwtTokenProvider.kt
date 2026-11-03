package com.ucua.ucua.security

import com.ucua.ucua.entity.MobileToken
import com.ucua.ucua.entity.User
import com.ucua.ucua.repository.MobileTokenRepository
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val mobileTokenRepository: MobileTokenRepository
) {

    @Value("\${jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${jwt.expiration-days:7}")
    private var jwtExpirationDays: Long = 7

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray(Charsets.UTF_8))
    }

    fun generateToken(staffId: String, accessLevel: Int): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationDays * 24 * 60 * 60 * 1000)

        val token = Jwts.builder()
            .subject(staffId)
            .claim("lvl", accessLevel)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()

        mobileTokenRepository.save(
            MobileToken(
                token = token,
                staffId = staffId,
                expiresAt = expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                createdAt = LocalDateTime.now()
            )
        )

        return token
    }

    fun validateToken(token: String): Boolean {
        return try {
            val parsed = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)

            val expiration = parsed.payload.expiration
            expiration != null && expiration.after(Date())
        } catch (e: Exception) {
            false
        }
    }

    fun getStaffIdFromToken(token: String): String? {
        return try {
            val claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
            claims.subject
        } catch (e: Exception) {
            null
        }
    }

    fun getAccessLevelFromToken(token: String): Int? {
        return try {
            val claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
            claims["lvl"] as? Int
        } catch (e: Exception) {
            null
        }
    }

    fun extractToken(bearer: String?): String? {
        return if (!bearer.isNullOrBlank() && bearer.startsWith("Bearer ")) {
            bearer.substring(7)
        } else null
    }
}
