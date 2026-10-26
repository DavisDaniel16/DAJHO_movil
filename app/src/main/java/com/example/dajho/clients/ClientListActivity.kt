package com.example.dajho.clients

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dajho.R

/**
 * Activity para administrar Clientes (Fase 2 - solo frontend).
 * Listado, buscador en vivo, badges de saldo ("AL DÍA" / "CON DEUDA") y edicion.
 */
class ClientListActivity : AppCompatActivity() {

    private lateinit var adapter: ClientAdapter
    private val clientList = mutableListOf<Client>()
    private val visibleClients = mutableListOf<Client>()

    private lateinit var etSearch: EditText
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_list)

        etSearch = findViewById(R.id.etSearchClient)
        tvEmpty = findViewById(R.id.tvEmptyClients)
        val rvClients: RecyclerView = findViewById(R.id.rvClients)
        val btnNewClient: View = findViewById(R.id.btnNewClient)
        val btnBack: ImageView = findViewById(R.id.btnBackClients)

        adapter = ClientAdapter(visibleClients) { client ->
            showClientForm(client)
        }
        rvClients.layoutManager = LinearLayoutManager(this)
        rvClients.adapter = adapter

        btnBack.setOnClickListener { finish() }

        btnNewClient.setOnClickListener {
            showClientForm(null)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterClients(s?.toString().orEmpty())
            }
        })
    }

    private fun filterClients(query: String) {
        val q = query.trim().lowercase()
        visibleClients.clear()
        if (q.isEmpty()) {
            visibleClients.addAll(clientList)
        } else {
            visibleClients.addAll(
                clientList.filter {
                    it.name.lowercase().contains(q) || it.phone.lowercase().contains(q)
                }
            )
        }
        if (::adapter.isInitialized) {
            adapter.updateData(visibleClients)
            tvEmpty.visibility = if (visibleClients.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showClientForm(client: Client?) {
        val sheet = ClientFormSheet.newInstance(client)
        sheet.onClientSaved = { savedClient ->
            val index = clientList.indexOfFirst { it.id == savedClient.id }
            if (index >= 0) {
                clientList[index] = savedClient
            } else {
                clientList.add(savedClient)
            }
            filterClients(etSearch.text?.toString().orEmpty())
        }
        sheet.show(supportFragmentManager, ClientFormSheet.TAG)
    }
}
