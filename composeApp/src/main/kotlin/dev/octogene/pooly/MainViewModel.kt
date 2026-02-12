package dev.octogene.pooly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.octogene.pooly.core.Prize
import dev.octogene.pooly.pooltogether.DrawsRepository
import dev.octogene.pooly.pooltogether.PoolTogetherRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@ContributesIntoMap(AppScope::class)
@ViewModelKey(MainViewModel::class)
@Inject
class MainViewModel(
    private val poolTogetherRepository: PoolTogetherRepository,
    private val drawsRepository: DrawsRepository,
) : ViewModel() {
    val draws: StateFlow<List<Prize>> = poolTogetherRepository.getAllDraws()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList(),
        )

    val city: StateFlow<String>
        field = MutableStateFlow("")

    val alldraws = drawsRepository.getAllDrawsPaged()
}
