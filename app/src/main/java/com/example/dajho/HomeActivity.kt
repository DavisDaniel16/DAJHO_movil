package com.example.dajho

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.dajho.cash.CashDebtFragment
import com.example.dajho.catalog.ProductCatalogFragment
import com.example.dajho.clients.ClientListFragment
import com.example.dajho.data.local.datastore.UserPreferences
import com.example.dajho.data.sync.SyncManager
import com.example.dajho.pos.PosFragment
import com.example.dajho.profile.ProfileActivity
import com.example.dajho.profile.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

/**
 * Activity principal con diseño y arquitectura oficial estilo Treinta (Fase 14).
 * Banner superior amarillo, navegación de 5 pestañas, sincronización automática con WorkManager.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var userPreferences: UserPreferences

    private val homeDashboardFragment by lazy {
        HomeDashboardFragment().apply {
            onNavigateRequested = { navItemId ->
                if (::bottomNavigation.isInitialized) {
                    bottomNavigation.selectedItemId = navItemId
                }
            }
        }
    }
    private val posFragment by lazy { PosFragment() }
    private val productCatalogFragment by lazy { ProductCatalogFragment() }
    private val clientListFragment by lazy { ClientListFragment() }
    private val cashDebtFragment by lazy { CashDebtFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_home)

            userPreferences = UserPreferences(this)

            // Programar sincronización periódica en segundo plano
            SyncManager.schedulePeriodicSync(this)

            // 1. Nombre real del usuario desde DataStore
            val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
            lifecycleScope.launch {
                userPreferences.userNameFlow.collect { name ->
                    tvGreeting?.text = name
                }
            }

            // 2. Profile Button Click -> Open ProfileActivity
            val btnProfile = findViewById<ImageButton>(R.id.btnProfile)
            btnProfile?.setOnClickListener {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }

            // 3. Search Icon Click -> Sincronización Manual + Abre Buscador
            val btnSearch = findViewById<ImageButton>(R.id.btnHeaderSearch)
            btnSearch?.setOnClickListener {
                SyncManager.triggerImmediateSync(this)
                Toast.makeText(this, "Sincronizando datos con Supabase...", Toast.LENGTH_SHORT).show()
                val searchSheet = GlobalSearchSheet()
                searchSheet.show(supportFragmentManager, GlobalSearchSheet.TAG)
            }

            // 4. Gear Settings Icon Click -> Open SettingsActivity
            val btnSettings = findViewById<ImageButton>(R.id.btnHeaderFilter)
            btnSettings?.setOnClickListener {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }

            // 5. Calendar Date Picker Click -> Open GlobalSearchSheet or Date Picker
            val btnCalendar = findViewById<android.widget.ImageView>(R.id.btnCalendarPicker)
            btnCalendar?.setOnClickListener {
                val searchSheet = GlobalSearchSheet()
                searchSheet.show(supportFragmentManager, GlobalSearchSheet.TAG)
            }

            // 6. Bottom Navigation View setup (Treinta style: Inicio, Balance, Venta, Deudas, Inventario)
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
                    R.id.nav_balance -> {
                        loadFragment(cashDebtFragment)
                        true
                    }
                    R.id.nav_sales -> {
                        loadFragment(posFragment)
                        true
                    }
                    R.id.nav_clients -> {
                        loadFragment(clientListFragment)
                        true
                    }
                    R.id.nav_inventory -> {
                        loadFragment(productCatalogFragment)
                        true
                    }
                    else -> false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.cardContainer, fragment)
                .commitAllowingStateLoss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
