package com.inspiredandroid.betabase.ui.util

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.sin

actual fun playHangCue(cue: HangCue) {
    val (freqHz, durationMs) = when (cue) {
        HangCue.HangStart -> 880.0 to 160 // A5 — sharp "go"
        HangCue.HangEnd -> 523.25 to 260 // C5 — lower "stop"
    }
    thread(name = "hang-cue", isDaemon = true) {
        playSineBeep(freqHz = freqHz, durationMs = durationMs)
    }
}

private fun playSineBeep(freqHz: Double, durationMs: Int, sampleRate: Int = 22_050) {
    val format = AudioFormat(sampleRate.toFloat(), 16, 1, true, false)
    val frameCount = sampleRate * durationMs / 1000
    val buffer = ByteArray(frameCount * 2)
    val amplitude = 0.28
    for (i in 0 until frameCount) {
        // Short fade-in/out to avoid clicks.
        val t = i.toDouble() / frameCount
        val envelope = when {
            t < 0.05 -> t / 0.05
            t > 0.85 -> (1.0 - t) / 0.15
            else -> 1.0
        }.coerceIn(0.0, 1.0)
        val sample = (sin(2.0 * PI * freqHz * i / sampleRate) * amplitude * envelope * Short.MAX_VALUE)
            .toInt()
            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            .toShort()
        buffer[i * 2] = (sample.toInt() and 0xff).toByte()
        buffer[i * 2 + 1] = ((sample.toInt() shr 8) and 0xff).toByte()
    }
    var line: SourceDataLine? = null
    try {
        line = AudioSystem.getSourceDataLine(format)
        line.open(format)
        line.start()
        line.write(buffer, 0, buffer.size)
        line.drain()
    } catch (_: Exception) {
        // Audio device unavailable — ignore.
    } finally {
        try {
            line?.stop()
            line?.close()
        } catch (_: Exception) {
            // ignore
        }
    }
}
