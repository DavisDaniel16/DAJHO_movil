package com.example.dajho

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * BottomSheetDialogFragment para el buscador comercial global (Fase 6).
 */
class GlobalSearchSheet : BottomSheetDialogFragment() {

    var onSuggestionSelected: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sheet_global_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseSearchSheet)
        val chipPolo = view.findViewById<TextView>(R.id.chipSearchPolo)
        val chipCarlos = view.findViewById<TextView>(R.id.chipSearchCarlos)
        val chipZapatilla = view.findViewById<TextView>(R.id.chipSearchZapatilla)

        btnClose.setOnClickListener { dismiss() }

        chipPolo.setOnClickListener {
            onSuggestionSelected?.invoke("Camiseta Polo")
            dismiss()
        }

        chipCarlos.setOnClickListener {
            onSuggestionSelected?.invoke("Carlos Mendoza")
            dismiss()
        }

        chipZapatilla.setOnClickListener {
            onSuggestionSelected?.invoke("Zapatilla")
            dismiss()
        }
    }

    companion object {
        const val TAG = "GlobalSearchSheet"
    }
}
