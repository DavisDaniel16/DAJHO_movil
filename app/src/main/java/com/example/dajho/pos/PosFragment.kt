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
import androidx.lifecycle.lifecycleScope
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
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.data.local.DatabaseSeeder
import com.example.dajho.data.local.datastore.UserPreferences
import com.example.dajho.data.local.entity.PaymentEntity
import com.example.dajho.data.local.entity.ClientEntity
import com.example.dajho.data.local.entity.SaleEntity
import com.example.dajho.data.local.entity.SaleItemEntity
import com.example.dajho.util.toCentsLong
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Módulo de Punto de Venta (POS - Fase 7).
 * Integración con Transacción Atómica SQLite en Room, Escáner QR, Carrito y validaciones estrictas.
 */
class PosFragment : Fragment() {

    private val cartItems = mutableListOf<CartItem>()
    private lateinit var cartAdapter: CartAdapter

    private val allRoomProducts = mutableListOf<Product>()
    private val visibleProducts = mutableListOf<Product>()
    private lateinit var catalogGridAdapter: ProductAdapter

    private var selectedPaymentMethod = PaymentMethod.CASH
    private var selectedClient: Client? = null // Default es Consumidor Final (null)
    private val clientIdsByUiId = mutableMapOf<Long, String>()

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
                val match = allRoomProducts.find { it.sku.equals(scannedCode, ignoreCase = true) }
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

        setupCatalogGrid()
        setupCartList()
        setupSearchTextWatcher()
        setupQrScannerButton()
        setupPaymentMethods()
        setupClientPicker()
        setupCheckout()
        observeRoomProductsForPos()
        updateCartSummary()
    }

    private fun observeRoomProductsForPos() {
        viewLifecycleOwner.lifecycleScope.launch {
            val ctx = context ?: return@launch
            val db = AppDatabase.getInstance(ctx)
            DatabaseSeeder.seedIfEmpty(db)

            val businessId = UserPreferences(ctx).businessIdFlow.first() ?: "DEFAULT_BUSINESS"

            db.productDao().getProductsByBusinessFlow(businessId).collect { roomWithVariants ->
                allRoomProducts.clear()
                val mapped = roomWithVariants.map { item ->
                    Product(
                        id = item.product.id,
                        sku = item.product.sku,
                        name = item.product.name,
                        brand = item.product.brand,
                        category = item.product.category,
                        cost = item.product.costCents / 100.0,
                        salePrice = item.product.salePriceCents / 100.0,
                        imageUri = item.product.imageUri,
                        variants = item.variants.map { v ->
                            com.example.dajho.catalog.ProductVariant(
                                id = v.id,
                                size = v.size,
                                color = v.color,
                                stock = v.stock
                            )
                        }.toMutableList()
                    )
                }

                allRoomProducts.addAll(mapped)
                filterCatalog(etSearch.text?.toString().orEmpty())
            }
        }
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
        val sourceList = allRoomProducts

        visibleProducts.clear()
        if (q.isEmpty()) {
            visibleProducts.addAll(sourceList)
        } else {
            visibleProducts.addAll(sourceList.filter {
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
            if (variant.stock > 0) {
                cartItems.add(CartItem(product, variant, 1))
            } else {
                Toast.makeText(requireContext(), "Variante sin stock disponible", Toast.LENGTH_SHORT).show()
            }
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

        tilTransferReference.visibility = if (method == PaymentMethod.TRANSFER) View.VISIBLE else View.GONE

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
        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            val businessId = UserPreferences(requireContext()).businessIdFlow.first()
                ?: return@launch
            val entities = db.clientSupplierDao().getAllClientsFlow(businessId).firstOrNull().orEmpty()
            val clients = entities.map { entity ->
                val uiId = entity.id.hashCode().toLong()
                clientIdsByUiId[uiId] = entity.id
                Client(uiId, entity.name, entity.phone, entity.info, entity.debtCents / 100.0)
            }
            val picker = ClientPickerSheet.newInstance(clients, allowConsumerFinal)
            picker.onClientSelected = { client ->
                selectedClient = client
                updateClientDisplay()
            }
            picker.onCreateNewClientRequested = {
                openNewClientForm()
            }
            picker.show(childFragmentManager, ClientPickerSheet.TAG)
        }
    }

    private fun openNewClientForm() {
        val formSheet = ClientFormSheet.newInstance(null)
        formSheet.onClientSaved = { newClient ->
            val entityId = UUID.randomUUID().toString()
            viewLifecycleOwner.lifecycleScope.launch {
                AppDatabase.getInstance(requireContext()).clientSupplierDao().insertClient(
                    ClientEntity(
                        id = entityId,
                        businessId = UserPreferences(requireContext()).businessIdFlow.first()
                            ?: return@launch,
                        name = newClient.name,
                        phone = newClient.phone,
                        info = newClient.info,
                        debtCents = newClient.debt.toCentsLong()
                    )
                )
            }
            clientIdsByUiId[newClient.id] = entityId
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

            // 1. Carrito no vacio
            if (cartItems.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_empty_cart, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Comprobante obligatorio para Transferencia
            var transferRef: String? = null
            if (selectedPaymentMethod == PaymentMethod.TRANSFER) {
                transferRef = etTransferReference.text?.toString()?.trim().orEmpty()
                if (transferRef.isEmpty()) {
                    tilTransferReference.error = getString(R.string.error_transfer_ref_required)
                    return@setOnClickListener
                }
            }

            // 3. Cliente obligatorio para Fiado / Crédito
            if (selectedPaymentMethod == PaymentMethod.CREDIT && selectedClient == null) {
                Toast.makeText(requireContext(), R.string.error_select_client_required, Toast.LENGTH_LONG).show()
                openClientPicker(allowConsumerFinal = false)
                return@setOnClickListener
            }

            val totalDouble = calculateTotal()
            val totalCents = totalDouble.toCentsLong()

            lifecycleScope.launch {
                try {
                    val db = AppDatabase.getInstance(requireContext())
                    val preferences = UserPreferences(requireContext())
                    val businessId = preferences.businessIdFlow.first()
                        ?: throw IllegalStateException("No hay un negocio activo")
                    val userId = preferences.userIdFlow.first().orEmpty()
                    val activeShift = db.cashDao().getActiveShiftFlow().firstOrNull()

                    val saleId = UUID.randomUUID().toString()
                    val saleNum = String.format(Locale.US, "%06d", (1000..9999).random())

                    val saleEntity = SaleEntity(
                        id = saleId,
                        businessId = businessId,
                        userId = userId,
                        saleNumber = saleNum,
                        clientId = selectedClient?.let {
                            clientIdsByUiId[it.id] ?: it.id.toString()
                        },
                        paymentMethod = selectedPaymentMethod.name,
                        transferReference = transferRef,
                        totalCents = totalCents
                    )

                    val saleItemEntities = cartItems.map { cartItem ->
                        SaleItemEntity(
                            saleId = saleId,
                            productId = cartItem.product.id.toString(),
                            variantId = cartItem.variant.id.toString(),
                            quantity = cartItem.quantity,
                            unitPriceCents = cartItem.product.salePrice.toCentsLong(),
                            subtotalCents = cartItem.subtotal().toCentsLong()
                        )
                    }

                    val paymentEntity = PaymentEntity(
                        saleId = saleId,
                        method = selectedPaymentMethod.name,
                        amountCents = totalCents,
                        reference = transferRef
                    )

                    // EJECUCIÓN ATÓMICA EN SQLITE (Fase 7)
                    db.saleDao().recordCompleteSale(
                        sale = saleEntity,
                        items = saleItemEntities,
                        payments = listOf(paymentEntity),
                        activeShiftId = activeShift?.id
                    )

                    com.example.dajho.data.sync.SyncManager.triggerImmediateSync(requireContext())

                    // Descontar stock local
                    cartItems.forEach { cartItem ->
                        cartItem.variant.stock = (cartItem.variant.stock - cartItem.quantity).coerceAtLeast(0)
                    }

                    if (selectedPaymentMethod == PaymentMethod.CREDIT && selectedClient != null) {
                        selectedClient?.debt = (selectedClient?.debt ?: 0.0) + totalDouble
                    }

                    val format = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US)
                    val saleRecord = SaleRecord(
                        saleId = saleNum,
                        timestamp = format.format(Date()),
                        items = ArrayList(cartItems),
                        paymentMethod = selectedPaymentMethod,
                        client = selectedClient,
                        transferReference = transferRef,
                        totalAmount = totalDouble
                    )

                    // Mostrar recibo SOLO tras confirmar que se guardó exitosamente
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

                } catch (ex: Exception) {
                    // Si la transacción SQLite falla, NO vacía el carrito ni muestra recibo
                    Toast.makeText(requireContext(), "Error al guardar venta en base de datos: ${ex.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun calculateTotal(): Double = cartItems.sumOf { it.subtotal() }

    private fun updateCartSummary() {
        val total = calculateTotal()
        tvCartTotalAmount.text = String.format(Locale.US, "$%.2f", total)
        tvEmptyCart.visibility = if (cartItems.isEmpty()) View.VISIBLE else View.GONE
    }
}
