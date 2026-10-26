package com.example.dajho.reports

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
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
 * Generador nativo de Documentos PDF oficiales para Android (Fase 11).
 */
object PdfReportGenerator {

    fun generateSalesPdf(context: Context, title: String, sales: List<SaleEntity>): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paintTitle = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            isFakeBoldText = true
        }
        val paintSub = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
        }
        val paintText = Paint().apply {
            color = Color.BLACK
            textSize = 10f
        }
        val paintHeader = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            isFakeBoldText = true
        }

        canvas.drawText("DAJHO ECUADOR - $title", 40f, 50f, paintTitle)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        canvas.drawText("Fecha de Generación: ${dateFormat.format(Date())}", 40f, 70f, paintSub)

        canvas.drawLine(40f, 85f, 555f, 85f, paintHeader)

        // Headers
        var y = 110f
        canvas.drawText("Venta #", 40f, y, paintHeader)
        canvas.drawText("Fecha", 120f, y, paintHeader)
        canvas.drawText("Método", 260f, y, paintHeader)
        canvas.drawText("Total ($)", 460f, y, paintHeader)

        y += 20f
        var totalCentsSum = 0L

        for (sale in sales) {
            if (y > 780f) break
            val dateStr = SimpleDateFormat("dd/MM/yy HH:mm", Locale.US).format(Date(sale.timestamp))
            canvas.drawText("#${sale.saleNumber}", 40f, y, paintText)
            canvas.drawText(dateStr, 120f, y, paintText)
            canvas.drawText(sale.paymentMethod, 260f, y, paintText)
            canvas.drawText(sale.totalCents.toCurrencyString(), 460f, y, paintText)
            totalCentsSum += sale.totalCents
            y += 20f
        }

        canvas.drawLine(40f, y, 555f, y, paintHeader)
        y += 25f
        canvas.drawText("TOTAL VENTAS: ${totalCentsSum.toCurrencyString()}", 320f, y, paintTitle)

        document.finishPage(page)

        val dir = File(context.getExternalFilesDir(null), "reports").apply { if (!exists()) mkdirs() }
        val file = File(dir, "Reporte_Ventas_${System.currentTimeMillis()}.pdf")

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }

    fun generateInventoryPdf(context: Context, products: List<ProductWithVariants>): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paintTitle = Paint().apply { color = Color.BLACK; textSize = 18f; isFakeBoldText = true }
        val paintSub = Paint().apply { color = Color.DKGRAY; textSize = 12f }
        val paintText = Paint().apply { color = Color.BLACK; textSize = 10f }
        val paintHeader = Paint().apply { color = Color.BLACK; textSize = 11f; isFakeBoldText = true }

        canvas.drawText("DAJHO ECUADOR - Reporte de Inventario", 40f, 50f, paintTitle)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        canvas.drawText("Fecha: ${dateFormat.format(Date())}", 40f, 70f, paintSub)

        var y = 110f
        canvas.drawText("SKU", 40f, y, paintHeader)
        canvas.drawText("Producto", 120f, y, paintHeader)
        canvas.drawText("Stock", 320f, y, paintHeader)
        canvas.drawText("Precio", 460f, y, paintHeader)

        y += 20f
        for (item in products) {
            if (y > 780f) break
            val totalStock = item.variants.sumOf { it.stock }
            canvas.drawText(item.product.sku, 40f, y, paintText)
            canvas.drawText(item.product.name, 120f, y, paintText)
            canvas.drawText("$totalStock und", 320f, y, paintText)
            canvas.drawText(item.product.salePriceCents.toCurrencyString(), 460f, y, paintText)
            y += 20f
        }

        document.finishPage(page)

        val dir = File(context.getExternalFilesDir(null), "reports").apply { if (!exists()) mkdirs() }
        val file = File(dir, "Reporte_Inventario_${System.currentTimeMillis()}.pdf")

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }

    fun generateDebtorsPdf(context: Context, debtors: List<ClientEntity>): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paintTitle = Paint().apply { color = Color.BLACK; textSize = 18f; isFakeBoldText = true }
        val paintSub = Paint().apply { color = Color.DKGRAY; textSize = 12f }
        val paintText = Paint().apply { color = Color.BLACK; textSize = 10f }
        val paintHeader = Paint().apply { color = Color.BLACK; textSize = 11f; isFakeBoldText = true }

        canvas.drawText("DAJHO ECUADOR - Clientes con Deuda", 40f, 50f, paintTitle)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        canvas.drawText("Fecha: ${dateFormat.format(Date())}", 40f, 70f, paintSub)

        var y = 110f
        canvas.drawText("Cliente", 40f, y, paintHeader)
        canvas.drawText("Teléfono", 220f, y, paintHeader)
        canvas.drawText("Deuda Pendiente ($)", 400f, y, paintHeader)

        y += 20f
        var totalDebtCents = 0L

        for (client in debtors) {
            if (y > 780f) break
            canvas.drawText(client.name, 40f, y, paintText)
            canvas.drawText(client.phone, 220f, y, paintText)
            canvas.drawText(client.debtCents.toCurrencyString(), 400f, y, paintText)
            totalDebtCents += client.debtCents
            y += 20f
        }

        canvas.drawLine(40f, y, 555f, y, paintHeader)
        y += 25f
        canvas.drawText("TOTAL POR COBRAR: ${totalDebtCents.toCurrencyString()}", 280f, y, paintTitle)

        document.finishPage(page)

        val dir = File(context.getExternalFilesDir(null), "reports").apply { if (!exists()) mkdirs() }
        val file = File(dir, "Reporte_Deudas_${System.currentTimeMillis()}.pdf")

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }
}
