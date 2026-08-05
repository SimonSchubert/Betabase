package com.inspiredandroid.betabase.data

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual val remindersSupported: Boolean = true

actual fun createEventReminderScheduler(): EventReminderScheduler = AndroidEventReminderScheduler()

internal const val REMINDER_CHANNEL_ID = "competition_reminders"
internal const val EXTRA_EVENT_ID = "event_id"
internal const val EXTRA_TITLE = "title"
internal const val EXTRA_BODY = "body"

/**
 * Optional suspend callback registered by the host Activity to request
 * POST_NOTIFICATIONS at runtime (API 33+).
 */
object ReminderPermissionBridge {
    var requestPermission: (suspend () -> Boolean)? = null
}

class AndroidEventReminderScheduler : EventReminderScheduler {

    override suspend fun ensurePermission(): Boolean {
        ensureChannel()
        if (Build.VERSION.SDK_INT < 33) {
            return NotificationManagerCompat.from(appContext).areNotificationsEnabled()
        }
        val granted = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) return NotificationManagerCompat.from(appContext).areNotificationsEnabled()

        val requester = ReminderPermissionBridge.requestPermission
        if (requester == null) {
            return NotificationManagerCompat.from(appContext).areNotificationsEnabled()
        }
        val allowed = requester()
        return allowed && NotificationManagerCompat.from(appContext).areNotificationsEnabled()
    }

    override fun schedule(record: ReminderRecord) {
        ensureChannel()
        val triggerAt = record.triggerEpochMillis
        if (triggerAt <= System.currentTimeMillis()) return

        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = alarmPendingIntent(record, create = true) ?: return

        // Prefer exact alarms when the OS allows them; otherwise fall back to
        // inexact so a reminder still fires (possibly batched).
        if (Build.VERSION.SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    override fun cancel(eventId: String) {
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = alarmPendingIntent(
            ReminderRecord(eventId = eventId, title = "", body = "", triggerEpochMillis = 0),
            create = false,
        ) ?: return
        alarmManager.cancel(pending)
        pending.cancel()
    }

    override fun rescheduleAll(records: Collection<ReminderRecord>) {
        ensureChannel()
        val now = System.currentTimeMillis()
        records.filter { it.triggerEpochMillis > now }.forEach { schedule(it) }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < 26) return
        val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(REMINDER_CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            "Competition reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Alerts when a competition starts"
        }
        manager.createNotificationChannel(channel)
    }

    private fun alarmPendingIntent(record: ReminderRecord, create: Boolean): PendingIntent? {
        val intent = Intent(appContext, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_FIRE
            putExtra(EXTRA_EVENT_ID, record.eventId)
            putExtra(EXTRA_TITLE, record.title)
            putExtra(EXTRA_BODY, record.body)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val requestCode = record.eventId.hashCode()
        return if (create) {
            PendingIntent.getBroadcast(appContext, requestCode, intent, flags)
        } else {
            PendingIntent.getBroadcast(
                appContext,
                requestCode,
                intent,
                flags or PendingIntent.FLAG_NO_CREATE,
            )
        }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_FIRE) return
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "Competition starting" }
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty().ifBlank { "Starting now" }
        val eventId = intent.getStringExtra(EXTRA_EVENT_ID).orEmpty()

        ensureChannel(context)
        val smallIcon = context.applicationInfo.icon.takeIf { it != 0 }
            ?: android.R.drawable.ic_dialog_info
        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val id = if (eventId.isNotEmpty()) eventId.hashCode() else title.hashCode()
        NotificationManagerCompat.from(context).notify(id, notification)

        if (eventId.isNotEmpty()) {
            runCatching {
                createReminderStore().remove(eventId)
            }
        }
    }

    companion object {
        const val ACTION_FIRE = "com.inspiredandroid.betabase.REMINDER_FIRE"

        private fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < 26) return
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(REMINDER_CHANNEL_ID) != null) return
            manager.createNotificationChannel(
                NotificationChannel(
                    REMINDER_CHANNEL_ID,
                    "Competition reminders",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
        }
    }
}

class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }
        initBetabase(context)
        val store = createReminderStore()
        val now = System.currentTimeMillis()
        val records = store.prunePast(now)
        AndroidEventReminderScheduler().rescheduleAll(records)
    }
}

/**
 * Helper for host Activity to expose a suspend permission request.
 * Call [complete] once with the grant result.
 */
class ReminderPermissionRequestSession {
    private var cont: (kotlin.coroutines.Continuation<Boolean>)? = null

    suspend fun await(startRequest: () -> Unit): Boolean = suspendCoroutine { continuation ->
        cont = continuation
        startRequest()
    }

    fun complete(granted: Boolean) {
        cont?.resume(granted)
        cont = null
    }
}
