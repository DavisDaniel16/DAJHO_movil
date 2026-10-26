package com.example.dajho.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dajho.domain.repository.AuthRepository
import com.example.dajho.util.PasswordUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val message: String) : AuthUiState()
    data class Error(val errorMsg: String) : AuthUiState()
}

/**
 * ViewModel para gestionar el flujo de autenticación, sesión y registro (Fase 5).
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    val isLoggedInFlow = authRepository.isLoggedInFlow()

    fun registerUser(
        fullName: String,
        email: String,
        phone: String,
        businessName: String,
        passwordRaw: String,
        passwordConfirmRaw: String
    ) {
        if (fullName.isBlank()) {
            _uiState.value = AuthUiState.Error("Ingresa tu nombre completo")
            return
        }

        if (phone.length < 9) {
            _uiState.value = AuthUiState.Error("Ingresa un número de celular ecuatoriano válido")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = AuthUiState.Error("Ingresa un correo electrónico válido")
            return
        }

        if (passwordRaw.length < 6) {
            _uiState.value = AuthUiState.Error("La contraseña debe tener al menos 6 caracteres")
            return
        }

        if (passwordRaw != passwordConfirmRaw) {
            _uiState.value = AuthUiState.Error("Las contraseñas no coinciden")
            return
        }

        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            val passHash = PasswordUtils.hashPassword(passwordRaw)
            val result = authRepository.registerLocalUser(
                fullName = fullName,
                email = email,
                phone = phone,
                businessName = businessName,
                passwordHash = passHash
            )

            result.fold(
                onSuccess = {
                    _uiState.value = AuthUiState.Success("¡Registro exitoso! Sesión iniciada.")
                },
                onFailure = { ex ->
                    _uiState.value = AuthUiState.Error(ex.message ?: "Error al registrar usuario")
                }
            )
        }
    }

    fun loginUser(email: String, passwordRaw: String) {
        if (email.isBlank() || passwordRaw.isBlank()) {
            _uiState.value = AuthUiState.Error("Ingresa tu correo y contraseña")
            return
        }

        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            val passHash = PasswordUtils.hashPassword(passwordRaw)
            val result = authRepository.loginLocalUser(email, passHash)

            result.fold(
                onSuccess = {
                    _uiState.value = AuthUiState.Success("¡Bienvenido de nuevo!")
                },
                onFailure = { ex ->
                    _uiState.value = AuthUiState.Error(ex.message ?: "Error al iniciar sesión")
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
