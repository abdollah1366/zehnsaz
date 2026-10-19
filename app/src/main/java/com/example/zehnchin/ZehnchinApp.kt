package com.example.zehnchin

import android.app.Application
import com.example.zehnchin.reminder.DailyReminder
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ZehnchinApp : Application() {

    override fun onCreate() {
        super.onCreate()
        DailyReminder.createChannel(this)
    }
}