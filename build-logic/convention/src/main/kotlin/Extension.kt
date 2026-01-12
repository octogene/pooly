
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

val Project.versionCatalog
    get(): VersionCatalog? = runCatching {
        extensions.getByType<VersionCatalogsExtension>().named("libs")
    }.onFailure {
        logger.warn("Unable to find libs catalog: $it")
    }.getOrNull()
