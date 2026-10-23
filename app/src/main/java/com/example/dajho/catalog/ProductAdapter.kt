package com.example.dajho.catalog

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dajho.R
import java.util.Locale

/**
 * Adapter del listado del catalogo (Fase 1 - diseño enfocado en imagen e info esencial).
 */
class ProductAdapter(
    private var products: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductSubline: TextView = itemView.findViewById(R.id.tvProductSubline)
        val tvVariantChips: TextView = itemView.findViewById(R.id.tvVariantChips)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvStockBadge: TextView = itemView.findViewById(R.id.tvStockBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        val context = holder.itemView.context

        holder.tvProductName.text = product.name
        holder.tvProductSubline.text = "${product.sku} · ${product.category}"

        val sizes = product.sizesSummary()
        holder.tvVariantChips.text = if (sizes.isNotBlank()) {
            context.getString(R.string.sizes_prefix, sizes)
        } else {
            context.getString(R.string.variants_count_format, product.variantCount())
        }

        holder.tvPrice.text = formatPrice(product.salePrice)
        holder.tvStockBadge.text = context.getString(R.string.stock_format, product.totalStock())

        if (!product.imageUri.isNullOrBlank()) {
            try {
                holder.ivProductImage.setImageURI(Uri.parse(product.imageUri))
                holder.ivProductImage.setPadding(0, 0, 0, 0)
            } catch (_: Exception) {
                setDefaultPlaceholder(holder.ivProductImage, context)
            }
        } else {
            setDefaultPlaceholder(holder.ivProductImage, context)
        }

        holder.itemView.setOnClickListener { onItemClick(product) }
    }

    override fun getItemCount(): Int = products.size

    fun updateData(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }

    private fun setDefaultPlaceholder(imageView: ImageView, context: android.content.Context) {
        imageView.setImageResource(R.drawable.ic_product_placeholder)
        val p = (14 * context.resources.displayMetrics.density).toInt()
        imageView.setPadding(p, p, p, p)
    }

    companion object {
        fun formatPrice(value: Double): String =
            String.format(Locale.US, "$%.2f", value)
    }
}
