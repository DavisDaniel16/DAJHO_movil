package com.example.dajho

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.dajho.catalog.ProductCatalogActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

class HomeActivity : AppCompatActivity() {

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
            // TODO: Open Profile Activity
        }

        // 3. Quick Actions Click Listeners
        findViewById<View>(R.id.cardNewSale).setOnClickListener {
            // TODO: Open New Sale Activity
        }
        findViewById<View>(R.id.cardInventory).setOnClickListener {
            startActivity(Intent(this, ProductCatalogActivity::class.java))
        }
        findViewById<View>(R.id.cardClients).setOnClickListener {
            // TODO: Open Clients Activity
        }
        findViewById<View>(R.id.cardReports).setOnClickListener {
            // TODO: Open Reports Activity
        }

        // 4. Logout Button Click
        val btnLogout = findViewById<AppCompatButton>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // 5. Bottom Navigation View
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_home
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_sales -> true
                R.id.nav_inventory -> {
                    startActivity(Intent(this, ProductCatalogActivity::class.java))
                    false
                }
                R.id.nav_clients -> true
                R.id.nav_settings -> true
                else -> false
            }
        }
    }
}
