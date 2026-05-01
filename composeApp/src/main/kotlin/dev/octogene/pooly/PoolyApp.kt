package dev.octogene.pooly

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composer
import androidx.compose.runtime.tooling.ComposeStackTraceMode
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import dev.octogene.pooly.app.BuildConfig.OTEL_BASE_URL
import dev.octogene.pooly.di.AppGraph
import dev.octogene.pooly.di.DatabaseBindings
import dev.octogene.pooly.pooltogether.DrawWorker
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.android.MetroAppComponentProviders
import dev.zacsweers.metrox.android.MetroApplication
import io.opentelemetry.android.OpenTelemetryRum
import io.opentelemetry.android.agent.OpenTelemetryRumInitializer
import io.opentelemetry.api.common.AttributeKey.stringKey
import io.opentelemetry.api.common.Attributes
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

private const val WORKER_REPEAT_INTERVAL_MIN = 5L
class PoolyApp :
    Application(),
    MetroApplication,
    Configuration.Provider {

    var otelRum: OpenTelemetryRum? = null

    private val appGraph by lazy {
        createGraphFactory<AppGraph.Factory>()
            .create(
                application = this,
                databaseBindings = DatabaseBindings(),
            )
    }

    override val appComponentProviders: MetroAppComponentProviders
        get() = appGraph

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(appGraph.workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        otelRum = initOTel(this)

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
            PeriodicWorkRequestBuilder<DrawWorker>(WORKER_REPEAT_INTERVAL_MIN, TimeUnit.MINUTES)
                .setInputData(Data.Builder().putString("workName", "onCreate").build())
                .build()
        appGraph.workManager.enqueueUniquePeriodicWork(
            "updateDraws",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest,
        )
    }
}

private fun initOTel(context: Context): OpenTelemetryRum? = runCatching {
    OpenTelemetryRumInitializer.initialize(
        context = context,
        configuration = {
            httpExport {
                baseUrl = OTEL_BASE_URL
                baseHeaders = mapOf("foo" to "bar")
            }
            instrumentations {
                activity {
                    enabled(true)
                }
                fragment {
                    enabled(false)
                }
            }
            session {
                backgroundInactivityTimeout = 5.minutes
                maxLifetime = 1.days
            }
            globalAttributes {
                Attributes.of(stringKey("pooly-otel"), "test")
            }
        },
    )
}.onFailure {
    Log.e("OpenTelemetryRumInitializer", "Initialization failed", it)
}.getOrNull()
