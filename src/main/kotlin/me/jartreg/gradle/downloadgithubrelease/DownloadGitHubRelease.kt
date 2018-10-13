package me.jartreg.gradle.downloadgithubrelease

import me.jartreg.gradle.downloadgithubrelease.internal.GitHubReleaseDownloadAction
import okhttp3.OkHttpClient
import org.gradle.api.DefaultTask
import org.gradle.api.Transformer
import org.gradle.api.tasks.*
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import java.io.File
import javax.inject.Inject

open class DownloadGitHubRelease @Inject constructor(private val progressLoggerFactory: ProgressLoggerFactory) : DefaultTask() {
    @Input
    var repository: String = ""

    @Input
    @Optional
    var tagName: String? = null

    @Internal
    var destination: Any? = null

    val computedDestination: Any
        @OutputDirectory
        get() = destination ?: File(project.buildDir, "github/$repository/${tagName ?: "latest"}")

    private var renamingAction: Transformer<String, String>? = null

    fun rename(renamingAction: Transformer<String, String>) {
        this.renamingAction = renamingAction
    }

    @TaskAction
    fun download() {
        if(project.gradle.startParameter.isOffline) {
            throw IllegalStateException("Unable to download in offline mode")
        }

        val dest = project.file(computedDestination)
        if(!dest.isDirectory)
            dest.mkdirs()

        GitHubReleaseDownloadAction(OkHttpClient(), project.logger, progressLoggerFactory)
                .download(repository, tagName, dest, renamingAction)
    }
}