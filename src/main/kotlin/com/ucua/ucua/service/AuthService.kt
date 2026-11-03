package com.ucua.ucua.service

import com.ucua.ucua.dto.*
import com.ucua.ucua.entity.History
import com.ucua.ucua.entity.User
import com.ucua.ucua.repository.*
import com.ucua.ucua.security.JwtTokenProvider
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigInteger
import java.security.MessageDigest
import java.time.LocalDateTime

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository,
    private val historyRepository: HistoryRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {

    companion object {
        const val STATUS_DRAFT = "Draft"
        const val STATUS_HSE = "Pending PSSD HSE Officer"
        const val STATUS_HOS = "Approved by PSSD HSE Officer"
        const val STATUS_HOD = "Approved by HOS"
        const val STATUS_CLOSED = "Approved by HOD"
        const val STATUS_REJECTED = "Rejected by PSSD HSE Officer"

        fun md5(input: String): String {
            val md = MessageDigest.getInstance("MD5")
            return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
        }
    }

    fun login(request: LoginRequest, httpRequest: HttpServletRequest): LoginResponse {
        if (request.staffId.isBlank() || request.password.isBlank()) {
            return LoginResponse(valid = false, error = "staffId and password are required")
        }

        val user = userRepository.findByStaffIdAndIsDelete(request.staffId, 0)
            ?: return LoginResponse(valid = false, error = "Wrong username/password combination")

        if (user.password != md5(request.password)) {
            return LoginResponse(valid = false, error = "Wrong username/password combination")
        }

        if (user.status == 3) {
            return LoginResponse(valid = false, error = "Your account has not been approved")
        }
        if (user.status == 2) {
            return LoginResponse(valid = false, error = "Your account has been DISABLED")
        }

        val token = jwtTokenProvider.generateToken(user.staffId.trim(), user.accessLevel)
        logHistory(user.staffId.trim(), "LOGIN", "Mobile app login", httpRequest)

        val role = userRoleRepository.findById(user.accessLevel).orElse(null)

        return LoginResponse(
            valid = true,
            token = token,
            user = UserProfileDto(
                staffId = user.staffId.trim(),
                name = user.name,
                phoneNo = user.phoneNo,
                department = user.department,
                designation = user.designation,
                email = user.email,
                accessLevel = user.accessLevel,
                roleName = role?.roleName ?: "",
                companyId = user.companyId
            )
        )
    }

    @Transactional
    fun register(request: RegisterRequest, httpRequest: HttpServletRequest): RegisterResponse {
        val errors = mutableMapOf<String, String>()
        if (request.staffId.isBlank()) errors["staffId"] = "Staff ID is required"
        if (request.name.isBlank()) errors["name"] = "Full name is required"
        if (request.password.length < 6) errors["password"] = "Password must be at least 6 characters"

        if (errors.isNotEmpty()) {
            return RegisterResponse(registered = false, error = errors.values.first(), fields = errors)
        }

        if (userRepository.existsByStaffId(request.staffId.trim())) {
            return RegisterResponse(registered = false, error = "This Staff ID is already registered")
        }

        val now = LocalDateTime.now()
        val companyId = request.companyId?.toIntOrNull()?.takeIf { it > 0 } ?: 1

        val user = User(
            staffId = request.staffId.trim(),
            companyCode = "JPB",
            branchCode = "PG",
            password = md5(request.password),
            password1 = md5(request.password),
            name = request.name,
            phoneNo = request.phoneNo ?: "",
            companyId = companyId,
            department = request.department ?: "",
            designation = request.designation ?: "",
            accessLevel = 1,
            email = request.email ?: "",
            status = 3,
            createBy = request.staffId.trim(),
            createDate = now,
            editBy = "",
            editDate = now,
            deleteBy = "",
            deleteDate = now
        )

        userRepository.save(user)
        logHistory(user.staffId.trim(), "REGISTER", "Registered via mobile app", httpRequest)

        return RegisterResponse(
            registered = true,
            staffId = user.staffId.trim(),
            status = "pending_approval",
            message = "Registration submitted. Your account is pending PSSD approval."
        )
    }

    fun forgotPassword(request: ForgotRequest, httpRequest: HttpServletRequest): ForgotResponse {
        if (request.staffId.isBlank()) {
            throw IllegalArgumentException("Staff ID is required")
        }

        val user = userRepository.findByStaffIdAndIsDelete(request.staffId.trim(), 0)
        if (user != null) {
            logHistory(request.staffId.trim(), "FORGOT", "Password recovery requested via mobile app", httpRequest)
        }

        return ForgotResponse()
    }

    private fun logHistory(staffId: String, action: String, activity: String, request: HttpServletRequest) {
        try {
            historyRepository.save(
                History(
                    companyCode = "JPB",
                    branchCode = "PG",
                    ucuaId = 0,
                    staffId = staffId,
                    accessLevel = 0,
                    action = action,
                    activity = activity,
                    status = "SUCCESS",
                    sql = "",
                    ipAddress = request.remoteAddr ?: "",
                    userAgent = request.getHeader("User-Agent") ?: "",
                    dateTimestamp = LocalDateTime.now()
                )
            )
        } catch (_: Exception) { }
    }
}
