package com.example.dajho.cash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dajho.R
import com.example.dajho.clients.Client
import java.util.Locale

/**
 * Módulo de Caja y Deudas (Fase 4).
 * Cuadre de caja (Apertura, Ventas, Abonos, Arqueo/Cierre) y Cuentas por Cobrar.
 */
class CashDebtFragment : Fragment() {

    private var isCashOpen = true
    private var initialCash = 20.00
    private var cashSales = 25.00
    private var collectedDebts = 15.00

    private val debtorsList = mutableListOf<Client>()
    private lateinit var debtorAdapter: DebtorAdapter

    private lateinit var tvStatus: TextView
    private lateinit var llCashDetails: View
    private lateinit var tvInitialCash: TextView
    private lateinit var tvCashSales: TextView
    private lateinit var tvCollectedDebts: TextView
    private lateinit var tvExpectedCash: TextView
    private lateinit var btnToggleCash: Button
    private lateinit var rvDebtors: RecyclerView
    private lateinit var tvEmptyDebtors: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cash_debt, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvStatus = view.findViewById(R.id.tvCashRegisterStatus)
        llCashDetails = view.findViewById(R.id.llCashDetails)
        tvInitialCash = view.findViewById(R.id.tvInitialCashAmount)
        tvCashSales = view.findViewById(R.id.tvCashSalesAmount)
        tvCollectedDebts = view.findViewById(R.id.tvCollectedDebtsAmount)
        tvExpectedCash = view.findViewById(R.id.tvExpectedCashAmount)
        btnToggleCash = view.findViewById(R.id.btnToggleCash)
        rvDebtors = view.findViewById(R.id.rvDebtors)
        tvEmptyDebtors = view.findViewById(R.id.tvEmptyDebtors)

        loadSeedData()
        setupDebtorsList()
        setupCashControls()
        updateCashDisplay()
    }

    private fun loadSeedData() {
        if (debtorsList.isEmpty()) {
            debtorsList.add(
                Client(2L, "Carlos Alberto Mendoza", "098 765 4321", "carlos@gmail.com", 28.50)
            )
            debtorsList.add(
                Client(3L, "Lucía Beatriz Ramos", "096 987 6543", "Mercado Central", 15.00)
            )
        }
    }

    private fun setupDebtorsList() {
        debtorAdapter = DebtorAdapter(debtorsList) { debtor ->
            val sheet = DebtPaymentSheet.newInstance(debtor)
            sheet.onPaymentRecorded = { amount ->
                // Actualizar la deuda del cliente
                debtor.debt = (debtor.debt - amount).coerceAtLeast(0.0)

                // Si la caja esta abierta, sumar el abono en efectivo a la caja
                if (isCashOpen) {
                    collectedDebts += amount
                    updateCashDisplay()
                }

                // Filtrar lista para mantener solo clientes con deuda activa
                updateDebtorsList()
            }
            sheet.show(childFragmentManager, DebtPaymentSheet.TAG)
        }
        rvDebtors.layoutManager = LinearLayoutManager(requireContext())
        rvDebtors.adapter = debtorAdapter
        updateDebtorsList()
    }

    private fun updateDebtorsList() {
        val activeDebtors = debtorsList.filter { it.debt > 0.001 }
        debtorAdapter.updateData(activeDebtors)
        tvEmptyDebtors.visibility = if (activeDebtors.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupCashControls() {
        btnToggleCash.setOnClickListener {
            if (isCashOpen) {
                // Abrir modal de Cierre de Caja / Arqueo
                val closeSheet = CashCloseSheet.newInstance(getExpectedCash())
                closeSheet.onCashClosed = {
                    isCashOpen = false
                    updateCashDisplay()
                }
                closeSheet.show(childFragmentManager, CashCloseSheet.TAG)
            } else {
                // Abrir modal de Apertura de Caja
                val openSheet = CashOpenSheet()
                openSheet.onCashOpened = { initial ->
                    isCashOpen = true
                    initialCash = initial
                    cashSales = 0.0
                    collectedDebts = 0.0
                    updateCashDisplay()
                }
                openSheet.show(childFragmentManager, CashOpenSheet.TAG)
            }
        }
    }

    private fun getExpectedCash(): Double = initialCash + cashSales + collectedDebts

    private fun updateCashDisplay() {
        val ctx = requireContext()
        if (isCashOpen) {
            tvStatus.text = getString(R.string.status_cash_open)
            tvStatus.background = ContextCompat.getDrawable(ctx, R.drawable.bg_badge_green)
            tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.badge_green_text))

            llCashDetails.visibility = View.VISIBLE
            tvInitialCash.text = String.format(Locale.US, "$%.2f", initialCash)
            tvCashSales.text = String.format(Locale.US, "$%.2f", cashSales)
            tvCollectedDebts.text = String.format(Locale.US, "$%.2f", collectedDebts)
            tvExpectedCash.text = String.format(Locale.US, "$%.2f", getExpectedCash())

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
