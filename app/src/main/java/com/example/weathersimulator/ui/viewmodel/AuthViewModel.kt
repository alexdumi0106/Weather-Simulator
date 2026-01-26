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
import android.util.Patterns
import com.google.firebase.auth.UserProfileChangeRequest
import com.example.weathersimulator.data.repository.UserRepository
import kotlinx.coroutines.flow.asStateFlow



data class AuthState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val user: User? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val loginUserUseCase: LoginUserUseCase,
    private val registerUserUseCase: RegisterUserUseCase,
    private val userRepository: com.example.weathersimulator.data.repository.UserRepository

) : ViewModel() {

    init {
        loadCurrentUser()
    }
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, error = null)
    }

    fun onFirstNameChange(value: String) {
        _state.value =   _state.value.copy(firstName = value, error = null)
    }

    fun onLastNameChange(value: String) {
        _state.value = _state.value.copy(lastName = value, error = null)
    }

    fun onConfirmPasswordChange(value: String) {
        _state.value = _state.value.copy(confirmPassword = value, error = null)
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

        when {
            s.firstName.isBlank() -> {
                _state.value = s.copy(error = "Numele este obligatoriu")
                return
            }
            s.lastName.isBlank() -> {
                _state.value = s.copy(error = "Prenumele este obligatoriu")
                return
            }
            s.email.isBlank() -> {
                _state.value = s.copy(error = "Email-ul este obligatoriu")
                return
            }

            !Patterns.EMAIL_ADDRESS.matcher(s.email).matches() -> {
                _state.value = s.copy(error = "Email invalid")
                return
            }
            s.password.isBlank() -> {
                _state.value = s.copy(error = "Parola este obligatorie")
                return
            }
            s.password.length < 6 -> {
                _state.value = s.copy(error = "Parola trebuie să aiba minim 6 caractere")
                return
            }
            s.confirmPassword.isBlank() -> {
                _state.value = s.copy(error = "Confirmarea parolei este obligatorie")
                return
            }
            s.password != s.confirmPassword -> {
                _state.value = s.copy(error = "Parolele nu coincid")
                return
            }
        }

        _state.value = s.copy(isLoading = true)

        viewModelScope.launch {
            val result = registerUserUseCase(
                email = s.email,
                password = s.password,
                firstName = s.firstName,
                lastName = s.lastName
            )

            result
                .onSuccess { user ->
                    val fullName = "${_state.value.firstName.trim()} ${_state.value.lastName.trim()}".trim()

                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null && fullName.isNotBlank()) {
                        val request = UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .build()

                        firebaseUser.updateProfile(request)
                            .addOnCompleteListener {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    success = true,
                                    user = user
                                )
                            }
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            success = true,
                            user = user
                        )
                    }
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

    fun loadCurrentUser() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val localUser = userRepository.getLocalUser(uid)
            if (localUser != null) {
                _state.value = _state.value.copy(user = localUser)
            }
        }
    }


}
