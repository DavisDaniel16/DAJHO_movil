package com.example.dajho.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dajho.R
import org.json.JSONArray
import org.json.JSONObject

class AuditAdminAdapter(private val items: JSONArray) : RecyclerView.Adapter<AuditAdminAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAction: TextView = view.findViewById(R.id.tvAuditAction)
        val tvDate: TextView = view.findViewById(R.id.tvAuditDate)
        val tvDetails: TextView = view.findViewById(R.id.tvAuditDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_audit, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val obj: JSONObject = items.getJSONObject(position)
        holder.tvAction.text = "${obj.optString("accion")} [${obj.optString("entidad")}]"
        holder.tvDate.text = obj.optString("creado_en", "")
        holder.tvDetails.text = obj.optJSONObject("datos_nuevos")?.toString(2) ?: obj.toString()
    }

    override fun getItemCount(): Int = items.length()
}
