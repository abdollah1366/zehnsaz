package com.example.zehnchin.reminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.zehnchin.R
import java.util.Calendar
import java.util.concurrent.TimeUnit

/** An optional, once-a-day reminder to train. Off unless the player turns it on in settings. */
object DailyReminder {

    const val CHANNEL_ID = "daily_reminder"
    const val NOTIFICATION_ID = 1001

    private const val WORK_NAME = "daily_reminder"
    private const val REMIND_HOUR = 19

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(millisUntilReminder(System.currentTimeMillis()), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /** Time from [now] until the next 19:00 on the device clock. */
    internal fun millisUntilReminder(now: Long): Long {
        val target = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, REMIND_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now
    }
}

class ReminderWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {

    @SuppressLint("MissingPermission")
    override fun doWork(): Result {
        val canNotify = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!canNotify) return Result.success()

        val notification = NotificationCompat.Builder(applicationContext, DailyReminder.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(applicationContext.getString(R.string.reminder_title))
            .setContentText(applicationContext.getString(R.string.reminder_text))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(DailyReminder.NOTIFICATION_ID, notification)
        return Result.success()
    }
}
