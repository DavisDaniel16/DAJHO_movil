package com.example.dajho.catalog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dajho.R
import java.util.Locale

/**
 * Adapter del listado del catalogo (Fase 1 - solo frontend, sin ViewBinding).
 * Estilo consistente con el resto del proyecto (findViewById).
 */
class ProductAdapter(
    private var products: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSkuBadge: TextView = itemView.findViewById(R.id.tvSkuBadge)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductBrand: TextView = itemView.findViewById(R.id.tvProductBrand)
        val tvCategoryTag: TextView = itemView.findViewById(R.id.tvCategoryTag)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvStockValue: TextView = itemView.findViewById(R.id.tvStockValue)
        val tvVariantsSummary: TextView = itemView.findViewById(R.id.tvVariantsSummary)
        val tvVariantChips: TextView = itemView.findViewById(R.id.tvVariantChips)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        val context = holder.itemView.context
        holder.tvSkuBadge.text = context.getString(R.string.sku_prefix, product.sku)
        holder.tvProductName.text = product.name
        holder.tvProductBrand.text = context.getString(R.string.brand_prefix, product.brand)
        holder.tvCategoryTag.text = product.category
        holder.tvPrice.text = formatPrice(product.salePrice)
        holder.tvStockValue.text = context.getString(R.string.stock_format, product.totalStock())
        holder.tvVariantsSummary.text =
            context.getString(R.string.variants_count_format, product.variantCount())
        holder.tvVariantChips.text = context.getString(
            R.string.variant_summary_format,
            product.sizesSummary(),
            product.colorsSummary()
        )
        holder.itemView.setOnClickListener { onItemClick(product) }
    }

    override fun getItemCount(): Int = products.size

    fun updateData(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }

    companion object {
        fun formatPrice(value: Double): String =
            String.format(Locale.US, "$%.2f", value)
    }
}
