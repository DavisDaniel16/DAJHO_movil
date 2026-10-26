package com.example.dajho.catalog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.dajho.R
import java.util.Locale

/**
 * Adapter para la lista compacta del Inventario con Alerta de Stock Bajo (Fase 6).
 */
class InventoryAdapter(
    private val products: MutableList<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventory_product, parent, false)
        return InventoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun updateData(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

    inner class InventoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvSubline: TextView = itemView.findViewById(R.id.tvProductSubline)
        private val tvStockBadge: TextView = itemView.findViewById(R.id.tvStockBadge)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)

        fun bind(product: Product) {
            val ctx = itemView.context
            tvName.text = product.name
            tvSubline.text = "${product.category} · SKU: ${product.sku}"

            val totalStock = product.variants.sumOf { it.stock }

            if (totalStock <= 3) {
                tvStockBadge.text = "⚠️ Stock bajo: $totalStock und"
                tvStockBadge.background = ContextCompat.getDrawable(ctx, R.drawable.bg_badge_red)
                tvStockBadge.setTextColor(ContextCompat.getColor(ctx, R.color.badge_red_text))
            } else {
                tvStockBadge.text = "$totalStock unidades en stock"
                tvStockBadge.background = ContextCompat.getDrawable(ctx, R.drawable.bg_badge_tag)
                tvStockBadge.setTextColor(ContextCompat.getColor(ctx, R.color.badge_tag_text))
            }

            tvPrice.text = String.format(Locale.US, "$%.2f", product.salePrice)

            itemView.setOnClickListener {
                onItemClick(product)
            }
        }
    }
}
