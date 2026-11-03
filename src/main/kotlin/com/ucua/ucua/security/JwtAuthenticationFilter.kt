package com.ucua.ucua.security

import com.ucua.ucua.entity.User
import com.ucua.ucua.repository.MobileTokenRepository
import com.ucua.ucua.repository.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.LocalDateTime

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository,
    private val mobileTokenRepository: MobileTokenRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = jwtTokenProvider.extractToken(request.getHeader("Authorization"))
            ?: request.getParameter("token")

        if (!token.isNullOrBlank() && jwtTokenProvider.validateToken(token)) {
            val staffId = jwtTokenProvider.getStaffIdFromToken(token)
            val dbToken = mobileTokenRepository.findByTokenAndExpiresAtAfter(token, LocalDateTime.now())

            if (staffId != null && dbToken != null) {
                val user = userRepository.findByStaffIdAndIsDelete(staffId, 0)
                if (user != null) {
                    val auth = UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        listOf(SimpleGrantedAuthority("ROLE_USER"))
                    )
                    SecurityContextHolder.getContext().authentication = auth
                }
            }
        }

        filterChain.doFilter(request, response)
    }
}
