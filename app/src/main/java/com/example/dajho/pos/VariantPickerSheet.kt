package com.example.dajho.pos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.dajho.R
import com.example.dajho.catalog.Product
import com.example.dajho.catalog.ProductVariant

/**
 * BottomSheetDialogFragment para seleccionar variante (Talla/Color) de un producto.
 */
class VariantPickerSheet : BottomSheetDialogFragment() {

    var onVariantSelected: ((ProductVariant) -> Unit)? = null
    private var product: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sheet_variant_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvName: TextView = view.findViewById(R.id.tvPickerProductName)
        val btnClose: ImageButton = view.findViewById(R.id.btnCloseVariantPicker)
        val rvVariants: RecyclerView = view.findViewById(R.id.rvVariantOptions)

        btnClose.setOnClickListener { dismiss() }

        tvName.text = product?.name ?: ""

        rvVariants.layoutManager = LinearLayoutManager(requireContext())
        rvVariants.adapter = VariantOptionAdapter(product?.variants ?: emptyList()) { variant ->
            onVariantSelected?.invoke(variant)
            dismiss()
        }
    }

    class VariantOptionAdapter(
        private val options: List<ProductVariant>,
        private val onSelect: (ProductVariant) -> Unit
    ) : RecyclerView.Adapter<VariantOptionAdapter.ViewHolder>() {

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvText: TextView = v.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = options[position]
            val text = "Talla: ${item.size} · Color: ${item.color}  (Stock: ${item.stock} und)"
            holder.tvText.text = text
            holder.tvText.setTextColor(
                androidx.core.content.ContextCompat.getColor(holder.itemView.context, R.color.text_primary)
            )
            holder.itemView.setOnClickListener { onSelect(item) }
        }

        override fun getItemCount(): Int = options.size
    }

    companion object {
        const val TAG = "VariantPickerSheet"
        fun newInstance(product: Product): VariantPickerSheet {
            val sheet = VariantPickerSheet()
            sheet.product = product
            return sheet
        }
    }
}
