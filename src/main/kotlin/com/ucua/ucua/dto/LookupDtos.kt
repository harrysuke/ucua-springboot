package com.ucua.ucua.dto

// Lookups
 data class LookupResponse(
    val locations: List<LocationDto> = emptyList(),
    val offences: List<OffenceDto> = emptyList(),
    val companies: List<CompanyDto> = emptyList(),
    val departments: List<DepartmentDto> = emptyList()
)

 data class LocationDto(
    val locationId: String = "",
    val locationName: String = ""
)

 data class OffenceDto(
    val offenceCode: String = "",
    val offKeyword: String = "",
    val offence: String = ""
)

 data class CompanyDto(
    val id: Int = 0,
    val companyName: String = ""
)

 data class DepartmentDto(
    val deptId: Int = 0,
    val dept: String = "",
    val deptName: String = ""
)
