package com.ucua.ucua.repository

import com.ucua.ucua.entity.Location
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LocationRepository : JpaRepository<Location, String> {
    fun findByIsDeleteOrderByLocationNameAsc(isDelete: Int): List<Location>
}
