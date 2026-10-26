package com.example.dajho.domain.model

/**
 * Clase centralizada de Permisos y Roles de DAJHO.
 * Roles de negocio: PROPIETARIO, VENDEDOR.
 * Rol global: ADMINISTRADOR DEL SISTEMA.
 * Solo el PROPIETARIO puede crear y gestionar perfiles de empleado.
 */
data class UserPermissions(
    val userId: String,
    val businessId: String,
    val role: String, // "PROPIETARIO", "VENDEDOR"
    val isSystemAdmin: Boolean = false
) {
    val isOwner: Boolean
        get() = role.equals("PROPIETARIO", ignoreCase = true) || isSystemAdmin

    val isSeller: Boolean
        get() = role.equals("VENDEDOR", ignoreCase = true)

    val canManageProducts: Boolean
        get() = isOwner || isSystemAdmin

    val canManageEmployees: Boolean
        get() = isOwner // Solo el propietario puede crear el perfil de empleado

    val canRegisterSales: Boolean
        get() = isOwner || isSeller

    val canManageCash: Boolean
        get() = isOwner

    val canViewReports: Boolean
        get() = isOwner

    val canManageBusinessSettings: Boolean
        get() = isOwner

    val canDeleteSales: Boolean
        get() = isOwner

    val canManageSuppliers: Boolean
        get() = isOwner

    val canManageClients: Boolean
        get() = isOwner || isSeller
}
