package com.example.dajho

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * Fragmento para el Dashboard Principal (Home Tab).
 * Métricas clave, resumen semanal y feed de actividad reciente.
 */
class HomeDashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_dashboard, container, false)
    }
}
