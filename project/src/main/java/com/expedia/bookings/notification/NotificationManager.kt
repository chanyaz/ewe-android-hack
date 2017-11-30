package com.expedia.bookings.notification

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import com.activeandroid.Model
import com.activeandroid.query.Delete
import com.activeandroid.query.Select
import com.mobiata.android.Log

open class NotificationManager(private val context: Context) {

    private val LOGGING_TAG = "NotificationManager"

    //PUBLIC METHODS
    /**
     * Schedule this notification with the OS AlarmManager. Multiple calls to this method
     * will not result in multiple notifications, as long as the ItinId*NotificationType remains the same.
     *
     */
    fun scheduleNotification(notification: Notification) {
        val pendingIntent = NotificationReceiver.generateSchedulePendingIntent(context, notification)
        val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.set(AlarmManager.RTC_WAKEUP, notification.triggerTimeMillis, pendingIntent)
    }

    /**
     * Cancel a previously scheduled notification with the OS AlarmManager.
     *
     * @param notification
     */
    fun cancelNotificationIntent(notification: Notification) {
        val pendingIntent = NotificationReceiver.generateSchedulePendingIntent(context, notification)

        // Cancel if in the future
        val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.cancel(pendingIntent)
    }

    fun cancelAndDeleteNotification(notification: Notification) {
        cancelNotificationIntent(notification)
        notification.delete()
    }


    fun dismissNotification(notification: Notification) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(notification.uniqueId, 0)
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
    fun setNotificationStatusToDismissed(notification: Notification) {
        val notifications = Select().from(Notification::class.java)
                .where("UniqueId=? AND NotificationType=?", notification.uniqueId, notification.notificationType)
                .execute<Notification>()
        for (n in notifications) {
            n.status = Notification.StatusType.DISMISSED
            n.save()
        }
    }

    /**
     * Schedules all new or updated notifications.
     */
    fun scheduleAll() {
        val notifications = Select()
                .from(Notification::class.java)
                .where("Status=? AND ExpirationTimeMillis>?", Notification.StatusType.NEW.name, System.currentTimeMillis())
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
        val notifications = Select()
                .from(Notification::class.java)
                .where("ExpirationTimeMillis<?", System.currentTimeMillis())
                .execute<Notification>()

        // Cancel all newly expired notifications
        for (notification in notifications) {
            cancelAndDeleteNotification(notification)
        }
    }

    /**
     * Cancels and removes _all_ notifications from the database.
     */
    fun deleteAll() {
        val notifications = Select().from(Notification::class.java).execute<Notification>()

        for (notification in notifications) {
            cancelNotificationIntent(notification)
            dismissNotification(notification)
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
            cancelNotificationIntent(notification)
            dismissNotification(notification)
        }

        // Delete all here instead of individually in the loop, for efficiency.
        Delete().from(Notification::class.java).where("ItinId=?", itinId).execute<Model>()
    }

    fun searchForExistingAndUpdate(notification: Notification) {
        // If we already have this notification, don't notify again.
        val existing = findExisting(notification)
        val newNotificationTime = com.expedia.bookings.utils.DateUtils
                .convertMilliSecondsForLogging(notification.triggerTimeMillis)
        if (existing != null) {
            if (notification != existing) {
                notification.save()
                existing.delete()
                Log.i(LOGGING_TAG, "Existing notification found and updated: scheduled for " + newNotificationTime)
            } else {
                val existingNotificationTime = com.expedia.bookings.utils.DateUtils
                        .convertMilliSecondsForLogging(existing.triggerTimeMillis)
                Log.i(LOGGING_TAG, "Existing notification found and not updated: scheduled for " + existingNotificationTime)
            }
        } else {
            notification.save()
            Log.i(LOGGING_TAG, "New Notification scheduled for " + newNotificationTime)
        }
    }
}