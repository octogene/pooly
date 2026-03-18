package dev.octogene.pooly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.octogene.pooly.common.mobile.CredentialRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@ContributesIntoMap(AppScope::class)
@ViewModelKey(MainViewModel::class)
@Inject
class MainViewModel(credentialRepository: CredentialRepository) : ViewModel() {
    val state: StateFlow<MainUiState>
        field = MutableStateFlow<MainUiState>(MainUiState.Loading)

    init {
        viewModelScope.launch {
            credentialRepository.isLogged.collect { isLogged ->
                Logger.d { "Is logged: $isLogged" }
                state.update {
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
