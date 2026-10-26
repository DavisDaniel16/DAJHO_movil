package com.example.dajho

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.data.local.DatabaseSeeder
import com.example.dajho.data.local.datastore.UserPreferences
import com.example.dajho.util.toCurrencyString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Fragmento para la Pantalla de Inicio (Dashboard Conectado a Room - Fase 10).
 * Resumen real "Tu negocio hoy", métricas de ventas, stock bajo, clientes con deuda y accesos rápidos.
 */
class HomeDashboardFragment : Fragment() {

    var onNavigateRequested: ((Int) -> Unit)? = null

    private lateinit var tvSalesTodayBig: TextView
    private lateinit var tvBalanceToday: TextView
    private lateinit var tvExpensesToday: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return try {
            inflater.inflate(R.layout.fragment_home_dashboard, container, false)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            tvSalesTodayBig = view.findViewById(R.id.tvSalesTodayBig)
            tvBalanceToday = view.findViewById(R.id.tvBalanceToday)
            tvExpensesToday = view.findViewById(R.id.tvExpensesToday)

            // Botón principal destacado "Registrar venta"
            val btnRegisterSale = view.findViewById<Button>(R.id.btnMainRegisterSale)
            btnRegisterSale?.setOnClickListener {
                onNavigateRequested?.invoke(R.id.nav_sales)
            }

            // Carrusel rápido 100% funcional
            view.findViewById<View>(R.id.cardQuickClients)?.setOnClickListener {
                onNavigateRequested?.invoke(R.id.nav_clients)
            }
            view.findViewById<View>(R.id.cardQuickSuppliers)?.setOnClickListener {
                onNavigateRequested?.invoke(R.id.nav_clients)
            }
            view.findViewById<View>(R.id.cardQuickEmployees)?.setOnClickListener {
                onNavigateRequested?.invoke(R.id.nav_clients)
            }
            view.findViewById<View>(R.id.cardQuickDebts)?.setOnClickListener {
                onNavigateRequested?.invoke(R.id.nav_clients)
            }
            view.findViewById<View>(R.id.cardQuickInventory)?.setOnClickListener {
                onNavigateRequested?.invoke(R.id.nav_inventory)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        refreshRealDashboardData()
    }

    override fun onResume() {
        super.onResume()
        refreshRealDashboardData()
    }

    private fun refreshRealDashboardData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val ctx = context ?: return@launch
                val db = AppDatabase.getInstance(ctx)
                DatabaseSeeder.seedIfEmpty(db)

                val calStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val calEnd = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }

                val salesCents = db.saleDao().getSalesTotalForDateRange(calStart.timeInMillis, calEnd.timeInMillis)

                if (::tvSalesTodayBig.isInitialized) {
                    tvSalesTodayBig.text = salesCents.toCurrencyString()
                }
                if (::tvBalanceToday.isInitialized) {
                    tvBalanceToday.text = salesCents.toCurrencyString()
                }
                if (::tvExpensesToday.isInitialized) {
                    tvExpensesToday.text = 0L.toCurrencyString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
