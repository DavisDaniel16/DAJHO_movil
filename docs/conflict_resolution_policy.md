# POLÍTICA OFICIAL DE RESOLUCIÓN DE CONFLICTOS OFFLINE-FIRST (DAJHO)

Este documento establece las reglas determinísticas para la resolución de conflictos entre la base de datos local SQLite (Room) y la base de datos remota PostgreSQL (Supabase).

---

## Principios Fundamentales
1. **Inmutabilidad Financiera:** Las Ventas (`sales`), Detalle de Ventas (`sale_items`), Pagos (`payments`), Abonos de Deuda (`debt_payments`) y Movimientos de Caja (`cash_movements`) son eventos financieros inmutables. Nunca se editan ni se eliminan.
2. **Cero Pérdida de Datos Locales:** Ante cualquier conflicto no trivial, los datos no coincidentes se guardan en la tabla auditada `sync_conflicts` para revisión y decisión explícita del comerciante.
3. **Multi-Negocio (Aislamiento RLS):** Toda operación valida la pertenencia al `business_id` mediante políticas Row Level Security (RLS) en Supabase.

---

## Reglas por Escenario de Conflicto

### 1. Sobreventa de Stock (Dos dispositivos vendiendo la última unidad)
* **Regla:** Se procesan ambas ventas en SQLite y Supabase (fidelidad financiera del dinero cobrado al cliente).
* **Acción:** La variante del producto calcula el stock restante que resulta en un valor negativo (`stock < 0`). El motor de sincronización genera una alerta de auditoría `SyncConflictEntity(conflictType = "OVERSELL_STOCK_WARNING")` notificando al usuario en la interfaz para reabastecimiento.

### 2. Edición Concurrente de Productos
* **Regla:** Algoritmo **Last-Write-Wins (LWW)** basado en la marca de tiempo `updatedAt` UTC.
* **Acción:** Prevalece el registro con `updatedAt` más reciente para atributos descriptivos (Nombre, Marca, Categoría). Si existe discrepancia en costo o precio de venta, se guarda una copia del registro anterior en `sync_conflicts`.

### 3. Abonos Concurrentes de Deudas
* **Regla:** **Inmutabilidad y Aditividad Acumulativa**.
* **Acción:** Ambos abonos creados offline se insertan en Supabase con sus respectivos UUIDs. La deuda acumulada del cliente en el servidor se recalcula dinámicamente como:
  $$\text{Deuda Restante} = \sum \text{Ventas Crédito} - \sum \text{Abonos Registrados}$$

### 4. Creación Concurrente de Gastos
* **Regla:** **Inmutabilidad Aditiva**.
* **Acción:** Cada gasto se inserta de forma independiente en la tabla `expenses` con su ID UUID.

### 5. Eliminaciones Offline
* **Regla:** **Borrado Suave (Soft Delete)** con `deletedAt IS NOT NULL`.
* **Acción:** Se conserva la fila en la base de datos con la marca de tiempo de eliminación. Si el objeto posee ventas o comprobantes vinculados históricamente, la base de datos preserva la integridad referencial.

### 6. Cambios Remotos durante Estado Offline (Pull Sync)
* **Regla:** Si un registro remoto cambió mientras el dispositivo estuvo sin conexión:
  * Para registros inmutables: Se descargan e insertan en Room por UUID.
  * Para registros editables: Si no existen cambios locales `PENDING_UPDATE`, actualiza Room. Si existen ediciones locales pendientes, guarda la colisión en `sync_conflicts` para decisión manual del usuario.
