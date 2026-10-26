package com.example.dajho.data.sync

/**
 * Estados visuales de la sincronización offline-first para la interfaz de usuario (Fase 14).
 */
enum class SyncState(val label: String, val isPending: Boolean) {
    SYNCED("🟢 Sincronizado", false),
    PENDING_NETWORK("🟠 Pendiente de red", true),
    SYNCING("🔄 Sincronizando...", true),
    ERROR("⚠️ Error en sincronización", true)
}
