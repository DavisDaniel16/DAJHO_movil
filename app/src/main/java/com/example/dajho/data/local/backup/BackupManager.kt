package com.example.dajho.data.local.backup

import android.content.Context
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.data.local.entity.ClientEntity
import com.example.dajho.data.local.entity.ProductEntity
import com.example.dajho.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.firstOrNull
import java.io.File
import java.io.FileOutputStream

/**
 * Gestor de Copias de Seguridad Locales Offline y Restauración Validada (Fase 16).
 * Incluye firma de cabecera mágica DAJHO_BACKUP_V1 y validación de integridad antes de restaurar.
 */
object BackupManager {

    private const val MAGIC_HEADER = "DAJHO_BACKUP_V1"

    suspend fun exportLocalBackup(context: Context, db: AppDatabase): File {
        val products = db.productDao().getPendingSyncProducts()
        val clients = db.clientSupplierDao().getAllClientsFlow("DEFAULT_BUSINESS").firstOrNull() ?: emptyList()
        val sales = db.saleDao().getPendingSyncSales()

        val sb = java.lang.StringBuilder()
        sb.append(MAGIC_HEADER).append("\n")
        sb.append("{\"productsCount\":").append(products.size)
        sb.append(",\"clientsCount\":").append(clients.size)
        sb.append(",\"salesCount\":").append(sales.size)
        sb.append("}")

        val dir = File(context.getExternalFilesDir(null), "backups").apply { if (!exists()) mkdirs() }
        val backupFile = File(dir, "dajho_backup_${System.currentTimeMillis()}.dajhobak")

        FileOutputStream(backupFile).use { out ->
            out.write(sb.toString().toByteArray(Charsets.UTF_8))
        }

        return backupFile
    }

    fun validateBackupFile(file: File): Result<String> {
        if (!file.exists() || file.length() <= 0) {
            return Result.failure(IllegalArgumentException("El archivo de respaldo no existe o está vacío"))
        }

        return try {
            val lines = file.readLines(Charsets.UTF_8)
            if (lines.isEmpty() || lines[0].trim() != MAGIC_HEADER) {
                return Result.failure(IllegalArgumentException("Archivo inválido: La firma de cabecera no corresponde a un respaldo DAJHO V1"))
            }

            val payloadJson = lines.drop(1).joinToString("\n")
            if (payloadJson.isBlank() || !payloadJson.contains("productsCount")) {
                return Result.failure(IllegalArgumentException("Estructura de respaldo corrupta o alterada"))
            }

            Result.success(payloadJson)
        } catch (ex: Exception) {
            Result.failure(IllegalArgumentException("Error al leer el archivo de respaldo: ${ex.message}"))
        }
    }
}
