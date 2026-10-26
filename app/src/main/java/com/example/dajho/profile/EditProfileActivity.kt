package com.example.dajho.profile

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
import org.json.JSONObject

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
        val etDocumentNumber = findViewById<EditText>(R.id.etDocumentNumber)
        val etPhone = findViewById<EditText>(R.id.etProfilePhone)
        val etEmail = findViewById<EditText>(R.id.etProfileEmail)

        btnBack.setOnClickListener { finish() }

        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@EditProfileActivity)
            val currentEmail = userPreferences.userEmailFlow.first()
            if (!currentEmail.isNullOrBlank()) {
                val profile = db.userProfileDao().getUserByEmail(currentEmail)
                if (profile != null) {
                    val business = db.businessDao().getBusinessById(profile.businessId)
                    val names = profile.fullName.split(" ")
                    etFirstName.setText(names.firstOrNull().orEmpty())
                    etLastName.setText(names.drop(1).joinToString(" "))
                    etPhone.setText(profile.phone)
                    etEmail.setText(profile.email)
                    etDocumentNumber.setText(business?.rucOrCedula.orEmpty())
                }
            }
        }

        btnSave.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val fullName = "$firstName $lastName".trim()
            val documentNumber = etDocumentNumber.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (firstName.isBlank()) {
                etFirstName.error = "Ingresa tu nombre"
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Ingresa un correo válido"
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            lifecycleScope.launch {
                try {
                    val db = AppDatabase.getInstance(this@EditProfileActivity)
                    val currentEmail = userPreferences.userEmailFlow.first()
                    val existingProfile = currentEmail
                        ?.takeIf { it.isNotBlank() }
                        ?.let { db.userProfileDao().getUserByEmail(it) }
                    val userId = existingProfile?.userId
                        ?: userPreferences.userIdFlow.first()
                        ?: throw IllegalStateException("No hay un usuario autenticado")
                    val businessId = existingProfile?.businessId
                        ?: userPreferences.businessIdFlow.first()
                        ?: throw IllegalStateException("No hay un negocio activo")
                    val existingBusiness = db.businessDao().getBusinessById(businessId)
                        ?: throw IllegalStateException("No existe el negocio local")

                    val updatedProfile = UserProfileEntity(
                        userId = userId,
                        businessId = businessId,
                        fullName = fullName,
                        email = email,
                        phone = phone,
                        role = "PROPIETARIO",
                        updatedAt = System.currentTimeMillis()
                    )
                    val updatedBusiness = existingBusiness.copy(
                        rucOrCedula = documentNumber,
                        updatedAt = System.currentTimeMillis()
                    )

                    db.userProfileDao().insertUserProfile(updatedProfile)
                    db.businessDao().insertBusiness(updatedBusiness)
                    db.syncOperationDao().insertOperation(
                        SyncOperationEntity(
                            entityName = "user_profiles",
                            entityId = userId,
                            operation = "UPDATE",
                            payloadJson = JSONObject().apply {
                                put("usuario_id", userId)
                                put("negocio_id", businessId)
                                put("nombre_completo", fullName)
                                put("correo", email)
                                put("telefono", phone)
                                put("rol", "PROPIETARIO")
                            }.toString()
                        )
                    )
                    db.syncOperationDao().insertOperation(
                        SyncOperationEntity(
                            entityName = "businesses",
                            entityId = businessId,
                            operation = "UPDATE",
                            payloadJson = JSONObject().apply {
                                put("id", updatedBusiness.id)
                                put("nombre", updatedBusiness.name)
                                put("ruc_o_cedula", updatedBusiness.rucOrCedula)
                                put("telefono", updatedBusiness.phone)
                                put("direccion", updatedBusiness.address)
                                put("tipo_rimpe", updatedBusiness.rimpeType)
                            }.toString()
                        )
                    )

                    userPreferences.saveSession(
                        userId = userId,
                        userName = fullName,
                        email = email,
                        businessId = businessId
                    )

                    showMessage("Datos sincronizados")
                } catch (ex: Exception) {
                    btnSave.isEnabled = true
                    showMessage(ex.message ?: "No se pudo guardar el perfil")
                }
            }
        }
    }

    private fun showMessage(message: String) {
        val textView = TextView(this).apply {
            setText(message)
            textSize = 14f
            setTextIsSelectable(true)
            setPadding(48, 16, 48, 16)
        }
        AlertDialog.Builder(this)
            .setTitle("Resultado")
            .setView(ScrollView(this).apply { addView(textView) })
            .setPositiveButton("Cerrar", null)
            .show()
    }

}
