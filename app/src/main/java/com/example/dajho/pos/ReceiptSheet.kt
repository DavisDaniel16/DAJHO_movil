package com.example.dajho.pos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.dajho.R
import java.util.Locale

/**
 * BottomSheetDialogFragment para mostrar la Nota de Venta Simple (Régimen RIMPE Negocio Popular).
 */
class ReceiptSheet : BottomSheetDialogFragment() {

    var saleRecord: SaleRecord? = null
    var onClose: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sheet_receipt_rimpe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val record = saleRecord ?: return

        val tvNumber: TextView = view.findViewById(R.id.tvReceiptNumber)
        val tvDate: TextView = view.findViewById(R.id.tvReceiptDate)
        val tvClient: TextView = view.findViewById(R.id.tvReceiptClient)
        val tvPayment: TextView = view.findViewById(R.id.tvReceiptPayment)
        val tvTransferRef: TextView = view.findViewById(R.id.tvReceiptTransferRef)
        val tvItems: TextView = view.findViewById(R.id.tvReceiptItemsList)
        val tvTotal: TextView = view.findViewById(R.id.tvReceiptTotal)
        val btnClose: Button = view.findViewById(R.id.btnCloseReceipt)

        tvNumber.text = "No. ${record.saleId}"
        tvDate.text = record.timestamp
        tvClient.text = "Cliente: ${record.client?.name ?: "Consumidor Final"}"
        tvPayment.text = "Forma de pago: ${record.paymentMethod.displayName}"

        if (!record.transferReference.isNullOrBlank()) {
            tvTransferRef.text = "Comprobante: #${record.transferReference}"
            tvTransferRef.visibility = View.VISIBLE
        } else {
            tvTransferRef.visibility = View.GONE
        }

        val itemsText = record.items.joinToString("\n") { item ->
            "• ${item.product.name} (${item.variantDescription()}) x${item.quantity} — ${formatPrice(item.subtotal())}"
        }
        tvItems.text = itemsText
        tvTotal.text = formatPrice(record.totalAmount)

        btnClose.setOnClickListener {
            dismiss()
            onClose?.invoke()
        }
    }

    private fun formatPrice(value: Double): String =
        String.format(Locale.US, "$%.2f", value)

    companion object {
        const val TAG = "ReceiptSheet"
        fun newInstance(sale: SaleRecord): ReceiptSheet {
            val sheet = ReceiptSheet()
            sheet.saleRecord = sale
            return sheet
        }
    }
}
