package com.example.dajho

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dajho.admin.AuditAdminAdapter
import com.example.dajho.admin.BusinessAdminAdapter
import com.example.dajho.admin.UserAdminAdapter
import com.example.dajho.data.local.datastore.UserPreferences
import com.google.android.material.button.MaterialButton
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray

/**
 * Panel de Superusuario / Administrador Global DAJHO (dajho1617@gmail.com) con KPIs y RecyclerViews profesionales.
 */
class SystemAdminDashboardActivity : AppCompatActivity() {

    private lateinit var userPreferences: UserPreferences
    private lateinit var tvSectionTitle: TextView
    private lateinit var rvAdminContent: RecyclerView
    private lateinit var tvKpiBusinessCount: TextView
    private lateinit var tvKpiUserCount: TextView
    private lateinit var tvKpiAuditCount: TextView

    private lateinit var btnTabBusinesses: MaterialButton
    private lateinit var btnTabUsers: MaterialButton
    private lateinit var btnTabAudit: MaterialButton

    private companion object {
        const val TAG = "SystemAdminDashboard"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system_admin_dashboard)

        userPreferences = UserPreferences(this)
        tvSectionTitle = findViewById(R.id.tvSectionTitle)
        rvAdminContent = findViewById(R.id.rvAdminContent)
        rvAdminContent.layoutManager = LinearLayoutManager(this)

        tvKpiBusinessCount = findViewById(R.id.tvKpiBusinessCount)
        tvKpiUserCount = findViewById(R.id.tvKpiUserCount)
        tvKpiAuditCount = findViewById(R.id.tvKpiAuditCount)

        btnTabBusinesses = findViewById(R.id.btnTabBusinesses)
        btnTabUsers = findViewById(R.id.btnTabUsers)
        btnTabAudit = findViewById(R.id.btnTabAudit)

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarAdmin)?.subtitle = "dajho1617@gmail.com"

        findViewById<Button>(R.id.btnLogoutAdmin)?.setOnClickListener {
            lifecycleScope.launch {
                userPreferences.clearSession()
                Toast.makeText(this@SystemAdminDashboardActivity, "Sesión de administrador cerrada", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@SystemAdminDashboardActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        findViewById<ImageButton>(R.id.btnRefresh)?.setOnClickListener {
            loadAllKpisAndActiveTab()
        }

        btnTabBusinesses.setOnClickListener {
            setActiveTab(0)
            loadBusinesses()
        }

        btnTabUsers.setOnClickListener {
            setActiveTab(1)
            loadUsers()
        }

        btnTabAudit.setOnClickListener {
            setActiveTab(2)
            loadAuditLogs()
        }

        loadAllKpisAndActiveTab()
        loadBusinesses()
    }

    private fun setActiveTab(index: Int) {
        val primaryColor = Color.parseColor("#B71C1C")
        val tonalColor = Color.parseColor("#E0E0E0")
        val textColorPrimary = Color.WHITE
        val textColorTonal = Color.GRAY

        btnTabBusinesses.setBackgroundColor(if (index == 0) primaryColor else tonalColor)
        btnTabBusinesses.setTextColor(if (index == 0) textColorPrimary else textColorTonal)

        btnTabUsers.setBackgroundColor(if (index == 1) primaryColor else tonalColor)
        btnTabUsers.setTextColor(if (index == 1) textColorPrimary else textColorTonal)

        btnTabAudit.setBackgroundColor(if (index == 2) primaryColor else tonalColor)
        btnTabAudit.setTextColor(if (index == 2) textColorPrimary else textColorTonal)
    }

    private fun loadAllKpisAndActiveTab() {
        lifecycleScope.launch {
            try {
                val accessToken = userPreferences.accessTokenFlow.first().orEmpty()
                val client = HttpClient(Android)

                val respBiz = client.get("${BuildConfig.SUPABASE_URL}/rest/v1/negocios?select=id") {
                    header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                }
                if (respBiz.status.isSuccess()) {
                    tvKpiBusinessCount.text = JSONArray(respBiz.bodyAsText()).length().toString()
                }

                val respUsers = client.get("${BuildConfig.SUPABASE_URL}/rest/v1/perfiles?select=usuario_id") {
                    header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                }
                if (respUsers.status.isSuccess()) {
                    tvKpiUserCount.text = JSONArray(respUsers.bodyAsText()).length().toString()
                }

                val respAudit = client.get("${BuildConfig.SUPABASE_URL}/rest/v1/registros_auditoria?select=id") {
                    header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                }
                if (respAudit.status.isSuccess()) {
                    tvKpiAuditCount.text = JSONArray(respAudit.bodyAsText()).length().toString()
                }

                client.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando KPIs: ${e.message}", e)
            }
        }
    }

    private fun loadBusinesses() {
        tvSectionTitle.text = "Negocios Registrados"
        lifecycleScope.launch {
            try {
                val accessToken = userPreferences.accessTokenFlow.first().orEmpty()
                val client = HttpClient(Android)
                val response = client.get("${BuildConfig.SUPABASE_URL}/rest/v1/negocios?select=*&order=creado_en.desc") {
                    header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                }
                val body = response.bodyAsText()
                client.close()

                if (response.status.isSuccess()) {
                    val array = JSONArray(body)
                    tvKpiBusinessCount.text = array.length().toString()
                    rvAdminContent.adapter = BusinessAdminAdapter(array)
                } else {
                    Toast.makeText(this@SystemAdminDashboardActivity, "Error al cargar negocios", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SystemAdminDashboardActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUsers() {
        tvSectionTitle.text = "Usuarios y Roles del Sistema"
        lifecycleScope.launch {
            try {
                val accessToken = userPreferences.accessTokenFlow.first().orEmpty()
                val client = HttpClient(Android)
                val response = client.get("${BuildConfig.SUPABASE_URL}/rest/v1/perfiles?select=*&order=creado_en.desc") {
                    header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                }
                val body = response.bodyAsText()
                client.close()

                if (response.status.isSuccess()) {
                    val array = JSONArray(body)
                    tvKpiUserCount.text = array.length().toString()
                    rvAdminContent.adapter = UserAdminAdapter(array)
                } else {
                    Toast.makeText(this@SystemAdminDashboardActivity, "Error al cargar perfiles", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SystemAdminDashboardActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadAuditLogs() {
        tvSectionTitle.text = "Registros de Auditoría Global"
        lifecycleScope.launch {
            try {
                val accessToken = userPreferences.accessTokenFlow.first().orEmpty()
                val client = HttpClient(Android)
                val response = client.get("${BuildConfig.SUPABASE_URL}/rest/v1/registros_auditoria?select=*&order=creado_en.desc&limit=50") {
                    header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                }
                val body = response.bodyAsText()
                client.close()

                if (response.status.isSuccess()) {
                    val array = JSONArray(body)
                    tvKpiAuditCount.text = array.length().toString()
                    rvAdminContent.adapter = AuditAdminAdapter(array)
                } else {
                    Toast.makeText(this@SystemAdminDashboardActivity, "Error al cargar auditoría", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SystemAdminDashboardActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
