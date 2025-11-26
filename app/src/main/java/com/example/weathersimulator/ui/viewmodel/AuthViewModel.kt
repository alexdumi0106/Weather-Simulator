import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUser: LoginUserUseCase,
    private val registerUser: RegisterUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = loginUser(email, password)
            _state.value = if (result.isSuccess) {
                AuthState(success = true, user = result.getOrNull())
            } else {
                AuthState(error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            val result = registerUser(email, password)
            _state.value = if (result.isSuccess) {
                AuthState(success = true, user = result.getOrNull())
            } else {
                AuthState(error = result.exceptionOrNull()?.message)
            }
        }
    }
}

data class AuthState(
    val success: Boolean = false,
    val user: User? = null,
    val error: String? = null
)
