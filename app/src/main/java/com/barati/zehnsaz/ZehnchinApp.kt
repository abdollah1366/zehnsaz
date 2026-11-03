package com.barati.zehnsaz

import android.app.Application
import com.barati.zehnsaz.reminder.DailyReminder
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ZehnchinApp : Application() {

    override fun onCreate() {
        super.onCreate()
        DailyReminder.createChannel(this)
    }
}