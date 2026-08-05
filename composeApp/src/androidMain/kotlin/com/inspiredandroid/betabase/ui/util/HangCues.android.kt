package com.inspiredandroid.betabase.ui.util

import android.media.AudioManager
import android.media.ToneGenerator

private val toneGenerator: ToneGenerator by lazy {
    ToneGenerator(AudioManager.STREAM_MUSIC, 90)
}

actual fun playHangCue(cue: HangCue) {
    // Distinct system tones: sharp ack for hang start, longer prop beep for hang end.
    when (cue) {
        HangCue.HangStart -> toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 180)
        HangCue.HangEnd -> toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 280)
    }
}
