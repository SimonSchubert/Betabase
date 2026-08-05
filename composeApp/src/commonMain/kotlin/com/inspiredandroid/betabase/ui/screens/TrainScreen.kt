package com.inspiredandroid.betabase.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.inspiredandroid.betabase.ui.components.BetaButton
import com.inspiredandroid.betabase.ui.components.BetaCard
import com.inspiredandroid.betabase.ui.components.BetaOutlineButton
import com.inspiredandroid.betabase.ui.components.BetaText
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme
import com.inspiredandroid.betabase.ui.util.HangCue
import com.inspiredandroid.betabase.ui.util.playHangCue
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.time.Clock

private val ScreenSidePadding = 20.dp

private const val DefaultHangSeconds = 7
private const val DefaultRestSeconds = 3
private const val DefaultTotalMinutes = 10

private const val MinIntervalSeconds = 1
private const val MaxIntervalSeconds = 60
private const val MinTotalMinutes = 1
private const val MaxTotalMinutes = 60

enum class HangPhase {
    Ready,
    Hang,
    Rest,
    Done,
}

data class HangSessionConfig(
    val hangSeconds: Int = DefaultHangSeconds,
    val restSeconds: Int = DefaultRestSeconds,
    val totalMinutes: Int = DefaultTotalMinutes,
) {
    val totalSeconds: Int get() = totalMinutes * 60
    val cycleSeconds: Int get() = hangSeconds + restSeconds
}

data class HangSessionSnapshot(
    val phase: HangPhase,
    val phaseRemainingSeconds: Int,
    val elapsedSeconds: Int,
    val totalSeconds: Int,
    val completedHangs: Int,
) {
    val sessionRemainingSeconds: Int get() = max(0, totalSeconds - elapsedSeconds)
}

/**
 * @param elapsedSeconds whole seconds completed since session start (0 at the first tick).
 * @param active when false and elapsed is 0, returns [HangPhase.Ready].
 */
fun hangSnapshot(
    elapsedSeconds: Int,
    config: HangSessionConfig,
    active: Boolean = true,
): HangSessionSnapshot {
    val total = config.totalSeconds
    val clamped = elapsedSeconds.coerceIn(0, total)

    if (!active && clamped == 0) {
        return HangSessionSnapshot(
            phase = HangPhase.Ready,
            phaseRemainingSeconds = config.hangSeconds,
            elapsedSeconds = 0,
            totalSeconds = total,
            completedHangs = 0,
        )
    }
    if (clamped >= total) {
        val pos = total % config.cycleSeconds
        val hangs = (total / config.cycleSeconds) +
            if (pos >= config.hangSeconds) 1 else 0
        return HangSessionSnapshot(
            phase = HangPhase.Done,
            phaseRemainingSeconds = 0,
            elapsedSeconds = total,
            totalSeconds = total,
            completedHangs = hangs,
        )
    }

    val pos = clamped % config.cycleSeconds
    val phase = if (pos < config.hangSeconds) HangPhase.Hang else HangPhase.Rest
    val phaseRemaining = if (phase == HangPhase.Hang) {
        config.hangSeconds - pos
    } else {
        config.cycleSeconds - pos
    }
    val completedHangs = (clamped / config.cycleSeconds) +
        if (pos >= config.hangSeconds) 1 else 0

    return HangSessionSnapshot(
        phase = phase,
        phaseRemainingSeconds = phaseRemaining,
        elapsedSeconds = clamped,
        totalSeconds = total,
        completedHangs = completedHangs,
    )
}

fun formatMmSs(totalSeconds: Int): String {
    val s = max(0, totalSeconds)
    val m = s / 60
    val r = s % 60
    return "$m:${r.toString().padStart(2, '0')}"
}

@Composable
fun TrainScreen(modifier: Modifier = Modifier) {
    var hangSeconds by rememberSaveable { mutableIntStateOf(DefaultHangSeconds) }
    var restSeconds by rememberSaveable { mutableIntStateOf(DefaultRestSeconds) }
    var totalMinutes by rememberSaveable { mutableIntStateOf(DefaultTotalMinutes) }

    var running by rememberSaveable { mutableStateOf(false) }
    var elapsedSeconds by rememberSaveable { mutableIntStateOf(0) }
    // Wall-clock anchor so ticks stay accurate across recompositions.
    var runningSinceEpochMs by rememberSaveable { mutableLongStateOf(0L) }
    var baseElapsedWhenStarted by rememberSaveable { mutableIntStateOf(0) }

    val config = remember(hangSeconds, restSeconds, totalMinutes) {
        HangSessionConfig(hangSeconds, restSeconds, totalMinutes)
    }
    val active = running || elapsedSeconds > 0
    val snapshot = remember(elapsedSeconds, config, active) {
        hangSnapshot(elapsedSeconds, config, active = active)
    }

    // Announce hang start / end with short audio cues (not on resume of same phase).
    var announcedPhase by rememberSaveable { mutableStateOf(HangPhase.Ready) }
    LaunchedEffect(snapshot.phase) {
        val curr = snapshot.phase
        val prev = announcedPhase
        if (curr == prev) return@LaunchedEffect
        when {
            curr == HangPhase.Hang &&
                (prev == HangPhase.Ready || prev == HangPhase.Rest || prev == HangPhase.Done) -> {
                playHangCue(HangCue.HangStart)
            }
            prev == HangPhase.Hang && (curr == HangPhase.Rest || curr == HangPhase.Done) -> {
                playHangCue(HangCue.HangEnd)
            }
        }
        announcedPhase = curr
    }

    LaunchedEffect(running, runningSinceEpochMs, baseElapsedWhenStarted, config.totalSeconds) {
        if (!running) return@LaunchedEffect
        while (true) {
            val now = Clock.System.now().toEpochMilliseconds()
            val next = (
                baseElapsedWhenStarted + ((now - runningSinceEpochMs) / 1000L).toInt()
                ).coerceAtMost(config.totalSeconds)
            elapsedSeconds = next
            if (next >= config.totalSeconds) {
                running = false
                break
            }
            delay(50)
        }
    }

    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val scroll = rememberScrollState()
    val canEditSettings = !running && elapsedSeconds == 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BetabaseTheme.colors.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(scroll)
            .padding(
                start = ScreenSidePadding,
                end = ScreenSidePadding,
                top = 12.dp,
                bottom = 32.dp + bottomInset,
            ),
    ) {
        BetaText(
            text = "TRAIN",
            style = BetabaseTheme.typography.displayMedium,
            color = BetabaseTheme.colors.ink,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(6.dp))
        BetaText(
            text = "Home sessions you can do on a pull-up bar or hangboard. Start with repeaters.",
            style = BetabaseTheme.typography.label,
            color = BetabaseTheme.colors.inkMuted,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(BetabaseTheme.shapes.pill)
                .background(BetabaseTheme.colors.ink),
        )

        Spacer(Modifier.height(20.dp))

        BetaText(
            text = "PULL-UP BAR",
            style = BetabaseTheme.typography.label,
            color = BetabaseTheme.colors.inkMuted,
        )
        Spacer(Modifier.height(8.dp))

        HangSessionCard(
            config = config,
            snapshot = snapshot,
            running = running,
            canEditSettings = canEditSettings,
            onHangChange = { hangSeconds = it.coerceIn(MinIntervalSeconds, MaxIntervalSeconds) },
            onRestChange = { restSeconds = it.coerceIn(MinIntervalSeconds, MaxIntervalSeconds) },
            onTotalMinutesChange = { totalMinutes = it.coerceIn(MinTotalMinutes, MaxTotalMinutes) },
            onStart = {
                val now = Clock.System.now().toEpochMilliseconds()
                if (elapsedSeconds >= config.totalSeconds) {
                    elapsedSeconds = 0
                }
                baseElapsedWhenStarted = elapsedSeconds
                runningSinceEpochMs = now
                running = true
            },
            onPause = {
                // Freeze elapsed at the current second.
                val now = Clock.System.now().toEpochMilliseconds()
                elapsedSeconds = (
                    baseElapsedWhenStarted + ((now - runningSinceEpochMs) / 1000L).toInt()
                    ).coerceAtMost(config.totalSeconds)
                running = false
            },
            onReset = {
                running = false
                elapsedSeconds = 0
                baseElapsedWhenStarted = 0
                runningSinceEpochMs = 0L
            },
        )
    }
}

@Composable
private fun HangSessionCard(
    config: HangSessionConfig,
    snapshot: HangSessionSnapshot,
    running: Boolean,
    canEditSettings: Boolean,
    onHangChange: (Int) -> Unit,
    onRestChange: (Int) -> Unit,
    onTotalMinutesChange: (Int) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
) {
    val phaseColor = when (snapshot.phase) {
        HangPhase.Hang -> BetabaseTheme.colors.accent
        HangPhase.Rest -> BetabaseTheme.colors.lead
        HangPhase.Done -> BetabaseTheme.colors.youth
        HangPhase.Ready -> BetabaseTheme.colors.ink
    }
    val phaseLabel = when (snapshot.phase) {
        HangPhase.Ready -> "READY"
        HangPhase.Hang -> "HANG"
        HangPhase.Rest -> "REST"
        HangPhase.Done -> "DONE"
    }
    val countdown = when (snapshot.phase) {
        HangPhase.Ready -> config.hangSeconds
        HangPhase.Done -> 0
        else -> snapshot.phaseRemainingSeconds
    }

    BetaCard(
        modifier = Modifier.fillMaxWidth(),
        background = BetabaseTheme.colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BetaText(
                text = "7/3 HANG REPEATERS",
                style = BetabaseTheme.typography.titleMedium,
                color = BetabaseTheme.colors.ink,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(4.dp))
            BetaText(
                text = "Hang for the work interval, rest for the recovery interval. Repeat until the session clock runs out.",
                style = BetabaseTheme.typography.bodySmall,
                color = BetabaseTheme.colors.inkMuted,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))

            // Phase + big countdown
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(BetabaseTheme.colors.surfaceMuted)
                    .border(4.dp, phaseColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BetaText(
                        text = phaseLabel,
                        style = BetabaseTheme.typography.label,
                        color = phaseColor,
                    )
                    Spacer(Modifier.height(4.dp))
                    BetaText(
                        text = countdown.toString(),
                        style = BetabaseTheme.typography.displayLarge,
                        color = BetabaseTheme.colors.ink,
                        textAlign = TextAlign.Center,
                    )
                    BetaText(
                        text = "SEC",
                        style = BetabaseTheme.typography.labelSmall,
                        color = BetabaseTheme.colors.inkMuted,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatCell(label = "SESSION", value = formatMmSs(snapshot.sessionRemainingSeconds))
                StatCell(label = "ELAPSED", value = formatMmSs(snapshot.elapsedSeconds))
                StatCell(label = "HANGS", value = snapshot.completedHangs.toString())
            }

            Spacer(Modifier.height(20.dp))

            DurationStepper(
                label = "HANG",
                valueLabel = "${config.hangSeconds}s",
                enabled = canEditSettings,
                onDecrement = { onHangChange(config.hangSeconds - 1) },
                onIncrement = { onHangChange(config.hangSeconds + 1) },
            )
            Spacer(Modifier.height(10.dp))
            DurationStepper(
                label = "REST",
                valueLabel = "${config.restSeconds}s",
                enabled = canEditSettings,
                onDecrement = { onRestChange(config.restSeconds - 1) },
                onIncrement = { onRestChange(config.restSeconds + 1) },
            )
            Spacer(Modifier.height(10.dp))
            DurationStepper(
                label = "TOTAL",
                valueLabel = "${config.totalMinutes} min",
                enabled = canEditSettings,
                onDecrement = { onTotalMinutesChange(config.totalMinutes - 1) },
                onIncrement = { onTotalMinutesChange(config.totalMinutes + 1) },
            )

            if (!canEditSettings) {
                Spacer(Modifier.height(8.dp))
                BetaText(
                    text = "Reset to change intervals",
                    style = BetabaseTheme.typography.bodySmall,
                    color = BetabaseTheme.colors.inkMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (running) {
                    BetaButton(
                        label = "Pause",
                        onClick = onPause,
                        modifier = Modifier.weight(1f),
                        background = BetabaseTheme.colors.ink,
                    )
                } else {
                    val startLabel = when {
                        snapshot.phase == HangPhase.Done -> "Restart"
                        snapshot.elapsedSeconds > 0 -> "Resume"
                        else -> "Start"
                    }
                    BetaButton(
                        label = startLabel,
                        onClick = onStart,
                        modifier = Modifier.weight(1f),
                        background = BetabaseTheme.colors.accent,
                        onBackground = BetabaseTheme.colors.onAccent,
                    )
                }
                BetaOutlineButton(
                    label = "Reset",
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        BetaText(
            text = label,
            style = BetabaseTheme.typography.labelSmall,
            color = BetabaseTheme.colors.inkMuted,
        )
        Spacer(Modifier.height(2.dp))
        BetaText(
            text = value,
            style = BetabaseTheme.typography.titleMedium,
            color = BetabaseTheme.colors.ink,
        )
    }
}

@Composable
private fun DurationStepper(
    label: String,
    valueLabel: String,
    enabled: Boolean,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    val ink = if (enabled) BetabaseTheme.colors.ink else BetabaseTheme.colors.inkMuted
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(BetabaseTheme.shapes.button)
            .background(BetabaseTheme.colors.surfaceMuted)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BetaText(
            text = label,
            style = BetabaseTheme.typography.label,
            color = ink,
            modifier = Modifier.width(64.dp),
        )
        Spacer(Modifier.weight(1f))
        StepperButton(label = "−", enabled = enabled, onClick = onDecrement)
        BetaText(
            text = valueLabel,
            style = BetabaseTheme.typography.titleSmall,
            color = ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(72.dp),
        )
        StepperButton(label = "+", enabled = enabled, onClick = onIncrement)
    }
}

@Composable
private fun StepperButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (enabled) BetabaseTheme.colors.surface else BetabaseTheme.colors.surfaceMuted
    val fg = if (enabled) BetabaseTheme.colors.ink else BetabaseTheme.colors.inkMuted
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(BetabaseTheme.shapes.button)
            .background(bg)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        BetaText(
            text = label,
            style = BetabaseTheme.typography.titleMedium,
            color = fg,
            textAlign = TextAlign.Center,
        )
    }
}
