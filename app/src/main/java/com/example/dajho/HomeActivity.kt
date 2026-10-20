package com.example.dajho

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Barra de estado con iconos claros (encabezado oscuro).
        // Barra de navegación con iconos oscuros (fondo claro).
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContentView(R.layout.activity_home)

        // 1. Ajustar el contenido a las barras del sistema (Edge-to-Edge)
        val root = findViewById<View>(R.id.rootHome)
        val header = findViewById<View>(R.id.flHeader)

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // El encabezado oscuro se extiende bajo la barra de estado
            header.updatePadding(top = bars.top + dp(24))

            // Evita que el botón inferior quede bajo la barra de navegación
            root.updatePadding(bottom = bars.bottom)

            insets
        }

        // 2. Referenciar el botón de cerrar sesión
        val btnLogout = findViewById<AppCompatButton>(R.id.btnLogout)

        // 3. Lógica para cerrar sesión
        btnLogout.setOnClickListener {
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)

            // Limpia el historial para que "Atrás" no regrese al Home sin iniciar sesión
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}