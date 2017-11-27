package com.expedia.bookings.notification

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import com.activeandroid.ActiveAndroid
import com.activeandroid.Model
import com.activeandroid.query.Delete
import com.activeandroid.query.Select
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.mobiata.android.util.SettingUtils

class NotificationManager(private val context: Context) {

    //PUBLIC METHODS
    /**
     * Schedule this notification with the OS AlarmManager. Multiple calls to this method
     * will not result in multiple notifications, as long as the ItinId*NotificationType remains the same.
     *
     */
    fun scheduleNotification(notification: Notification) {
        val pendingIntent = NotificationReceiver.generateSchedulePendingIntent(context, notification)
        val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (BuildConfig.DEBUG && SettingUtils
                .get(context, context.getString(R.string.preference_launch_all_trip_notifications), false)) {
            val testTriggerTime = System.currentTimeMillis() + 5000
            mgr.set(AlarmManager.RTC_WAKEUP, testTriggerTime, pendingIntent)
        }
        else {
            mgr.set(AlarmManager.RTC_WAKEUP, notification.triggerTimeMillis, pendingIntent)
        }
    }

    /**
     * Cancel a previously scheduled notification with the OS AlarmManager.
     *
     * @param notification
     */
    fun cancelNotification(notification: Notification) {
        val pendingIntent = NotificationReceiver.generateSchedulePendingIntent(context, notification)

        // Cancel if in the future
        val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.cancel(pendingIntent)

        // Dismiss a possibly displayed notification
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(notification.uniqueId, 0)
    }

    private fun cancelScheduledNotification(notification: Notification) {
        val pendingIntent = NotificationReceiver.generateSchedulePendingIntent(context, notification)

        // Cancel if in the future
        val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.cancel(pendingIntent)
    }

    //DATABASE OPERATIONS
    /**
     * Returns a Notification, if found, from the table whose UniqueId*NotificationType matches the
     * data in the passed notification object.
     * @param notification
     * @return
     */
    fun findExisting(notification: Notification): Notification? {
        val notifications = Select().from(Notification::class.java)
                .where("UniqueId=? AND NotificationType=?", notification.uniqueId, notification.notificationType)
                .limit("1").execute<Notification>()
        if (notifications == null || notifications.size == 0) {
            return null
        } else {
            return notifications[0]
        }
    }

    fun hasExisting(notification: Notification): Boolean = findExisting(notification) != null

    /**
     * Dismiss notifications matching the UniqueId and NotificationType of the passed Notification
     * object. There may be more than one row sharing the same UniqueId and NotificationType
     * (even though that's not intended).
     * @param notification
     */
    fun dismissExisting(notification: Notification) {
        val notifications = Select().from(Notification::class.java)
                .where("UniqueId=? AND NotificationType=?", notification.uniqueId, notification.notificationType)
                .execute<Notification>()
        for (n in notifications) {
            n.status = Notification.StatusType.DISMISSED
            n.save()
        }
    }

    /**
     * Schedules all new or notified notifications.
     */
    fun scheduleAll() {
        val notifications = Select()
                .from(Notification::class.java)
                .where("Status IN (?,?)", Notification.StatusType.NEW.name, Notification.StatusType.NOTIFIED.name)
                .orderBy("TriggerTimeMillis").execute<Notification>()

        for (notification in notifications) {
                scheduleNotification(notification)
        }
    }

    /**
     * Cancels all new or notified notifications, and removes them
     * from the notification bar if they've already been notified.
     */
    fun cancelAllExpired() {
        if (BuildConfig.DEBUG && SettingUtils
                .get(context, context.getString(R.string.preference_launch_all_trip_notifications), false)) {
            return
        }
        val notifications = Select()
                .from(Notification::class.java)
                .where("Status IN (?,?) AND ExpirationTimeMillis<?", Notification.StatusType.NEW.name, Notification.StatusType.NOTIFIED.name, System.currentTimeMillis())
                .execute<Notification>()

        // Set all to expired at once
        ActiveAndroid.beginTransaction()
        try {
            for (notification in notifications) {
                notification.status = Notification.StatusType.EXPIRED
                notification.save()
            }
            ActiveAndroid.setTransactionSuccessful()
        } finally {
            ActiveAndroid.endTransaction()
        }

        // Cancel all newly expired notifications
        for (notification in notifications) {
            cancelNotification(notification)
        }
    }


    /**
     * Cancels and removes _all_ notifications from the database.
     */
    fun deleteAll() {
        val notifications = Select().from(Notification::class.java).execute<Notification>()

        for (notification in notifications) {
            cancelNotification(notification)
        }

        // Delete all here instead of individually in the loop, for efficiency.
        Delete().from(Notification::class.java).execute<Model>()
    }

    fun deleteAllKeepDisplayed() {
        val notifications = Select().from(Notification::class.java).execute<Notification>()

        for (notification in notifications) {
            cancelScheduledNotification(notification)
        }

        // Delete all here instead of individually in the loop, for efficiency.
        Delete().from(Notification::class.java).execute<Model>()
    }

    /**
     * Cancels and deletes all notifications related to the passed itinId.
     * @param itinId
     */
    fun deleteAll(itinId: String) {
        val notifications = Select().from(Notification::class.java).where("ItinId=?", itinId).execute<Notification>()

        for (notification in notifications) {
            cancelNotification(notification)
        }

        // Delete all here instead of individually in the loop, for efficiency.
        Delete().from(Notification::class.java).where("ItinId=?", itinId).execute<Model>()
    }
}