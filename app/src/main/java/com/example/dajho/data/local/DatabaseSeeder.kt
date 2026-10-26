package com.example.dajho.data.local

import com.example.dajho.data.local.entity.ClientEntity
import com.example.dajho.data.local.entity.ProductEntity
import com.example.dajho.data.local.entity.ProductVariantEntity
import java.util.UUID

/**
 * Poblador inicial de semillas en Room SQLite (Fase 6).
 * Asegura sincronización 100% idéntica entre POS, Inventario y Clientes desde Room.
 */
object DatabaseSeeder {

    suspend fun seedIfEmpty(db: AppDatabase) {
        val existingCam = db.productDao().getProductBySku("CAM-001")
        if (existingCam == null) {
            val p1Id = UUID.randomUUID().toString()
            val p1 = ProductEntity(
                id = p1Id,
                sku = "CAM-001",
                name = "Camiseta Polo Bordada",
                brand = "DAJHO",
                category = "Ropa",
                costCents = 650L,
                salePriceCents = 1250L
            )
            val p1Variants = listOf(
                ProductVariantEntity(productId = p1Id, size = "S", color = "Negro", stock = 5),
                ProductVariantEntity(productId = p1Id, size = "M", color = "Negro", stock = 8),
                ProductVariantEntity(productId = p1Id, size = "L", color = "Blanco", stock = 6)
            )

            val p2Id = UUID.randomUUID().toString()
            val p2 = ProductEntity(
                id = p2Id,
                sku = "ZAP-042",
                name = "Zapatilla Deportiva Urbana",
                brand = "Genérica",
                category = "Calzado",
                costCents = 1400L,
                salePriceCents = 2500L
            )
            val p2Variants = listOf(
                ProductVariantEntity(productId = p2Id, size = "38", color = "Blanco", stock = 4),
                ProductVariantEntity(productId = p2Id, size = "40", color = "Negro", stock = 7)
            )

            val p3Id = UUID.randomUUID().toString()
            val p3 = ProductEntity(
                id = p3Id,
                sku = "GOR-009",
                name = "Gorra Visera Curva",
                brand = "Genérica",
                category = "Accesorio",
                costCents = 320L,
                salePriceCents = 700L
            )
            val p3Variants = listOf(
                ProductVariantEntity(productId = p3Id, size = "Única", color = "Negro", stock = 12)
            )

            db.productDao().insertProduct(p1)
            db.productDao().insertVariants(p1Variants)
            db.productDao().insertProduct(p2)
            db.productDao().insertVariants(p2Variants)
            db.productDao().insertProduct(p3)
            db.productDao().insertVariants(p3Variants)
        }

        val existingClient = db.clientSupplierDao().getClientById("C1")
        if (existingClient == null) {
            db.clientSupplierDao().insertClient(
                ClientEntity(
                    id = "C1",
                    name = "María Fernanda López",
                    phone = "099 123 4567",
                    info = "Centro de Guayaquil",
                    debtCents = 0L
                )
            )
            db.clientSupplierDao().insertClient(
                ClientEntity(
                    id = "C2",
                    name = "Carlos Alberto Mendoza",
                    phone = "098 765 4321",
                    info = "carlos.mendoza@gmail.com",
                    debtCents = 2850L
                )
            )
            db.clientSupplierDao().insertClient(
                ClientEntity(
                    id = "C3",
                    name = "Lucía Beatriz Ramos",
                    phone = "096 987 6543",
                    info = "Mercado Central",
                    debtCents = 1500L
                )
            )
        }
    }
}
