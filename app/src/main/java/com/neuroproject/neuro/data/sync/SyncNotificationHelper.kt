package com.neuroproject.neuro.data.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.neuroproject.neuro.R
import com.neuroproject.neuro.data.BatchUploadProgress
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Помощник для отображения уведомлений о синхронизации.
 *
 * Создаёт канал уведомлений и показывает результаты фоновой синхронизации:
 * - Успешное завершение
 * - Частичное завершение (некоторые пакеты не отправлены)
 * - Нет данных для отправки
 * - Ошибка синхронизации
 * - Остановка синхронизации
 *
 * Уведомления отображаются только если:
 * - Пользователь не отключил их в настройках
 * - Приложение имеет разрешение POST_NOTIFICATIONS (Android 13+)
 *
 * @see BatchUploadProgress
 */
@Singleton
class SyncNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /** NotificationManager для отправки уведомлений */
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    /** Создать канал уведомлений для синхронизации */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Синхронизация данных",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Уведомления о результатах фоновой синхронизации данных с сервером"
        }
        notificationManager.createNotificationChannel(channel)
        Log.d(TAG, "Notification channel created")
    }

    /**
     * Показать уведомление о результате синхронизации.
     *
     * @param progress Прогресс синхронизации
     */
    fun showSyncResult(progress: BatchUploadProgress) {
        Log.d(TAG, "showSyncResult called with: ${progress::class.simpleName}")

        if (!isNotificationsEnabled()) {
            Log.d(TAG, "Notifications disabled by user")
            return
        }

        if (!hasNotificationPermission()) {
            Log.d(TAG, "No notification permission granted")
            return
        }

        when (progress) {
            is BatchUploadProgress.Completed -> showSuccessNotification(progress)
            is BatchUploadProgress.PartialSuccess -> showPartialSuccessNotification(progress)
            is BatchUploadProgress.NoData -> showNoDataNotification()
            is BatchUploadProgress.Error -> showErrorNotification(progress.message)
            is BatchUploadProgress.Stopped -> showStoppedNotification(progress.reason)
            else -> {
                Log.d(TAG, "Unhandled progress type: ${progress::class.simpleName}")
            }
        }
    }

    /** Показать уведомление об успешном завершении */
    private fun showSuccessNotification(progress: BatchUploadProgress.Completed) {
        Log.d(TAG, "Showing success: ${progress.sentCount} records")
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Синхронизация завершена")
            .setContentText("Отправлено ${progress.sentCount} записей (${progress.totalBatches} пакетов)")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Отправлено ${progress.sentCount} записей\nПакетов: ${progress.totalBatches}\nВсе данные успешно переданы на сервер")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_SUCCESS, notification)
    }

    /** Показать уведомление о частичном успехе */
    private fun showPartialSuccessNotification(progress: BatchUploadProgress.PartialSuccess) {
        Log.d(TAG, "Showing partial success")
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Синхронизация завершена с ошибками")
            .setContentText("Успешно: ${progress.sentCount} из ${progress.totalCount} записей")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Успешно: ${progress.sentCount} из ${progress.totalCount} записей\n" +
                                "Не отправлено: ${progress.failedRecords} записей (${progress.failedBatches} пакетов)\n" +
                                "Неотправленные данные будут отправлены при следующей попытке"
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_PARTIAL, notification)
    }

    /** Показать уведомление об отсутствии данных */
    private fun showNoDataNotification() {
        Log.d(TAG, "Showing no data")
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Синхронизация")
            .setContentText("Нет данных для отправки")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_NO_DATA, notification)
    }

    /** Показать уведомление об ошибке */
    private fun showErrorNotification(error: String) {
        Log.d(TAG, "Showing error: $error")
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Ошибка синхронизации")
            .setContentText(error)
            .setStyle(NotificationCompat.BigTextStyle().bigText(error))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_ERROR, notification)
    }

    /** Показать уведомление об остановке синхронизации */
    private fun showStoppedNotification(reason: String) {
        Log.d(TAG, "Showing stopped: $reason")
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Синхронизация остановлена")
            .setContentText(reason)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID_STOPPED, notification)
    }

    /** Проверить, включены ли уведомления в настройках */
    private fun isNotificationsEnabled(): Boolean {
        val prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    /** Проверить наличие разрешения на уведомления (Android 13+) */
    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    companion object {
        private const val TAG = "SyncNotificationHelper"
        const val CHANNEL_ID = "sync_channel"
        private const val KEY_NOTIFICATIONS_ENABLED = "sync_notifications_enabled"

        private const val NOTIFICATION_ID_SUCCESS = 1001
        private const val NOTIFICATION_ID_PARTIAL = 1002
        private const val NOTIFICATION_ID_NO_DATA = 1003
        private const val NOTIFICATION_ID_ERROR = 1004
        private const val NOTIFICATION_ID_STOPPED = 1005
    }
}
