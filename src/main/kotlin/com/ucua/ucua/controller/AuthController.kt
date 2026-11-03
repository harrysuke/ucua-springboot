package com.ucua.ucua.controller

import com.ucua.ucua.dto.*
import com.ucua.ucua.entity.User
import com.ucua.ucua.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest, httpRequest: HttpServletRequest): ResponseEntity<LoginResponse> {
        val response = authService.login(request, httpRequest)
        return if (response.valid) {
            ResponseEntity.ok(response)
        } else {
            when (response.error) {
                "Wrong username/password combination" -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
                "Your account has not been approved", "Your account has been DISABLED" -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(response)
                else -> ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response)
            }
        }
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest, httpRequest: HttpServletRequest): ResponseEntity<RegisterResponse> {
        val response = authService.register(request, httpRequest)
        return if (response.registered) {
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } else if (response.error?.contains("already registered") == true) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(response)
        } else {
            ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response)
        }
    }

    @PostMapping("/forgot")
    fun forgotPassword(@Valid @RequestBody request: ForgotRequest, httpRequest: HttpServletRequest): ResponseEntity<ForgotResponse> {
        return ResponseEntity.ok(authService.forgotPassword(request, httpRequest))
    }

    @GetMapping("/me")
    fun getCurrentUser(@AuthenticationPrincipal user: User): ResponseEntity<UserProfileDto> {
        return ResponseEntity.ok(
            UserProfileDto(
                staffId = user.staffId.trim(),
                name = user.name,
                phoneNo = user.phoneNo,
                department = user.department,
                designation = user.designation,
                email = user.email,
                accessLevel = user.accessLevel,
                roleName = "",
                companyId = user.companyId
            )
        )
    }
}
