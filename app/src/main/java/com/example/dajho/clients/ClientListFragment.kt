package com.example.dajho.clients

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dajho.R
import androidx.lifecycle.lifecycleScope
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.data.local.DatabaseSeeder
import com.example.dajho.data.local.entity.ClientEntity
import com.example.dajho.data.local.entity.SupplierEntity
import com.example.dajho.data.local.datastore.UserPreferences
import com.example.dajho.util.toCentsLong
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Módulo de Deudas y Clientes (Estilo Treinta - Fase 4).
 * Pestañas Por cobrar | Por pagar, resumen de deudas y botones de acción rápida.
 */
class ClientListFragment : Fragment() {

    private enum class DebtTab { POR_COBRAR, POR_PAGAR }
    private var currentTab = DebtTab.POR_COBRAR

    private lateinit var adapter: ClientAdapter
    private val supplierList = mutableListOf<Client>()
    private val clientList = mutableListOf<Client>()
    private val clientIdsByUiId = mutableMapOf<Long, String>()
    private val supplierIdsByUiId = mutableMapOf<Long, String>()
    private val visibleItems = mutableListOf<Client>()

    private lateinit var tabPorCobrar: TextView
    private lateinit var tabPorPagar: TextView
    private lateinit var tabUnderline: View
    private lateinit var etSearch: EditText
    private lateinit var tvEmpty: TextView
    private lateinit var tvTotalDebtAmount: TextView
    private lateinit var tvTotalDebtorsCount: TextView
    private lateinit var badgeDebtorsCount: TextView
    private lateinit var btnNewSale: Button
    private lateinit var btnNewClient: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_client_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabPorCobrar = view.findViewById(R.id.tabPorCobrar)
        tabPorPagar = view.findViewById(R.id.tabPorPagar)
        tabUnderline = view.findViewById(R.id.tabUnderlineDeudas)
        etSearch = view.findViewById(R.id.etSearchClient)
        tvEmpty = view.findViewById(R.id.tvEmptyClients)
        tvTotalDebtAmount = view.findViewById(R.id.tvTotalDebtAmount)
        tvTotalDebtorsCount = view.findViewById(R.id.tvTotalDebtorsCount)
        badgeDebtorsCount = view.findViewById(R.id.badgeDebtorsCount)

        val rvClients: RecyclerView = view.findViewById(R.id.rvClients)
        btnNewSale = view.findViewById(R.id.btnNewSaleFloatingDeudas)
        btnNewClient = view.findViewById(R.id.btnNewClientFloating)
        val btnReports: Button = view.findViewById(R.id.btnReportsDeudas)

        adapter = ClientAdapter(visibleItems) { item ->
            showClientForm(item)
        }
        rvClients.layoutManager = LinearLayoutManager(requireContext())
        rvClients.adapter = adapter

        setupTabs()

        btnNewSale.setOnClickListener {
            activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)?.selectedItemId = R.id.nav_sales
        }

        btnNewClient.setOnClickListener {
            showClientForm(null)
        }

        btnReports.setOnClickListener {
            val type = if (currentTab == DebtTab.POR_COBRAR) "cuentas por cobrar" else "cuentas por pagar"
            Toast.makeText(requireContext(), "Generando reporte de $type...", Toast.LENGTH_SHORT).show()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterItems(s?.toString().orEmpty())
            }
        })

        selectTab(DebtTab.POR_COBRAR)
        observeClients()
    }

    override fun onResume() {
        super.onResume()
        filterItems(etSearch.text?.toString().orEmpty())
    }

    private fun setupTabs() {
        tabPorCobrar.setOnClickListener { selectTab(DebtTab.POR_COBRAR) }
        tabPorPagar.setOnClickListener { selectTab(DebtTab.POR_PAGAR) }
    }

    private fun selectTab(tab: DebtTab) {
        currentTab = tab
        val ctx = context ?: return

        val colorActive = ContextCompat.getColor(ctx, R.color.btn_black)
        val colorInactive = ContextCompat.getColor(ctx, R.color.text_secondary)

        val params = tabUnderline.layoutParams as ConstraintLayout.LayoutParams
        if (tab == DebtTab.POR_COBRAR) {
            tabPorCobrar.setTextColor(colorActive)
            tabPorCobrar.setTypeface(null, android.graphics.Typeface.BOLD)
            tabPorPagar.setTextColor(colorInactive)
            tabPorPagar.setTypeface(null, android.graphics.Typeface.NORMAL)

            params.startToStart = R.id.tabHeaderDeudas
            params.endToEnd = ConstraintLayout.LayoutParams.UNSET
            btnNewClient.text = "+ Crear cliente"
        } else {
            tabPorCobrar.setTextColor(colorInactive)
            tabPorCobrar.setTypeface(null, android.graphics.Typeface.NORMAL)
            tabPorPagar.setTextColor(colorActive)
            tabPorPagar.setTypeface(null, android.graphics.Typeface.BOLD)

            params.startToStart = ConstraintLayout.LayoutParams.UNSET
            params.endToEnd = R.id.tabHeaderDeudas
            btnNewClient.text = "+ Crear proveedor"
        }
        tabUnderline.layoutParams = params

        filterItems(etSearch.text?.toString().orEmpty())
    }

    private fun observeClients() {
        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            DatabaseSeeder.seedIfEmpty(db)
            val businessId = UserPreferences(requireContext()).businessIdFlow.first()
                ?: return@launch
            launch {
                db.clientSupplierDao().getAllClientsFlow(businessId).collect { entities ->
                    clientList.clear()
                    clientIdsByUiId.clear()
                    clientList.addAll(entities.map { entity ->
                        val uiId = entity.id.hashCode().toLong()
                        clientIdsByUiId[uiId] = entity.id
                        Client(
                            uiId,
                            entity.name,
                            entity.phone,
                            entity.info,
                            entity.debtCents / 100.0
                        )
                    })
                    filterItems(etSearch.text?.toString().orEmpty())
                }
            }
            launch {
                db.clientSupplierDao().getAllSuppliersFlow(businessId).collect { entities ->
                    supplierList.clear()
                    supplierIdsByUiId.clear()
                    supplierList.addAll(entities.map { entity ->
                        val uiId = entity.id.hashCode().toLong()
                        supplierIdsByUiId[uiId] = entity.id
                        Client(uiId, entity.name, entity.phone, entity.info, entity.debtCents / 100.0)
                    })
                    filterItems(etSearch.text?.toString().orEmpty())
                }
            }
        }
    }

    private fun filterItems(query: String) {
        val targetList = if (currentTab == DebtTab.POR_COBRAR) clientList else supplierList
        val q = query.trim().lowercase()

        visibleItems.clear()
        if (q.isEmpty()) {
            visibleItems.addAll(targetList)
        } else {
            visibleItems.addAll(
                targetList.filter {
                    it.name.lowercase().contains(q) || it.phone.lowercase().contains(q)
                }
            )
        }

        if (::adapter.isInitialized) {
            adapter.updateData(visibleItems)
            tvEmpty.visibility = if (visibleItems.isEmpty()) View.VISIBLE else View.GONE
            updateDebtSummary(targetList)
        }
    }

    private fun updateDebtSummary(list: List<Client>) {
        val debtors = list.filter { it.debt > 0.001 }
        val totalDebt = debtors.sumOf { it.debt }

        tvTotalDebtAmount.text = String.format(Locale.US, "$%.2f", totalDebt)
        val entityLabel = if (currentTab == DebtTab.POR_COBRAR) "Clientes con deuda" else "Proveedores con deuda"
        tvTotalDebtorsCount.text = "${debtors.size} $entityLabel"
        badgeDebtorsCount.text = "${debtors.size}"
    }

    private fun showClientForm(client: Client?) {
        val sheet = ClientFormSheet.newInstance(client)
        sheet.onClientSaved = { savedClient ->
            if (currentTab == DebtTab.POR_COBRAR) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val entityId = clientIdsByUiId[savedClient.id] ?: java.util.UUID.randomUUID().toString()
                    AppDatabase.getInstance(requireContext()).clientSupplierDao().insertClient(
                        ClientEntity(
                            id = entityId,
                            businessId = UserPreferences(requireContext()).businessIdFlow.first()
                                ?: return@launch,
                            name = savedClient.name,
                            phone = savedClient.phone,
                            info = savedClient.info,
                            debtCents = savedClient.debt.toCentsLong()
                        )
                    )
                }
            } else {
                viewLifecycleOwner.lifecycleScope.launch {
                    val businessId = UserPreferences(requireContext()).businessIdFlow.first()
                        ?: return@launch
                    val supplierId = supplierIdsByUiId[savedClient.id] ?: java.util.UUID.randomUUID().toString()
                    AppDatabase.getInstance(requireContext()).clientSupplierDao().insertSupplier(
                        SupplierEntity(
                            id = supplierId,
                            businessId = businessId,
                            name = savedClient.name,
                            phone = savedClient.phone,
                            info = savedClient.info,
                            debtCents = savedClient.debt.toCentsLong()
                        )
                    )
                }
            }
            filterItems(etSearch.text?.toString().orEmpty())
        }
        sheet.show(childFragmentManager, ClientFormSheet.TAG)
    }
}
