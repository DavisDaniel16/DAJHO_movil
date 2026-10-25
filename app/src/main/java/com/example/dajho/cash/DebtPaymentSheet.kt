package com.example.dajho.cash

import android.os.Bundle
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
import com.example.dajho.clients.Client
import java.util.Locale

/**
 * BottomSheetDialogFragment para Cobro de Deuda / Abono de Cliente (Fase 4).
 * Incluye validacion estricta RN-08 (no superar la deuda restante).
 */
class DebtPaymentSheet : BottomSheetDialogFragment() {

    var client: Client? = null
    var onPaymentRecorded: ((Double) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sheet_debt_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val debtor = client ?: return

        val btnClose: ImageButton = view.findViewById(R.id.btnCloseDebtPayment)
        val tvName: TextView = view.findViewById(R.id.tvPaymentDebtorName)
        val tvCurrentDebt: TextView = view.findViewById(R.id.tvPaymentCurrentDebt)
        val tilAmount: TextInputLayout = view.findViewById(R.id.tilPaymentAmount)
        val etAmount: TextInputEditText = view.findViewById(R.id.etPaymentAmount)
        val btnConfirm: Button = view.findViewById(R.id.btnConfirmPayment)

        btnClose.setOnClickListener { dismiss() }

        tvName.text = debtor.name
        tvCurrentDebt.text = String.format(Locale.US, "Deuda Actual: $%.2f", debtor.debt)

        btnConfirm.setOnClickListener {
            tilAmount.error = null
            val amount = etAmount.text?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

            if (amount <= 0.0) {
                tilAmount.error = getString(R.string.error_invalid_amount)
                return@setOnClickListener
            }

            // Validacion estricta RN-08: El abono no puede superar la deuda actual
            if (amount > debtor.debt + 0.001) {
                tilAmount.error = getString(R.string.error_amount_exceeds_debt, debtor.debt)
                return@setOnClickListener
            }

            onPaymentRecorded?.invoke(amount)
            Toast.makeText(requireContext(), R.string.debt_payment_success, Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    companion object {
        const val TAG = "DebtPaymentSheet"
        fun newInstance(debtor: Client): DebtPaymentSheet {
            val sheet = DebtPaymentSheet()
            sheet.client = debtor
            return sheet
        }
    }
}
