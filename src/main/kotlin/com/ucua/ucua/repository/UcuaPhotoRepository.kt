package com.ucua.ucua.repository

import com.ucua.ucua.entity.UcuaPhoto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UcuaPhotoRepository : JpaRepository<UcuaPhoto, Int> {
    fun findByUcuaFormIdAndIsDelete(ucuaFormId: Int, isDelete: Int): List<UcuaPhoto>
    fun deleteByUcuaFormId(ucuaFormId: Int)
}
