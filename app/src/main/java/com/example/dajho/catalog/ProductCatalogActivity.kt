package com.example.dajho.catalog

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dajho.R

/**
 * Pantalla de Gestion de Catalogo (Fase 1 - solo frontend).
 * Lista, busqueda, creacion y edicion de productos con variantes.
 */
class ProductCatalogActivity : AppCompatActivity() {

    private lateinit var adapter: ProductAdapter
    private val productList = mutableListOf<Product>()
    private val visibleProducts = mutableListOf<Product>()

    private lateinit var etSearch: EditText
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_catalog)

        etSearch = findViewById(R.id.etSearchProduct)
        tvEmpty = findViewById(R.id.tvEmptyCatalog)
        val rvProducts: RecyclerView = findViewById(R.id.rvProducts)
        val btnNewProduct: View = findViewById(R.id.btnNewProduct)
        val btnBack: ImageView = findViewById(R.id.btnBackCatalog)

        loadSeedData()

        adapter = ProductAdapter(visibleProducts) { product ->
            showProductForm(product)
        }
        rvProducts.layoutManager = LinearLayoutManager(this)
        rvProducts.adapter = adapter

        btnBack.setOnClickListener { finish() }

        btnNewProduct.setOnClickListener {
            showProductForm(null)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterProducts(s?.toString().orEmpty())
            }
        })
    }

    private fun loadSeedData() {
        productList.clear()
        productList.add(
            Product(
                id = 1L,
                sku = "CAM-001",
                name = "Camiseta Polo Bordada",
                brand = "DAJHO",
                category = "Ropa",
                cost = 6.50,
                salePrice = 12.50,
                variants = mutableListOf(
                    ProductVariant(1L, "S", "Negro", 5),
                    ProductVariant(2L, "M", "Negro", 8),
                    ProductVariant(3L, "L", "Blanco", 6)
                )
            )
        )
        productList.add(
            Product(
                id = 2L,
                sku = "ZAP-042",
                name = "Zapatilla Deportiva Urbana",
                brand = "Genérica",
                category = "Calzado",
                cost = 14.00,
                salePrice = 25.00,
                variants = mutableListOf(
                    ProductVariant(1L, "38", "Blanco", 4),
                    ProductVariant(2L, "40", "Negro", 7)
                )
            )
        )
        productList.add(
            Product(
                id = 3L,
                sku = "GOR-009",
                name = "Gorra Visera Curva",
                brand = "Genérica",
                category = "Accesorio",
                cost = 3.20,
                salePrice = 7.00,
                variants = mutableListOf(
                    ProductVariant(1L, "Única", "Negro", 12)
                )
            )
        )
        filterProducts(etSearch.text?.toString().orEmpty())
    }

    private fun filterProducts(query: String) {
        val q = query.trim().lowercase()
        visibleProducts.clear()
        if (q.isEmpty()) {
            visibleProducts.addAll(productList)
        } else {
            visibleProducts.addAll(
                productList.filter {
                    it.sku.lowercase().contains(q) ||
                        it.name.lowercase().contains(q) ||
                        it.category.lowercase().contains(q)
                }
            )
        }
        if (::adapter.isInitialized) {
            adapter.updateData(visibleProducts)
            tvEmpty.visibility = if (visibleProducts.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    /**
     * Abre el formulario en modo creacion (product = null) o edicion.
     */
    private fun showProductForm(product: Product?) {
        val sheet = ProductFormSheet.newInstance(product)
        sheet.onProductSaved = { savedProduct ->
            val existingIndex = productList.indexOfFirst { it.id == savedProduct.id }
            if (existingIndex >= 0) {
                productList[existingIndex] = savedProduct
            } else {
                productList.add(savedProduct)
            }
            filterProducts(etSearch.text?.toString().orEmpty())
        }
        sheet.show(supportFragmentManager, ProductFormSheet.TAG)
    }
}
