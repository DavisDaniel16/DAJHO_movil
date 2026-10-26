package com.example.dajho.data.local.entity

/**
 * Estados de sincronización offline-first con Supabase (Fase 2).
 */
enum class SyncStatus {
    PENDING_INSERT,
    PENDING_UPDATE,
    PENDING_DELETE,
    SYNCED
}
