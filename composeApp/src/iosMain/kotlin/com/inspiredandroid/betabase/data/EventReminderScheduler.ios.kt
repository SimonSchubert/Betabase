package com.inspiredandroid.betabase.data

import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Clock

actual val remindersSupported: Boolean = true

actual fun createEventReminderScheduler(): EventReminderScheduler = IosEventReminderScheduler()

private class IosEventReminderScheduler : EventReminderScheduler {

    private val center: UNUserNotificationCenter
        get() = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun ensurePermission(): Boolean = suspendCoroutine { cont ->
        center.getNotificationSettingsWithCompletionHandler { settings ->
            when (settings?.authorizationStatus) {
                UNAuthorizationStatusAuthorized,
                UNAuthorizationStatusProvisional,
                UNAuthorizationStatusEphemeral,
                -> cont.resume(true)

                UNAuthorizationStatusDenied -> cont.resume(false)

                else -> {
                    center.requestAuthorizationWithOptions(
                        UNAuthorizationOptionAlert or UNAuthorizationOptionSound,
                    ) { granted, _ ->
                        cont.resume(granted)
                    }
                }
            }
        }
    }

    override fun schedule(record: ReminderRecord) {
        val nowMs = Clock.System.now().toEpochMilliseconds()
        val intervalSeconds = (record.triggerEpochMillis - nowMs) / 1000.0
        if (intervalSeconds <= 0) return

        val content = UNMutableNotificationContent().apply {
            setTitle(record.title)
            setBody(record.body)
            setSound(UNNotificationSound.defaultSound)
        }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            intervalSeconds,
            repeats = false,
        )
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = notificationId(record.eventId),
            content = content,
            trigger = trigger,
        )
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    override fun cancel(eventId: String) {
        val id = notificationId(eventId)
        center.removePendingNotificationRequestsWithIdentifiers(listOf(id))
        center.removeDeliveredNotificationsWithIdentifiers(listOf(id))
    }

    override fun rescheduleAll(records: Collection<ReminderRecord>) {
        val now = Clock.System.now().toEpochMilliseconds()
        records.filter { it.triggerEpochMillis > now }.forEach { schedule(it) }
    }

    private fun notificationId(eventId: String): String = "comp-reminder-$eventId"
}
