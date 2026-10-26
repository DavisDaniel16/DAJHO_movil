package com.example.dajho.data.repository

import android.util.Log
import com.example.dajho.data.local.dao.BusinessDao
import com.example.dajho.data.local.dao.SyncOperationDao
import com.example.dajho.data.local.dao.UserProfileDao
import com.example.dajho.data.local.datastore.UserPreferences
import com.example.dajho.data.local.datastore.PendingBusiness
import com.example.dajho.data.local.entity.BusinessEntity
import com.example.dajho.data.local.entity.SyncOperationEntity
import com.example.dajho.data.local.entity.UserProfileEntity
import com.example.dajho.domain.repository.AuthRepository
import com.example.dajho.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.header
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.json.JSONObject
import org.json.JSONArray
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Implementación del Repositorio de Autenticación con Supabase Auth, JWT y Validación RLS (Fase 18).
 */
class AuthRepositoryImpl(
    private val userProfileDao: UserProfileDao,
    private val businessDao: BusinessDao,
    private val syncDao: SyncOperationDao,
    private val userPreferences: UserPreferences
) : AuthRepository {

    private companion object {
        const val TAG = "AuthRepository"
    }

    override fun isLoggedInFlow(): Flow<Boolean> = userPreferences.isLoggedInFlow

    override fun activeUserNameFlow(): Flow<String> = userPreferences.userNameFlow.map { it.orEmpty() }

    override suspend fun getUserByEmail(email: String): UserProfileEntity? =
        userProfileDao.getUserByEmail(email)

    override suspend fun registerLocalUser(
        fullName: String,
        email: String,
        phone: String,
        businessName: String,
        passwordHash: String,
        rucOrCedula: String,
        businessPhone: String,
        address: String,
        rimpeType: String
    ): Result<UserProfileEntity> {
        val existing = userProfileDao.getUserByEmail(email)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("El correo electrónico ya está registrado"))
        }

        val auth = authenticate("/auth/v1/signup", JSONObject().apply {
            put("email", email)
            put("password", passwordHash)
            put("data", JSONObject().put("full_name", fullName).put("phone", phone))
        }.toString(), allowEmptyToken = true)

        val userId = auth.userId
            ?: return Result.failure(IllegalStateException("Supabase no devolvió el usuario registrado"))

        val token = auth.accessToken.orEmpty()
        if (token.isNotBlank()) {
            validateAndLogJwt(token, userId)
        }

        val businessId = UUID.randomUUID().toString()

        val business = BusinessEntity(
            id = businessId,
            name = businessName,
            rucOrCedula = rucOrCedula,
            phone = businessPhone,
            address = address,
            rimpeType = rimpeType
        )

        val profile = UserProfileEntity(
            userId = userId,
            businessId = businessId,
            fullName = fullName,
            email = email,
            phone = phone,
            role = "PROPIETARIO"
        )

        businessDao.insertBusiness(business)
        userProfileDao.insertUserProfile(profile)

        syncDao.insertOperation(
            SyncOperationEntity(
                entityName = "negocios",
                entityId = businessId,
                operation = "INSERT",
                payloadJson = JSONObject().apply {
                    put("id", businessId)
                    put("nombre", businessName)
                    put("ruc_o_cedula", rucOrCedula)
                    put("telefono", businessPhone)
                    put("direccion", address)
                    put("tipo_rimpe", rimpeType)
                }.toString()
            )
        )
        syncDao.insertOperation(
            SyncOperationEntity(
                entityName = "perfiles",
                entityId = userId,
                operation = "INSERT",
                payloadJson = JSONObject().apply {
                    put("usuario_id", userId)
                    put("negocio_id", businessId)
                    put("nombre_completo", fullName)
                    put("correo", email)
                    put("telefono", phone)
                    put("rol", "PROPIETARIO")
                }.toString()
            )
        )
        syncDao.insertOperation(
            SyncOperationEntity(
                entityName = "miembros_negocio",
                entityId = "$businessId:$userId",
                operation = "INSERT",
                payloadJson = JSONObject().apply {
                    put("id", UUID.randomUUID().toString())
                    put("negocio_id", businessId)
                    put("usuario_id", userId)
                    put("rol", "PROPIETARIO")
                }.toString()
            )
        )

        if (auth.accessToken != null) {
            try {
                ensureRemoteWorkspace(profile, business, auth.accessToken)
            } catch (ex: Exception) {
                Log.e(TAG, "Error al sincronizar workspace remoto durante registro: ${ex.message}", ex)
            }
            userPreferences.saveSession(
                userId = userId,
                userName = fullName,
                email = email,
                businessId = businessId,
                accessToken = auth.accessToken,
                refreshToken = auth.refreshToken
            )
        }

        return Result.success(profile)
    }

    override suspend fun loginLocalUser(email: String, passwordHash: String): Result<UserProfileEntity> {
        val auth = authenticate(
            "/auth/v1/token?grant_type=password",
            JSONObject().apply {
                put("email", email)
                put("password", passwordHash)
            }.toString()
        )
        val userId = auth.userId ?: return Result.failure(IllegalStateException("Supabase no devolvió el usuario"))
        val token = auth.accessToken.orEmpty()
        if (token.isBlank() || token == BuildConfig.SUPABASE_ANON_KEY) {
            return Result.failure(IllegalStateException("No se recibió un token de sesión válido. Cierra sesión e inicia sesión nuevamente."))
        }
        validateAndLogJwt(token, userId)

        val isAdminEmail = email.equals("dajho1617@gmail.com", ignoreCase = true)
        val isRemoteAdmin = isAdminEmail || checkRemoteSystemAdmin(userId, auth.accessToken)

        var user = userProfileDao.getUserProfile(userId)
            ?: userProfileDao.getUserByEmail(email)
            ?: restoreRemoteProfile(userId, auth.accessToken)

        if (user == null && isRemoteAdmin) {
            val adminProfile = UserProfileEntity(
                userId = userId,
                businessId = "SYSTEM_GLOBAL",
                fullName = "Administrador Global",
                email = email,
                phone = "",
                role = "ADMINISTRADOR"
            )
            userPreferences.saveSession(
                userId = userId,
                userName = "Administrador Global",
                email = email,
                businessId = "SYSTEM_GLOBAL",
                accessToken = token,
                refreshToken = auth.refreshToken
            )
            return Result.success(adminProfile)
        }

        if (user == null) {
            userPreferences.saveAuthenticatedUser(
                userId, email, token, auth.refreshToken
            )
            return Result.failure(IllegalStateException("SETUP_REQUIRED"))
        }

        if (!isRemoteAdmin) {
            val business = businessDao.getBusinessById(user.businessId)
            if (business == null) {
                userPreferences.saveAuthenticatedUser(
                    userId, email, token, auth.refreshToken
                )
                return Result.failure(IllegalStateException("SETUP_REQUIRED"))
            }

            try {
                ensureRemoteWorkspace(user, business, token)
            } catch (ex: Exception) {
                Log.e(TAG, "Error al asegurar workspace remoto en login: ${ex.message}", ex)
            }
        }

        userPreferences.saveSession(
            userId = userId,
            userName = user.fullName,
            email = user.email,
            businessId = user.businessId,
            accessToken = token,
            refreshToken = auth.refreshToken
        )

        return Result.success(user)
    }

    suspend fun completeBusinessSetup(
        fullName: String,
        email: String,
        phone: String,
        businessName: String,
        rucOrCedula: String,
        businessPhone: String,
        address: String,
        rimpeType: String
    ): Result<UserProfileEntity> = runCatching {
        val userId = userPreferences.userIdFlow.first()
            ?: throw IllegalStateException("No se encontró el usuario autenticado")
        val accessToken = userPreferences.accessTokenFlow.first()
            ?: throw IllegalStateException("No se recibió un token de sesión válido. Cierra sesión e inicia sesión nuevamente.")

        validateAndLogJwt(accessToken, userId)

        val businessId = UUID.randomUUID().toString()
        val business = BusinessEntity(
            id = businessId,
            name = businessName,
            rucOrCedula = rucOrCedula,
            phone = businessPhone,
            address = address,
            rimpeType = rimpeType
        )
        val profile = UserProfileEntity(
            userId = userId,
            businessId = businessId,
            fullName = fullName,
            email = email,
            phone = phone,
            role = "PROPIETARIO"
        )

        ensureRemoteWorkspace(profile, business, accessToken)

        businessDao.insertBusiness(business)
        userProfileDao.insertUserProfile(profile)

        userPreferences.saveSession(
            userId, fullName, email, businessId, accessToken,
            userPreferences.refreshTokenFlow.first()
        )
        profile
    }

    private suspend fun ensureRemoteWorkspace(
        profile: UserProfileEntity,
        business: BusinessEntity,
        accessToken: String?
    ) {
        if (accessToken.isNullOrBlank() || accessToken == BuildConfig.SUPABASE_ANON_KEY) {
            throw IllegalStateException("Token de autenticación inválido para RLS (Se requiere token de usuario autenticado)")
        }
        val client = HttpClient(Android)
        try {
            upsertRemote(client, accessToken, "negocios", JSONObject().apply {
                put("id", business.id)
                put("nombre", business.name)
                put("ruc_o_cedula", business.rucOrCedula)
                put("telefono", business.phone)
                put("direccion", business.address)
                put("tipo_rimpe", business.rimpeType)
            }, useConflict = false)
            upsertRemote(client, accessToken, "perfiles", JSONObject().apply {
                put("usuario_id", profile.userId)
                put("negocio_id", profile.businessId)
                put("nombre_completo", profile.fullName)
                put("correo", profile.email)
                put("telefono", profile.phone)
                put("rol", profile.role)
            }, "usuario_id")
            upsertRemote(client, accessToken, "miembros_negocio", JSONObject().apply {
                put("id", UUID.randomUUID().toString())
                put("negocio_id", profile.businessId)
                put("usuario_id", profile.userId)
                put("rol", profile.role)
            }, "negocio_id,usuario_id")
        } finally {
            client.close()
        }
    }

    private suspend fun upsertRemote(
        client: HttpClient,
        accessToken: String,
        table: String,
        payload: JSONObject,
        conflict: String = "id",
        useConflict: Boolean = true
    ) {
        val endpoint = if (useConflict) {
            "${BuildConfig.SUPABASE_URL}/rest/v1/$table?on_conflict=$conflict"
        } else {
            "${BuildConfig.SUPABASE_URL}/rest/v1/$table"
        }
        val response = client.post(
            endpoint
        ) {
            header("apikey", BuildConfig.SUPABASE_ANON_KEY)
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            header(
                "Prefer",
                if (useConflict) {
                    "resolution=merge-duplicates,return=minimal"
                } else {
                    "return=minimal"
                }
            )
            contentType(ContentType.Application.Json)
            setBody(payload.toString())
        }
        if (!response.status.isSuccess()) {
            val details = response.bodyAsText().trim()
            throw IllegalStateException(
                "No se pudo configurar $table en Supabase: HTTP ${response.status.value}" +
                    if (details.isNotBlank()) " - $details" else ""
            )
        }
    }

    private fun validateAndLogJwt(token: String, userId: String) {
        val segments = token.split('.')
        Log.d(TAG, "userId=$userId")
        Log.d(TAG, "accessTokenLength=${token.length}")
        Log.d(TAG, "accessTokenSegments=${segments.size}")
        Log.d(TAG, "accessTokenLooksJwt=${segments.size == 3}")
        if (token.length > 14) {
            Log.d(TAG, "tokenStart=${token.take(8)}")
            Log.d(TAG, "tokenEnd=${token.takeLast(6)}")
        }

        if (segments.size == 3) {
            try {
                val payloadJson = String(
                    android.util.Base64.decode(segments[1], android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING),
                    Charsets.UTF_8
                )
                val json = JSONObject(payloadJson)
                val role = json.optString("role")
                Log.d(TAG, "jwtRole=$role")
                if (role != "authenticated") {
                    throw IllegalStateException("Supabase rechazó la operación por permisos RLS. Revisa que el JWT tenga role 'authenticated' (actual: $role).")
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Error decodificando payload JWT: ${ex.message}", ex)
                if (ex is IllegalStateException && ex.message?.contains("role 'authenticated'") == true) {
                    throw ex
                }
            }
        } else {
            throw IllegalStateException("El token de sesión no tiene formato JWT válido de 3 segmentos.")
        }
    }

    private suspend fun checkRemoteSystemAdmin(userId: String, accessToken: String?): Boolean {
        if (accessToken.isNullOrBlank() || accessToken == BuildConfig.SUPABASE_ANON_KEY) return false
        val client = HttpClient(Android)
        return try {
            val response = client.get("${BuildConfig.SUPABASE_URL}/rest/v1/administradores_sistema?usuario_id=eq.$userId&activo=eq.true&select=*") {
                header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            if (!response.status.isSuccess()) return false
            val array = JSONArray(response.bodyAsText())
            array.length() > 0
        } catch (e: Exception) {
            false
        } finally {
            client.close()
        }
    }

    private suspend fun restoreRemoteProfile(
        userId: String,
        accessToken: String?
    ): UserProfileEntity? {
        if (accessToken.isNullOrBlank() || accessToken == BuildConfig.SUPABASE_ANON_KEY) return null
        val client = HttpClient(Android)
        return try {
            val profileResponse = client.get(
                "${BuildConfig.SUPABASE_URL}/rest/v1/perfiles?usuario_id=eq.$userId&select=*"
            ) {
                header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            if (!profileResponse.status.isSuccess()) return null
            val profiles = JSONArray(profileResponse.bodyAsText())
            if (profiles.length() == 0) return null

            val row = profiles.getJSONObject(0)
            val businessId = row.optString("negocio_id").takeUnless { it.isBlank() } ?: return null
            val profile = UserProfileEntity(
                userId = userId,
                businessId = businessId,
                fullName = row.optString("nombre_completo"),
                email = row.optString("correo"),
                phone = row.optString("telefono"),
                role = row.optString("rol", "PROPIETARIO")
            )
            val businessResponse = client.get(
                "${BuildConfig.SUPABASE_URL}/rest/v1/negocios?id=eq.$businessId&select=*"
            ) {
                header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            if (!businessResponse.status.isSuccess()) return null
            val businesses = JSONArray(businessResponse.bodyAsText())
            if (businesses.length() == 0) return null
            val businessRow = businesses.getJSONObject(0)
            businessDao.insertBusiness(
                BusinessEntity(
                    id = businessId,
                    name = businessRow.optString("nombre"),
                    rucOrCedula = businessRow.optString("ruc_o_cedula"),
                    phone = businessRow.optString("telefono"),
                    address = businessRow.optString("direccion"),
                    rimpeType = businessRow.optString("tipo_rimpe", "RIMPE_POPULAR")
                )
            )
            userProfileDao.insertUserProfile(profile)
            profile
        } finally {
            client.close()
        }
    }

    override suspend fun logout() {
        userPreferences.clearSession()
    }

    suspend fun requestPasswordReset(email: String): Result<Unit> = runCatching {
        val client = HttpClient(Android)
        try {
            val response = client.post("${BuildConfig.SUPABASE_URL}/auth/v1/recover") {
                header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                header(HttpHeaders.Authorization, "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                contentType(ContentType.Application.Json)
                setBody(JSONObject().put("email", email).toString())
            }
            if (!response.status.isSuccess()) {
                throw IllegalArgumentException(
                    "Supabase rechazó la recuperación: HTTP ${response.status.value}"
                )
            }
        } finally {
            client.close()
        }
    }

    private suspend fun authenticate(
        path: String,
        body: String,
        allowEmptyToken: Boolean = false
    ): AuthResponse {
        val client = HttpClient(Android)
        return try {
            val response = client.post("${BuildConfig.SUPABASE_URL}$path") {
                header("apikey", BuildConfig.SUPABASE_ANON_KEY)
                header(HttpHeaders.Authorization, "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            if (!response.status.isSuccess()) {
                val errText = response.bodyAsText()
                throw IllegalArgumentException("Supabase Auth rechazó la solicitud: HTTP ${response.status.value} - $errText")
            }
            val json = JSONObject(response.bodyAsText())
            AuthResponse(
                accessToken = json.optString("access_token").takeUnless { it.isBlank() },
                refreshToken = json.optString("refresh_token").takeUnless { it.isBlank() },
                userId = json.optJSONObject("user")?.optString("id")
                    ?.takeUnless { it.isNullOrBlank() }
            ).also {
                if (!allowEmptyToken && (it.accessToken == null || it.accessToken == BuildConfig.SUPABASE_ANON_KEY)) {
                    throw IllegalStateException("Supabase no devolvió un token de sesión válido (access_token nulo o anon_key)")
                }
            }
        } finally {
            client.close()
        }
    }

    private data class AuthResponse(
        val accessToken: String?,
        val refreshToken: String?,
        val userId: String?
    )
}
