package com.ucua.ucua.repository

import com.ucua.ucua.entity.Company
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CompanyRepository : JpaRepository<Company, Int> {
    fun findByIsDeletedOrderByCompanyNameAsc(isDeleted: Int): List<Company>
}
