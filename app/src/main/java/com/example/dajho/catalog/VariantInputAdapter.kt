package com.example.dajho.catalog

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.example.dajho.R

/**
 * Adapter de las filas de variantes dentro del formulario (Fase 1).
 * Cada fila: Talla - Color - Stock - boton eliminar.
 */
class VariantInputAdapter(
    private val variants: MutableList<ProductVariant>,
    private val onChanged: () -> Unit
) : RecyclerView.Adapter<VariantInputAdapter.VariantViewHolder>() {

    class VariantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvVariantIndex: TextView = itemView.findViewById(R.id.tvVariantIndex)
        val btnDeleteVariant: ImageButton = itemView.findViewById(R.id.btnDeleteVariant)
        val etVariantSize: TextInputEditText = itemView.findViewById(R.id.etVariantSize)
        val etVariantColor: TextInputEditText = itemView.findViewById(R.id.etVariantColor)
        val etVariantStock: TextInputEditText = itemView.findViewById(R.id.etVariantStock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VariantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_variant_input, parent, false)
        return VariantViewHolder(view)
    }

    override fun onBindViewHolder(holder: VariantViewHolder, position: Int) {
        val variant = variants[position]
        val watchers = getWatchers(holder.etVariantSize)
        watchers.forEach { holder.etVariantSize.removeTextChangedListener(it) }
        getWatchers(holder.etVariantColor).forEach { holder.etVariantColor.removeTextChangedListener(it) }
        getWatchers(holder.etVariantStock).forEach { holder.etVariantStock.removeTextChangedListener(it) }

        holder.tvVariantIndex.text =
            holder.itemView.context.getString(R.string.variant_label_format, position + 1)
        if (holder.etVariantSize.text.toString() != variant.size) {
            holder.etVariantSize.setText(variant.size)
        }
        if (holder.etVariantColor.text.toString() != variant.color) {
            holder.etVariantColor.setText(variant.color)
        }
        holder.etVariantStock.setText(if (variant.stock > 0) variant.stock.toString() else "")

        attachWatcher(holder.etVariantSize) { variants[position].size = it }
        attachWatcher(holder.etVariantColor) { variants[position].color = it }
        attachWatcher(holder.etVariantStock) { value ->
            variants[position].stock = value.toIntOrNull() ?: 0
        }

        holder.btnDeleteVariant.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION && variants.size > 1) {
                variants.removeAt(currentPosition)
                notifyItemRemoved(currentPosition)
                notifyItemRangeChanged(currentPosition, variants.size - currentPosition)
                onChanged()
            }
        }
    }

    override fun getItemCount(): Int = variants.size

    private fun attachWatcher(editText: TextInputEditText, onText: (String) -> Unit) {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                onText(s?.toString() ?: "")
            }
        }
        editText.addTextChangedListener(watcher)
    }

    private fun getWatchers(editText: TextInputEditText): List<TextWatcher> {
        // Reflexion ligera para limpiar watchers previos al rebind y evitar duplicados.
        return try {
            val field = android.widget.TextView::class.java.getDeclaredField("mListeners")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val list = field.get(editText) as? ArrayList<TextWatcher> ?: emptyList()
            list.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
