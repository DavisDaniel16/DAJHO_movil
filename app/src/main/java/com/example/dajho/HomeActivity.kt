package com.example.dajho

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.dajho.catalog.ProductCatalogFragment
import com.example.dajho.clients.ClientListFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

/**
 * Activity contenedora principal que aloja los modulos (Dashboard, Inventario, Clientes, POS).
 * Navegacion fluida sin abrir ventanas separadas.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    private val homeDashboardFragment by lazy {
        HomeDashboardFragment().apply {
            onModuleNavigate = { navItemId ->
                bottomNavigation.selectedItemId = navItemId
            }
        }
    }
    private val productCatalogFragment by lazy { ProductCatalogFragment() }
    private val clientListFragment by lazy { ClientListFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 1. Dynamic Greeting based on time of day
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greetingText = when (hour) {
            in 0..11 -> getString(R.string.greeting_morning)
            in 12..18 -> getString(R.string.greeting_afternoon)
            else -> getString(R.string.greeting_evening)
        }
        val userName = intent.getStringExtra("USER_NAME") ?: "Davis"
        tvGreeting.text = getString(R.string.user_greeting, greetingText, userName)

        // 2. Profile Button Click
        val btnProfile = findViewById<ImageButton>(R.id.btnProfile)
        btnProfile.setOnClickListener {
            Toast.makeText(this, "Perfil de comercio DAJHO", Toast.LENGTH_SHORT).show()
        }

        // 3. Bottom Navigation View setup
        bottomNavigation = findViewById(R.id.bottomNavigation)

        if (savedInstanceState == null) {
            loadFragment(homeDashboardFragment)
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(homeDashboardFragment)
                    true
                }
                R.id.nav_sales -> {
                    Toast.makeText(this, "Punto de Venta (POS) - Próxima Fase", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_inventory -> {
                    loadFragment(productCatalogFragment)
                    true
                }
                R.id.nav_clients -> {
                    loadFragment(clientListFragment)
                    true
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Ajustes del comercio", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.cardContainer, fragment)
            .commit()
    }
}
