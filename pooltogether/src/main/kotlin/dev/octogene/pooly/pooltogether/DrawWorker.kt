package dev.octogene.pooly.pooltogether

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.touchlab.kermit.Logger
import dev.octogene.pooly.common.mobile.di.MetroWorkerFactory
import dev.octogene.pooly.common.mobile.di.WorkerKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding

@AssistedInject
class DrawWorker(appContext: Context, @Assisted params: WorkerParameters, val repository: PoolTogetherRepository) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = try {
        repository.updateAllVaults().fold(
            ifLeft = { error ->
                Logger.e { "Failed to update vaults: $error" }
                Result.failure()
            },
            ifRight = { Result.success() },
        )
    } catch (e: Throwable) {
        Logger.e(e) { "Unexpected error updating vaults" }
        Result.failure()
    }

    @WorkerKey(DrawWorker::class)
    @ContributesIntoMap(
        AppScope::class,
        binding = binding<MetroWorkerFactory.WorkerInstanceFactory<*>>(),
    )
    @AssistedFactory
    abstract class Factory : MetroWorkerFactory.WorkerInstanceFactory<DrawWorker>
}
