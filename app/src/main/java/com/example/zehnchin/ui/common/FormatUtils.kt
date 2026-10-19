package com.example.zehnchin.ui.common

import java.text.NumberFormat
import java.util.Locale

object FormatUtils {
    private val persianLocale = Locale("fa", "IR")
    private val numberFormat = NumberFormat.getInstance(persianLocale)
    private val percentFormat = NumberFormat.getPercentInstance(persianLocale)

    fun formatNumber(number: Number): String {
        return numberFormat.format(number)
    }

    fun formatPercent(percent: Float): String {
        // Assuming percent is 0.0 to 1.0 or 0 to 100
        return if (percent > 1f) {
            "${numberFormat.format(percent.toInt())}٪"
        } else {
            percentFormat.format(percent)
        }
    }
    
    fun toPersianDigits(text: String): String {
        var result = text
        val englishDigits = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
        val persianDigits = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
        for (i in 0..9) {
            result = result.replace(englishDigits[i], persianDigits[i])
        }
        return result
    }
}
