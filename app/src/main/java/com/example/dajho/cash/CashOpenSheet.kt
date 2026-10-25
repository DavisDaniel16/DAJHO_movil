package com.example.dajho.cash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.dajho.R

/**
 * BottomSheetDialogFragment para Apertura de Caja (Fase 4).
 */
class CashOpenSheet : BottomSheetDialogFragment() {

    var onCashOpened: ((Double) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sheet_cash_open, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnClose: ImageButton = view.findViewById(R.id.btnCloseCashOpen)
        val tilInitial: TextInputLayout = view.findViewById(R.id.tilInitialCash)
        val etInitial: TextInputEditText = view.findViewById(R.id.etInitialCash)
        val btnConfirm: Button = view.findViewById(R.id.btnConfirmOpenCash)

        btnClose.setOnClickListener { dismiss() }

        btnConfirm.setOnClickListener {
            tilInitial.error = null
            val amount = etInitial.text?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: -1.0
            if (amount < 0.0) {
                tilInitial.error = getString(R.string.error_invalid_amount)
                return@setOnClickListener
            }
            onCashOpened?.invoke(amount)
            Toast.makeText(requireContext(), R.string.cash_open_success, Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    companion object {
        const val TAG = "CashOpenSheet"
    }
}
