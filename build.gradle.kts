import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.2.71"
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
    implementation("org.kohsuke", "github-api", "1.94")
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