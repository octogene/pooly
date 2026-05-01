package dev.octogene.pooly.di

import android.app.Application
import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import dev.octogene.pooly.common.mobile.di.MetroWorkerFactory
import dev.octogene.pooly.di.DatabaseBindings
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.android.MetroAppComponentProviders
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import kotlin.reflect.KClass

@DependencyGraph(AppScope::class)
interface AppGraph :
    MetroAppComponentProviders,
    ViewModelGraph {

    @Provides
    fun provideApplicationContext(application: Application): Context = application

    val workManager: WorkManager

    @Provides
    fun providesWorkManager(application: Context): WorkManager = WorkManager.getInstance(application)

    @Multibinds
    val workerProviders:
        Map<KClass<out ListenableWorker>, () -> MetroWorkerFactory.WorkerInstanceFactory<*>>

    val workerFactory: MetroWorkerFactory

    @DependencyGraph.Factory
    interface Factory {
        fun create(@Provides application: Application, @Includes databaseBindings: DatabaseBindings): AppGraph
    }
}
