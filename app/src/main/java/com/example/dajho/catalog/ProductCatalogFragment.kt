package com.example.dajho.catalog

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dajho.R
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.data.local.DatabaseSeeder
import com.example.dajho.data.local.datastore.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Módulo de Gestión de Inventario / Catálogo (Estilo Treinta - Reactivo con Room - Fase 6).
 */
class ProductCatalogFragment : Fragment() {

    private lateinit var adapter: InventoryAdapter
    private val allRoomProducts = mutableListOf<Product>()
    private val visibleProducts = mutableListOf<Product>()

    private lateinit var etSearch: EditText
    private lateinit var llEmpty: View
    private lateinit var rvProducts: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_catalog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etSearch = view.findViewById(R.id.etSearchProduct)
        llEmpty = view.findViewById(R.id.llEmptyCatalog)
        rvProducts = view.findViewById(R.id.rvProducts)
        val btnNewProduct: View = view.findViewById(R.id.btnNewProduct)

        adapter = InventoryAdapter(visibleProducts) { product ->
            showProductForm(product)
        }
        rvProducts.layoutManager = LinearLayoutManager(requireContext())
        rvProducts.adapter = adapter

        btnNewProduct.setOnClickListener {
            showProductForm(null)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateProductList(s?.toString().orEmpty())
            }
        })

        observeRoomProducts()
    }

    private fun observeRoomProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            val ctx = context ?: return@launch
            val db = AppDatabase.getInstance(ctx)
            DatabaseSeeder.seedIfEmpty(db)

            val businessId = UserPreferences(ctx).businessIdFlow.first() ?: "DEFAULT_BUSINESS"
            android.util.Log.d("DEBUG_INV", "businessId actual: $businessId")

            db.productDao().getProductsByBusinessFlow(businessId).collect { roomWithVariants ->
                android.util.Log.d("DEBUG_INV", "Productos recibidos del Flow: ${roomWithVariants.size}")
                allRoomProducts.clear()
                val mapped = roomWithVariants.map { item ->
                    Product(
                        id = item.product.id,
                        sku = item.product.sku,
                        name = item.product.name,
                        brand = item.product.brand,
                        category = item.product.category,
                        cost = item.product.costCents / 100.0,
                        salePrice = item.product.salePriceCents / 100.0,
                        imageUri = item.product.imageUri,
                        variants = item.variants.map { v ->
                            ProductVariant(
                                id = v.id,
                                size = v.size,
                                color = v.color,
                                stock = v.stock
                            )
                        }.toMutableList()
                    )
                }

                allRoomProducts.addAll(mapped)
                updateProductList(etSearch.text?.toString().orEmpty())
            }
        }
    }

    private fun updateProductList(query: String) {
        val q = query.trim().lowercase()
        visibleProducts.clear()

        val sourceList = allRoomProducts

        if (q.isEmpty()) {
            visibleProducts.addAll(sourceList)
        } else {
            visibleProducts.addAll(
                sourceList.filter {
                    it.sku.lowercase().contains(q) ||
                        it.name.lowercase().contains(q) ||
                        it.category.lowercase().contains(q)
                }
            )
        }

        if (::adapter.isInitialized) {
            adapter.updateData(visibleProducts)
            val isEmpty = visibleProducts.isEmpty()
            llEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            rvProducts.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    private fun showProductForm(product: Product?) {
        val sheet = ProductFormSheet.newInstance(product)
        sheet.onProductSaved = {
            // Room Flow collects and updates automatically
        }
        sheet.show(childFragmentManager, ProductFormSheet.TAG)
    }
}
