package com.example.dajho.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dajho.R
import org.json.JSONArray
import org.json.JSONObject

class BusinessAdminAdapter(private val items: JSONArray) : RecyclerView.Adapter<BusinessAdminAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvBizName)
        val tvRimpe: TextView = view.findViewById(R.id.tvBizRimpe)
        val tvRuc: TextView = view.findViewById(R.id.tvBizRuc)
        val tvPhone: TextView = view.findViewById(R.id.tvBizPhone)
        val tvAddress: TextView = view.findViewById(R.id.tvBizAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_business, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val obj: JSONObject = items.getJSONObject(position)
        holder.tvName.text = obj.optString("nombre", "Sin Nombre")
        holder.tvRimpe.text = obj.optString("tipo_rimpe", "RIMPE")
        holder.tvRuc.text = "RUC / Cédula: " + obj.optString("ruc_o_cedula").ifBlank { "N/A" }
        holder.tvPhone.text = "Teléfono: " + obj.optString("telefono").ifBlank { "N/A" }
        holder.tvAddress.text = "Dirección: " + obj.optString("direccion").ifBlank { "N/A" }
    }

    override fun getItemCount(): Int = items.length()
}
