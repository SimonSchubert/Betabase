package com.inspiredandroid.betabase.ui.util

/**
 * Web Audio is possible but needs a user-gesture-linked AudioContext and
 * extra JS interop. Silent no-op until we add a dedicated web audio path.
 */
actual fun playHangCue(cue: HangCue) = Unit
