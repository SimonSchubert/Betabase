package com.inspiredandroid.betabase.data

/**
 * Common indoor/outdoor climbing hold types for the grades education section.
 * Shapes describe the hold; orientation (sidepull, undercling) is noted where relevant.
 */
data class HoldType(
    val id: String,
    val name: String,
    val description: String,
)

val ClimbingHoldTypes: List<HoldType> = listOf(
    HoldType(
        id = "jug",
        name = "Jug",
        description = "A large, deep hold you can wrap your whole hand around. The friendliest shape — positive and secure. Common on warm-ups and easier routes.",
    ),
    HoldType(
        id = "crimp",
        name = "Crimp",
        description = "A small edge that only fits fingertips. Full crimp closes the thumb over the index finger for max power but stresses tendons; half-crimp (open thumb) is safer for training.",
    ),
    HoldType(
        id = "sloper",
        name = "Sloper",
        description = "A rounded, sloping surface with little or no lip. You grip open-handed and rely on friction, body position, and keeping the weight over your feet.",
    ),
    HoldType(
        id = "pinch",
        name = "Pinch",
        description = "A hold you squeeze between fingers and thumb. Width varies from narrow to wide; strength comes from thumb opposition and an engaged core.",
    ),
    HoldType(
        id = "pocket",
        name = "Pocket",
        description = "A hole that fits one to three fingers (mono, two-finger, three-finger). Load carefully — mono and two-finger pockets are hard on tendons.",
    ),
    HoldType(
        id = "edge",
        name = "Edge",
        description = "A thin, flat ledge — longer than a crimp but still shallow. Often held open-handed or half-crimped; common on both gym and outdoor rock.",
    ),
    HoldType(
        id = "undercling",
        name = "Undercling",
        description = "A hold gripped from below, palms facing up. Works best when your feet are high so you can push into it and stay close to the wall.",
    ),
    HoldType(
        id = "sidepull",
        name = "Sidepull",
        description = "A hold oriented vertically so you pull sideways rather than down. Pair it with opposing feet or a flag to keep your body from swinging out.",
    ),
    HoldType(
        id = "volume",
        name = "Volume",
        description = "A large geometric feature bolted to the wall (often plywood or fiberglass). Holds can be set on it; the volume itself may be used as a sloper or edge.",
    ),
)
