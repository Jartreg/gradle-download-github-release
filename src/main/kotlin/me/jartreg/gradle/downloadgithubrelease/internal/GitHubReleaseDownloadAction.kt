package me.jartreg.gradle.downloadgithubrelease.internal

import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Okio
import org.gradle.api.Transformer
import org.gradle.api.logging.Logger
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import java.io.File

class GitHubReleaseDownloadAction(
        private val client: OkHttpClient,
        private val logger: Logger,
        private val progressLoggerFactory: ProgressLoggerFactory
) {

    fun download(repositoryName: String, tagName: String?, destination: File, nameTransformer: Transformer<String, String>?) {
        val progress = progressLoggerFactory.newOperation(GitHubReleaseDownloadAction::class.java)
        progress.start("Download assets for ${tagName ?: "latest"}", "fetching release information")

        try {
            val release = if (tagName == null) {
                getLatestRelease(client, repositoryName)
            } else {
                getReleaseByTag(client, repositoryName, tagName)
            }

            release.assets.forEach { asset ->
                val destinationFile = File(destination, nameTransformer?.transform(asset.name) ?: asset.name)
                logger.info("Download ${asset.name} to ${destinationFile.absolutePath}")

                val req = Request.Builder()
                        .url(asset.url)
                        .header("Accept", "application/octet-stream")
                        .build()

                client.newCall(req).execute().use { res ->
                    progress.progress("0 B/${asset.size.toSizeString()}")
                    val source = ProgressSource(progress, res.body()!!.source(), asset.size)

                    Okio.buffer(Okio.sink(destinationFile)).use {
                        it.writeAll(source)
                    }
                }
            }
        } finally {
            progress.completed()
        }
    }
}