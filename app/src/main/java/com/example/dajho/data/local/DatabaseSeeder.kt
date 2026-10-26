package com.example.dajho.data.local

import com.example.dajho.data.local.entity.ClientEntity
import com.example.dajho.data.local.entity.ProductEntity
import com.example.dajho.data.local.entity.ProductVariantEntity
import java.util.UUID

/**
 * Poblador inicial de semillas en Room SQLite (Fase 6).
 * Asegura sincronización 100% idéntica entre POS, Inventario y Clientes desde Room.
 */
object DatabaseSeeder {

    suspend fun seedIfEmpty(db: AppDatabase) {
        // La aplicación empieza vacía. Los datos se crean únicamente desde la UI
        // o se descargan desde Supabase después de autenticar al usuario.
    }
}
