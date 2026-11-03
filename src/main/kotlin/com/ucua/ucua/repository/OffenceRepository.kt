package com.ucua.ucua.repository

import com.ucua.ucua.entity.Offence
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OffenceRepository : JpaRepository<Offence, String> {
    fun findByIsDeleteOrderByOffenceCodeAsc(isDelete: Int): List<Offence>
}
