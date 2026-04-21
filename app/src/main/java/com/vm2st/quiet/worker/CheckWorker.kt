package com.vm2st.quiet.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.vm2st.quiet.data.AppDatabase
import com.vm2st.quiet.utils.LocaleHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class CheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val CHANNEL_ID = "ritual_check_channel"
        private const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "ritual_check_work"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val randomDelay = (1..4).random().toLong()

            val workRequest = OneTimeWorkRequestBuilder<CheckWorker>()
                .setInitialDelay(randomDelay, TimeUnit.HOURS)
                .setConstraints(constraints)
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }

        private const val FIVE_MIN_CHECK_WORK_NAME = "ritual_five_min_check"
        private const val CHECK_DELAY_MINUTES = 5L

        fun scheduleFiveMinuteCheck(context: Context, ritualId: Int) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<CheckWorker>()
                .setInitialDelay(CHECK_DELAY_MINUTES, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .addTag(FIVE_MIN_CHECK_WORK_NAME)
                .setInputData(workDataOf("ritual_id" to ritualId, "is_five_min_check" to true))
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                FIVE_MIN_CHECK_WORK_NAME + "_$ritualId",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }

    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val isFiveMinCheck = inputData.getBoolean("is_five_min_check", false)
            val ritualId = inputData.getInt("ritual_id", -1)

            val db = AppDatabase.getDatabase(applicationContext)

            if (isFiveMinCheck && ritualId != -1) {
                // Проверяем конкретный ритуал
                val ritual = db.ritualDao().getRitualById(ritualId)
                if (ritual != null && ritual.isConfirmedToday) {
                    // Ритуал всё ещё подтверждён – показываем уведомление с вопросом
                    showFiveMinuteNotification(ritual.name)
                }
            } else {
                // Обычная проверка неподтверждённых ритуалов (через 1-4 часа)
                val rituals = db.ritualDao().getAllRituals().first()
                val unconfirmedRituals = rituals.filter { !it.isConfirmedToday }
                if (unconfirmedRituals.isNotEmpty()) {
                    val ritualToCheck = unconfirmedRituals.random()
                    showNotification(ritualToCheck.name)
                }
            }

            // Планируем следующую обычную проверку
            schedule(applicationContext)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun showFiveMinuteNotification(ritualName: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val isRussian = LocaleHelper.isRussian(applicationContext)
        val title = if (isRussian) "Точно выключил?" else "Are you sure you turned it off?"
        val content = if (isRussian) {
            "Проверьте: $ritualName. Может, перепроверить?"
        } else {
            "Check: $ritualName. Maybe double-check?"
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 200, 500))  // вибрация
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun showNotification(ritualName: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel(notificationManager)

        val isRussian = LocaleHelper.isRussian(applicationContext)
        val title = if (isRussian) "Проверьте свой ритуал" else "Check your ritual"
        val content = if (isRussian) {
            "Вы не забыли «$ritualName»?"
        } else {
            "Did you remember to \"$ritualName\"?"
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val isRussian = LocaleHelper.isRussian(applicationContext)
            val channelName = if (isRussian) "Напоминания о ритуалах" else "Ritual Reminders"
            val channel = NotificationChannel(
                CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}