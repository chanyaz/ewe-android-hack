package com.expedia.bookings.notification

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.data.trips.TripHotel
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.Strings
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.ArrayList
import javax.inject.Inject

class HotelNotificationGenerator @Inject constructor(val context: Context, val stringProvider: StringSource, val notificationManager: INotificationManager) {
    //TODO use fallback given by api (when api work is done) and we could get rid of context
    fun generateNotifications(dataHotel: ItinCardDataHotel): List<Notification> {
        val notifications = ArrayList<Notification?>()
        notifications.add(generateCheckinNotification(context, dataHotel))
        notifications.add(generateCheckoutNotification(context, dataHotel))
        if (dataHotel.guestCount > 2 || isDurationLongerThanDays(2, dataHotel)) {
            notifications.add(generateGetReadyNotification(context, dataHotel))
            notifications.add(generateActivityCrossSellNotification(dataHotel))
        }
        OmnitureTracking.trackLXNotificationTest()
        if ((AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidLXNotifications) && isDurationLongerThanDays(1, dataHotel))) {
            notifications.add(generateActivityInTripNotification(dataHotel))
        }

        val validForHotelReviewNotification = Strings.isNotEmpty((dataHotel.tripComponent as TripHotel).reviewLink) && AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppTripsUserReviews)
        if (validForHotelReviewNotification) {
            notifications.add(generateHotelReviewNotification(dataHotel))
        }
        return notifications.filterNotNull()
    }

    private fun generateCheckinNotification(context: Context, data: ItinCardDataHotel): Notification? {

        val itinId = data.id
        val uniqueID = itinId + "_checkin"
        val isPastDisplayDate = data.startDate.millis < DateTime.now().millis
        if (notificationManager.wasFired(uniqueID) || isPastDisplayDate) {
            return null
        }

        val startDate = roundTime(data.startDate)
        var trigger = startDate.toMutableDateTime()
        trigger.addDays(-1)
        val triggerTimeMillis = trigger.millis

        trigger = startDate.toMutableDateTime()
        trigger.hourOfDay = 23
        trigger.minuteOfHour = 59
        val expirationTimeMillis = trigger.millis

        val notification = Notification(uniqueID, itinId, triggerTimeMillis)
        notification.notificationType = Notification.NotificationType.HOTEL_CHECK_IN
        notification.expirationTimeMillis = expirationTimeMillis
        notification.flags = Notification.FLAG_LOCAL or Notification.FLAG_DIRECTIONS or Notification.FLAG_CALL
        notification.iconResId = R.drawable.ic_stat_hotel

        val title = stringProvider.fetch(R.string.check_in_notification_title)

        notification.ticker = title
        notification.title = title
        val body = stringProvider.fetchWithPhrase(R.string.check_in_notification_body_TEMPLATE,
                mapOf("hotel" to data.propertyName!!, "checkin" to data.getFallbackCheckInTime(context)))

        notification.body = body

        notification.imageUrls = data.headerImageUrls

        return notification
    }

    private fun generateCheckoutNotification(context: Context, data: ItinCardDataHotel): Notification? {

        val itinId = data.id
        val uniqueID = itinId + "_checkout"
        val isPastDisplayDate = data.endDate.millis < DateTime.now().millis
        if (notificationManager.wasFired(uniqueID) || isPastDisplayDate) {
            return null
        }
        val endDate = roundTime(data.endDate)
        var trigger = endDate.toMutableDateTime()
        if (isDurationLongerThanDays(2, data)) {
            trigger.addDays(-1)
        } else {
            trigger.addHours(-12)
        }
        val triggerTimeMillis = trigger.millis

        trigger = endDate.toMutableDateTime()
        trigger.hourOfDay = 23
        trigger.minuteOfHour = 59
        val expirationTimeMillis = trigger.millis

        val notification = Notification(uniqueID, itinId, triggerTimeMillis)
        notification.notificationType = Notification.NotificationType.HOTEL_CHECK_OUT
        notification.expirationTimeMillis = expirationTimeMillis
        notification.flags = Notification.FLAG_LOCAL or Notification.FLAG_DIRECTIONS or Notification.FLAG_CALL
        notification.iconResId = R.drawable.ic_stat_hotel
        val title: String
        val body: String
        if (hasLastDayStarted(data)) {
            title = stringProvider.fetchWithPhrase(R.string.check_out_notification_title_day_of_TEMPLATE,
                    mapOf("checkout" to data.getFallbackCheckOutTime(context)))
            body = stringProvider.fetchWithPhrase(R.string.check_out_notification_body_day_of_TEMPLATE,
                    mapOf("hotel" to data.propertyName!!, "checkout" to data.getFallbackCheckOutTime(context)))
        } else {
            title = stringProvider.fetchWithPhrase(R.string.check_out_notification_title_day_before_TEMPLATE,
                    mapOf("checkout" to data.getFallbackCheckOutTime(context)))
            body = stringProvider.fetchWithPhrase(R.string.check_out_notification_body_day_before_TEMPLATE,
                    mapOf("hotel" to data.propertyName!!, "checkout" to data.getFallbackCheckOutTime(context)))
        }
        notification.ticker = title
        notification.title = title
        notification.body = body

        notification.imageUrls = data.headerImageUrls

        return notification
    }

    private fun generateGetReadyNotification(context: Context, data: ItinCardDataHotel): Notification? {

        val itinId = data.id
        val uniqueID = itinId + "_getready"
        val isPastDisplayDate = data.startDate.millis < DateTime.now().millis
        if (notificationManager.wasFired(uniqueID) || isPastDisplayDate) {
            return null
        }
        val hotel = data.tripComponent as TripHotel
        val startDate = roundTime(data.startDate)

        var trigger = startDate.toMutableDateTime()
        trigger.addDays(-3)
        val triggerTimeMillis = trigger.millis

        trigger = startDate.toMutableDateTime()
        trigger.hourOfDay = 23
        trigger.minuteOfHour = 59
        val expirationTimeMillis = trigger.millis

        val notification = Notification(uniqueID, itinId, triggerTimeMillis)
        notification.notificationType = Notification.NotificationType.HOTEL_GET_READY
        notification.expirationTimeMillis = expirationTimeMillis
        notification.flags = Notification.FLAG_LOCAL or Notification.FLAG_DIRECTIONS or Notification.FLAG_CALL
        notification.iconResId = R.drawable.ic_stat_hotel

        val title = stringProvider.fetch(R.string.get_ready_for_trip)
        notification.ticker = title
        notification.title = title
        val body = stringProvider.fetchWithPhrase(R.string.get_ready_for_trip_body_TEMPLATE,
                mapOf("firstname" to hotel.primaryTraveler.firstName, "hotel" to hotel.property.name, "startday" to data.getFormattedDetailsCheckInDate(context)))
        notification.body = body

        notification.imageUrls = data.headerImageUrls

        return notification
    }

    private fun generateActivityCrossSellNotification(data: ItinCardDataHotel): Notification? {

        val itinId = data.id
        val uniqueID = itinId + "_activityCross"
        if (notificationManager.wasFired(uniqueID)) {
            return null
        }
        val startDate = roundTime(data.startDate)

        var trigger = startDate.toMutableDateTime()
        trigger.addDays(-7)
        val triggerTimeMillis = trigger.millis

        trigger = startDate.toMutableDateTime()
        trigger.hourOfDay = 23
        trigger.minuteOfHour = 59
        val expirationTimeMillis = trigger.millis

        val notification = Notification(uniqueID, itinId, triggerTimeMillis)
        notification.notificationType = Notification.NotificationType.HOTEL_ACTIVITY_CROSSSEll
        notification.expirationTimeMillis = expirationTimeMillis
        notification.flags = Notification.FLAG_LOCAL
        notification.iconResId = R.drawable.ic_stat

        val title = stringProvider.fetchWithPhrase(R.string.hotel_book_activities_cross_sell_notification_title_TEMPLATE,
                mapOf("destination" to data.propertyCity!!))
        notification.ticker = title
        notification.title = title
        val body = stringProvider.fetchWithPhrase(R.string.hotel_book_activities_cross_sell_notification_body_TEMPLATE,
                mapOf("destination" to data.propertyCity!!))
        notification.body = body

        return notification
    }

    private fun generateActivityInTripNotification(data: ItinCardDataHotel): Notification? {
        val hotel = data.tripComponent as TripHotel

        val itinId = data.id
        val uniqueID = itinId + "_activityInTrip"
        if (notificationManager.wasFired(uniqueID)) {
            return null
        }
        val startDate = roundTime(data.startDate)
        val endDate = roundTime(data.endDate)

        var trigger = startDate.toMutableDateTime()

        if (startDate.hourOfDay < 8) {
            trigger.hourOfDay = 10
        } else if (startDate.hourOfDay > 18) {
            trigger.addDays(1)
            trigger.hourOfDay = 10
        } else {
            trigger.addHours(2)
        }
        val triggerTimeMillis = trigger.millis

        trigger = endDate.toMutableDateTime()
        trigger.hourOfDay = 23
        trigger.minuteOfHour = 59
        val expirationTimeMillis = trigger.millis

        val notification = Notification(itinId + "_activityInTrip", itinId, triggerTimeMillis)
        notification.notificationType = Notification.NotificationType.HOTEL_ACTIVITY_IN_TRIP
        notification.expirationTimeMillis = expirationTimeMillis
        notification.flags = Notification.FLAG_LOCAL
        notification.iconResId = R.drawable.ic_stat

        val title = stringProvider.fetch(R.string.things_to_do_near_hotel)

        notification.ticker = title
        notification.title = title
        val body = stringProvider.fetchWithPhrase(R.string.hotel_book_activities_in_trip_notification_body_TEMPLATE,
                mapOf("firstname" to hotel.primaryTraveler.firstName))
        notification.body = body

        return notification
    }

    private fun generateHotelReviewNotification(data: ItinCardDataHotel): Notification? {
        val hotel = data.tripComponent as TripHotel
        val deepLink = hotel.reviewLink
        val itinId = data.id
        val uniqueID = itinId + "_hotelReview"
        if (notificationManager.wasFired(uniqueID)) {
            return null
        }

        val endDate = roundTime(data.endDate)

        val trigger = endDate.toMutableDateTime()
        trigger.addDays(1)

        val triggerTimeMillis = trigger.millis

        val hotelName = hotel.property.name
        val title = stringProvider.fetchWithPhrase(R.string.hotel_review_title_notification_TEMPLATE,
                mapOf("hotelname" to hotelName))
        val body = stringProvider.fetchWithPhrase(R.string.hotel_review_body_notification_TEMPLATE,
                mapOf("firstname" to hotel.primaryTraveler.firstName, "hotelname" to hotelName))

        val notification = Notification(uniqueID, itinId, triggerTimeMillis)
        notification.notificationType = Notification.NotificationType.HOTEL_REVIEW
        notification.flags = Notification.FLAG_LOCAL
        notification.iconResId = R.drawable.ic_stat_hotel
        notification.title = title
        notification.ticker = title
        notification.body = body
        notification.deepLink = deepLink
        return notification
    }

    fun hasLastDayStarted(dataHotel: ItinCardDataHotel): Boolean {
        val endDate = dataHotel.endDate.toMutableDateTime()
        endDate.setZoneRetainFields(DateTimeZone.getDefault())
        endDate.minuteOfDay = 1
        return endDate.millis < DateTime.now().millis
    }

    fun roundTime(time: DateTime): DateTime {
        val trigger = time.toMutableDateTime()
        trigger.setRounding(trigger.chronology.minuteOfHour())
        return trigger.toDateTime()
    }

    fun isDurationLongerThanDays(days: Int, dataHotel: ItinCardDataHotel): Boolean {
        val endDate = dataHotel.endDate
        val dateToCheck = dataHotel.startDate.plusDays(days)
        return endDate.isAfter(dateToCheck)
    }
}
