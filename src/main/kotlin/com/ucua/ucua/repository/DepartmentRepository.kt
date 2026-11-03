package com.ucua.ucua.repository

import com.ucua.ucua.entity.Department
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DepartmentRepository : JpaRepository<Department, Int> {
    fun findByIsDeleteOrderByDeptNameAsc(isDelete: Int): List<Department>
}
