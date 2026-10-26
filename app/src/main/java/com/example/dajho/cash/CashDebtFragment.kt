package com.example.dajho.cash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dajho.R
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.data.local.entity.CashSessionEntity
import com.example.dajho.util.toCentsLong
import com.example.dajho.util.toCurrencyString
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Módulo de Balance, Caja y Arqueo 100% Conectado a Room (Fase 9).
 */
class CashDebtFragment : Fragment() {

    private enum class CashTab { INGRESOS, EGRESOS }
    private var currentTab = CashTab.INGRESOS

    private var activeShift: CashSessionEntity? = null
    private var isCashOpen = false

    private var initialCashCents = 0L
    private var cashSalesCents = 0L
    private var collectedDebtsCents = 0L
    private var totalExpensesCents = 0L

    private val incomeList = mutableListOf<CashTransaction>()
    private val expenseList = mutableListOf<CashTransaction>()
    private val visibleTransactions = mutableListOf<CashTransaction>()
    private lateinit var transactionAdapter: TransactionAdapter

    private lateinit var tvTotalBalance: TextView
    private lateinit var tvIngresos: TextView
    private lateinit var tvEgresos: TextView
    private lateinit var tabIngresos: TextView
    private lateinit var tabEgresos: TextView
    private lateinit var tabUnderline: View
    private lateinit var tvStatus: TextView
    private lateinit var llCashDetails: View
    private lateinit var tvInitialCash: TextView
    private lateinit var tvCashSales: TextView
    private lateinit var tvCollectedDebts: TextView
    private lateinit var tvExpectedCash: TextView
    private lateinit var btnToggleCash: Button
    private lateinit var rvTransactions: RecyclerView
    private lateinit var tvEmptyTransactions: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cash_debt, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTotalBalance = view.findViewById(R.id.tvTotalBalanceAmount)
        tvIngresos = view.findViewById(R.id.tvIngresosAmount)
        tvEgresos = view.findViewById(R.id.tvEgresosAmount)
        tabIngresos = view.findViewById(R.id.tabIngresos)
        tabEgresos = view.findViewById(R.id.tabEgresos)
        tabUnderline = view.findViewById(R.id.tabUnderlineCash)
        tvStatus = view.findViewById(R.id.tvCashRegisterStatus)
        llCashDetails = view.findViewById(R.id.llCashDetails)
        tvInitialCash = view.findViewById(R.id.tvInitialCashAmount)
        tvCashSales = view.findViewById(R.id.tvCashSalesAmount)
        tvCollectedDebts = view.findViewById(R.id.tvCollectedDebtsAmount)
        tvExpectedCash = view.findViewById(R.id.tvExpectedCashAmount)
        btnToggleCash = view.findViewById(R.id.btnToggleCash)
        rvTransactions = view.findViewById(R.id.rvCashTransactions)
        tvEmptyTransactions = view.findViewById(R.id.tvEmptyTransactions)

        transactionAdapter = TransactionAdapter(visibleTransactions)
        rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        rvTransactions.adapter = transactionAdapter

        setupCashControls()
        setupSubTabs()
        setupActionLinks(view)
        setupFloatingButtons(view)
        selectSubTab(CashTab.INGRESOS)
        observeRoomCashData()
    }

    override fun onResume() {
        super.onResume()
        observeRoomCashData()
    }

    private fun observeRoomCashData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val ctx = context ?: return@launch
            val db = AppDatabase.getInstance(ctx)

            db.cashDao().getActiveShiftFlow().collect { shift ->
                activeShift = shift
                isCashOpen = (shift != null && shift.isOpen)
                initialCashCents = shift?.initialCashCents ?: 0L

                val movements = if (shift != null) {
                    db.cashDao().getMovementsForSessionFlow(shift.id).firstOrNull() ?: emptyList()
                } else {
                    db.cashDao().getAllTransactionsFlow().firstOrNull() ?: emptyList()
                }

                incomeList.clear()
                expenseList.clear()

                cashSalesCents = 0L
                collectedDebtsCents = 0L
                totalExpensesCents = 0L

                movements.forEach { m ->
                    val tx = CashTransaction(
                        id = m.id.hashCode().toLong(),
                        concept = m.concept,
                        amount = m.amountCents / 100.0,
                        timestamp = "Hoy",
                        isIncome = (m.movementType == "INGRESO"),
                        category = if (m.movementType == "INGRESO") "Cobro en Efectivo" else "Gasto Operativo"
                    )

                    if (m.movementType == "INGRESO") {
                        incomeList.add(tx)
                        if (m.relatedSaleId != null) {
                            cashSalesCents += m.amountCents
                        } else {
                            collectedDebtsCents += m.amountCents
                        }
                    } else {
                        expenseList.add(tx)
                        totalExpensesCents += m.amountCents
                    }
                }

                filterTransactions()
            }
        }
    }

    private fun setupSubTabs() {
        tabIngresos.setOnClickListener { selectSubTab(CashTab.INGRESOS) }
        tabEgresos.setOnClickListener { selectSubTab(CashTab.EGRESOS) }
    }

    private fun selectSubTab(tab: CashTab) {
        currentTab = tab
        val ctx = context ?: return

        val colorActive = ContextCompat.getColor(ctx, R.color.btn_black)
        val colorInactive = ContextCompat.getColor(ctx, R.color.text_secondary)

        val params = tabUnderline.layoutParams as ConstraintLayout.LayoutParams
        if (tab == CashTab.INGRESOS) {
            tabIngresos.setTextColor(colorActive)
            tabIngresos.setTypeface(null, android.graphics.Typeface.BOLD)
            tabEgresos.setTextColor(colorInactive)
            tabEgresos.setTypeface(null, android.graphics.Typeface.NORMAL)

            params.startToStart = R.id.tabHeaderContainerCash
            params.endToEnd = ConstraintLayout.LayoutParams.UNSET
        } else {
            tabIngresos.setTextColor(colorInactive)
            tabIngresos.setTypeface(null, android.graphics.Typeface.NORMAL)
            tabEgresos.setTextColor(colorActive)
            tabEgresos.setTypeface(null, android.graphics.Typeface.BOLD)

            params.startToStart = ConstraintLayout.LayoutParams.UNSET
            params.endToEnd = R.id.tabHeaderContainerCash
        }
        tabUnderline.layoutParams = params

        filterTransactions()
    }

    private fun filterTransactions() {
        val targetList = if (currentTab == CashTab.INGRESOS) incomeList else expenseList
        visibleTransactions.clear()
        visibleTransactions.addAll(targetList)

        if (::transactionAdapter.isInitialized) {
            transactionAdapter.updateData(visibleTransactions)
            if (visibleTransactions.isEmpty()) {
                tvEmptyTransactions.visibility = View.VISIBLE
                tvEmptyTransactions.text = if (currentTab == CashTab.INGRESOS) "No tienes ingresos registrados en esta fecha." else "No tienes egresos registrados en esta fecha."
            } else {
                tvEmptyTransactions.visibility = View.GONE
            }
        }
        updateCashDisplay()
    }

    private fun setupActionLinks(view: View) {
        val btnDownloadReports = view.findViewById<TextView>(R.id.btnDownloadReports)
        val btnViewBalanceDetail = view.findViewById<TextView>(R.id.btnViewBalanceDetail)

        btnDownloadReports?.setOnClickListener {
            val reportsSheet = ReportsSheet()
            reportsSheet.show(childFragmentManager, ReportsSheet.TAG)
        }

        btnViewBalanceDetail?.setOnClickListener {
            val balanceSheet = BalanceDetailSheet()
            balanceSheet.show(childFragmentManager, BalanceDetailSheet.TAG)
        }
    }

    private fun setupCashControls() {
        btnToggleCash.setOnClickListener {
            if (isCashOpen && activeShift != null) {
                val closeSheet = CashCloseSheet.newInstance(getExpectedCashCents() / 100.0)
                closeSheet.onCashClosed = {
                    observeRoomCashData()
                }
                closeSheet.show(childFragmentManager, CashCloseSheet.TAG)
            } else {
                val openSheet = CashOpenSheet()
                openSheet.onCashOpened = {
                    observeRoomCashData()
                }
                openSheet.show(childFragmentManager, CashOpenSheet.TAG)
            }
        }
    }

    private fun setupFloatingButtons(view: View) {
        val btnNewSale = view.findViewById<Button>(R.id.btnNewSaleFloating)
        val btnNewExpense = view.findViewById<Button>(R.id.btnNewExpenseFloating)

        btnNewSale?.setOnClickListener {
            activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)?.selectedItemId = R.id.nav_sales
        }

        btnNewExpense?.setOnClickListener {
            val sheet = NewExpenseSheet()
            sheet.onExpenseRecorded = {
                observeRoomCashData()
            }
            sheet.show(childFragmentManager, NewExpenseSheet.TAG)
        }
    }

    private fun getExpectedCashCents(): Long =
        initialCashCents + cashSalesCents + collectedDebtsCents - totalExpensesCents

    private fun updateCashDisplay() {
        val ctx = context ?: return
        val totalIncomeCents = initialCashCents + cashSalesCents + collectedDebtsCents
        val netBalanceCents = getExpectedCashCents()

        tvTotalBalance.text = netBalanceCents.toCurrencyString()
        tvIngresos.text = totalIncomeCents.toCurrencyString()
        tvEgresos.text = totalExpensesCents.toCurrencyString()

        if (isCashOpen) {
            tvStatus.text = getString(R.string.status_cash_open)
            tvStatus.background = ContextCompat.getDrawable(ctx, R.drawable.bg_badge_green)
            tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.badge_green_text))

            llCashDetails.visibility = View.VISIBLE
            tvInitialCash.text = initialCashCents.toCurrencyString()
            tvCashSales.text = cashSalesCents.toCurrencyString()
            tvCollectedDebts.text = collectedDebtsCents.toCurrencyString()
            tvExpectedCash.text = netBalanceCents.toCurrencyString()

            btnToggleCash.text = getString(R.string.btn_close_cash)
        } else {
            tvStatus.text = getString(R.string.status_cash_closed)
            tvStatus.background = ContextCompat.getDrawable(ctx, R.drawable.bg_badge_red)
            tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.badge_red_text))

            llCashDetails.visibility = View.GONE
            btnToggleCash.text = getString(R.string.btn_open_cash)
        }
    }
}
