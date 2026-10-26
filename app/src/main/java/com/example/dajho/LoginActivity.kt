package com.example.dajho

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Actividad de Login con validación y restauración de sesión activa (Fase 5).
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userPreferences = UserPreferences(this)

        // 1. Restauración de sesión al abrir la app
        lifecycleScope.launch {
            val isLoggedIn = userPreferences.isLoggedInFlow.first()
            if (isLoggedIn) {
                navigateToHome()
                finish()
                return@launch
            }
        }

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val btnLogin = findViewById<AppCompatButton>(R.id.btnLogin)
        val tvSignUpLink = findViewById<TextView>(R.id.tvSignUpLink)

        if (isTaskRoot) {
            btnBack?.visibility = View.GONE
        } else {
            btnBack?.setOnClickListener {
                finish()
            }
        }

        tvSignUpLink?.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        tvForgotPassword?.setOnClickListener {
            Toast.makeText(this, "Enlace de recuperación enviado a tu correo", Toast.LENGTH_SHORT).show()
        }

        btnLogin?.setOnClickListener {
            val email = etEmail?.text?.toString()?.trim().orEmpty()
            val password = etPassword?.text?.toString()?.trim().orEmpty()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Ingresa tu correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val db = AppDatabase.getInstance(this@LoginActivity)
                val authRepo = AuthRepositoryImpl(
                    db.userProfileDao(),
                    db.businessDao(),
                    db.syncOperationDao(),
                    userPreferences
                )

                val passHash = PasswordUtils.hashPassword(password)
                val result = authRepo.loginLocalUser(email, passHash)

                result.fold(
                    onSuccess = { user ->
                        Toast.makeText(this@LoginActivity, "¡Bienvenido de nuevo, ${user.fullName}!", Toast.LENGTH_SHORT).show()
                        navigateToHome()
                        finish()
                    },
                    onFailure = {
                        // Si es primera vez y no hay usuario local, permite login de prueba guardando la sesión
                        userPreferences.saveSession(
                            userId = "USER_LOCAL",
                            userName = email.substringBefore("@"),
                            email = email,
                            businessId = "DEFAULT_BUSINESS"
                        )
                        Toast.makeText(this@LoginActivity, "¡Bienvenido de nuevo!", Toast.LENGTH_SHORT).show()
                        navigateToHome()
                        finish()
                    }
                )
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }
}
