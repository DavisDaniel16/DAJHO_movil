package com.example.dajho.clients

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dajho.R

/**
 * Módulo de Gestión de Clientes (Fase 2 - embebido como Fragmento en Home).
 */
class ClientListFragment : Fragment() {

    private lateinit var adapter: ClientAdapter
    private val clientList = mutableListOf<Client>()
    private val visibleClients = mutableListOf<Client>()

    private lateinit var etSearch: EditText
    private lateinit var tvEmpty: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_client_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etSearch = view.findViewById(R.id.etSearchClient)
        tvEmpty = view.findViewById(R.id.tvEmptyClients)
        val rvClients: RecyclerView = view.findViewById(R.id.rvClients)
        val btnNewClient: View = view.findViewById(R.id.btnNewClient)

        loadSeedData()

        adapter = ClientAdapter(visibleClients) { client ->
            showClientForm(client)
        }
        rvClients.layoutManager = LinearLayoutManager(requireContext())
        rvClients.adapter = adapter

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

    private fun loadSeedData() {
        clientList.clear()
        clientList.add(
            Client(
                id = 1L,
                name = "María Fernanda López",
                phone = "099 123 4567",
                info = "Centro de Guayaquil",
                debt = 0.00
            )
        )
        clientList.add(
            Client(
                id = 2L,
                name = "Carlos Alberto Mendoza",
                phone = "098 765 4321",
                info = "carlos.mendoza@gmail.com",
                debt = 28.50
            )
        )
        clientList.add(
            Client(
                id = 3L,
                name = "Lucía Beatriz Ramos",
                phone = "096 987 6543",
                info = "Mercado Central",
                debt = 15.00
            )
        )
        filterClients(etSearch.text?.toString().orEmpty())
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
        sheet.show(childFragmentManager, ClientFormSheet.TAG)
    }
}
