package com.example.dajho

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.dajho.cash.CashDebtFragment
import com.example.dajho.catalog.ProductCatalogFragment
import com.example.dajho.clients.ClientListFragment
import com.example.dajho.data.local.datastore.UserPreferences
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.data.sync.SyncManager
import com.example.dajho.pos.PosFragment
import com.example.dajho.profile.ProfileActivity
import com.example.dajho.profile.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

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
                    tvGreeting?.text = name.orEmpty()
                }
            }
            lifecycleScope.launch {
                val businessId = userPreferences.businessIdFlow.first() ?: return@launch
                val business = AppDatabase.getInstance(this@HomeActivity)
                    .businessDao()
                    .getBusinessById(businessId)
                findViewById<TextView>(R.id.tvBusinessName)?.text = business?.name.orEmpty()
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

            setupDateSelector()

            // 6. Bottom Navigation View setup (Treinta style: Inicio, Balance, Venta, Deudas, Inventario)
            bottomNavigation = findViewById(R.id.bottomNavigation)

            if (savedInstanceState == null) {
                loadFragment(homeDashboardFragment)
            }

            bottomNavigation.setOnItemSelectedListener { item ->
                val dateStrip = findViewById<android.view.View>(R.id.dateStripBar)
                when (item.itemId) {
                    R.id.nav_home -> {
                        dateStrip?.visibility = android.view.View.VISIBLE
                        loadFragment(homeDashboardFragment)
                        true
                    }
                    R.id.nav_balance -> {
                        dateStrip?.visibility = android.view.View.GONE
                        loadFragment(cashDebtFragment)
                        true
                    }
                    R.id.nav_sales -> {
                        dateStrip?.visibility = android.view.View.GONE
                        loadFragment(posFragment)
                        true
                    }
                    R.id.nav_clients -> {
                        dateStrip?.visibility = android.view.View.GONE
                        loadFragment(clientListFragment)
                        true
                    }
                    R.id.nav_inventory -> {
                        dateStrip?.visibility = android.view.View.GONE
                        loadFragment(productCatalogFragment)
                        true
                    }
                    else -> false
                }
            }

        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo cargar la pantalla principal", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupDateSelector() {
        val chips = listOf(
            findViewById<TextView>(R.id.chipDateMinus3),
            findViewById<TextView>(R.id.chipDateMinus2),
            findViewById<TextView>(R.id.chipDateMinus1),
            findViewById<TextView>(R.id.chipDateSelected)
        )
        val dates = (3 downTo 0).map { offset ->
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }
        }
        val formatter = SimpleDateFormat("d MMM", Locale("es", "EC"))
        chips.forEachIndexed { index, chip ->
            chip.text = formatter.format(dates[index].time).lowercase(Locale("es", "EC"))

            // Estado inicial: solo el último (hoy, ej. 26 oct) seleccionado
            if (index == 3) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected)
                chip.setTextColor(getColor(R.color.btn_text_color))
                chip.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected)
                chip.setTextColor(getColor(R.color.btn_black))
                chip.setTypeface(null, android.graphics.Typeface.NORMAL)
            }

            chip.setOnClickListener {
                chips.forEach { c ->
                    c.setBackgroundResource(R.drawable.bg_chip_unselected)
                    c.setTextColor(getColor(R.color.btn_black))
                    c.setTypeface(null, android.graphics.Typeface.NORMAL)
                }
                chip.setBackgroundResource(R.drawable.bg_chip_selected)
                chip.setTextColor(getColor(R.color.btn_text_color))
                chip.setTypeface(null, android.graphics.Typeface.BOLD)
                homeDashboardFragment.selectDate(dates[index])
            }
        }
        findViewById<android.widget.ImageView>(R.id.btnCalendarPicker)?.setOnClickListener {
            val now = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    homeDashboardFragment.selectDate(Calendar.getInstance().apply {
                        set(year, month, day)
                    })
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
            ).show()
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
