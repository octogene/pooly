package buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.net.HttpURLConnection
import java.net.URI

@CacheableTask
abstract class DownloadArtifactTask : DefaultTask() {
    @get:Input
    abstract val artifactUrl: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun download() {
        outputFile.get().asFile.parentFile.mkdirs()
        logger.quiet("Downloading ${artifactUrl.get()} to ${outputFile.get().asFile.absolutePath}")

        val connection = URI(artifactUrl.get()).toURL().openConnection() as HttpURLConnection
        connection.connect()

        connection.inputStream.use { input ->
            outputFile.get().asFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        logger.quiet("Download completed successfully")
    }
}
