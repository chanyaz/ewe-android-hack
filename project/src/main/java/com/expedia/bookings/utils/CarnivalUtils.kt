package com.expedia.bookings.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.util.Log
import com.carnival.sdk.AttributeMap
import com.carnival.sdk.Carnival
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.R
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.services.HotelCheckoutResponse
import org.joda.time.Days
import org.joda.time.LocalDate
import com.carnival.sdk.Carnival.CarnivalHandler
import com.carnival.sdk.CarnivalMessageListener
import com.carnival.sdk.Message
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.trips.Trip

open class CarnivalUtils {

    companion object {
        private val TAG = "CarnivalTracker"
        private lateinit var appContext: Context
        private var carnivalUtils: CarnivalUtils? = null
        private var initialized = false

        @JvmStatic @Synchronized
        fun getInstance(): CarnivalUtils {
            if (carnivalUtils == null) {
                carnivalUtils = CarnivalUtils()
            }
            return carnivalUtils as CarnivalUtils
        }
    }

    fun initialize(context: Context) {
        appContext = context
        if (isFeatureToggledOn()) {
            initialized = true
            Carnival.setMessageReceivedListener(CustomCarnivalListener::class.java)
            Carnival.startEngine(appContext, appContext.getString(R.string.carnival_sdk_key))
        }
    }

    fun trackLaunch(isLocationEnabled: Boolean, isSignedIn: Boolean, traveler: Traveler?, bookedProducts: MutableCollection<Trip>, loyaltyTier: LoyaltyMembershipTier?, latitude: Double?, longitude: Double?, posUrl: String) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = AttributeMap()
            val coordinates = latitude.toString() + ", " + longitude.toString()
            val bookedTrips = bookedProducts
                    .filter { it.tripComponents.any() }
                    .map { it.tripComponents.first()?.type.toString() }
                    .toSet()

            attributes.putBoolean("app_open_launch_relaunch_location_enabled", isLocationEnabled)
            traveler?.tuid?.toInt()?.let { attributes.putInt("app_open_launch_relaunch_userid", it) }
            attributes.putString("app_open_launch_relaunch_user_email", traveler?.email)
            attributes.putBoolean("app_open_launch_relaunch_sign-in", isSignedIn)
            attributes.putStringArray("app_open_launch_relaunch_booked_product", ArrayList(bookedTrips.distinct()))
            attributes.putString("app_open_launch_relaunch_loyalty_tier", loyaltyTier?.toApiValue())
            attributes.putString("app_open_launch_relaunch_last_location", coordinates)
            attributes.putStringArray("app_open_launch_relaunch_notification_type", arrayListOf("MKTG", "SERV", "PROMO")) //by default give them all types until the control is created to set these values
            attributes.putString("app_open_launch_relaunch_pos", posUrl)
            setAttributes(attributes, "app_open_launch_relaunch")
        }
    }

    fun trackFlightSearch(destination: String?, adults: Int, departure_date: LocalDate) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = AttributeMap()
            attributes.putString("search_flight_destination", destination)
            attributes.putInt("search_flight_number_of_adults", adults)
            attributes.putDate("search_flight_departure_date", departure_date.toDate())
            setAttributes(attributes, "search_flight")
        }
    }

    fun trackFlightCheckoutStart(destination: String?, adults: Int, departure_date: LocalDate, outboundFlight: FlightLeg?, inboundFlight: FlightLeg?, isRoundTrip: Boolean) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = AttributeMap()
            attributes.putString("checkout_start_flight_destination", destination)
            attributes.putStringArray("checkout_start_flight_airline", getAllAirlinesInTrip(outboundFlight, inboundFlight, isRoundTrip))
            attributes.putStringArray("checkout_start_flight_flight_number", getAllFlightNumbersInTrip(outboundFlight, inboundFlight, isRoundTrip))
            attributes.putInt("checkout_start_flight_number_of_adults", adults)
            attributes.putDate("checkout_start_flight_departure_date", departure_date.toDate())
            attributes.putString("checkout_start_flight_length_of_flight", calculateTotalTravelTime(outboundFlight, inboundFlight, isRoundTrip))
            setAttributes(attributes, "checkout_start_flight")
        }
    }

    fun trackFlightCheckoutConfirmation(destination: String?, adults: Int, departure_date: LocalDate, outboundFlight: FlightLeg?, inboundFlight: FlightLeg?, isRoundTrip: Boolean) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = AttributeMap()
            attributes.putString("confirmation_flight_destination", destination)
            attributes.putStringArray("confirmation_flight_airline", getAllAirlinesInTrip(outboundFlight, inboundFlight, isRoundTrip))
            attributes.putStringArray("confirmation_flight_flight_number", getAllFlightNumbersInTrip(outboundFlight, inboundFlight, isRoundTrip))
            attributes.putInt("confirmation_flight_number_of_adults", adults)
            attributes.putDate("confirmation_flight_departure_date", departure_date.toDate())
            attributes.putString("confirmation_flight_length_of_flight", calculateTotalTravelTime(outboundFlight, inboundFlight, isRoundTrip))
            setAttributes(attributes, "confirmation_flight")
        }
    }

    fun trackHotelSearch(searchParams: HotelSearchParams) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = AttributeMap()
            attributes.putString("search_hotel_destination", searchParams.suggestion.regionNames.fullName)
            attributes.putInt("search_hotel_number_of_adults", searchParams.adults)
            attributes.putDate("search_hotel_check-in_date", searchParams.checkIn.toDate())
            attributes.putInt("search_hotel_length_of_stay", JodaUtils.daysBetween(searchParams.checkIn, searchParams.checkOut))
            setAttributes(attributes, "search_hotel")
        }
    }

    fun trackHotelInfoSite(hotelOffersResponse: HotelOffersResponse, searchParams: HotelSearchParams) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = AttributeMap()
            attributes.putString("product_view_hotel_destination", searchParams.suggestion.regionNames.fullName)
            attributes.putString("product_view_hotel_hotel_name", hotelOffersResponse.hotelName)
            attributes.putInt("product_view_hotel_number_of_adults", searchParams.adults)
            attributes.putDate("product_view_hotel_check-in_date", searchParams.checkIn.toDate())
            attributes.putInt("product_view_hotel_length_of_stay", JodaUtils.daysBetween(searchParams.checkIn, searchParams.checkOut))
            setAttributes(attributes, "product_view_hotel")
        }
    }

    fun trackHotelCheckoutStart(hotelCreateTripResponse: HotelCreateTripResponse, hotelSearchParams: HotelSearchParams) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = AttributeMap()
            attributes.putString("checkout_start_hotel_destination", hotelSearchParams.suggestion.regionNames.fullName)
            attributes.putString("checkout_start_hotel_hotel_name", hotelCreateTripResponse.newHotelProductResponse.getHotelName())
            attributes.putInt("checkout_start_hotel_number_of_adults", hotelSearchParams.adults)
            attributes.putDate("checkout_start_hotel_check-in_date", hotelSearchParams.checkIn.toDate())
            attributes.putInt("checkout_start_hotel_length_of_stay", JodaUtils.daysBetween(hotelSearchParams.checkIn, hotelSearchParams.checkOut))
            setAttributes(attributes, "checkout_start_hotel")
        }
    }

    fun trackHotelConfirmation(hotelCheckoutResponse: HotelCheckoutResponse, hotelSearchParams: HotelSearchParams) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = AttributeMap()
            attributes.putString("confirmation_hotel_destination", hotelSearchParams.suggestion.regionNames.fullName)
            attributes.putString("confirmation_hotel_hotel_name", hotelCheckoutResponse.checkoutResponse.productResponse.hotelName)
            attributes.putInt("confirmation_hotel_number_of_adults", hotelSearchParams.adults)
            attributes.putDate("confirmation_hotel_check-in_date", hotelSearchParams.checkIn.toDate())
            attributes.putInt("confirmation_hotel_length_of_stay", JodaUtils.daysBetween(hotelSearchParams.checkIn, hotelSearchParams.checkOut))
            setAttributes(attributes, "confirmation_hotel")
        }
    }

    fun trackLxConfirmation(activityTitle: String, activityDate: String) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = AttributeMap()
            attributes.putString("confirmation_lx_activity_name", activityTitle)
            attributes.putDate("confirmation_lx_date_of_activity", ApiDateUtils.yyyyMMddHHmmssToLocalDate(activityDate).toDate())
            setAttributes(attributes, "confirmation_lx")
        }
    }

    fun trackPackagesConfirmation(packageParams: PackageSearchParams) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = AttributeMap()
            attributes.putString("confirmation_pkg_destination", packageParams.destination?.regionNames?.fullName)
            attributes.putDate("confirmation_pkg_departure_date", packageParams.startDate.toDate())
            attributes.putInt("confirmation_pkg_length_of_stay", Days.daysBetween(packageParams.startDate, packageParams.endDate).days)
            setAttributes(attributes, "confirmation_pkg")
        }
    }

    fun trackRailConfirmation(railCheckoutResponse: RailCheckoutResponse) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = AttributeMap()
            val railLeg = railCheckoutResponse.railDomainProduct.railOffer.railProductList.first()?.legOptionList?.first()

            attributes.putString("confirmation_rail_destination", railLeg?.arrivalStation?.stationDisplayName + ", " + railLeg?.arrivalStation?.stationCity)
            attributes.putDate("confirmation_rail_departure_date", railLeg?.departureDateTime?.toDateTime()?.toDate())
            setAttributes(attributes, "confirmation_rail")
        }
    }

    private fun isFeatureToggledOn(): Boolean = ProductFlavorFeatureConfiguration.getInstance().isCarnivalEnabled

    open fun setAttributes(attributes: AttributeMap, eventName: String) {
        Carnival.logEvent(eventName)
        Carnival.setAttributes(attributes, object : Carnival.AttributesHandler {
            override fun onSuccess() {
                Log.d(TAG, "Carnival attributes sent successfully.")
            }

            override fun onFailure(error: Error) {
                Log.d(TAG, error.message)
            }
        })
    }

    open fun setUserInfo(userId: String?, userEmail: String?) {
        if (isFeatureToggledOn() && initialized) {
            Carnival.setUserId(userId, object : CarnivalHandler<Void> {
                override fun onSuccess(value: Void) {
                    Log.d(TAG, "Carnival UserId set successfully.")
                }

                override fun onFailure(error: Error) {
                    Log.d(TAG, error.message)
                }
            })

            Carnival.setUserEmail(userEmail, object : CarnivalHandler<Void> {
                override fun onSuccess(value: Void) {
                    Log.d(TAG, "Carnival User Email set successfully.")
                }

                override fun onFailure(error: Error) {
                    Log.d(TAG, error.message)
                }
            })
        }
    }

    fun toggleNotifications(enableNotifications: Boolean) {
        if (isFeatureToggledOn() && initialized) {
            Carnival.setInAppNotificationsEnabled(enableNotifications)
        }
    }

    fun clearUserInfo() {
        setUserInfo(null, null)
    }

    private fun getAllAirlinesInTrip(outboundLeg: FlightLeg?, inboundLeg: FlightLeg?, isRoundTrip: Boolean): ArrayList<String> {
        val segmentAirlines = hashSetOf<String>()
        outboundLeg?.segments?.mapTo(segmentAirlines) { it.airlineName }

        if (isRoundTrip) {
            inboundLeg?.segments?.mapTo(segmentAirlines) { it.airlineName }
        }

        return ArrayList(segmentAirlines.distinct())
    }

    private fun getAllFlightNumbersInTrip(outboundLeg: FlightLeg?, inboundLeg: FlightLeg?, isRoundTrip: Boolean): ArrayList<String> {
        val segmentFlightNumbers = hashSetOf<String>()
        outboundLeg?.segments?.mapTo(segmentFlightNumbers) { it.flightNumber }

        if (isRoundTrip) {
            inboundLeg?.segments?.mapTo(segmentFlightNumbers) { it.flightNumber }
        }

        return ArrayList(segmentFlightNumbers.distinct())
    }

    private fun calculateTotalTravelTime(outboundFlight: FlightLeg?, inboundFlight: FlightLeg?, isRoundTrip: Boolean): String {
        var totalDuration = 0
        val totalSegments = arrayListOf<FlightLeg.FlightSegment>()

        if (isRoundTrip && inboundFlight != null) {
            totalSegments.addAll(inboundFlight.segments)
        }

        if (outboundFlight != null) {
            totalSegments.addAll(outboundFlight.segments)
        }

        for (segment in totalSegments) {
            totalDuration += (segment.durationHours * 60) + segment.durationMinutes
            totalDuration += (segment.layoverDurationHours * 60) + segment.layoverDurationMinutes
        }

        val hours = totalDuration / 60
        val minutes = totalDuration % 60
        return String.format("%d:%02d", hours, minutes)
    }

    open class CustomCarnivalListener : CarnivalMessageListener() {

        companion object {
            val KEY_PAYLOAD_DEEPLINK: String = "deeplink"
            val KEY_PAYLOAD_ALERT: String = "alert"
            val KEY_PAYLOAD_TITLE: String = "title"
            val KEY_PROVIDER: String = "provider"
            val KEY_PROVIDER_VALUE: String = "carnival"
        }

        fun isNotificationFromCarnival(bundle: Bundle): Boolean {
            return bundle.containsKey(KEY_PROVIDER) && bundle.getString(KEY_PROVIDER) == KEY_PROVIDER_VALUE
        }

        fun createPendingIntent(context: Context, bundle: Bundle, deepLink: String?): PendingIntent {
            val pendingIntent: PendingIntent

            val intent = Intent()
                    .putExtras(bundle)
                    .setAction(Intent.ACTION_VIEW)
                    .setData(android.net.Uri.parse(if (deepLink.isNullOrEmpty()) { context.getString(R.string.deeplink_home) } else { deepLink }))
                    .addFlags(Intent.FLAG_FROM_BACKGROUND)

            val stackBuilder = TaskStackBuilder.create(context).addNextIntent(intent)
            pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            return pendingIntent
    }

        override fun onMessageReceived(context: Context, bundle: Bundle, message: Message?): Boolean {
            return if (isNotificationFromCarnival(bundle)) {
                val builder = NotificationCompat.Builder(context)
                        .setContentTitle(bundle.getString(KEY_PAYLOAD_TITLE))
                        .setContentText(bundle.getString(KEY_PAYLOAD_ALERT))
                        .setContentIntent(createPendingIntent(context, bundle, bundle.getString(KEY_PAYLOAD_DEEPLINK)))
                        .setSmallIcon(R.drawable.ic_stat_expedia)
                        .setAutoCancel(true)

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
                true
            } else {
                false
            }
        }
    }
}
