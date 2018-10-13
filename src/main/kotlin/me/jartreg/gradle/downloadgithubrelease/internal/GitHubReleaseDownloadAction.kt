package me.jartreg.gradle.downloadgithubrelease.internal

import okhttp3.OkHttpClient
import okhttp3.Request
import okio.*
import org.gradle.api.Transformer
import org.gradle.api.logging.Logger
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import org.kohsuke.github.GitHub
import java.io.File

class GitHubReleaseDownloadAction(
        private val gitHub: GitHub,
        private val client: OkHttpClient,
        private val logger: Logger,
        private val progressLoggerFactory: ProgressLoggerFactory
) {

    fun download(repositoryName: String, tagName: String?, destination: File, nameTransformer: Transformer<String, String>?) {
        val progress = progressLoggerFactory.newOperation(GitHubReleaseDownloadAction::class.java)
        progress.start("Download assets for ${tagName ?: "latest"}", "fetching release information")

        val repository = gitHub.getRepository(repositoryName)
        val release = if(tagName == null) {
            repository.latestRelease
        } else {
            repository.getReleaseByTagName(tagName) ?: throw NoSuchElementException("Release `$tagName` could not be found")
        }

        release.assets.forEach { asset ->
            val destinationFile = File(destination, nameTransformer?.transform(asset.name) ?: asset.name)
            logger.info("Download ${asset.name} to ${destinationFile.absolutePath}")

            val req = Request.Builder()
                    .url(asset.url)
                    .header("Accept", "application/octet-stream")
                    .build()

            client.newCall(req).execute().use { res ->
                val body = res.body()!!
                val size = body.contentLength().takeIf { it != -1L }

                var source: Source = body.source()
                if(size != null) {
                    progress.progress("0 B/${size.toSizeString()}")
                    source = ProgressSource(progress, source, size)
                }

                Okio.buffer(Okio.sink(destinationFile)).use {
                    it.writeAll(source)
                }
            }
        }

        progress.completed()
    }
}