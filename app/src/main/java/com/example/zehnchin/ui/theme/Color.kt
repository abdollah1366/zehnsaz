package com.example.zehnchin.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// Brand: a bright, cheerful palette. Violet for focus, berry pink for energy, sunny amber for rewards,
// on warm cream and soft pastel surfaces. Every text colour keeps at least 4.5:1 contrast on the
// surface it is drawn on (checked against WCAG 2.1), so the app stays readable for long sessions.

val Violet700 = Color(0xFF6A3DE8)   // primary, 5.9:1 with white text
val Violet100 = Color(0xFFEBE3FF)
val Violet900 = Color(0xFF22005D)
val Violet200 = Color(0xFFCDBDFF)
val Violet800 = Color(0xFF4F23C4)
val Violet50 = Color(0xFFF4EFFF)

val Berry700 = Color(0xFFC2185B)    // secondary, 5.9:1 with white text
val Berry100 = Color(0xFFFFD9E4)
val Berry900 = Color(0xFF3E001D)
val Berry200 = Color(0xFFFFB1C8)
val Berry800 = Color(0xFF8E0F43)
val Berry50 = Color(0xFFFFECF1)

val Sun700 = Color(0xFFB45309)      // tertiary, 5.0:1 with white text
val Sun100 = Color(0xFFFFEDB8)
val Sun900 = Color(0xFF3B2000)
val Sun200 = Color(0xFFFFC870)
val Sun800 = Color(0xFF7A3A00)
val Sun50 = Color(0xFFFFF6D9)

val Mint100 = Color(0xFFD3F8E8)

val Ink900 = Color(0xFF1E1B2E)
val Ink50 = Color(0xFFEDE9F8)
val Cream50 = Color(0xFFFFF8F1)
val Lavender100 = Color(0xFFF1ECFF)
val Lavender200 = Color(0xFFE6DEFA)
val Plum700 = Color(0xFF4E4566)
val Plum400 = Color(0xFF8B80A6)
val Plum200 = Color(0xFFD9D0EE)
val Night950 = Color(0xFF15122A)
val Night900 = Color(0xFF1F1B38)
val Night800 = Color(0xFF332D55)
val Plum300 = Color(0xFFB0A6CC)
val Error700 = Color(0xFFBA1A1A)
val Error200 = Color(0xFFFFB4AB)

// Soft pastel backdrop for every non-game screen: blush at the top, lavender in the middle, mint below.
val BackdropBlush = Color(0xFFFFEEF4)
val BackdropLavender = Color(0xFFF4EEFF)
val BackdropMint = Color(0xFFE9FAF3)
val BackdropSun = Color(0xFFFFE9A8)

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
    correct = Color(0xFF1B7A34),
    incorrect = Color(0xFFC62828),
    warning = Color(0xFF9A5B00)
)

val DarkFeedbackColors = FeedbackColors(
    correct = Color(0xFF81C784),
    incorrect = Color(0xFFEF9A9A),
    warning = Color(0xFFF5C26B)
)

/** The accent colour of a chapter (cognitive domain). Chapter 0 is the tutorial and uses the brand violet. */
fun domainColor(chapterId: Int): Color = when (chapterId) {
    1 -> DomainAttention
    2 -> DomainMemory
    3 -> DomainSpeed
    4 -> DomainControl
    5 -> DomainFlexibility
    6 -> DomainProblemSolving
    else -> Violet700
}

/**
 * A light pastel tint of a chapter colour, for card backgrounds. Dark text keeps well over 7:1 on it.
 */
fun domainTint(chapterId: Int): Color = lerp(domainColor(chapterId), Color.White, 0.86f)
