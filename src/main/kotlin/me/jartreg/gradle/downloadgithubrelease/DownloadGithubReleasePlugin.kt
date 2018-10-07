package me.jartreg.gradle.downloadgithubrelease

import org.gradle.api.Plugin
import org.gradle.api.Project

class DownloadGithubReleasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val downloadGitHubRelease = DownloadGitHubRelease::class.java
        project.extensions.extraProperties[downloadGitHubRelease.simpleName] = downloadGitHubRelease
    }
}