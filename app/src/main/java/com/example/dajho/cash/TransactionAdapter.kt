package com.example.dajho.cash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.dajho.R
import java.util.Locale

/**
 * Adapter para la lista de Ingresos y Egresos en Caja/Balance (Estilo Treinta - Fase 3).
 */
class TransactionAdapter(
    private val transactions: MutableList<CashTransaction>
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cash_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    fun updateData(newTransactions: List<CashTransaction>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconBg: View = itemView.findViewById(R.id.flTransactionIconBg)
        private val tvSymbol: TextView = itemView.findViewById(R.id.tvTransactionSymbol)
        private val tvConcept: TextView = itemView.findViewById(R.id.tvTransactionConcept)
        private val tvSubline: TextView = itemView.findViewById(R.id.tvTransactionDateCategory)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvTransactionAmount)

        fun bind(tx: CashTransaction) {
            val ctx = itemView.context
            tvConcept.text = tx.concept
            tvSubline.text = "${tx.timestamp} · ${tx.category}"

            if (tx.isIncome) {
                iconBg.background = ContextCompat.getDrawable(ctx, R.drawable.bg_badge_green)
                tvSymbol.text = "↗"
                tvSymbol.setTextColor(ContextCompat.getColor(ctx, R.color.badge_green_text))
                tvAmount.text = String.format(Locale.US, "+$%.2f", tx.amount)
                tvAmount.setTextColor(ContextCompat.getColor(ctx, R.color.badge_green_text))
            } else {
                iconBg.background = ContextCompat.getDrawable(ctx, R.drawable.bg_badge_red)
                tvSymbol.text = "↙"
                tvSymbol.setTextColor(ContextCompat.getColor(ctx, R.color.badge_red_text))
                tvAmount.text = String.format(Locale.US, "-$%.2f", tx.amount)
                tvAmount.setTextColor(ContextCompat.getColor(ctx, R.color.badge_red_text))
            }
        }
    }
}
