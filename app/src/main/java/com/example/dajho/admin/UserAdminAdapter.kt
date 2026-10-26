package com.example.dajho.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dajho.R
import org.json.JSONArray
import org.json.JSONObject

class UserAdminAdapter(private val items: JSONArray) : RecyclerView.Adapter<UserAdminAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvUserName)
        val tvRole: TextView = view.findViewById(R.id.tvUserRole)
        val tvEmail: TextView = view.findViewById(R.id.tvUserEmail)
        val tvPhone: TextView = view.findViewById(R.id.tvUserPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val obj: JSONObject = items.getJSONObject(position)
        holder.tvName.text = obj.optString("nombre_completo", "Sin Nombre")
        holder.tvRole.text = obj.optString("rol", "USUARIO")
        holder.tvEmail.text = obj.optString("correo", "N/A")
        holder.tvPhone.text = "Teléfono: " + obj.optString("telefono").ifBlank { "N/A" }
    }

    override fun getItemCount(): Int = items.length()
}
