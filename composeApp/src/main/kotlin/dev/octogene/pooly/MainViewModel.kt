package dev.octogene.pooly

import androidx.lifecycle.ViewModel
import dev.octogene.pooly.core.ChainNetwork
import dev.octogene.pooly.shared.model.Draw
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@ContributesIntoMap(AppScope::class)
@ViewModelKey(MainViewModel::class)
@Inject
class MainViewModel : ViewModel() {
    val draws: StateFlow<Map<ChainNetwork, List<Draw>>> = MutableStateFlow(emptyMap())
}
