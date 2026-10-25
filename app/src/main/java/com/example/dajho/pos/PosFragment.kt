package com.example.dajho.pos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.dajho.R
import com.example.dajho.catalog.Product
import com.example.dajho.catalog.ProductAdapter
import com.example.dajho.clients.Client
import com.example.dajho.clients.ClientFormSheet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Módulo de Punto de Venta (POS - Fase 3).
 * Integración de Catálogo Visual Grid (2 columnas) + Escáner QR a pantalla completa (QrScannerActivity) + Carrito + Fiado estricto.
 */
class PosFragment : Fragment() {

    private val cartItems = mutableListOf<CartItem>()
    private lateinit var cartAdapter: CartAdapter

    private val visibleProducts = mutableListOf<Product>()
    private lateinit var catalogGridAdapter: ProductAdapter

    private var selectedPaymentMethod = PaymentMethod.CASH
    private var selectedClient: Client? = null // Default es Consumidor Final (null)

    // Base de datos en memoria para simular el API del POS
    private val catalogList = mutableListOf<Product>()
    private val clientsList = mutableListOf<Client>()

    private lateinit var etSearch: EditText
    private lateinit var btnScanQr: ImageButton
    private lateinit var rvCatalogGrid: RecyclerView
    private lateinit var rvCartItems: RecyclerView
    private lateinit var tvEmptyCart: TextView
    private lateinit var btnPayCash: TextView
    private lateinit var btnPayTransfer: TextView
    private lateinit var btnPayCredit: TextView
    private lateinit var tilTransferReference: TextInputLayout
    private lateinit var etTransferReference: TextInputEditText
    private lateinit var cardClientSelector: View
    private lateinit var tvSelectedClientName: TextView
    private lateinit var btnSelectClient: View
    private lateinit var tvCartTotalAmount: TextView
    private lateinit var btnCheckout: View

    // Scanner Activity Result Launcher
    private val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val scannedCode = result.data?.getStringExtra(QrScannerActivity.EXTRA_SCAN_RESULT).orEmpty()
            if (scannedCode.isNotBlank()) {
                val match = catalogList.find { it.sku.equals(scannedCode, ignoreCase = true) }
                if (match != null) {
                    promptSelectProductVariant(match)
                    Toast.makeText(requireContext(), "Escaneado: ${match.name}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Código no encontrado: $scannedCode", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Camera Permission Launcher
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openQrScannerActivity()
        } else {
            Toast.makeText(
                requireContext(),
                "Se requiere permiso de cámara para escaneo QR/Barras",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etSearch = view.findViewById(R.id.etSearchPos)
        btnScanQr = view.findViewById(R.id.btnScanQr)
        rvCatalogGrid = view.findViewById(R.id.rvPosCatalogGrid)
        rvCartItems = view.findViewById(R.id.rvCartItems)
        tvEmptyCart = view.findViewById(R.id.tvEmptyCart)
        btnPayCash = view.findViewById(R.id.btnPayCash)
        btnPayTransfer = view.findViewById(R.id.btnPayTransfer)
        btnPayCredit = view.findViewById(R.id.btnPayCredit)
        tilTransferReference = view.findViewById(R.id.tilTransferReference)
        etTransferReference = view.findViewById(R.id.etTransferReference)
        cardClientSelector = view.findViewById(R.id.cardClientSelector)
        tvSelectedClientName = view.findViewById(R.id.tvSelectedClientName)
        btnSelectClient = view.findViewById(R.id.btnSelectClient)
        tvCartTotalAmount = view.findViewById(R.id.tvCartTotalAmount)
        btnCheckout = view.findViewById(R.id.btnCheckout)

        loadSeedData()
        setupCatalogGrid()
        setupCartList()
        setupSearchTextWatcher()
        setupQrScannerButton()
        setupPaymentMethods()
        setupClientPicker()
        setupCheckout()
        updateCartSummary()
    }

    private fun loadSeedData() {
        if (catalogList.isEmpty()) {
            catalogList.add(
                Product(1L, "CAM-001", "Camiseta Polo Bordada", "DAJHO", "Ropa", 6.50, 12.50, null,
                    mutableListOf(
                        com.example.dajho.catalog.ProductVariant(1L, "S", "Negro", 5),
                        com.example.dajho.catalog.ProductVariant(2L, "M", "Negro", 8),
                        com.example.dajho.catalog.ProductVariant(3L, "L", "Blanco", 6)
                    )
                )
            )
            catalogList.add(
                Product(2L, "ZAP-042", "Zapatilla Deportiva Urbana", "Genérica", "Calzado", 14.00, 25.00, null,
                    mutableListOf(
                        com.example.dajho.catalog.ProductVariant(1L, "38", "Blanco", 4),
                        com.example.dajho.catalog.ProductVariant(2L, "40", "Negro", 7)
                    )
                )
            )
            catalogList.add(
                Product(3L, "GOR-009", "Gorra Visera Curva", "Genérica", "Accesorio", 3.20, 7.00, null,
                    mutableListOf(
                        com.example.dajho.catalog.ProductVariant(1L, "Única", "Negro", 12)
                    )
                )
            )
        }

        if (clientsList.isEmpty()) {
            clientsList.add(Client(1L, "María Fernanda López", "099 123 4567", "Centro", 0.0))
            clientsList.add(Client(2L, "Carlos Alberto Mendoza", "098 765 4321", "carlos@gmail.com", 28.50))
            clientsList.add(Client(3L, "Lucía Beatriz Ramos", "096 987 6543", "Mercado Central", 15.00))
        }

        filterCatalog(etSearch.text?.toString().orEmpty())
    }

    private fun setupCatalogGrid() {
        catalogGridAdapter = ProductAdapter(visibleProducts) { selectedProduct ->
            promptSelectProductVariant(selectedProduct)
        }
        rvCatalogGrid.layoutManager = GridLayoutManager(requireContext(), 2)
        rvCatalogGrid.adapter = catalogGridAdapter
        rvCatalogGrid.isNestedScrollingEnabled = false
    }

    private fun setupCartList() {
        cartAdapter = CartAdapter(cartItems) {
            updateCartSummary()
        }
        rvCartItems.layoutManager = LinearLayoutManager(requireContext())
        rvCartItems.adapter = cartAdapter
        rvCartItems.isNestedScrollingEnabled = false
    }

    private fun setupSearchTextWatcher() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterCatalog(s?.toString().orEmpty())
            }
        })
    }

    private fun filterCatalog(query: String) {
        val q = query.trim().lowercase()
        visibleProducts.clear()
        if (q.isEmpty()) {
            visibleProducts.addAll(catalogList)
        } else {
            visibleProducts.addAll(catalogList.filter {
                it.sku.lowercase().contains(q) ||
                    it.name.lowercase().contains(q) ||
                    it.category.lowercase().contains(q)
            })
        }
        if (::catalogGridAdapter.isInitialized) {
            catalogGridAdapter.updateData(visibleProducts)
        }
    }

    private fun setupQrScannerButton() {
        btnScanQr.setOnClickListener {
            checkCameraPermissionAndScan()
        }
    }

    private fun checkCameraPermissionAndScan() {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            openQrScannerActivity()
        } else {
            requestCameraPermissionLauncher.launch(permission)
        }
    }

    private fun openQrScannerActivity() {
        val intent = Intent(requireContext(), QrScannerActivity::class.java)
        scannerLauncher.launch(intent)
    }

    private fun promptSelectProductVariant(product: Product) {
        if (product.variants.size == 1) {
            addProductVariantToCart(product, product.variants[0])
        } else if (product.variants.size > 1) {
            val picker = VariantPickerSheet.newInstance(product)
            picker.onVariantSelected = { variant ->
                addProductVariantToCart(product, variant)
            }
            picker.show(childFragmentManager, VariantPickerSheet.TAG)
        }
    }

    private fun addProductVariantToCart(product: Product, variant: com.example.dajho.catalog.ProductVariant) {
        val existing = cartItems.find { it.product.id == product.id && it.variant.id == variant.id }
        if (existing != null) {
            if (existing.quantity < variant.stock) {
                existing.quantity++
            } else {
                Toast.makeText(requireContext(), "Stock máximo alcanzado", Toast.LENGTH_SHORT).show()
            }
        } else {
            cartItems.add(CartItem(product, variant, 1))
        }
        cartAdapter.notifyDataSetChanged()
        updateCartSummary()
    }

    private fun setupPaymentMethods() {
        selectPaymentMethod(PaymentMethod.CASH)

        btnPayCash.setOnClickListener { selectPaymentMethod(PaymentMethod.CASH) }
        btnPayTransfer.setOnClickListener { selectPaymentMethod(PaymentMethod.TRANSFER) }
        btnPayCredit.setOnClickListener { selectPaymentMethod(PaymentMethod.CREDIT) }
    }

    private fun selectPaymentMethod(method: PaymentMethod) {
        selectedPaymentMethod = method

        val bgSelected = R.drawable.bg_chip_selected
        val bgUnselected = R.drawable.bg_chip_unselected
        val colorSelected = ContextCompat.getColor(requireContext(), R.color.btn_text_color)
        val colorUnselected = ContextCompat.getColor(requireContext(), R.color.text_primary)

        btnPayCash.background = ContextCompat.getDrawable(requireContext(), if (method == PaymentMethod.CASH) bgSelected else bgUnselected)
        btnPayCash.setTextColor(if (method == PaymentMethod.CASH) colorSelected else colorUnselected)

        btnPayTransfer.background = ContextCompat.getDrawable(requireContext(), if (method == PaymentMethod.TRANSFER) bgSelected else bgUnselected)
        btnPayTransfer.setTextColor(if (method == PaymentMethod.TRANSFER) colorSelected else colorUnselected)

        btnPayCredit.background = ContextCompat.getDrawable(requireContext(), if (method == PaymentMethod.CREDIT) bgSelected else bgUnselected)
        btnPayCredit.setTextColor(if (method == PaymentMethod.CREDIT) colorSelected else colorUnselected)

        // Mostrar u ocultar campo de Comprobante de Transferencia
        tilTransferReference.visibility = if (method == PaymentMethod.TRANSFER) View.VISIBLE else View.GONE

        // Regla estricta para Fiado/Crédito: Exige cliente obligatoriamente (no Consumidor Final)
        if (method == PaymentMethod.CREDIT && selectedClient == null) {
            openClientPicker(allowConsumerFinal = false)
        }

        updateClientDisplay()
    }

    private fun setupClientPicker() {
        btnSelectClient.setOnClickListener {
            val allowConsumer = (selectedPaymentMethod != PaymentMethod.CREDIT)
            openClientPicker(allowConsumerFinal = allowConsumer)
        }
        updateClientDisplay()
    }

    private fun openClientPicker(allowConsumerFinal: Boolean) {
        val picker = ClientPickerSheet.newInstance(clientsList, allowConsumerFinal)
        picker.onClientSelected = { client ->
            selectedClient = client
            updateClientDisplay()
        }
        picker.onCreateNewClientRequested = {
            openNewClientForm()
        }
        picker.show(childFragmentManager, ClientPickerSheet.TAG)
    }

    private fun openNewClientForm() {
        val formSheet = ClientFormSheet.newInstance(null)
        formSheet.onClientSaved = { newClient ->
            clientsList.add(newClient)
            selectedClient = newClient
            updateClientDisplay()
        }
        formSheet.show(childFragmentManager, "NewClientInPos")
    }

    private fun updateClientDisplay() {
        val ctx = context ?: return
        if (selectedClient != null) {
            tvSelectedClientName.text = "${selectedClient?.name} (${selectedClient?.phone})"
            tvSelectedClientName.setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
        } else if (selectedPaymentMethod == PaymentMethod.CREDIT) {
            tvSelectedClientName.text = "Seleccionar cliente obligatoriamente"
            tvSelectedClientName.setTextColor(ContextCompat.getColor(ctx, R.color.badge_red_text))
        } else {
            tvSelectedClientName.text = getString(R.string.consumer_final_label)
            tvSelectedClientName.setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
        }
    }

    private fun setupCheckout() {
        btnCheckout.setOnClickListener {
            tilTransferReference.error = null

            if (cartItems.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_empty_cart, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validacion de comprobante para Transferencia
            var transferRef: String? = null
            if (selectedPaymentMethod == PaymentMethod.TRANSFER) {
                transferRef = etTransferReference.text?.toString()?.trim().orEmpty()
                if (transferRef.isEmpty()) {
                    tilTransferReference.error = getString(R.string.error_transfer_ref_required)
                    return@setOnClickListener
                }
            }

            // Validacion de cliente obligatorio para Fiado / Crédito
            if (selectedPaymentMethod == PaymentMethod.CREDIT && selectedClient == null) {
                Toast.makeText(requireContext(), R.string.error_select_client_required, Toast.LENGTH_LONG).show()
                openClientPicker(allowConsumerFinal = false)
                return@setOnClickListener
            }

            val total = calculateTotal()

            // Descontar stock
            cartItems.forEach { cartItem ->
                cartItem.variant.stock = (cartItem.variant.stock - cartItem.quantity).coerceAtLeast(0)
            }

            // Si es Fiado, sumar la deuda al cliente
            if (selectedPaymentMethod == PaymentMethod.CREDIT && selectedClient != null) {
                selectedClient?.debt = (selectedClient?.debt ?: 0.0) + total
            }

            val format = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US)
            val saleRecord = SaleRecord(
                saleId = String.format(Locale.US, "%06d", (100..999).random()),
                timestamp = format.format(Date()),
                items = ArrayList(cartItems),
                paymentMethod = selectedPaymentMethod,
                client = selectedClient,
                transferReference = transferRef,
                totalAmount = total
            )

            // Mostrar Nota de Venta Simple RIMPE
            val receiptSheet = ReceiptSheet.newInstance(saleRecord)
            receiptSheet.onClose = {
                cartItems.clear()
                cartAdapter.notifyDataSetChanged()
                etTransferReference.text?.clear()
                selectedClient = null
                selectPaymentMethod(PaymentMethod.CASH)
                updateCartSummary()
                if (::catalogGridAdapter.isInitialized) {
                    catalogGridAdapter.notifyDataSetChanged()
                }
            }
            receiptSheet.show(childFragmentManager, ReceiptSheet.TAG)
        }
    }

    private fun calculateTotal(): Double = cartItems.sumOf { it.subtotal() }

    private fun updateCartSummary() {
        val total = calculateTotal()
        tvCartTotalAmount.text = String.format(Locale.US, "$%.2f", total)
        tvEmptyCart.visibility = if (cartItems.isEmpty()) View.VISIBLE else View.GONE
    }
}
