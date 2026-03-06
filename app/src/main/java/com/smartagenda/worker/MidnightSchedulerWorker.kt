package com.smartagenda.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.smartagenda.repository.Result
import com.smartagenda.repository.SmartAgendaRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class MidnightSchedulerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: SmartAgendaRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "SmartAgendaMidnightScheduler"

        fun schedule(context: Context) {
            val delay = computeDelayUntilMidnight()
            val request = PeriodicWorkRequestBuilder<MidnightSchedulerWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        private fun computeDelayUntilMidnight(): Long {
            val now = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 22)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                // Si 22h est déjà passée aujourd'hui, décaler à demain
                if (before(now)) add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
            return target.timeInMillis - now.timeInMillis
        }
    }

    override suspend fun doWork(): Result {
        return try {
            when (val r = repository.fetchToday()) {
                is com.smartagenda.repository.Result.Success -> {
                    // Programmer les alarmes pour tous les événements du jour
                    EventNotificationReceiver.scheduleAll(applicationContext, r.data.events)
                    Result.success()
                }
                else -> Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
