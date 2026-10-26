package com.example.dajho.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dajho.data.local.dao.CashDao
import com.example.dajho.data.local.dao.ClientSupplierDao
import com.example.dajho.data.local.dao.ProductDao
import com.example.dajho.data.local.dao.SaleDao
import com.example.dajho.data.local.datastore.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardUiState(
    val userName: String = "",
    val businessName: String = "",
    val todaySalesCents: Long = 0L,
    val todayBalanceCents: Long = 0L,
    val todayExpensesCents: Long = 0L,
    val lowStockCount: Int = 0,
    val debtorsCount: Int = 0,
    val totalDebtCents: Long = 0L,
    val isCashOpen: Boolean = false,
    val topProductName: String = ""
)

/**
 * ViewModel para conectar el Dashboard con datos reales de Room y DataStore (Fase 10).
 */
class DashboardViewModel(
    private val userPreferences: UserPreferences,
    private val saleDao: SaleDao,
    private val productDao: ProductDao,
    private val clientSupplierDao: ClientSupplierDao,
    private val cashDao: CashDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    fun loadDashboardData(targetCalendar: Calendar = Calendar.getInstance()) {
        viewModelScope.launch {
            try {
                val userName = userPreferences.userNameFlow.first().orEmpty()
                val activeShift = cashDao.getActiveShiftFlow().firstOrNull()

                val calStart = (targetCalendar.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val calEnd = (targetCalendar.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }

                val salesCents = saleDao.getSalesTotalForDateRange(calStart.timeInMillis, calEnd.timeInMillis)
                val lowStock = productDao.getLowStockVariantsCount()
                val debtors = clientSupplierDao.getDebtorsCount()
                val totalDebt = clientSupplierDao.getTotalClientsDebt()

                _uiState.value = DashboardUiState(
                    userName = userName,
                    businessName = "",
                    todaySalesCents = salesCents,
                    todayBalanceCents = salesCents,
                    todayExpensesCents = 0L,
                    lowStockCount = lowStock,
                    debtorsCount = debtors,
                    totalDebtCents = totalDebt,
                    isCashOpen = (activeShift != null && activeShift.isOpen),
                    topProductName = if (salesCents > 0) "Consulta el detalle de ventas" else "Registra ventas para ver tu producto estrella"
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
