package com.example.dajho

import com.example.dajho.data.local.backup.BackupManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Pruebas unitarias para validación de copias de seguridad locales y firma mágica (Fase 16).
 */
class BackupValidationUnitTest {

    @Test
    fun validBackupFileHeaderTest() {
        val tempFile = File.createTempFile("test_backup", ".dajhobak").apply {
            writeText("DAJHO_BACKUP_V1\n{\"productsCount\":3,\"clientsCount\":2,\"salesCount\":1}")
            deleteOnExit()
        }

        val result = BackupManager.validateBackupFile(tempFile)

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull()?.contains("productsCount"))
    }

    @Test
    fun corruptBackupHeaderRejectionTest() {
        val tempFile = File.createTempFile("corrupt_backup", ".dajhobak").apply {
            writeText("CORRUPTED_HEADER_V9\n{\"productsCount\":3}")
            deleteOnExit()
        }

        val result = BackupManager.validateBackupFile(tempFile)

        assertFalse(result.isSuccess)
        assertEquals(true, result.exceptionOrNull()?.message?.contains("firma de cabecera"))
    }

    @Test
    fun emptyFileBackupRejectionTest() {
        val tempFile = File.createTempFile("empty_backup", ".dajhobak").apply {
            writeText("")
            deleteOnExit()
        }

        val result = BackupManager.validateBackupFile(tempFile)

        assertFalse(result.isSuccess)
        assertEquals(true, result.exceptionOrNull()?.message?.contains("vacío"))
    }
}
