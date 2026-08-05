package com.inspiredandroid.betabase.ui.util

import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.SystemSoundID

// Built-in short UI sounds (no bundled assets). IDs are stable Apple system sounds.
private const val SOUND_HANG_START: SystemSoundID = 1057u // "Tink" — short high
private const val SOUND_HANG_END: SystemSoundID = 1114u // "End record" — lower / done

actual fun playHangCue(cue: HangCue) {
    val id = when (cue) {
        HangCue.HangStart -> SOUND_HANG_START
        HangCue.HangEnd -> SOUND_HANG_END
    }
    AudioServicesPlaySystemSound(id)
}
