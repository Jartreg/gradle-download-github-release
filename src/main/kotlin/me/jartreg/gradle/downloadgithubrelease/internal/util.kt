package me.jartreg.gradle.downloadgithubrelease.internal

import okio.Buffer
import okio.ForwardingSource
import okio.Source
import org.gradle.internal.logging.progress.ProgressLogger
import kotlin.math.pow

internal class ProgressSource(private val progress: ProgressLogger, source: Source, size: Long) : ForwardingSource(source) {
    private val size = size.toSizeString()
    private var totalBytes = 0L

    override fun read(source: Buffer, byteCount: Long): Long {
        val readBytes = super.read(source, byteCount)
        if(readBytes == -1L)
            return -1

        totalBytes += readBytes
        progress.progress("${totalBytes.toSizeString()}/$size")
        return readBytes
    }
}

internal fun Long.toSizeString(): String = when {
    this < 1024 -> "$this B"
    this < 1024.0.pow(2) -> "${this / 1024} KiB"
    this < 1024.0.pow(3) -> "%.2f MiB".format(this / 1024.0.pow(2))
    else -> "%.2f GiB".format(this / 1024.0.pow(3))
}