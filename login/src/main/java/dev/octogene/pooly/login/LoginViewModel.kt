package dev.octogene.pooly.login

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.octogene.pooly.common.mobile.CredentialRepository
import dev.octogene.pooly.pooltogether.PoolyApiClient
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@ContributesIntoMap(AppScope::class)
@ViewModelKey(LoginViewModel::class)
@Inject
class LoginViewModel(
    private val credentialRepository: CredentialRepository,
    private val poolyApiClient: PoolyApiClient,
) : ViewModel() {

    val emailState = TextFieldState()
    val usernameState = TextFieldState()
    val passwordState = TextFieldState()
    var isRegisterMode by mutableStateOf(false)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun toggleMode() {
        isRegisterMode = !isRegisterMode
    }

    fun onAction() {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            if (isRegisterMode) {
                register()
            } else {
                login()
            }
        }
    }

    private suspend fun login() {
        val username = usernameState.text.toString()
        val password = passwordState.text.toString()
        poolyApiClient.loginUser(username, password).onLeft {
            Logger.d { "Failed to login with $username : $it" }
            _uiState.value = LoginUiState.Error(it.toString())
        }.onRight {
            credentialRepository.setLogin(username, password)
            poolyApiClient.getWallets().onRight {
                Logger.d { "Setting login for $username" }
                _uiState.value = LoginUiState.Success
            }
        }
    }

    private suspend fun register() {
        TODO("Registration not implemented")
    }
}

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}
