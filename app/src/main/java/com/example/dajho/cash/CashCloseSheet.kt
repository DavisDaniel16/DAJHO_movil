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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.dajho.R
import java.util.Locale

/**
 * BottomSheetDialogFragment para Cierre de Caja y Arqueo (Fase 4).
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
            val counted = etCounted.text?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: -1.0
            if (counted < 0.0) {
                tilCounted.error = getString(R.string.error_invalid_amount)
                return@setOnClickListener
            }
            onCashClosed?.invoke()
            Toast.makeText(requireContext(), R.string.cash_close_success, Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun updateDifferenceDisplay(tvDiff: TextView, counted: Double) {
        val diff = counted - expectedCash
        when {
            Math.abs(diff) < 0.01 -> {
                tvDiff.text = "Diferencia: $0.00 (Cuadrada perfectas)"
                tvDiff.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.badge_green_text))
            }
            diff > 0.0 -> {
                tvDiff.text = String.format(Locale.US, "Sobrante: +$%.2f", diff)
                tvDiff.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.badge_tag_text))
            }
            else -> {
                tvDiff.text = String.format(Locale.US, "Faltante: -$%.2f", Math.abs(diff))
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
