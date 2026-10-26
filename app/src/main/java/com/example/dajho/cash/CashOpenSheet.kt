package com.example.dajho.cash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.dajho.R
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.data.local.entity.CashSessionEntity
import com.example.dajho.data.local.entity.SyncOperationEntity
import com.example.dajho.util.toCentsLong
import kotlinx.coroutines.launch

/**
 * BottomSheetDialogFragment para Apertura de Caja Persistida en Room (Fase 9).
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
            val amountStr = etInitial.text?.toString()?.replace(",", ".")?.trim().orEmpty()
            val amount = amountStr.toDoubleOrNull() ?: -1.0

            if (amount < 0.0) {
                tilInitial.error = getString(R.string.error_invalid_amount)
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val db = AppDatabase.getInstance(requireContext())
                    val initialCents = amountStr.toCentsLong()

                    val sessionEntity = CashSessionEntity(
                        initialCashCents = initialCents,
                        expectedCashCents = initialCents
                    )
                    db.cashDao().insertShift(sessionEntity)

                    db.syncOperationDao().insertOperation(
                        SyncOperationEntity(
                            entityName = "cash_sessions",
                            entityId = sessionEntity.id,
                            operation = "INSERT",
                            payloadJson = "{\"initialCashCents\":$initialCents}"
                        )
                    )

                    onCashOpened?.invoke(amount)
                    Toast.makeText(requireContext(), R.string.cash_open_success, Toast.LENGTH_SHORT).show()
                    dismiss()
                } catch (ex: Exception) {
                    Toast.makeText(requireContext(), "Error al abrir la caja: ${ex.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        const val TAG = "CashOpenSheet"
    }
}
