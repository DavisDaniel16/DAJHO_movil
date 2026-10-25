package com.example.dajho.cash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dajho.R
import com.example.dajho.clients.Client
import java.util.Locale

/**
 * Adapter para la lista de clientes con deuda (Cuentas por Cobrar - Fase 4).
 */
class DebtorAdapter(
    private var debtors: List<Client>,
    private val onCollectClick: (Client) -> Unit
) : RecyclerView.Adapter<DebtorAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvDebtorName)
        val tvPhone: TextView = itemView.findViewById(R.id.tvDebtorPhone)
        val tvAmount: TextView = itemView.findViewById(R.id.tvDebtorAmount)
        val btnCollect: Button = itemView.findViewById(R.id.btnCollectDebt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_debtor, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val client = debtors[position]

        holder.tvName.text = client.name
        holder.tvPhone.text = client.phone
        holder.tvAmount.text = String.format(Locale.US, "$%.2f", client.debt)

        holder.btnCollect.setOnClickListener { onCollectClick(client) }
    }

    override fun getItemCount(): Int = debtors.size

    fun updateData(newDebtors: List<Client>) {
        debtors = newDebtors
        notifyDataSetChanged()
    }
}
