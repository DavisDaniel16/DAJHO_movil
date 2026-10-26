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
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.dajho.R
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.data.local.datastore.UserPreferences
import com.example.dajho.data.local.entity.LocalImageEntity
import com.example.dajho.data.local.entity.ProductEntity
import com.example.dajho.data.local.entity.ProductVariantEntity
import com.example.dajho.data.local.entity.SyncStatus
import com.example.dajho.util.toCentsLong
import com.example.dajho.util.toCurrencyString
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID

/**
 * Formulario de Nuevo/Editar Producto con Validaciones Estrictas y Almacenamiento Privado de Imagen (Fase 6).
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

    private var currentProductId: String = UUID.randomUUID().toString()
    private var isEditMode = false
    private var selectedLocalImagePath: String? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val privatePath = copyImageToPrivateStorage(it)
            if (privatePath != null) {
                selectedLocalImagePath = privatePath
                ivFormProductImage.setImageURI(Uri.fromFile(File(privatePath)))
                ivFormProductImage.setPadding(0, 0, 0, 0)
            } else {
                Toast.makeText(requireContext(), "Error al guardar imagen privada", Toast.LENGTH_SHORT).show()
            }
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
            isEditMode = true
            currentProductId = arguments?.getString(ARG_ID) ?: UUID.randomUUID().toString()
            selectedLocalImagePath = arguments?.getString(ARG_IMAGE)

            if (!selectedLocalImagePath.isNullOrBlank()) {
                try {
                    val file = File(selectedLocalImagePath!!)
                    if (file.exists()) {
                        ivFormProductImage.setImageURI(Uri.fromFile(file))
                        ivFormProductImage.setPadding(0, 0, 0, 0)
                    }
                } catch (_: Exception) {}
            }

            etSku.setText(editSku)
            etProdName.setText(arguments?.getString(ARG_NAME) ?: "")
            etBrand.setText(arguments?.getString(ARG_BRAND) ?: "")
            etCategory.setText(arguments?.getString(ARG_CATEGORY) ?: "")
            etCost.setText(formatNumber(arguments?.getDouble(ARG_COST) ?: 0.0))
            etSalePrice.setText(formatNumber(arguments?.getDouble(ARG_PRICE) ?: 0.0))
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

            val costStr = etCost.text?.toString()?.replace(",", ".")?.trim().orEmpty()
            val priceStr = etSalePrice.text?.toString()?.replace(",", ".")?.trim().orEmpty()

            var valid = true

            // 1. Validacion de SKU
            if (sku.isEmpty()) {
                tilSku.error = "Ingresa el código SKU"
                valid = false
            }

            // 2. Validacion de Nombre
            if (name.isEmpty()) {
                tilProdName.error = "Ingresa el nombre del producto"
                valid = false
            }

            // 3. Validacion de Costo positivo
            val costVal = costStr.toDoubleOrNull()
            if (costVal == null || costVal <= 0.0) {
                Toast.makeText(requireContext(), "El costo debe ser mayor a $0.00", Toast.LENGTH_LONG).show()
                valid = false
            }

            // 4. Validacion de Precio de venta >= Costo
            val priceVal = priceStr.toDoubleOrNull()
            if (priceVal == null || (costVal != null && priceVal < costVal)) {
                Toast.makeText(requireContext(), "El precio de venta no puede ser menor que el costo", Toast.LENGTH_LONG).show()
                valid = false
            }

            // 5. Validacion de Variantes
            val validVariants = variantList.filter { it.size.isNotBlank() || it.color.isNotBlank() }
            if (validVariants.isEmpty()) {
                Toast.makeText(requireContext(), "Agrega al menos una variante con talla/color", Toast.LENGTH_SHORT).show()
                valid = false
            }

            for (v in validVariants) {
                if (v.stock < 0) {
                    Toast.makeText(requireContext(), "El stock no puede ser negativo", Toast.LENGTH_SHORT).show()
                    valid = false
                    break
                }
            }

            if (!valid) return@setOnClickListener

            lifecycleScope.launch {
                val db = AppDatabase.getInstance(requireContext())
                val existingProduct = db.productDao().getProductBySku(sku)
                if (existingProduct != null && existingProduct.id != currentProductId) {
                    tilSku.error = "El código SKU ya existe en otro producto"
                    return@launch
                }

                val costCents = costStr.toCentsLong()
                val priceCents = priceStr.toCentsLong()
                val businessId = UserPreferences(requireContext()).businessIdFlow.first()
                    ?: run {
                        Toast.makeText(requireContext(), "No hay un negocio activo", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                android.util.Log.d("DEBUG_FORM", "businessId al guardar producto: $businessId")

                val productEntity = ProductEntity(
                    id = currentProductId,
                    businessId = businessId,
                    sku = sku,
                    name = name,
                    brand = brand.ifEmpty { "DAJHO" },
                    category = category.ifEmpty { "General" },
                    costCents = costCents,
                    salePriceCents = priceCents,
                    imageUri = selectedLocalImagePath,
                    syncStatus = SyncStatus.PENDING_INSERT
                )

                val variantEntities = validVariants.map { v ->
                    ProductVariantEntity(
                        id = UUID.randomUUID().toString(),
                        productId = currentProductId,
                        size = v.size.ifEmpty { "Única" },
                        color = v.color.ifEmpty { "Único" },
                        stock = v.stock.coerceAtLeast(0)
                    )
                }

                val productRepo = com.example.dajho.data.repository.ProductRepositoryImpl(
                    db.productDao(),
                    db.syncOperationDao(),
                    db.localImageDao()
                )
                val localImage = if (!selectedLocalImagePath.isNullOrBlank()) {
                    LocalImageEntity(
                        localFilePath = selectedLocalImagePath!!,
                        remoteStoragePath = "products/$currentProductId.jpg",
                        entityType = "PRODUCT",
                        entityId = currentProductId
                    )
                } else null

                productRepo.saveProductWithImage(productEntity, variantEntities, localImage)
                com.example.dajho.data.sync.SyncManager.triggerImmediateSync(requireContext())

                val savedProduct = Product(
                    id = currentProductId,
                    sku = sku,
                    name = name,
                    brand = brand.ifEmpty { "DAJHO" },
                    category = category.ifEmpty { "General" },
                    cost = costVal ?: 0.0,
                    salePrice = priceVal ?: 0.0,
                    imageUri = selectedLocalImagePath,
                    variants = validVariants.toMutableList()
                )

                onProductSaved?.invoke(savedProduct)
                Toast.makeText(requireContext(), "Producto guardado correctamente", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }

    private fun updateMargin() {
        val cost = etCost.text?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
        val price = etSalePrice.text?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

        if (cost > 0.0 && price > 0.0 && price >= cost) {
            val marginValue = price - cost
            val marginPercent = ((marginValue / price) * 100).toInt()
            val marginStr = marginValue.toCentsLong().toCurrencyString()
            tvMargin.text = "Margen: $marginStr (${marginPercent}%)"
            tvMargin.setTextColor(ContextCompat.getColor(requireContext(), R.color.badge_green_text))
            tvMargin.visibility = View.VISIBLE
        } else {
            tvMargin.text = "Margen: $0.00 (0%)"
            tvMargin.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            tvMargin.visibility = View.INVISIBLE
        }
    }

    private fun copyImageToPrivateStorage(sourceUri: Uri): String? {
        return try {
            val context = requireContext()
            val imagesDir = File(context.filesDir, "product_images").apply { if (!exists()) mkdirs() }
            val destFile = File(imagesDir, "prod_${UUID.randomUUID()}.jpg")

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    private fun clearErrors() {
        tilSku.error = null
        tilProdName.error = null
    }

    private fun newEmptyVariant() = ProductVariant(
        id = UUID.randomUUID().toString(),
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

        fun newInstance(product: Product?): ProductFormSheet {
            val sheet = ProductFormSheet()
            if (product != null) {
                val args = Bundle().apply {
                    putString(ARG_ID, product.id)
                    putString(ARG_SKU, product.sku)
                    putString(ARG_NAME, product.name)
                    putString(ARG_BRAND, product.brand)
                    putString(ARG_CATEGORY, product.category)
                    putDouble(ARG_COST, product.cost)
                    putDouble(ARG_PRICE, product.salePrice)
                    putString(ARG_IMAGE, product.imageUri)
                }
                sheet.arguments = args
            }
            return sheet
        }
    }
}
