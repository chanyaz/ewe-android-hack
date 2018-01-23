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
}
