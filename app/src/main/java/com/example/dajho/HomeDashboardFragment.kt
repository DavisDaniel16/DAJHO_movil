package com.example.dajho

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment

/**
 * Fragmento para el Dashboard Principal (Home Tab).
 */
class HomeDashboardFragment : Fragment() {

    var onModuleNavigate: ((Int) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.cardNewSale).setOnClickListener {
            onModuleNavigate?.invoke(R.id.nav_sales)
        }
        view.findViewById<View>(R.id.cardInventory).setOnClickListener {
            onModuleNavigate?.invoke(R.id.nav_inventory)
        }
        view.findViewById<View>(R.id.cardClients).setOnClickListener {
            onModuleNavigate?.invoke(R.id.nav_clients)
        }
        view.findViewById<View>(R.id.cardReports).setOnClickListener {
            Toast.makeText(requireContext(), "Módulo de Reportes", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<AppCompatButton>(R.id.btnLogout).setOnClickListener {
            Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }
}
