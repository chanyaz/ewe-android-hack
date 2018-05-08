package com.expedia.bookings.notification

interface INotificationManager {
    /**
     * Schedules all new or updated notifications.
     */
    fun scheduleAll()

    /**
     * Cancels all new or notified notifications, and removes them
     * from the notification bar if they've already been notified.
     */
    fun cancelAllExpired()

    fun searchForExistingAndUpdate(notification: Notification)

    fun wasFired(uniqueId: String): Boolean

    fun scheduleNotification(notification: Notification)

    fun cancelNotificationIntent(notification: Notification)

    fun cancelAndDeleteNotification(notification: Notification)

    fun dismissNotification(notification: Notification)

    fun findExisting(notification: Notification): Notification?

    fun hasExisting(notification: Notification): Boolean

    fun setNotificationStatusToDismissed(notification: Notification)

    fun deleteAll()

    fun deleteAll(itinId: String)
}
