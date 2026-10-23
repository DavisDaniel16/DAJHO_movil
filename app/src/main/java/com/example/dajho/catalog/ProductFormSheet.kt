package com.example.dajho.catalog

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.dajho.R
import java.util.Locale

/**
 * Formulario de Nuevo/Editar Producto con soporte de imagen e info esencial.
 */
class ProductFormSheet : BottomSheetDialogFragment() {

    var onProductSaved: ((Product) -> Unit)? = null

    private lateinit var tilSku: TextInputLayout
    private lateinit var tilProdName: TextInputLayout
    private lateinit var etSku: TextInputEditText
    private lateinit var etProdName: TextInputEditText
    private lateinit var etBrand: TextInputEditText
    private lateinit var etCategory: TextInputEditText
    private lateinit var etCost: TextInputEditText
    private lateinit var etSalePrice: TextInputEditText
    private lateinit var tvMargin: TextView
    private lateinit var rvVariants: RecyclerView
    private lateinit var btnAddVariant: View
    private lateinit var btnSaveProduct: View
    private lateinit var btnCloseForm: ImageButton
    private lateinit var cardSelectImage: View
    private lateinit var ivFormProductImage: ImageView

    private lateinit var variantAdapter: VariantInputAdapter
    private val variantList = mutableListOf<ProductVariant>()

    private var editProductId: Long = -1L
    private var selectedImageUri: String? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it.toString()
            ivFormProductImage.setImageURI(it)
            ivFormProductImage.setPadding(0, 0, 0, 0)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        @Suppress("DEPRECATION")
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sheet_product_form, container, false)
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tilSku = view.findViewById(R.id.tilSku)
        tilProdName = view.findViewById(R.id.tilProdName)
        etSku = view.findViewById(R.id.etSku)
        etProdName = view.findViewById(R.id.etProdName)
        etBrand = view.findViewById(R.id.etBrand)
        etCategory = view.findViewById(R.id.etCategory)
        etCost = view.findViewById(R.id.etCost)
        etSalePrice = view.findViewById(R.id.etSalePrice)
        tvMargin = view.findViewById(R.id.tvMargin)
        rvVariants = view.findViewById(R.id.rvVariants)
        btnAddVariant = view.findViewById(R.id.btnAddVariant)
        btnSaveProduct = view.findViewById(R.id.btnSaveProduct)
        btnCloseForm = view.findViewById(R.id.btnCloseForm)
        cardSelectImage = view.findViewById(R.id.cardSelectImage)
        ivFormProductImage = view.findViewById(R.id.ivFormProductImage)

        cardSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        val editSku = arguments?.getString(ARG_SKU) ?: ""
        if (editSku.isNotBlank()) {
            editProductId = arguments?.getLong(ARG_ID) ?: -1L
            selectedImageUri = arguments?.getString(ARG_IMAGE)
            if (!selectedImageUri.isNullOrBlank()) {
                try {
                    ivFormProductImage.setImageURI(Uri.parse(selectedImageUri))
                    ivFormProductImage.setPadding(0, 0, 0, 0)
                } catch (_: Exception) {}
            }
            etSku.setText(editSku)
            etProdName.setText(arguments?.getString(ARG_NAME) ?: "")
            etBrand.setText(arguments?.getString(ARG_BRAND) ?: "")
            etCategory.setText(arguments?.getString(ARG_CATEGORY) ?: "")
            etCost.setText(formatNumber(arguments?.getDouble(ARG_COST) ?: 0.0))
            etSalePrice.setText(formatNumber(arguments?.getDouble(ARG_PRICE) ?: 0.0))
            @Suppress("DEPRECATION")
            val encoded = arguments?.getStringArrayList(ARG_VARIANTS) ?: arrayListOf()
            variantList.clear()
            encoded.forEach { line ->
                val parts = line.split("~")
                if (parts.size == 3) {
                    variantList.add(
                        ProductVariant(
                            id = System.nanoTime(),
                            size = parts[0],
                            color = parts[1],
                            stock = parts[2].toIntOrNull() ?: 0
                        )
                    )
                }
            }
        } else {
            variantList.add(newEmptyVariant())
        }

        setupVariantsList()
        setupMarginCalculation()
        setupActions()
        updateMargin()
    }

    private fun setupVariantsList() {
        variantAdapter = VariantInputAdapter(variantList) {}
        rvVariants.layoutManager = LinearLayoutManager(requireContext())
        rvVariants.adapter = variantAdapter
        rvVariants.isNestedScrollingEnabled = false

        btnAddVariant.setOnClickListener {
            variantList.add(newEmptyVariant())
            variantAdapter.notifyItemInserted(variantList.size - 1)
            rvVariants.post {
                rvVariants.smoothScrollToPosition(variantList.size - 1)
            }
        }
    }

    private fun setupMarginCalculation() {
        val marginWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                updateMargin()
            }
        }
        etCost.addTextChangedListener(marginWatcher)
        etSalePrice.addTextChangedListener(marginWatcher)
    }

    private fun setupActions() {
        btnCloseForm.setOnClickListener { dismiss() }

        btnSaveProduct.setOnClickListener {
            clearErrors()
            val sku = etSku.text?.toString()?.trim().orEmpty()
            val name = etProdName.text?.toString()?.trim().orEmpty()
            val brand = etBrand.text?.toString()?.trim().orEmpty()
            val category = etCategory.text?.toString()?.trim().orEmpty()
            val cost = etCost.text?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
            val price = etSalePrice.text?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

            var valid = true
            if (sku.isEmpty()) { tilSku.error = getString(R.string.error_required_field); valid = false }
            if (name.isEmpty()) { tilProdName.error = getString(R.string.error_required_field); valid = false }

            val validVariants = variantList.filter { it.size.isNotBlank() || it.color.isNotBlank() }
            if (validVariants.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_min_one_variant, Toast.LENGTH_SHORT).show()
                valid = false
            }

            if (!valid) return@setOnClickListener

            val savedProduct = Product(
                id = if (editProductId != -1L) editProductId else System.nanoTime(),
                sku = sku,
                name = name,
                brand = brand.ifEmpty { getString(R.string.app_name) },
                category = category.ifEmpty { getString(R.string.default_category) },
                cost = cost,
                salePrice = price,
                imageUri = selectedImageUri,
                variants = validVariants.mapIndexed { index, v ->
                    ProductVariant(id = index.toLong(), size = v.size, color = v.color, stock = v.stock)
                }.toMutableList()
            )
            onProductSaved?.invoke(savedProduct)
            Toast.makeText(requireContext(), R.string.saved_product_ok, Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun updateMargin() {
        val cost = etCost.text?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
        val price = etSalePrice.text?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
        if (cost > 0.0 && price > 0.0) {
            val marginValue = price - cost
            val marginPercent = ((marginValue / cost) * 100).toInt()
            tvMargin.text = getString(R.string.margin_format, marginValue, marginPercent)
            tvMargin.visibility = View.VISIBLE
        } else {
            tvMargin.text = getString(R.string.margin_format, 0.0, 0)
            tvMargin.visibility = View.INVISIBLE
        }
    }

    private fun clearErrors() {
        tilSku.error = null
        tilProdName.error = null
    }

    private fun newEmptyVariant() = ProductVariant(
        id = System.nanoTime(),
        size = "",
        color = "",
        stock = 0
    )

    private fun formatNumber(value: Double): String =
        String.format(Locale.US, "%.2f", value)

    companion object {
        const val TAG = "ProductFormSheet"
        private const val ARG_ID = "arg_id"
        private const val ARG_SKU = "arg_sku"
        private const val ARG_NAME = "arg_name"
        private const val ARG_BRAND = "arg_brand"
        private const val ARG_CATEGORY = "arg_category"
        private const val ARG_COST = "arg_cost"
        private const val ARG_PRICE = "arg_price"
        private const val ARG_IMAGE = "arg_image"
        private const val ARG_VARIANTS = "arg_variants"
        private const val ARG_IS_EDIT = "arg_is_edit"

        fun newInstance(product: Product?): ProductFormSheet {
            val sheet = ProductFormSheet()
            if (product != null) {
                val args = Bundle().apply {
                    putLong(ARG_ID, product.id)
                    putString(ARG_SKU, product.sku)
                    putString(ARG_NAME, product.name)
                    putString(ARG_BRAND, product.brand)
                    putString(ARG_CATEGORY, product.category)
                    putDouble(ARG_COST, product.cost)
                    putDouble(ARG_PRICE, product.salePrice)
                    putString(ARG_IMAGE, product.imageUri)
                    putBoolean(ARG_IS_EDIT, true)
                    putStringArrayList(
                        ARG_VARIANTS,
                        ArrayList(product.variants.map { "${it.size}~${it.color}~${it.stock}" })
                    )
                }
                sheet.arguments = args
            }
            return sheet
        }
    }
}
