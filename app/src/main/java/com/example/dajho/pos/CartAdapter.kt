package com.example.dajho.pos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dajho.R
import java.util.Locale

/**
 * Adapter para los items del carrito de compras (Fase 3 - POS).
 */
class CartAdapter(
    private val cartItems: MutableList<CartItem>,
    private val onCartChanged: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCartName: TextView = itemView.findViewById(R.id.tvCartName)
        val tvCartVariant: TextView = itemView.findViewById(R.id.tvCartVariant)
        val tvCartSubtotal: TextView = itemView.findViewById(R.id.tvCartSubtotal)
        val tvCartQuantity: TextView = itemView.findViewById(R.id.tvCartQuantity)
        val btnMinus: ImageButton = itemView.findViewById(R.id.btnMinus)
        val btnPlus: ImageButton = itemView.findViewById(R.id.btnPlus)
        val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemoveCartItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]

        holder.tvCartName.text = item.product.name
        holder.tvCartVariant.text = item.variantDescription()
        holder.tvCartQuantity.text = item.quantity.toString()
        holder.tvCartSubtotal.text = formatPrice(item.subtotal())

        holder.btnPlus.setOnClickListener {
            if (item.quantity < item.variant.stock) {
                item.quantity++
                notifyItemChanged(holder.bindingAdapterPosition)
                onCartChanged()
            }
        }

        holder.btnMinus.setOnClickListener {
            if (item.quantity > 1) {
                item.quantity--
                notifyItemChanged(holder.bindingAdapterPosition)
                onCartChanged()
            }
        }

        holder.btnRemove.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                cartItems.removeAt(pos)
                notifyItemRemoved(pos)
                notifyItemRangeChanged(pos, cartItems.size - pos)
                onCartChanged()
            }
        }
    }

    override fun getItemCount(): Int = cartItems.size

    companion object {
        fun formatPrice(value: Double): String =
            String.format(Locale.US, "$%.2f", value)
    }
}
