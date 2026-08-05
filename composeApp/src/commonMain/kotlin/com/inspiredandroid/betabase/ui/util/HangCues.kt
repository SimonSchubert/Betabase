package com.inspiredandroid.betabase.ui.util

/** Short audio cues for hang-session phase changes. */
enum class HangCue {
    /** Begin hanging — higher / sharper beep. */
    HangStart,

    /** Stop hanging (rest or session end) — lower / softer beep. */
    HangEnd,
}

/** Play a hang-session cue on the current platform. Safe to call from the UI thread. */
expect fun playHangCue(cue: HangCue)
