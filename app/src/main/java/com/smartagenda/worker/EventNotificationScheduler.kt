package com.smartagenda.worker

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.smartagenda.MainActivity
import com.smartagenda.data.model.AgendaEvent

class EventNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("event_title") ?: return
        val type  = intent.getStringExtra("event_type") ?: "general"

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID, "Événements agenda",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Rappels d'événements SmartAgenda" }
        )

        val openIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        val emoji = when (type) {
            "travail"  -> "💼"
            "santé"    -> "🏥"
            "loisirs"  -> "🎉"
            "réunion"  -> "👥"
            else       -> "📅"
        }

        nm.notify(
            title.hashCode(),
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("$emoji $title")
                .setContentText("C'est maintenant !")
                .setContentIntent(openIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        )
    }

    companion object {
        const val CHANNEL_ID = "smartagenda_events"

        fun scheduleAll(context: Context, events: List<AgendaEvent>) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val today = java.util.Calendar.getInstance()

            events.forEach { event ->
                val parts = event.startTime.split(":")
                val hour  = parts.getOrNull(0)?.toIntOrNull() ?: return@forEach
                val min   = parts.getOrNull(1)?.toIntOrNull() ?: return@forEach

                val triggerTime = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, hour)
                    set(java.util.Calendar.MINUTE, min)
                    set(java.util.Calendar.SECOND, 0)
                }

                // Ne pas programmer si l'heure est déjà passée
                if (triggerTime.before(today)) return@forEach

                val intent = PendingIntent.getBroadcast(
                    context,
                    event.id.hashCode(),
                    Intent(context, EventNotificationReceiver::class.java).apply {
                        putExtra("event_title", event.title)
                        putExtra("event_type", event.type)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime.timeInMillis,
                        intent
                    )
                } else {
                    am.set(AlarmManager.RTC_WAKEUP, triggerTime.timeInMillis, intent)
                }
            }
        }
    }
}
