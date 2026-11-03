package com.ucua.ucua.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// Login
 data class LoginRequest(
    @field:NotBlank val staffId: String = "",
    @field:NotBlank val password: String = ""
)

 data class LoginResponse(
    val valid: Boolean = true,
    val token: String = "",
    val user: UserProfileDto? = null,
    val error: String? = null
)

// Register
 data class RegisterRequest(
    @field:NotBlank val staffId: String = "",
    @field:NotBlank val name: String = "",
    @field:Size(min = 6) val password: String = "",
    @field:Email val email: String? = null,
    val phoneNo: String? = null,
    val department: String? = null,
    val designation: String? = null,
    val companyId: String? = null
)

 data class RegisterResponse(
    val registered: Boolean = false,
    val staffId: String? = null,
    val status: String? = null,
    val message: String? = null,
    val error: String? = null,
    val fields: Map<String, String>? = null
)

// Forgot Password
 data class ForgotRequest(
    @field:NotBlank val staffId: String = "",
    val email: String? = null
)

 data class ForgotResponse(
    val requested: Boolean = true,
    val message: String = "Recovery request received. PSSD/ICT will contact you to reset your password."
)

// User Profile
 data class UserProfileDto(
    val staffId: String = "",
    val name: String = "",
    val phoneNo: String = "",
    val department: String = "",
    val designation: String = "",
    val email: String = "",
    val accessLevel: Int = 1,
    val roleName: String = "",
    val companyId: Int = 0
)
