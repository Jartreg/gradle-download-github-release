package me.jartreg.gradle.downloadgithubrelease.internal

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.IllegalArgumentException

/**
 * A GitHub release
 */
@JsonClass(generateAdapter = true)
internal data class GitHubRelease(
        /**
         * The assets attached to this release
         */
        val assets: List<GitHubReleaseAsset>
)

/**
 * An asset of a release
 */
@JsonClass(generateAdapter = true)
internal data class GitHubReleaseAsset(
        /**
         * The url used download the file
         */
        val url: String,

        /**
         * The file name of the asset
         */
        val name: String,

        /**
         * The file size of the asset
         */
        val size: Long
)

private val moshi = Moshi.Builder()
        .build()

private val releaseAdapter = moshi.adapter(GitHubRelease::class.java)

/**
 * Fetches release information for the latest release
 *
 * @param client the client to use for the request
 * @param repositoryName the name of the repository containing the release
 */
internal fun getLatestRelease(client: OkHttpClient, repositoryName: String): GitHubRelease {
    validateRepositoryName(repositoryName)
    val req = Request.Builder()
            .url("https://api.github.com/repos/$repositoryName/releases/latest")
            .addGitHubAPIHeader()
            .build()

    return executeRequest(client, req)
}

/**
 * Fetches release information for a specific tag
 *
 * @param client the client to use for the request
 * @param repositoryName the name of the repository containing the release
 * @param tagName the name of the tag of the release
 */
internal fun getReleaseByTag(client: OkHttpClient, repositoryName: String, tagName: String): GitHubRelease {
    validateRepositoryName(repositoryName)
    val req = Request.Builder()
            .url("https://api.github.com/repos/$repositoryName/releases/tags/$tagName")
            .addGitHubAPIHeader()
            .build()

    return executeRequest(client, req)
}

/**
 * Checks that the String contains exactly one slash, which is required to keep the URL valid
 */
private fun validateRepositoryName(repositoryName: String) {
    if(repositoryName.count { it == '/' } != 1)
        throw IllegalArgumentException("Repository names must contain exactly one slash")
}

/**
 * Adds an `Accept` header to the request to tell GitHub the requested API version
 */
private fun Request.Builder.addGitHubAPIHeader() = this.apply {
    addHeader("Accept", "application/vnd.github.v3+json")
}

/**
 * Executes the request and parses the response into an instance of [GitHubRelease]
 */
private fun executeRequest(client: OkHttpClient, req: Request): GitHubRelease {
    return client.newCall(req).execute().use { res ->
        if (res.isSuccessful)
            releaseAdapter.fromJson(res.body()!!.source())
        else
            throw Exception("Could not fetch release information. Response: ${res.code()} ${res.message()}")
    } ?: throw Exception("Could not parse release information")
}