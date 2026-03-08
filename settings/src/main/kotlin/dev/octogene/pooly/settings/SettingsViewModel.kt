package dev.octogene.pooly.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.octogene.pooly.core.ChainNetwork
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@ContributesIntoMap(AppScope::class)
@ViewModelKey(SettingsViewModel::class)
@Inject
class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    val activeNetworks: StateFlow<List<ChainNetwork>> =
        repository.observeNetworks().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val trackedWallets: StateFlow<List<String>> =
        repository.observeWallets().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun toggleNetwork(network: ChainNetwork) {
        repository.toggleNetwork(network)
    }

    fun addWallet(address: CharSequence, name: CharSequence? = null) {
        repository.addWallet(address.toString(), name?.toString())
    }
}
