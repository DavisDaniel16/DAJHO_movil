package com.example.dajho

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.data.local.datastore.UserPreferences
import com.example.dajho.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BusinessSetupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_setup)

        val preferences = UserPreferences(this)
        val name = findViewById<EditText>(R.id.etSetupBusinessName)
        val ruc = findViewById<EditText>(R.id.etSetupRuc)
        val phone = findViewById<EditText>(R.id.etSetupPhone)
        val address = findViewById<EditText>(R.id.etSetupAddress)
        val rimpe = findViewById<Spinner>(R.id.spinnerSetupRimpe)
        val save = findViewById<Button>(R.id.btnCompleteBusinessSetup)

        rimpe.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("RIMPE Popular", "RIMPE Emprendedor", "Régimen General")
        )
        save.setOnClickListener {
            val businessName = name.text.toString().trim()
            val businessPhone = phone.text.toString().trim()
            if (businessName.isBlank()) {
                name.error = "Ingresa el nombre del negocio"
                return@setOnClickListener
            }
            if (businessPhone.length < 9) {
                phone.error = "Ingresa un teléfono válido"
                return@setOnClickListener
            }
            save.isEnabled = false
            lifecycleScope.launch {
                val db = AppDatabase.getInstance(this@BusinessSetupActivity)
                val prefs = UserPreferences(this@BusinessSetupActivity)
                val email = prefs.userEmailFlow.first().orEmpty()
                val result = AuthRepositoryImpl(
                    db.userProfileDao(), db.businessDao(), db.syncOperationDao(), prefs
                ).completeBusinessSetup(
                    fullName = email.substringBefore("@"),
                    email = email,
                    phone = businessPhone,
                    businessName = businessName,
                    rucOrCedula = ruc.text.toString().trim(),
                    businessPhone = businessPhone,
                    address = address.text.toString().trim(),
                    rimpeType = when (rimpe.selectedItemPosition) {
                        1 -> "RIMPE_EMPRENDEDOR"
                        2 -> "GENERAL"
                        else -> "RIMPE_POPULAR"
                    }
                )
                result.onSuccess {
                    startActivity(
                        Intent(this@BusinessSetupActivity, HomeActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                }.onFailure {
                    save.isEnabled = true
                    val details = it.message ?: "No se pudo guardar el negocio"
                    Log.e(TAG, "Error al configurar el negocio: $details", it)
                    val detailsView = TextView(this@BusinessSetupActivity).apply {
                        setText(details)
                        textSize = 14f
                        setTextIsSelectable(true)
                        setPadding(48, 16, 48, 16)
                    }
                    AlertDialog.Builder(this@BusinessSetupActivity)
                        .setTitle("No se pudo configurar el negocio")
                        .setView(ScrollView(this@BusinessSetupActivity).apply {
                            addView(detailsView)
                        })
                        .setPositiveButton("Cerrar", null)
                        .show()
                }
            }
        }
    }

    private companion object {
        const val TAG = "BusinessSetup"
    }
}
