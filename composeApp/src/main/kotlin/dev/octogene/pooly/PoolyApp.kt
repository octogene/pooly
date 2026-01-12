package dev.octogene.pooly

import android.app.Application
import androidx.compose.runtime.Composer
import androidx.compose.runtime.tooling.ComposeStackTraceMode
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.PeriodicWorkRequestBuilder
import dev.octogene.pooly.di.AppGraph
import dev.octogene.pooly.pooltogether.DrawWorker
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.android.MetroAppComponentProviders
import dev.zacsweers.metrox.android.MetroApplication
import java.util.concurrent.TimeUnit

class PoolyApp : Application(), MetroApplication, Configuration.Provider {

    private val appGraph by lazy {
        createGraphFactory<AppGraph.Factory>()
            .create(
                application = this,
                databaseBindings = DatabaseBindings()
            )
    }

    override val appComponentProviders: MetroAppComponentProviders
        get() = appGraph

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(appGraph.workerFactory).build()

    override fun onCreate() {
        super.onCreate()

        // Enable Compose stack traces for minified builds only.
        // Composer.setDiagnosticStackTraceMode(ComposeStackTraceMode.Auto)

        // Alternatively:
        // Enable verbose Compose stack traces for local debugging
        if (BuildConfig.DEBUG) {
            Composer.setDiagnosticStackTraceMode(ComposeStackTraceMode.SourceInformation)
        }

        scheduleBackgroundWork()
    }

    private fun scheduleBackgroundWork() {
        val workRequest =
            PeriodicWorkRequestBuilder<DrawWorker>(15, TimeUnit.MINUTES)
                .setInputData(Data.Builder().putString("workName", "onCreate").build())
                .build()
        appGraph.workManager.enqueue(workRequest)
    }
}
