package com.example.zehnchin.ui.theme

import androidx.compose.ui.graphics.Color

// Brand: calm blue for focus, teal for growth, amber for rewards.
// Neutral surfaces keep the text contrast high for long sessions.

val Blue700 = Color(0xFF1F5C8B)
val Blue100 = Color(0xFFD6E7F5)
val Blue900 = Color(0xFF0B2D45)
val Blue200 = Color(0xFF9CC8EE)
val Blue800 = Color(0xFF00466F)
val Blue50 = Color(0xFFCDE5FF)

val Teal700 = Color(0xFF2E7D6F)
val Teal100 = Color(0xFFCDEFE8)
val Teal900 = Color(0xFF0B3D36)
val Teal200 = Color(0xFF8ED5C6)
val Teal800 = Color(0xFF00504A)
val Teal50 = Color(0xFFAEF2E2)

val Amber700 = Color(0xFFB26A00)
val Amber100 = Color(0xFFFFE7BF)
val Amber900 = Color(0xFF3D2500)
val Amber200 = Color(0xFFF5C26B)
val Amber800 = Color(0xFF664000)
val Amber50 = Color(0xFFFFDDA6)

val Ink900 = Color(0xFF14202B)
val Ink50 = Color(0xFFE3EAF1)
val Mist50 = Color(0xFFF6F8FA)
val Night950 = Color(0xFF0D1620)
val Slate100 = Color(0xFFE6EBF0)
val Slate700 = Color(0xFF475467)
val Slate400 = Color(0xFF7A8793)
val Night800 = Color(0xFF2A3A48)
val Night900 = Color(0xFF15212C)
val Slate200 = Color(0xFFB6C3CF)
val Slate300 = Color(0xFF8A99A8)
val Error700 = Color(0xFFB42318)
val Error200 = Color(0xFFFFB4AB)

// Domain accents: Okabe–Ito palette, distinguishable under common colour-vision deficiencies.
// Each chapter (cognitive domain) keeps one colour everywhere it appears.
val DomainAttention = Color(0xFF0072B2)
val DomainMemory = Color(0xFFCC79A7)
val DomainSpeed = Color(0xFF56B4E9)
val DomainControl = Color(0xFFD55E00)
val DomainFlexibility = Color(0xFF009E73)
val DomainProblemSolving = Color(0xFFE69F00)

/**
 * Feedback colours are used together with text labels, never on their own.
 * Light and dark variants keep at least 4.5:1 contrast on their backgrounds.
 */
data class FeedbackColors(
    val correct: Color,
    val incorrect: Color,
    val warning: Color
)

val LightFeedbackColors = FeedbackColors(
    correct = Color(0xFF2E7D32),
    incorrect = Color(0xFFC62828),
    warning = Color(0xFF9A5B00)
)

val DarkFeedbackColors = FeedbackColors(
    correct = Color(0xFF81C784),
    incorrect = Color(0xFFEF9A9A),
    warning = Color(0xFFF5C26B)
)

/** The accent colour of a chapter (cognitive domain). Chapter 0 is the tutorial and uses the brand blue. */
fun domainColor(chapterId: Int): Color = when (chapterId) {
    1 -> DomainAttention
    2 -> DomainMemory
    3 -> DomainSpeed
    4 -> DomainControl
    5 -> DomainFlexibility
    6 -> DomainProblemSolving
    else -> Blue700
}
