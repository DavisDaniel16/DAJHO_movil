package com.example.dajho.profile

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.example.dajho.R
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.data.local.datastore.UserPreferences
import com.example.dajho.data.local.entity.SyncOperationEntity
import com.example.dajho.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Actividad para editar información del perfil del comerciante con Persistencia en Room (Fase 16).
 */
class EditProfileActivity : AppCompatActivity() {

    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        userPreferences = UserPreferences(this)

        val btnBack = findViewById<ImageButton>(R.id.btnBackEditProfile)
        val btnSave = findViewById<AppCompatButton>(R.id.btnSaveProfile)
        val etFirstName = findViewById<EditText>(R.id.etProfileFirstName)
        val etLastName = findViewById<EditText>(R.id.etProfileLastName)
        val etPhone = findViewById<EditText>(R.id.etProfilePhone)
        val etEmail = findViewById<EditText>(R.id.etProfileEmail)

        btnBack?.setOnClickListener { finish() }

        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@EditProfileActivity)
            val currentEmail = userPreferences.userEmailFlow.first()
            if (!currentEmail.isNullOrBlank()) {
                val profile = db.userProfileDao().getUserByEmail(currentEmail)
                if (profile != null) {
                    val names = profile.fullName.split(" ")
                    etFirstName?.setText(names.firstOrNull() ?: "")
                    etLastName?.setText(names.drop(1).joinToString(" "))
                    etPhone?.setText(profile.phone)
                    etEmail?.setText(profile.email)
                }
            }
        }

        btnSave?.setOnClickListener {
            val firstName = etFirstName?.text?.toString()?.trim().orEmpty()
            val lastName = etLastName?.text?.toString()?.trim().orEmpty()
            val fullName = "$firstName $lastName".trim()
            val phone = etPhone?.text?.toString()?.trim().orEmpty()
            val email = etEmail?.text?.toString()?.trim().orEmpty()

            if (firstName.isBlank()) {
                Toast.makeText(this, "Ingresa tu nombre", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Ingresa un correo válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val db = AppDatabase.getInstance(this@EditProfileActivity)
                val currentEmail = userPreferences.userEmailFlow.first()
                val existingProfile = if (!currentEmail.isNullOrBlank()) db.userProfileDao().getUserByEmail(currentEmail) else null

                val userId = existingProfile?.userId ?: java.util.UUID.randomUUID().toString()
                val businessId = existingProfile?.businessId ?: "DEFAULT_BUSINESS"

                val updatedProfile = UserProfileEntity(
                    userId = userId,
                    businessId = businessId,
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    role = "PROPIETARIO",
                    updatedAt = System.currentTimeMillis()
                )

                db.userProfileDao().insertUserProfile(updatedProfile)
                db.syncOperationDao().insertOperation(
                    SyncOperationEntity(
                        entityName = "user_profiles",
                        entityId = userId,
                        operation = "UPDATE",
                        payloadJson = "{\"fullName\":\"$fullName\",\"phone\":\"$phone\"}"
                    )
                )

                userPreferences.saveSession(
                    userId = userId,
                    userName = fullName,
                    email = email,
                    businessId = businessId
                )

                Toast.makeText(this@EditProfileActivity, "Perfil guardado correctamente", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
