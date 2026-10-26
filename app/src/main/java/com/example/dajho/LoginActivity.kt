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
            val hasToken = !userPreferences.accessTokenFlow.first().isNullOrBlank()
            val hasBusiness = !userPreferences.businessIdFlow.first().isNullOrBlank()
            val email = userPreferences.userEmailFlow.first()
            if (isLoggedIn && hasToken) {
                if (email.equals("dajho1617@gmail.com", ignoreCase = true)) {
                    startActivity(Intent(this@LoginActivity, SystemAdminDashboardActivity::class.java))
                    finish()
                    return@launch
                } else if (hasBusiness) {
                    navigateToHome()
                    finish()
                    return@launch
                }
            }
            if (isLoggedIn) userPreferences.clearSession()
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
            val email = etEmail?.text?.toString()?.trim().orEmpty()
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail?.error = "Ingresa un correo válido"
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val db = AppDatabase.getInstance(this@LoginActivity)
                val result = AuthRepositoryImpl(
                    db.userProfileDao(), db.businessDao(), db.syncOperationDao(), userPreferences
                ).requestPasswordReset(email)
                result.onSuccess {
                    Toast.makeText(this@LoginActivity, "Revisa tu correo para restablecer la contraseña", Toast.LENGTH_LONG).show()
                }.onFailure {
                    Toast.makeText(this@LoginActivity, it.message ?: "No se pudo enviar el correo", Toast.LENGTH_LONG).show()
                }
            }
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

            // Acceso directo para Administrador Global
            if (email.equals("dajho1617@gmail.com", ignoreCase = true)) {
                if (password != "/852456Zx@@") {
                    Toast.makeText(this, "Contraseña de administrador incorrecta", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            lifecycleScope.launch {
                val db = AppDatabase.getInstance(this@LoginActivity)
                val authRepo = AuthRepositoryImpl(
                    db.userProfileDao(),
                    db.businessDao(),
                    db.syncOperationDao(),
                    userPreferences
                )

                val result = authRepo.loginLocalUser(email, password)

                result.fold(
                    onSuccess = { user ->
                        if (email.equals("dajho1617@gmail.com", ignoreCase = true)) {
                            Toast.makeText(this@LoginActivity, "¡Bienvenido, Administrador Global!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, SystemAdminDashboardActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "¡Bienvenido de nuevo, ${user.fullName}!", Toast.LENGTH_SHORT).show()
                            navigateToHome()
                            finish()
                        }
                    },
                    onFailure = { ex ->
                        if (email.equals("dajho1617@gmail.com", ignoreCase = true)) {
                            Toast.makeText(this@LoginActivity, "¡Bienvenido, Administrador Global!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, SystemAdminDashboardActivity::class.java))
                            finish()
                        } else if (ex.message == "SETUP_REQUIRED") {
                            startActivity(
                                Intent(this@LoginActivity, BusinessSetupActivity::class.java)
                            )
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                ex.message ?: "No se pudo iniciar sesión",
                                Toast.LENGTH_LONG
                            ).show()
                        }
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
