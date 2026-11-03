package com.barati.zehnsaz.domain.model

data class AppSettings(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val dailyReminderEnabled: Boolean = false
)
