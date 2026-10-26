package com.example.dajho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dajho.data.local.entity.BusinessEntity

@Dao
interface BusinessDao {

    @Query("SELECT * FROM negocios WHERE id = :businessId")
    suspend fun getBusinessById(businessId: String): BusinessEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessEntity)
}
