package com.example.dajho.reports

import android.content.Context
import com.example.dajho.data.local.entity.ClientEntity
import com.example.dajho.data.local.entity.ExpenseEntity
import com.example.dajho.data.local.entity.SaleEntity
import com.example.dajho.data.local.relation.ProductWithVariants
import com.example.dajho.util.toCurrencyString
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generador nativo de Reportes CSV para Excel (Fase 11).
 */
object CsvReportGenerator {

    fun generateSalesCsv(context: Context, sales: List<SaleEntity>): File {
        val dir = File(context.getExternalFilesDir(null), "reports").apply { if (!exists()) mkdirs() }
        val file = File(dir, "Reporte_Ventas_${System.currentTimeMillis()}.csv")

        val sb = StringBuilder()
        sb.append("Número de Venta,Fecha,Método de Pago,Referencia,Total ($)\n")

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        for (sale in sales) {
            val dateStr = dateFormat.format(Date(sale.timestamp))
            val totalStr = sale.totalCents.toCurrencyString().replace("$", "")
            sb.append("\"${sale.saleNumber}\",\"$dateStr\",\"${sale.paymentMethod}\",\"${sale.transferReference ?: ""}\",\"$totalStr\"\n")
        }

        FileOutputStream(file).use { out ->
            out.write(sb.toString().toByteArray(Charsets.UTF_8))
        }

        return file
    }

    fun generateInventoryCsv(context: Context, products: List<ProductWithVariants>): File {
        val dir = File(context.getExternalFilesDir(null), "reports").apply { if (!exists()) mkdirs() }
        val file = File(dir, "Reporte_Inventario_${System.currentTimeMillis()}.csv")

        val sb = StringBuilder()
        sb.append("SKU,Producto,Categoría,Talla,Color,Stock,Costo ($),Precio ($)\n")

        for (item in products) {
            val costStr = item.product.costCents.toCurrencyString().replace("$", "")
            val priceStr = item.product.salePriceCents.toCurrencyString().replace("$", "")
            for (v in item.variants) {
                sb.append("\"${item.product.sku}\",\"${item.product.name}\",\"${item.product.category}\",\"${v.size}\",\"${v.color}\",${v.stock},\"$costStr\",\"$priceStr\"\n")
            }
        }

        FileOutputStream(file).use { out ->
            out.write(sb.toString().toByteArray(Charsets.UTF_8))
        }

        return file
    }

    fun generateDebtorsCsv(context: Context, debtors: List<ClientEntity>): File {
        val dir = File(context.getExternalFilesDir(null), "reports").apply { if (!exists()) mkdirs() }
        val file = File(dir, "Reporte_Deudas_${System.currentTimeMillis()}.csv")

        val sb = StringBuilder()
        sb.append("Cliente,Teléfono,Detalle,Deuda Pendiente ($)\n")

        for (client in debtors) {
            val debtStr = client.debtCents.toCurrencyString().replace("$", "")
            sb.append("\"${client.name}\",\"${client.phone}\",\"${client.info}\",\"$debtStr\"\n")
        }

        FileOutputStream(file).use { out ->
            out.write(sb.toString().toByteArray(Charsets.UTF_8))
        }

        return file
    }

    fun generateExpensesCsv(context: Context, expenses: List<ExpenseEntity>): File {
        val dir = File(context.getExternalFilesDir(null), "reports").apply { if (!exists()) mkdirs() }
        val file = File(dir, "Reporte_Gastos_${System.currentTimeMillis()}.csv")

        val sb = StringBuilder()
        sb.append("Fecha,Categoría,Concepto,Monto ($)\n")

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        for (exp in expenses) {
            val dateStr = dateFormat.format(Date(exp.timestamp))
            val amountStr = exp.amountCents.toCurrencyString().replace("$", "")
            sb.append("\"$dateStr\",\"${exp.category}\",\"${exp.concept}\",\"$amountStr\"\n")
        }

        FileOutputStream(file).use { out ->
            out.write(sb.toString().toByteArray(Charsets.UTF_8))
        }

        return file
    }
}
