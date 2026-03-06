package com.smartagenda.worker
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.smartagenda.repository.Result
import com.smartagenda.repository.SmartAgendaRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class DailyNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: SmartAgendaRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "smartagenda_daily"
        const val WORK_NAME  = "SmartAgendaDailyNotif"
        const val NOTIF_ID   = 1001

        fun schedule(context: Context) {
            val delay = computeDelay()
            val request = PeriodicWorkRequestBuilder<DailyNotificationWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
        }

        private fun computeDelay(): Long {
            val now = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 7)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                if (before(now)) add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
            return target.timeInMillis - now.timeInMillis
        }
    }

    override suspend fun doWork(): Result {
        return try {
            when (val r = repository.fetchToday()) {
                is com.smartagenda.repository.Result.Success -> {
                    val d = r.data
                    val body = buildString {
                        d.location?.let { appendLine("📍 $it") }
                        d.weather?.let { appendLine("🌤 ${it.temperature.toInt()}°C — ${it.description}") }
                        d.uvIndex?.let { appendLine("☀️ UV ${it.value} — ${it.level}") }
                        append(if (d.events.isEmpty()) "Aucun événement" else "${d.events.size} événement(s)")
                    }
                    notify("📅 ${d.dayName} ${d.date}", body.trim())
                    Result.success()
                }
                else -> Result.retry()
            }
        } catch (e: Exception) { Result.retry() }
    }

    private fun notify(title: String, body: String) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Résumé quotidien", NotificationManager.IMPORTANCE_DEFAULT))
        nm.notify(NOTIF_ID, NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title).setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true).build())
    }
}
