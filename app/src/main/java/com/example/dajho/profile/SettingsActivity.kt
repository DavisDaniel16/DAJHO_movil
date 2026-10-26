package com.example.dajho.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dajho.R
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.data.local.backup.BackupManager
import com.example.dajho.data.local.datastore.UserPreferences
import com.example.dajho.data.repository.AuthRepositoryImpl
import com.example.dajho.data.sync.SyncManager
import kotlinx.coroutines.launch
import java.io.File

/**
 * Actividad para configuraciones generales del comercio, exportación e importación de respaldos locales (Fase 16).
 */
class SettingsActivity : AppCompatActivity() {

    private val importBackupLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { fileUri ->
            processBackupImportUri(fileUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btnBack = findViewById<ImageButton>(R.id.btnBackSettings)
        btnBack?.setOnClickListener { finish() }

        val cardTestConnection = findViewById<View>(R.id.cardTestConnection)
        cardTestConnection?.setOnClickListener {
            Toast.makeText(this, "Probando conexión con Supabase... Revisa el Logcat", Toast.LENGTH_LONG).show()
            lifecycleScope.launch {
                try {
                    val db = AppDatabase.getInstance(this@SettingsActivity)
                    val authRepo = AuthRepositoryImpl(
                        db.userProfileDao(),
                        db.businessDao(),
                        db.syncOperationDao(),
                        UserPreferences(this@SettingsActivity)
                    )
                    authRepo.probarConexionSupabase()
                    Toast.makeText(this@SettingsActivity, "Prueba ejecutada. Revisa Logcat (TAG: AuthRepository)", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@SettingsActivity, "Error en la prueba: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun processBackupImportUri(uri: Uri) {
        lifecycleScope.launch {
            try {
                val tempFile = File(cacheDir, "import_temp.dajhobak")
                contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                // 1. Validar copia antes de restaurar
                val validationResult = BackupManager.validateBackupFile(tempFile)

                validationResult.fold(
                    onSuccess = { payloadJson ->
                        // 2. Confirmar restauración mediante AlertDialog
                        AlertDialog.Builder(this@SettingsActivity)
                            .setTitle("Confirmar Restauración de Respaldo")
                            .setMessage("Se ha validado el archivo de respaldo correctamente.\n\n¿Deseas restaurar la copia de seguridad?")
                            .setPositiveButton("Restaurar") { _, _ ->
                                Toast.makeText(this@SettingsActivity, "Respaldo restaurado con éxito", Toast.LENGTH_SHORT).show()
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                    },
                    onFailure = { ex ->
                        Toast.makeText(this@SettingsActivity, "Error de validación: ${ex.message}", Toast.LENGTH_LONG).show()
                    }
                )
            } catch (ex: Exception) {
                Toast.makeText(this@SettingsActivity, "Error al importar el archivo: ${ex.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
