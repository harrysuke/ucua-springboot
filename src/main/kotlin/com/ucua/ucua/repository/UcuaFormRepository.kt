package com.ucua.ucua.repository

import com.ucua.ucua.entity.UcuaForm
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UcuaFormRepository : JpaRepository<UcuaForm, Int> {

    fun findByStaffIdAndIsDelete(staffId: String, isDelete: Int): List<UcuaForm>

    @Query("""
        SELECT f FROM UcuaForm f 
        WHERE f.isDelete = 0 
        AND (:staffId IS NULL OR f.staffId = :staffId) 
        AND (:formType IS NULL OR f.formType = :formType)
        AND (:status IS NULL OR f.status = :status)
        AND (:search IS NULL OR 
            LOWER(f.conditionDetails) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(f.violatorName) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(f.offenceCode) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY f.id DESC
    """)
    fun findReports(
        @Param("staffId") staffId: String?,
        @Param("formType") formType: String?,
        @Param("status") status: String?,
        @Param("search") search: String?
    ): List<UcuaForm>

    @Query("""
        SELECT f.formType, f.status, COUNT(f) 
        FROM UcuaForm f 
        WHERE f.staffId = :staffId AND f.isDelete = 0 
        GROUP BY f.formType, f.status
    """)
    fun countByStaffIdGrouped(@Param("staffId") staffId: String): List<Array<Any>>

    fun countByStatusIn(statuses: List<String>): Long
}
