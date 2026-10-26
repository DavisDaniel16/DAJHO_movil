package com.example.dajho.profile

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dajho.LoginActivity
import com.example.dajho.R
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.data.local.datastore.UserPreferences
import com.example.dajho.data.sync.SyncManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Pantalla de Menú de Perfil, Comercio y Sincronización Manual (Fase 16).
 */
class ProfileActivity : AppCompatActivity() {

    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userPreferences = UserPreferences(this)

        val btnBack = findViewById<ImageButton>(R.id.btnBackProfile)
        val cardEditProfile = findViewById<android.view.View>(R.id.cardEditProfile)
        val tvUserName = findViewById<TextView>(R.id.tvUserNameProfile)
        val tvUserPhone = findViewById<TextView>(R.id.tvUserPhoneProfile)
        val btnConfiguraciones = findViewById<android.view.View>(R.id.btnConfiguraciones)
        val btnPrinter = findViewById<android.view.View>(R.id.btnPrinterConfig)
        val btnLogout = findViewById<TextView>(R.id.btnLogout)

        // Botón de Impresora desactivado con explicación transparente
        btnPrinter?.isEnabled = false
        btnPrinter?.alpha = 0.5f

        btnBack?.setOnClickListener { finish() }

        lifecycleScope.launch {
            val userName = userPreferences.userNameFlow.first()
            val userEmail = userPreferences.userEmailFlow.first()
            tvUserName?.text = userName
            tvUserPhone?.text = userEmail.orEmpty()
        }

        cardEditProfile?.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        btnConfiguraciones?.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        btnPrinter?.setOnClickListener {
            Toast.makeText(this, "Impresión térmica Bluetooth/USB no configurada", Toast.LENGTH_SHORT).show()
        }

        btnLogout?.setOnClickListener {
            lifecycleScope.launch {
                userPreferences.clearSession()
                Toast.makeText(this@ProfileActivity, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}
