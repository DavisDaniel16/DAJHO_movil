package com.example.dajho

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.data.local.datastore.UserPreferences
import com.example.dajho.data.repository.AuthRepositoryImpl
import com.example.dajho.util.PasswordUtils
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

/**
 * Actividad de Registro con validaciones completas, hashing SHA-256 y guardado local (Fase 5).
 */
class SignUpActivity : AppCompatActivity() {

    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        userPreferences = UserPreferences(this)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val etFullName = findViewById<TextInputEditText>(R.id.etFullName)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnCreateAccount = findViewById<AppCompatButton>(R.id.btnCreateAccount)
        val tvLoginLink = findViewById<TextView>(R.id.tvLoginLink)

        btnBack?.setOnClickListener { finish() }
        tvLoginLink?.setOnClickListener { finish() }

        btnCreateAccount?.setOnClickListener {
            val fullName = etFullName?.text?.toString()?.trim().orEmpty()
            val phone = etPhone?.text?.toString()?.trim().orEmpty()
            val email = etEmail?.text?.toString()?.trim().orEmpty()
            val password = etPassword?.text?.toString()?.trim().orEmpty()

            // 1. Validacion de nombre
            if (fullName.isBlank()) {
                Toast.makeText(this, "Ingresa tu nombre completo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Validacion de teléfono (10 dígitos Ecuador)
            if (phone.length < 9) {
                Toast.makeText(this, "Ingresa un número de celular válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Validacion de correo
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 4. Validacion de contrasena
            if (password.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 5. Procesamiento de Registro Local seguro
            lifecycleScope.launch {
                val db = AppDatabase.getInstance(this@SignUpActivity)
                val authRepo = AuthRepositoryImpl(
                    db.userProfileDao(),
                    db.businessDao(),
                    db.syncOperationDao(),
                    userPreferences
                )

                // 6. Deteccion de correo duplicado
                val existing = authRepo.getUserByEmail(email)
                if (existing != null) {
                    Toast.makeText(this@SignUpActivity, "El correo electrónico ya se encuentra registrado", Toast.LENGTH_LONG).show()
                    return@launch
                }

                // 7. Persistencia del perfil y sesion activa
                val passHash = PasswordUtils.hashPassword(password)
                val result = authRepo.registerLocalUser(
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    businessName = "Comercio de $fullName",
                    passwordHash = passHash
                )

                result.fold(
                    onSuccess = {
                        Toast.makeText(this@SignUpActivity, "¡Registro exitoso! Sesión iniciada.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@SignUpActivity, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    },
                    onFailure = { ex ->
                        Toast.makeText(this@SignUpActivity, ex.message ?: "Error al registrar", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}
