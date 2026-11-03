package com.ucua.ucua.service

import com.ucua.ucua.dto.*
import com.ucua.ucua.repository.*
import org.springframework.stereotype.Service

@Service
class LookupService(
    private val locationRepository: LocationRepository,
    private val offenceRepository: OffenceRepository,
    private val companyRepository: CompanyRepository,
    private val departmentRepository: DepartmentRepository
) {

    fun getLookups(): LookupResponse {
        val locations = locationRepository.findByIsDeleteOrderByLocationNameAsc(0)
            .map { LocationDto(locationId = it.locationId.trim(), locationName = it.locationName.trim()) }

        val offences = offenceRepository.findByIsDeleteOrderByOffenceCodeAsc(0)
            .map { OffenceDto(offenceCode = it.offenceCode.trim(), offKeyword = it.offKeyword.trim(), offence = it.offence.trim()) }

        val companies = companyRepository.findByIsDeletedOrderByCompanyNameAsc(0)
            .map { CompanyDto(id = it.id, companyName = it.companyName.trim()) }

        val departments = departmentRepository.findByIsDeleteOrderByDeptNameAsc(0)
            .map { DepartmentDto(deptId = it.deptId, dept = it.dept.trim(), deptName = it.deptName.trim()) }

        return LookupResponse(
            locations = locations,
            offences = offences,
            companies = companies,
            departments = departments
        )
    }
}
