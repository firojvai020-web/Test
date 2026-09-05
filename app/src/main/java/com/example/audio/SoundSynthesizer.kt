package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class SoundSynthesizer {
    private val scope = CoroutineScope(Dispatchers.Default)
    var isMuted: Boolean = false

    private val sampleRate = 22050

    private fun playToneBuffer(buffer: ShortArray) {
        if (isMuted) return
        scope.launch {
            try {
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                // Give enough time to finish, then release
                Thread.sleep((buffer.size.toDouble() / sampleRate * 1000).toLong() + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
                // Ignore audio hardware transient issues
            }
        }
    }

    fun playSpawn() {
        if (isMuted) return
        val durationMs = 120
        val count = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val freq = 220.0 + (i * 180.0 / count)
            val envelope = 1.0 - (i.toDouble() / count)
            val sample = sin(2 * PI * freq * t) * envelope * 24000
            buffer[i] = sample.toInt().toShort()
        }
        playToneBuffer(buffer)
    }

    fun playSwordClash() {
        if (isMuted) return
        val durationMs = 90
        val count = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val decay = 1.0 - (i.toDouble() / count)
            // Metallic ring + noise
            val ring = sin(2 * PI * 1450.0 * t) * 0.6 + sin(2 * PI * 2890.0 * t) * 0.4
            val noise = (Math.random() * 2 - 1) * 0.3
            val sample = (ring + noise) * decay * 20000
            buffer[i] = sample.toInt().toShort()
        }
        playToneBuffer(buffer)
    }

    fun playArrowWhoosh() {
        if (isMuted) return
        val durationMs = 80
        val count = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val freq = 800.0 - (i * 500.0 / count)
            val decay = 1.0 - (i.toDouble() / count)
            val sample = sin(2 * PI * freq * t) * decay * 14000
            buffer[i] = sample.toInt().toShort()
        }
        playToneBuffer(buffer)
    }

    fun playExplosion() {
        if (isMuted) return
        val durationMs = 260
        val count = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val decay = 1.0 - (i.toDouble() / count)
            val rumble = sin(2 * PI * 65.0 * (i.toDouble() / sampleRate)) * 0.5
            val noise = (Math.random() * 2 - 1) * 0.5
            val sample = (rumble + noise) * decay * 28000
            buffer[i] = sample.toInt().toShort()
        }
        playToneBuffer(buffer)
    }

    fun playCoin() {
        if (isMuted) return
        val durationMs = 150
        val count = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(count)
        val f1 = 1760.0 // A6
        val f2 = 2637.0 // E7
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val decay = 1.0 - (i.toDouble() / count)
            val freq = if (i < count / 2) f1 else f2
            val sample = sin(2 * PI * freq * t) * decay * 18000
            buffer[i] = sample.toInt().toShort()
        }
        playToneBuffer(buffer)
    }

    fun playUpgrade() {
        if (isMuted) return
        val durationMs = 220
        val count = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(count)
        val notes = doubleArrayOf(440.0, 554.37, 659.25, 880.0) // A, C#, E, A
        val seg = count / notes.size
        for (i in 0 until count) {
            val noteIdx = (i / seg).coerceIn(0, notes.size - 1)
            val t = i.toDouble() / sampleRate
            val sample = sin(2 * PI * notes[noteIdx] * t) * 16000
            buffer[i] = sample.toInt().toShort()
        }
        playToneBuffer(buffer)
    }

    fun playEvolve() {
        if (isMuted) return
        val durationMs = 450
        val count = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(count)
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C, E, G, high C
        val seg = count / notes.size
        for (i in 0 until count) {
            val noteIdx = (i / seg).coerceIn(0, notes.size - 1)
            val t = i.toDouble() / sampleRate
            val decay = 1.0 - (i.toDouble() / count) * 0.3
            val sample = sin(2 * PI * notes[noteIdx] * t) * decay * 22000
            buffer[i] = sample.toInt().toShort()
        }
        playToneBuffer(buffer)
    }

    fun playVictory() {
        if (isMuted) return
        val durationMs = 550
        val count = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(count)
        val notes = doubleArrayOf(440.0, 554.0, 659.0, 880.0, 1108.0)
        val seg = count / notes.size
        for (i in 0 until count) {
            val noteIdx = (i / seg).coerceIn(0, notes.size - 1)
            val t = i.toDouble() / sampleRate
            val sample = sin(2 * PI * notes[noteIdx] * t) * 20000
            buffer[i] = sample.toInt().toShort()
        }
        playToneBuffer(buffer)
    }

    fun playDefeat() {
        if (isMuted) return
        val durationMs = 400
        val count = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(count)
        val notes = doubleArrayOf(440.0, 392.0, 349.0, 293.66)
        val seg = count / notes.size
        for (i in 0 until count) {
            val noteIdx = (i / seg).coerceIn(0, notes.size - 1)
            val t = i.toDouble() / sampleRate
            val decay = 1.0 - (i.toDouble() / count)
            val sample = sin(2 * PI * notes[noteIdx] * t) * decay * 20000
            buffer[i] = sample.toInt().toShort()
        }
        playToneBuffer(buffer)
    }

    fun playSkillTrigger() {
        if (isMuted) return
        val durationMs = 300
        val count = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val freq = 300.0 + sin(t * 30) * 200
            val decay = 1.0 - (i.toDouble() / count)
            val sample = sin(2 * PI * freq * t) * decay * 22000
            buffer[i] = sample.toInt().toShort()
        }
        playToneBuffer(buffer)
    }
}
