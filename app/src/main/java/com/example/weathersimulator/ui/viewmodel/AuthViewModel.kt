package com.example.weathersimulator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathersimulator.domain.model.User
import com.example.weathersimulator.domain.usecase.LoginUserUseCase
import com.example.weathersimulator.domain.usecase.RegisterUserUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val user: User? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val loginUserUseCase: LoginUserUseCase,
    private val registerUserUseCase: RegisterUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, error = null)
    }

    fun login() {
        val s = _state.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.value = s.copy(error = "Email și parola sunt obligatorii")
            return
        }

        _state.value = s.copy(isLoading = true)

        viewModelScope.launch {
            val result = loginUserUseCase(s.email, s.password)
            result
                .onSuccess { user ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        success = true,
                        user = user
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Eroare la login"
                    )
                }
        }
    }

    fun register() {
        val s = _state.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.value = s.copy(error = "Email și parola sunt obligatorii")
            return
        }

        _state.value = s.copy(isLoading = true)

        viewModelScope.launch {
            val result = registerUserUseCase(s.email, s.password)
            result
                .onSuccess { user ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        success = true,
                        user = user
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Eroare la înregistrare"
                    )
                }
        }
    }

    fun logout() {
        auth.signOut()

        _state.value = state.value.copy(
            user = null,
            isLoading = false,
            success = false
        )
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _state.value = _state.value.copy(error = "Introduceți un email valid")
            return
        }

        _state.value = _state.value.copy(isLoading = true)

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                _state.value = _state.value.copy(
                    isLoading = false,
                    success = true,
                    error = null
                )
            }
            .addOnFailureListener { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Eroare la resetarea parolei"
                )
            }
    }

}
