package dev.octogene.pooly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.octogene.pooly.common.mobile.CredentialRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@ContributesIntoMap(AppScope::class)
@ViewModelKey(MainViewModel::class)
@Inject
class MainViewModel(credentialRepository: CredentialRepository) : ViewModel() {
    private val _state =  MutableStateFlow<MainUiState>(MainUiState.Loading)
    val state: StateFlow<MainUiState> = _state

    init {
        viewModelScope.launch {
            credentialRepository.isLogged.collect { isLogged ->
                Logger.d { "Is logged: $isLogged" }
                _state.update {
                    if (isLogged) {
                        MainUiState.LoggedIn
                    } else {
                        MainUiState.LoggedOut
                    }
                }
            }
        }
    }
}

sealed class MainUiState {
    object Loading : MainUiState()
    object LoggedOut : MainUiState()
    object LoggedIn : MainUiState()
}
