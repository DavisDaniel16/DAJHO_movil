package com.example.dajho

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.LinearLayout
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.data.local.datastore.UserPreferences
import com.example.dajho.data.repository.AuthRepositoryImpl
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

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

        // Filtro de entrada para el nombre: solo letras, acentos y espacios
        etFullName?.filters = arrayOf(
            android.text.InputFilter { source, _, _, _, _, _ ->
                for (i in 0 until source.length) {
                    val c = source[i]
                    if (!c.isLetter() && !c.isWhitespace() && c !in "áéíóúÁÉÍÓÚñÑüÜ") {
                        return@InputFilter ""
                    }
                }
                null
            }
        )

        // Filtro de entrada para el teléfono: solo dígitos numéricos (0-9)
        etPhone?.filters = arrayOf(
            android.text.InputFilter { source, _, _, _, _, _ ->
                for (i in 0 until source.length) {
                    if (!source[i].isDigit()) {
                        return@InputFilter ""
                    }
                }
                null
            }
        )
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnCreateAccount = findViewById<AppCompatButton>(R.id.btnCreateAccount)
        val tvLoginLink = findViewById<TextView>(R.id.tvLoginLink)
        val tvStep = findViewById<TextView>(R.id.tvSignUpStep)
        val businessFields = findViewById<LinearLayout>(R.id.businessFieldsContainer)
        val etBusinessName = findViewById<TextInputEditText>(R.id.etBusinessName)
        val etBusinessRuc = findViewById<TextInputEditText>(R.id.etBusinessRuc)
        val etBusinessAddress = findViewById<TextInputEditText>(R.id.etBusinessAddress)
        val etBusinessPhone = findViewById<TextInputEditText>(R.id.etBusinessPhone)
        val spinnerRimpe = findViewById<Spinner>(R.id.spinnerRimpe)
        var businessStep = false
        btnCreateAccount?.text = "CONTINUAR"
        spinnerRimpe.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("RIMPE Popular", "RIMPE Emprendedor", "Régimen General")
        )

        btnBack?.setOnClickListener {
            if (businessStep) {
                businessStep = false
                tvStep?.text = "Paso 1 de 2"
                businessFields?.visibility = android.view.View.GONE
                btnCreateAccount?.text = "CONTINUAR"
            } else {
                finish()
            }
        }
        tvLoginLink?.setOnClickListener { finish() }

        btnCreateAccount?.setOnClickListener {
            val fullName = etFullName?.text?.toString()?.trim().orEmpty()
            val phone = etPhone?.text?.toString()?.trim().orEmpty()
            val email = etEmail?.text?.toString()?.trim().orEmpty()
            val password = etPassword?.text?.toString()?.trim().orEmpty()

            // 1. Validacion de nombre (Solo letras, acentos y espacios, mínimo 3 caracteres)
            val nameRegex = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$")
            if (fullName.isBlank() || fullName.length < 3 || !nameRegex.matches(fullName)) {
                Toast.makeText(this, "El nombre debe tener al menos 3 letras y solo contener letras y espacios", Toast.LENGTH_LONG).show()
                etFullName?.error = "Nombre inválido"
                return@setOnClickListener
            }

            // 2. Validacion de teléfono (Solo números, 7 a 15 dígitos)
            val phoneRegex = Regex("^[0-9]{7,15}$")
            if (!phoneRegex.matches(phone)) {
                Toast.makeText(this, "El número de teléfono solo debe contener números (entre 7 y 15 dígitos)", Toast.LENGTH_LONG).show()
                etPhone?.error = "Teléfono inválido"
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

            if (!businessStep) {
                businessStep = true
                tvStep?.text = "Paso 2 de 2"
                businessFields?.visibility = android.view.View.VISIBLE
                btnCreateAccount?.text = "CREAR CUENTA"
                etBusinessPhone?.setText(phone)
                return@setOnClickListener
            }

            val businessName = etBusinessName?.text?.toString()?.trim().orEmpty()
            val rucOrCedula = etBusinessRuc?.text?.toString()?.trim().orEmpty()
            val businessAddress = etBusinessAddress?.text?.toString()?.trim().orEmpty()
            val businessPhone = etBusinessPhone?.text?.toString()?.trim().orEmpty()
            if (businessName.isBlank()) {
                etBusinessName?.error = "Ingresa el nombre del negocio"
                return@setOnClickListener
            }
            if (businessPhone.length < 9) {
                etBusinessPhone?.error = "Ingresa un teléfono válido"
                return@setOnClickListener
            }
            val rimpeType = when (spinnerRimpe?.selectedItemPosition ?: 0) {
                1 -> "RIMPE_EMPRENDEDOR"
                2 -> "GENERAL"
                else -> "RIMPE_POPULAR"
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
                val result = authRepo.registerLocalUser(
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    businessName = businessName,
                    passwordHash = password,
                    rucOrCedula = rucOrCedula,
                    businessPhone = businessPhone,
                    address = businessAddress,
                    rimpeType = rimpeType
                )

                result.fold(
                    onSuccess = {
                        val loggedIn = userPreferences.isLoggedInFlow.first()
                        val intent = if (loggedIn) {
                            Toast.makeText(this@SignUpActivity, "¡Registro exitoso! Sesión iniciada.", Toast.LENGTH_SHORT).show()
                            Intent(this@SignUpActivity, HomeActivity::class.java)
                        } else {
                            Toast.makeText(this@SignUpActivity, "Cuenta creada. Confirma tu correo y luego inicia sesión.", Toast.LENGTH_LONG).show()
                            Intent(this@SignUpActivity, LoginActivity::class.java)
                        }
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    },
                    onFailure = { ex ->
                        val errorMsg = ex.message ?: "Error al registrar"
                        Toast.makeText(this@SignUpActivity, errorMsg, Toast.LENGTH_LONG).show()
                        android.app.AlertDialog.Builder(this@SignUpActivity)
                            .setTitle("Error de Registro")
                            .setMessage(errorMsg)
                            .setPositiveButton("Aceptar", null)
                            .show()
                    }
                )
            }
        }
    }
}
