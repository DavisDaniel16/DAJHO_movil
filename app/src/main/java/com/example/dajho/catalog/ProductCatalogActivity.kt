package com.example.dajho.catalog

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.dajho.R

/**
 * Pantalla de Gestion de Catalogo (Fase 1 - solo frontend).
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

        adapter = ProductAdapter(visibleProducts) { product ->
            showProductForm(product)
        }
        rvProducts.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 2)
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
