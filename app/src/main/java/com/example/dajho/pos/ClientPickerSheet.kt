package com.example.dajho.pos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.dajho.R
import com.example.dajho.clients.Client
import com.example.dajho.clients.ClientAdapter

/**
 * BottomSheetDialogFragment para buscar, seleccionar o crear cliente en POS (Fase 3).
 * Soporta ocultar "Consumidor Final" en ventas Fiadas/Crédito.
 */
class ClientPickerSheet : BottomSheetDialogFragment() {

    var allowConsumerFinal: Boolean = true
    var onClientSelected: ((Client?) -> Unit)? = null
    var onCreateNewClientRequested: (() -> Unit)? = null
    var clientList: List<Client> = emptyList()

    private val visibleClients = mutableListOf<Client>()
    private lateinit var adapter: ClientAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sheet_client_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnClose: ImageButton = view.findViewById(R.id.btnCloseClientPicker)
        val etSearch: EditText = view.findViewById(R.id.etSearchClientPicker)
        val rvClients: RecyclerView = view.findViewById(R.id.rvClientPicker)
        val btnConsumerFinal: Button = view.findViewById(R.id.btnSelectConsumerFinal)
        val btnCreateNew: Button = view.findViewById(R.id.btnCreateNewClientInPicker)

        btnClose.setOnClickListener { dismiss() }

        if (!allowConsumerFinal) {
            btnConsumerFinal.visibility = View.GONE
        } else {
            btnConsumerFinal.visibility = View.VISIBLE
            btnConsumerFinal.setOnClickListener {
                onClientSelected?.invoke(null)
                dismiss()
            }
        }

        btnCreateNew.setOnClickListener {
            dismiss()
            onCreateNewClientRequested?.invoke()
        }

        visibleClients.clear()
        visibleClients.addAll(clientList)

        adapter = ClientAdapter(visibleClients) { client ->
            onClientSelected?.invoke(client)
            dismiss()
        }

        rvClients.layoutManager = LinearLayoutManager(requireContext())
        rvClients.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString()?.trim()?.lowercase().orEmpty()
                visibleClients.clear()
                if (q.isEmpty()) {
                    visibleClients.addAll(clientList)
                } else {
                    visibleClients.addAll(clientList.filter {
                        it.name.lowercase().contains(q) || it.phone.lowercase().contains(q)
                    })
                }
                adapter.updateData(visibleClients)
            }
        })
    }

    companion object {
        const val TAG = "ClientPickerSheet"
        fun newInstance(clients: List<Client>, allowConsumerFinal: Boolean = true): ClientPickerSheet {
            val sheet = ClientPickerSheet()
            sheet.clientList = clients
            sheet.allowConsumerFinal = allowConsumerFinal
            return sheet
        }
    }
}
