package com.example.dajho.cash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.dajho.R
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.util.toCurrencyString
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * BottomSheetDialogFragment para ver el desglose detallado de balance general con datos reales de Room (Fase 11).
 */
class BalanceDetailSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sheet_balance_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseBalanceDetail)
        val tvInitial = view.findViewById<TextView>(R.id.tvInitialCashDetail)
        val tvCashSales = view.findViewById<TextView>(R.id.tvCashSalesDetail)
        val tvAbonos = view.findViewById<TextView>(R.id.tvAbonosDetail)
        val tvExpenses = view.findViewById<TextView>(R.id.tvExpensesDetail)
        val tvNetBalance = view.findViewById<TextView>(R.id.tvNetBalanceDetail)

        btnClose.setOnClickListener { dismiss() }

        lifecycleScope.launch {
            try {
                val ctx = context ?: return@launch
                val db = AppDatabase.getInstance(ctx)
                val activeShift = db.cashDao().getActiveShiftFlow().firstOrNull()

                var initialCents = 0L
                var cashSalesCents = 0L
                var abonosCents = 0L
                var expensesCents = 0L

                if (activeShift != null) {
                    initialCents = activeShift.initialCashCents
                    val movements = db.cashDao().getMovementsForSessionFlow(activeShift.id).firstOrNull() ?: emptyList()

                    movements.forEach { m ->
                        if (m.movementType == "INGRESO") {
                            if (m.relatedSaleId != null) {
                                cashSalesCents += m.amountCents
                            } else {
                                abonosCents += m.amountCents
                            }
                        } else {
                            expensesCents += m.amountCents
                        }
                    }
                }

                val netBalanceCents = initialCents + cashSalesCents + abonosCents - expensesCents

                tvInitial?.text = initialCents.toCurrencyString()
                tvCashSales?.text = cashSalesCents.toCurrencyString()
                tvAbonos?.text = abonosCents.toCurrencyString()
                tvExpenses?.text = expensesCents.toCurrencyString()
                tvNetBalance?.text = netBalanceCents.toCurrencyString()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val TAG = "BalanceDetailSheet"
    }
}
