package com.example.dajho.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dajho.data.local.entity.ClientEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room para Clientes y Proveedores (Fase 2).
 */
@Dao
interface ClientDao {

    @Query("SELECT * FROM clients WHERE type = :type ORDER BY name ASC")
    fun getClientsByTypeFlow(type: String): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :clientId")
    suspend fun getClientById(clientId: String): ClientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity)

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Query("SELECT * FROM clients WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSyncClients(): List<ClientEntity>
}
