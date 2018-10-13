import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.2.71"
    kotlin("kapt") version "1.2.71"
    `maven-publish`
}

group = "me.jartreg"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("com.squareup.okhttp3", "okhttp", "3.11.0")
    implementation("com.squareup.moshi", "moshi", "1.7.0")
    kapt("com.squareup.moshi", "moshi-kotlin-codegen", "1.7.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

gradlePlugin {
    plugins {
        create("downloadGithubRelease") {
            id = "me.jartreg.download-github-release"
            implementationClass = "me.jartreg.gradle.downloadgithubrelease.DownloadGithubReleasePlugin"
        }
    }
}