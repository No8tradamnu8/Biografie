package com.example.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action

        val prefs = ReminderPreferences(context)
        val settings = prefs.getSettings()

        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.MY_PACKAGE_REPLACED") {
            // Re-schedule alarm if enabled after reboot or update
            if (settings.isEnabled) {
                DiaryReminderScheduler.scheduleReminder(context, settings.hour, settings.minute)
            }
            return
        }

        // Triggered by AlarmManager
        if (settings.isEnabled) {
            showReminderNotification(context)
            // Schedule the alarm for tomorrow at the same user-defined time
            DiaryReminderScheduler.scheduleReminder(context, settings.hour, settings.minute)
        }
    }

    private fun showReminderNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Notification Channel for Android O (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Tagebuch-Erinnerung",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Sanfte tägliche Erinnerung, um im Tagebuch zu schreiben"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap intent to open MainActivity
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_REQUEST_CODE,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Select a poetic, gentle reminder phrase
        val gentlePhrases = listOf(
            "Ein Tag voller Momente neigt sich dem Ende zu. Halte deine Gedanken fest.",
            "Die Pergamentseiten warten auf deine Erlebnisse des heutigen Tages.",
            "Nimm dir einen kurzen, stillen Augenblick für deine Lebensgeschichte.",
            "Welcher Augenblick hat heute dein Herz berührt? Schreib ihn auf.",
            "Lass die Tinte fließen – deine Erinnerungen sind kostbar."
        )
        val reminderText = gentlePhrases.random()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_feather_pen)
            .setContentTitle("Zeit für dein Tagebuch")
            .setContentText(reminderText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(reminderText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "diary_gentle_reminder_channel"
        const val NOTIFICATION_ID = 1001
        const val NOTIFICATION_REQUEST_CODE = 2001
    }
}
