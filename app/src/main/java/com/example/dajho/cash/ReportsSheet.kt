package com.example.dajho.cash

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.dajho.R
import com.example.dajho.data.local.AppDatabase
import com.example.dajho.reports.CsvReportGenerator
import com.example.dajho.reports.PdfReportGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import com.example.dajho.data.local.datastore.UserPreferences
import java.io.File

/**
 * BottomSheetDialogFragment para generar y compartir reportes reales en PDF/CSV vía Android Sharesheet (Fase 11).
 */
class ReportsSheet : BottomSheetDialogFragment() {

    private enum class ReportType { SALES, INVENTORY, DEBTORS, EXPENSES }
    private var selectedReportType = ReportType.SALES

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sheet_reports, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseReports)
        val cardReportSales = view.findViewById<CardView>(R.id.cardReportSales)
        val cardReportCash = view.findViewById<CardView>(R.id.cardReportCash)
        val btnPdf = view.findViewById<Button>(R.id.btnDownloadPdf)

        btnClose.setOnClickListener { dismiss() }

        cardReportSales?.setOnClickListener {
            selectedReportType = ReportType.SALES
            Toast.makeText(requireContext(), "Seleccionado: Reporte de Ventas", Toast.LENGTH_SHORT).show()
        }

        cardReportCash?.setOnClickListener {
            selectedReportType = ReportType.DEBTORS
            Toast.makeText(requireContext(), "Seleccionado: Reporte de Deudas", Toast.LENGTH_SHORT).show()
        }

        btnPdf.setOnClickListener {
            generateAndShareReport(isPdf = true)
        }
    }

    private fun generateAndShareReport(isPdf: Boolean) {
        lifecycleScope.launch {
            try {
                val ctx = context ?: return@launch
                val db = AppDatabase.getInstance(ctx)
                val businessId = UserPreferences(ctx).businessIdFlow.first()
                    ?: throw IllegalStateException("No hay un negocio activo")

                val reportFile: File = when (selectedReportType) {
                    ReportType.SALES -> {
                        val sales = db.saleDao().getPendingSyncSales() // or all sales
                        if (isPdf) {
                            PdfReportGenerator.generateSalesPdf(ctx, "Reporte de Ventas", sales)
                        } else {
                            CsvReportGenerator.generateSalesCsv(ctx, sales)
                        }
                    }
                    ReportType.INVENTORY -> {
                        val products = db.productDao().getAllProductsWithVariantsFlow().firstOrNull() ?: emptyList()
                        if (isPdf) {
                            PdfReportGenerator.generateInventoryPdf(ctx, products)
                        } else {
                            CsvReportGenerator.generateInventoryCsv(ctx, products)
                        }
                    }
                    ReportType.DEBTORS -> {
                        val debtors = db.clientSupplierDao().getAllClientsFlow(businessId).firstOrNull() ?: emptyList()
                        val pendingDebtors = debtors.filter { it.debtCents > 0 }
                        if (isPdf) {
                            PdfReportGenerator.generateDebtorsPdf(ctx, pendingDebtors)
                        } else {
                            CsvReportGenerator.generateDebtorsCsv(ctx, pendingDebtors)
                        }
                    }
                    ReportType.EXPENSES -> {
                        val expenses = db.expenseDao().getAllExpensesFlow(businessId).firstOrNull() ?: emptyList()
                        if (isPdf) {
                            PdfReportGenerator.generateSalesPdf(ctx, "Reporte de Gastos", emptyList())
                        } else {
                            CsvReportGenerator.generateExpensesCsv(ctx, expenses)
                        }
                    }
                }

                // Validacion estricta: No muestra "reporte generado" si no existe el archivo
                if (reportFile.exists() && reportFile.length() > 0) {
                    val fileUri: Uri = FileProvider.getUriForFile(
                        ctx,
                        "com.example.dajho.fileprovider",
                        reportFile
                    )

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = if (isPdf) "application/pdf" else "text/csv"
                        putExtra(Intent.EXTRA_STREAM, fileUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    startActivity(Intent.createChooser(shareIntent, "Compartir Reporte DAJHO"))
                    dismiss()
                } else {
                    Toast.makeText(ctx, "Error: No se pudo generar el archivo de reporte", Toast.LENGTH_LONG).show()
                }

            } catch (ex: Exception) {
                Toast.makeText(requireContext(), "Error al generar reporte: ${ex.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val TAG = "ReportsSheet"
    }
}
