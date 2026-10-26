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
import com.example.dajho.R
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.data.local.entity.CashMovementEntity
import com.example.dajho.data.local.entity.ExpenseEntity
import com.example.dajho.data.local.entity.SyncOperationEntity
import com.example.dajho.util.toCentsLong
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * BottomSheetDialogFragment para registrar egresos/gastos del negocio con Persistencia en Room (Fase 9).
 */
class NewExpenseSheet : BottomSheetDialogFragment() {

    var onExpenseRecorded: ((Double) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sheet_new_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseExpense)
        val etAmount = view.findViewById<TextInputEditText>(R.id.etExpenseAmount)
        val etConcept = view.findViewById<TextInputEditText>(R.id.etExpenseConcept)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmitExpense)

        btnClose.setOnClickListener { dismiss() }

        btnSubmit.setOnClickListener {
            val amountStr = etAmount.text?.toString()?.replace(",", ".")?.trim().orEmpty()
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            val concept = etConcept?.text?.toString()?.trim().orEmpty().ifEmpty { "Gasto General" }

            if (amount <= 0) {
                Toast.makeText(requireContext(), "Ingresa un monto válido para el gasto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val db = AppDatabase.getInstance(requireContext())
                    val activeShift = db.cashDao().getActiveShiftFlow().firstOrNull()
                    val amountCents = amountStr.toCentsLong()

                    val expenseEntity = ExpenseEntity(
                        category = "Gasto Operativo",
                        concept = concept,
                        amountCents = amountCents
                    )
                    db.expenseDao().insertExpense(expenseEntity)

                    if (activeShift != null) {
                        val movementEntity = CashMovementEntity(
                            sessionId = activeShift.id,
                            concept = "Gasto: $concept",
                            amountCents = amountCents,
                            movementType = "EGRESO"
                        )
                        db.cashDao().insertTransaction(movementEntity)
                    }

                    db.syncOperationDao().insertOperation(
                        SyncOperationEntity(
                            entityName = "expenses",
                            entityId = expenseEntity.id,
                            operation = "INSERT",
                            payloadJson = "{\"amountCents\":$amountCents}"
                        )
                    )

                    onExpenseRecorded?.invoke(amount)
                    Toast.makeText(requireContext(), "Gasto registrado correctamente en Room", Toast.LENGTH_SHORT).show()
                    dismiss()
                } catch (ex: Exception) {
                    Toast.makeText(requireContext(), "Error al registrar gasto: ${ex.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        const val TAG = "NewExpenseSheet"
    }
}
