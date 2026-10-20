package com.example.dajho.clients

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.dajho.R
import java.util.Locale

/**
 * Adapter para la lista de clientes (Fase 2 - solo frontend, sin ViewBinding).
 */
class ClientAdapter(
    private var clients: List<Client>,
    private val onItemClick: (Client) -> Unit
) : RecyclerView.Adapter<ClientAdapter.ClientViewHolder>() {

    class ClientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvClientName: TextView = itemView.findViewById(R.id.tvClientName)
        val tvStatusBadge: TextView = itemView.findViewById(R.id.tvStatusBadge)
        val tvClientPhone: TextView = itemView.findViewById(R.id.tvClientPhone)
        val tvClientInfo: TextView = itemView.findViewById(R.id.tvClientInfo)
        val tvClientDebt: TextView = itemView.findViewById(R.id.tvClientDebt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_client, parent, false)
        return ClientViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        val client = clients[position]
        val context = holder.itemView.context

        holder.tvClientName.text = client.name
        holder.tvClientPhone.text = client.phone

        if (client.info.isNotBlank()) {
            holder.tvClientInfo.text = client.info
            holder.tvClientInfo.visibility = View.VISIBLE
        } else {
            holder.tvClientInfo.visibility = View.GONE
        }

        holder.tvClientDebt.text =
            context.getString(R.string.debt_balance_format, client.debt)

        if (client.hasDebt()) {
            holder.tvStatusBadge.text = context.getString(R.string.status_debt)
            holder.tvStatusBadge.background =
                ContextCompat.getDrawable(context, R.drawable.bg_badge_red)
            holder.tvStatusBadge.setTextColor(
                ContextCompat.getColor(context, R.color.badge_red_text)
            )
        } else {
            holder.tvStatusBadge.text = context.getString(R.string.status_ok)
            holder.tvStatusBadge.background =
                ContextCompat.getDrawable(context, R.drawable.bg_badge_green)
            holder.tvStatusBadge.setTextColor(
                ContextCompat.getColor(context, R.color.badge_green_text)
            )
        }

        holder.itemView.setOnClickListener { onItemClick(client) }
    }

    override fun getItemCount(): Int = clients.size

    fun updateData(newClients: List<Client>) {
        clients = newClients
        notifyDataSetChanged()
    }
}
