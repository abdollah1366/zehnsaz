package com.barati.zehnsaz.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.barati.zehnsaz.R

// Vazirmatn covers Persian and Latin script. Persian needs a little more line height than Latin.
val VazirmatnFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

private val Base = Typography()

private fun TextStyle.vazirmatn() = copy(fontFamily = VazirmatnFamily)

val Typography = Typography(
    displayLarge = Base.displayLarge.vazirmatn(),
    displayMedium = Base.displayMedium.vazirmatn(),
    displaySmall = Base.displaySmall.vazirmatn(),
    headlineLarge = Base.headlineLarge.vazirmatn(),
    headlineMedium = Base.headlineMedium.vazirmatn(),
    headlineSmall = Base.headlineSmall.vazirmatn(),
    titleLarge = Base.titleLarge.vazirmatn().copy(fontWeight = FontWeight.Bold, lineHeight = 30.sp),
    titleMedium = Base.titleMedium.vazirmatn().copy(fontWeight = FontWeight.Bold, lineHeight = 26.sp),
    titleSmall = Base.titleSmall.vazirmatn().copy(fontWeight = FontWeight.Medium),
    bodyLarge = Base.bodyLarge.vazirmatn().copy(fontSize = 16.sp, lineHeight = 28.sp, letterSpacing = 0.sp),
    bodyMedium = Base.bodyMedium.vazirmatn().copy(fontSize = 14.sp, lineHeight = 24.sp, letterSpacing = 0.sp),
    bodySmall = Base.bodySmall.vazirmatn().copy(fontSize = 12.sp, lineHeight = 20.sp, letterSpacing = 0.sp),
    labelLarge = Base.labelLarge.vazirmatn().copy(fontWeight = FontWeight.Medium),
    labelMedium = Base.labelMedium.vazirmatn().copy(fontWeight = FontWeight.Medium),
    labelSmall = Base.labelSmall.vazirmatn().copy(fontWeight = FontWeight.Medium)
)
