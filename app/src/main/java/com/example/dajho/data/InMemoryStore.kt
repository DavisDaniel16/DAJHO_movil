package com.example.dajho.data

import com.example.dajho.catalog.Product
import com.example.dajho.catalog.ProductVariant
import com.example.dajho.clients.Client

/**
 * Fuente de datos compartida centralizada pre-Room (FASE 1).
 * Garantiza que Inventario, Clientes y POS compartan la misma lista de datos.
 */
object InMemoryStore {

    val products = mutableListOf<Product>()
    val clients = mutableListOf<Client>()

    init {
        seedProducts()
        seedClients()
    }

    private fun seedProducts() {
        if (products.isEmpty()) {
            products.add(
                Product(
                    id = "CAM-001-ID",
                    sku = "CAM-001",
                    name = "Camiseta Polo Bordada",
                    brand = "DAJHO",
                    category = "Ropa",
                    cost = 6.50,
                    salePrice = 12.50,
                    variants = mutableListOf(
                        ProductVariant("V1", "S", "Negro", 5),
                        ProductVariant("V2", "M", "Negro", 8),
                        ProductVariant("V3", "L", "Blanco", 6)
                    )
                )
            )
            products.add(
                Product(
                    id = "ZAP-042-ID",
                    sku = "ZAP-042",
                    name = "Zapatilla Deportiva Urbana",
                    brand = "Genérica",
                    category = "Calzado",
                    cost = 14.00,
                    salePrice = 25.00,
                    variants = mutableListOf(
                        ProductVariant("V4", "38", "Blanco", 4),
                        ProductVariant("V5", "40", "Negro", 7)
                    )
                )
            )
            products.add(
                Product(
                    id = "GOR-009-ID",
                    sku = "GOR-009",
                    name = "Gorra Visera Curva",
                    brand = "Genérica",
                    category = "Accesorio",
                    cost = 3.20,
                    salePrice = 7.00,
                    variants = mutableListOf(
                        ProductVariant("V6", "Única", "Negro", 12)
                    )
                )
            )
        }
    }

    private fun seedClients() {
        if (clients.isEmpty()) {
            clients.add(
                Client(
                    id = 1L,
                    name = "María Fernanda López",
                    phone = "099 123 4567",
                    info = "Centro de Guayaquil",
                    debt = 0.00
                )
            )
            clients.add(
                Client(
                    id = 2L,
                    name = "Carlos Alberto Mendoza",
                    phone = "098 765 4321",
                    info = "carlos.mendoza@gmail.com",
                    debt = 28.50
                )
            )
            clients.add(
                Client(
                    id = 3L,
                    name = "Lucía Beatriz Ramos",
                    phone = "096 987 6543",
                    info = "Mercado Central",
                    debt = 15.00
                )
            )
        }
    }

    fun saveProduct(product: Product) {
        val index = products.indexOfFirst { it.id == product.id }
        if (index >= 0) {
            products[index] = product
        } else {
            products.add(product)
        }
    }

    fun saveClient(client: Client) {
        val index = clients.indexOfFirst { it.id == client.id }
        if (index >= 0) {
            clients[index] = client
        } else {
            clients.add(client)
        }
    }
}
