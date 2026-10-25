package com.example.dajho.pos

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dajho.R
import com.example.dajho.catalog.Product
import java.util.Locale

/**
 * Adapter para mostrar los resultados de búsqueda de productos en el POS (Fase 3).
 * El usuario debe TOCAR el producto resultante para agregarlo.
 */
class PosProductSearchAdapter(
    private var results: List<Product>,
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<PosProductSearchAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImg: ImageView = itemView.findViewById(R.id.ivPosSearchImg)
        val tvName: TextView = itemView.findViewById(R.id.tvPosSearchName)
        val tvSubline: TextView = itemView.findViewById(R.id.tvPosSearchSubline)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPosSearchPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pos_product_search, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = results[position]
        val context = holder.itemView.context

        holder.tvName.text = product.name
        holder.tvSubline.text = "${product.sku} · ${product.category}"
        holder.tvPrice.text = String.format(Locale.US, "$%.2f", product.salePrice)

        if (!product.imageUri.isNullOrBlank()) {
            try {
                holder.ivImg.setImageURI(Uri.parse(product.imageUri))
                holder.ivImg.setPadding(0, 0, 0, 0)
            } catch (_: Exception) {
                holder.ivImg.setImageResource(R.drawable.ic_product_placeholder)
                val p = (10 * context.resources.displayMetrics.density).toInt()
                holder.ivImg.setPadding(p, p, p, p)
            }
        } else {
            holder.ivImg.setImageResource(R.drawable.ic_product_placeholder)
            val p = (10 * context.resources.displayMetrics.density).toInt()
            holder.ivImg.setPadding(p, p, p, p)
        }

        holder.itemView.setOnClickListener { onProductClick(product) }
    }

    override fun getItemCount(): Int = results.size

    fun updateData(newResults: List<Product>) {
        results = newResults
        notifyDataSetChanged()
    }
}
