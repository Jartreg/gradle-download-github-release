package me.jartreg.gradle.downloadgithubrelease

import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Okio.buffer
import okio.Okio.sink
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.kohsuke.github.GitHub
import java.io.File

open class DownloadGitHubRelease : DefaultTask() {
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

    @TaskAction
    fun download() {
        val repository = GitHub.connectAnonymously().getRepository(repository)

        val release = if(tagName == null) {
            repository.latestRelease
        } else {
            repository.getReleaseByTagName(tagName) ?: throw NoSuchElementException("Release `$tagName` could not be found")
        }

        val dest = project.file(computedDestination)
        if(!dest.isDirectory)
            dest.mkdirs()

        val client = OkHttpClient()
        release.assets.forEach { asset ->
            val req = Request.Builder()
                    .url(asset.url)
                    .header("Accept", "application/octet-stream")
                    .build()

            client.newCall(req).execute().use { res ->
                buffer(sink(File(dest, asset.name))).use {
                    it.writeAll(res.body()!!.source())
                }
            }
        }
    }
}