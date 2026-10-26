package com.example.dajho.cash

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.dajho.R
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.util.toCentsLong
import com.example.dajho.util.toCurrencyString
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * BottomSheetDialogFragment para Cierre de Caja, Arqueo y Cierre Persistido en Room (Fase 9).
 */
class CashCloseSheet : BottomSheetDialogFragment() {

    var expectedCash: Double = 0.0
    var onCashClosed: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sheet_cash_close, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnClose: ImageButton = view.findViewById(R.id.btnCloseCashClose)
        val tvExpected: TextView = view.findViewById(R.id.tvCloseExpectedAmount)
        val tilCounted: TextInputLayout = view.findViewById(R.id.tilCountedCash)
        val etCounted: TextInputEditText = view.findViewById(R.id.etCountedCash)
        val tvDifference: TextView = view.findViewById(R.id.tvCashDifferenceResult)
        val btnConfirm: Button = view.findViewById(R.id.btnConfirmCloseCash)

        btnClose.setOnClickListener { dismiss() }

        tvExpected.text = String.format(Locale.US, "$%.2f", expectedCash)
        updateDifferenceDisplay(tvDifference, expectedCash)

        etCounted.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val counted = s?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                updateDifferenceDisplay(tvDifference, counted)
            }
        })

        btnConfirm.setOnClickListener {
            tilCounted.error = null
            val countedStr = etCounted.text?.toString()?.replace(",", ".")?.trim().orEmpty()
            val counted = countedStr.toDoubleOrNull() ?: -1.0

            if (counted < 0.0) {
                tilCounted.error = getString(R.string.error_invalid_amount)
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val db = AppDatabase.getInstance(requireContext())
                    val activeShift = db.cashDao().getActiveShiftFlow().firstOrNull()

                    if (activeShift != null) {
                        val actualCents = countedStr.toCentsLong()
                        val expectedCents = expectedCash.toCentsLong()

                        db.cashDao().closeShiftTransaction(
                            sessionId = activeShift.id,
                            actualCashCents = actualCents,
                            expectedCashCents = expectedCents
                        )
                    }

                    onCashClosed?.invoke()
                    Toast.makeText(requireContext(), R.string.cash_close_success, Toast.LENGTH_SHORT).show()
                    dismiss()
                } catch (ex: Exception) {
                    Toast.makeText(requireContext(), "Error al cerrar la caja: ${ex.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateDifferenceDisplay(tvDiff: TextView, counted: Double) {
        val diff = counted - expectedCash
        when {
            Math.abs(diff) < 0.01 -> {
                tvDiff.text = "Diferencia: $0.00 (Cuadrada perfecta)"
                tvDiff.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.badge_green_text))
            }
            diff > 0.0 -> {
                tvDiff.text = "Sobrante: +${(diff.toCentsLong()).toCurrencyString()}"
                tvDiff.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.badge_tag_text))
            }
            else -> {
                tvDiff.text = "Faltante: -${(Math.abs(diff).toCentsLong()).toCurrencyString()}"
                tvDiff.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.badge_red_text))
            }
        }
    }

    companion object {
        const val TAG = "CashCloseSheet"
        fun newInstance(expected: Double): CashCloseSheet {
            val sheet = CashCloseSheet()
            sheet.expectedCash = expected
            return sheet
        }
    }
}
