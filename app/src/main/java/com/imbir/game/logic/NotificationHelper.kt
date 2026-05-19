package com.imbir.game.logic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import androidx.work.Data
import com.imbir.game.R
import com.imbir.game.ui.MainActivity
import java.util.concurrent.TimeUnit

object NotificationHelper {
    private const val CHANNEL_ID = "imbir_cat"
    private const val WORK_TAG   = "cat_reminder"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                "Imbir Cat",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Reminders to check on your cat" }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(ch)
        }
    }

    fun scheduleReminder(context: Context, hunger: Float, happiness: Float) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)

        val message = when {
            hunger < 20f     -> "Imbir is starving! 🍗 Come feed your cat!"
            happiness < 20f  -> "Imbir is very sad 😿 Your cat misses you!"
            hunger < 50f     -> "Imbir is getting hungry... 🐱"
            happiness < 50f  -> "Imbir could use some love 💛"
            else             -> "Imbir is waiting for you! 😺"
        }

        val delayHours = when {
            hunger < 20f || happiness < 20f -> 1L
            hunger < 50f || happiness < 50f -> 3L
            else -> 6L
        }

        val data = Data.Builder().putString("message", message).build()
        val request = OneTimeWorkRequestBuilder<CatReminderWorker>()
            .setInitialDelay(delayHours, TimeUnit.HOURS)
            .setInputData(data)
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    fun cancelReminder(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
    }

    fun showNotification(context: Context, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.cat_idle)
            .setContentTitle("Imbir needs you!")
            .setContentText(message)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(1001, notif)
    }
}

class CatReminderWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        val message = inputData.getString("message") ?: "Imbir misses you! 🐱"
        NotificationHelper.showNotification(applicationContext, message)
        return Result.success()
    }
}