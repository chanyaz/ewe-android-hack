package com.expedia.bookings.marketing.carnival

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
import com.carnival.sdk.Carnival.CarnivalHandler
import com.carnival.sdk.CarnivalMessageListener
import com.carnival.sdk.Message
import com.expedia.bookings.R
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_BOOKED_PRODUCT
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_LAST_LOCATION
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_LOCATION_ENABLED
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_LOYALTY_TIER
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_NOTIFICATION_TYPE
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_POS
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_SIGN_IN
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_USERID
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.APP_OPEN_LAUNCH_RELAUNCH_USER_EMAIL
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CHECKOUT_START_FLIGHT
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CHECKOUT_START_FLIGHT_AIRLINE
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CHECKOUT_START_FLIGHT_DEPARTURE_DATE
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CHECKOUT_START_FLIGHT_DESTINATION
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CHECKOUT_START_FLIGHT_FLIGHT_NUMBER
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CHECKOUT_START_FLIGHT_LENGTH_OF_FLIGHT
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CHECKOUT_START_FLIGHT_NUMBER_OF_ADULTS
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CHECKOUT_START_HOTEL
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CHECKOUT_START_HOTEL_CHECK_IN_DATE
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CHECKOUT_START_HOTEL_DESTINATION
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CHECKOUT_START_HOTEL_HOTEL_NAME
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CHECKOUT_START_HOTEL_LENGTH_OF_STAY
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CHECKOUT_START_HOTEL_NUMBER_OF_ADULTS
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_FLIGHT
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_FLIGHT_AIRLINE
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_FLIGHT_DEPARTURE_DATE
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_FLIGHT_DESTINATION
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_FLIGHT_FLIGHT_NUMBER
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_FLIGHT_LENGTH_OF_FLIGHT
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_FLIGHT_NUMBER_OF_ADULTS
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_HOTEL
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_HOTEL_CHECK_IN_DATE
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_HOTEL_DESTINATION
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_HOTEL_HOTEL_NAME
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_HOTEL_LENGTH_OF_STAY
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_HOTEL_NUMBER_OF_ADULTS
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_LX
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_LX_ACTIVITY_NAME
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_LX_DATE_OF_ACTIVITY
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_PKG
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_PKG_DEPARTURE_DATE
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_PKG_DESTINATION
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_PKG_LENGTH_OF_STAY
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_RAIL
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_RAIL_DEPARTURE_DATE
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.CONFIRMATION_RAIL_DESTINATION
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.PRODUCT_VIEW_HOTEL
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.PRODUCT_VIEW_HOTEL_CHECK_IN_DATE
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.PRODUCT_VIEW_HOTEL_DESTINATION
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.PRODUCT_VIEW_HOTEL_HOTEL_NAME
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.PRODUCT_VIEW_HOTEL_LENGTH_OF_STAY
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.PRODUCT_VIEW_HOTEL_NUMBER_OF_ADULTS
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.SEARCH_FLIGHT
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.SEARCH_FLIGHT_DEPARTURE_DATE
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.SEARCH_FLIGHT_DESTINATION
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.SEARCH_FLIGHT_NUMBER_OF_ADULTS
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.SEARCH_HOTEL
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.SEARCH_HOTEL_CHECK_IN_DATE
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.SEARCH_HOTEL_DESTINATION
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.SEARCH_HOTEL_LENGTH_OF_STAY
import com.expedia.bookings.marketing.carnival.model.CarnivalConstants.SEARCH_HOTEL_NUMBER_OF_ADULTS
import com.expedia.bookings.marketing.carnival.model.CarnivalNotificationTypeConstants
import com.expedia.bookings.marketing.carnival.persistence.CarnivalPersistenceProvider
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.utils.ApiDateUtils
import com.expedia.bookings.utils.JodaUtils
import org.joda.time.Days
import org.joda.time.LocalDate
import org.json.JSONObject

open class CarnivalUtils {

    companion object {
        private val TAG = "CarnivalTracker"
        private lateinit var appContext: Context
        private var carnivalUtils: CarnivalUtils? = null
        private var initialized = false
        private lateinit var persistenceProvider: CarnivalPersistenceProvider

        @JvmStatic @Synchronized
        fun getInstance(): CarnivalUtils {
            if (carnivalUtils == null) {
                carnivalUtils = CarnivalUtils()
            }
            return carnivalUtils as CarnivalUtils
        }
    }

    fun initialize(context: Context, persistenceProvider: CarnivalPersistenceProvider) {
        appContext = context
        CarnivalUtils.persistenceProvider = persistenceProvider
        if (isFeatureToggledOn()) {
            initialized = true
            Carnival.setMessageReceivedListener(CustomCarnivalListener::class.java)
            Carnival.startEngine(appContext, appContext.getString(R.string.carnival_sdk_key))
        }
    }

    fun trackLaunch(isLocationEnabled: Boolean, isSignedIn: Boolean, traveler: Traveler?, bookedProducts: MutableCollection<Trip>, loyaltyTier: LoyaltyMembershipTier?, latitude: Double?, longitude: Double?, posUrl: String) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = JSONObject()
            val coordinates = latitude.toString() + ", " + longitude.toString()
            val bookedTrips = bookedProducts
                    .filter { it.tripComponents.any() }
                    .map { it.tripComponents.first()?.type.toString() }
                    .toSet()

            attributes.put(APP_OPEN_LAUNCH_RELAUNCH_LOCATION_ENABLED, isLocationEnabled)
            traveler?.tuid?.toInt()?.let { attributes.put(APP_OPEN_LAUNCH_RELAUNCH_USERID, it) }
            attributes.put(APP_OPEN_LAUNCH_RELAUNCH_USER_EMAIL, traveler?.email)
            attributes.put(APP_OPEN_LAUNCH_RELAUNCH_SIGN_IN, isSignedIn)
            attributes.put(APP_OPEN_LAUNCH_RELAUNCH_BOOKED_PRODUCT, ArrayList(bookedTrips.distinct()))
            attributes.put(APP_OPEN_LAUNCH_RELAUNCH_LOYALTY_TIER, loyaltyTier?.toApiValue())
            attributes.put(APP_OPEN_LAUNCH_RELAUNCH_LAST_LOCATION, coordinates)
            attributes.put(APP_OPEN_LAUNCH_RELAUNCH_NOTIFICATION_TYPE, arrayListOf(CarnivalNotificationTypeConstants.MKTG, CarnivalNotificationTypeConstants.SERV, CarnivalNotificationTypeConstants.PROMO)) //by default give them all types until the control is created to set these values
            attributes.put(APP_OPEN_LAUNCH_RELAUNCH_POS, posUrl)
            setAttributes(attributes, APP_OPEN_LAUNCH_RELAUNCH)
        }
    }

    fun trackFlightSearch(destination: String?, adults: Int, departure_date: LocalDate) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = JSONObject()
            attributes.put(SEARCH_FLIGHT_DESTINATION, destination)
            attributes.put(SEARCH_FLIGHT_NUMBER_OF_ADULTS, adults)
            attributes.put(SEARCH_FLIGHT_DEPARTURE_DATE, departure_date.toDate())
            setAttributes(attributes, SEARCH_FLIGHT)
        }
    }

    fun trackFlightCheckoutStart(destination: String?, adults: Int, departure_date: LocalDate, outboundFlight: FlightLeg?, inboundFlight: FlightLeg?, isRoundTrip: Boolean) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = JSONObject()
            attributes.put(CHECKOUT_START_FLIGHT_DESTINATION, destination)
            attributes.put(CHECKOUT_START_FLIGHT_AIRLINE, getAllAirlinesInTrip(outboundFlight, inboundFlight, isRoundTrip))
            attributes.put(CHECKOUT_START_FLIGHT_FLIGHT_NUMBER, getAllFlightNumbersInTrip(outboundFlight, inboundFlight, isRoundTrip))
            attributes.put(CHECKOUT_START_FLIGHT_NUMBER_OF_ADULTS, adults)
            attributes.put(CHECKOUT_START_FLIGHT_DEPARTURE_DATE, departure_date.toDate())
            attributes.put(CHECKOUT_START_FLIGHT_LENGTH_OF_FLIGHT, calculateTotalTravelTime(outboundFlight, inboundFlight, isRoundTrip))
            setAttributes(attributes, CHECKOUT_START_FLIGHT)
        }
    }

    fun trackFlightCheckoutConfirmation(destination: String?, adults: Int, departure_date: LocalDate, outboundFlight: FlightLeg?, inboundFlight: FlightLeg?, isRoundTrip: Boolean) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = JSONObject()
            attributes.put(CONFIRMATION_FLIGHT_DESTINATION, destination)
            attributes.put(CONFIRMATION_FLIGHT_AIRLINE, getAllAirlinesInTrip(outboundFlight, inboundFlight, isRoundTrip))
            attributes.put(CONFIRMATION_FLIGHT_FLIGHT_NUMBER, getAllFlightNumbersInTrip(outboundFlight, inboundFlight, isRoundTrip))
            attributes.put(CONFIRMATION_FLIGHT_NUMBER_OF_ADULTS, adults)
            attributes.put(CONFIRMATION_FLIGHT_DEPARTURE_DATE, departure_date.toDate())
            attributes.put(CONFIRMATION_FLIGHT_LENGTH_OF_FLIGHT, calculateTotalTravelTime(outboundFlight, inboundFlight, isRoundTrip))
            setAttributes(attributes, CONFIRMATION_FLIGHT)
        }
    }

    fun trackHotelSearch(searchParams: HotelSearchParams) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = JSONObject()
            attributes.put(SEARCH_HOTEL_DESTINATION, searchParams.suggestion.regionNames.fullName)
            attributes.put(SEARCH_HOTEL_NUMBER_OF_ADULTS, searchParams.adults)
            attributes.put(SEARCH_HOTEL_CHECK_IN_DATE, searchParams.checkIn.toDate())
            attributes.put(SEARCH_HOTEL_LENGTH_OF_STAY, JodaUtils.daysBetween(searchParams.checkIn, searchParams.checkOut))
            setAttributes(attributes, SEARCH_HOTEL)
        }
    }

    fun trackHotelInfoSite(hotelOffersResponse: HotelOffersResponse, searchParams: HotelSearchParams) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = JSONObject()
            attributes.put(PRODUCT_VIEW_HOTEL_DESTINATION, searchParams.suggestion.regionNames.fullName)
            attributes.put(PRODUCT_VIEW_HOTEL_HOTEL_NAME, hotelOffersResponse.hotelName)
            attributes.put(PRODUCT_VIEW_HOTEL_NUMBER_OF_ADULTS, searchParams.adults)
            attributes.put(PRODUCT_VIEW_HOTEL_CHECK_IN_DATE, searchParams.checkIn.toDate())
            attributes.put(PRODUCT_VIEW_HOTEL_LENGTH_OF_STAY, JodaUtils.daysBetween(searchParams.checkIn, searchParams.checkOut))
            setAttributes(attributes, PRODUCT_VIEW_HOTEL)
        }
    }

    fun trackHotelCheckoutStart(hotelCreateTripResponse: HotelCreateTripResponse, hotelSearchParams: HotelSearchParams) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = JSONObject()
            attributes.put(CHECKOUT_START_HOTEL_DESTINATION, hotelSearchParams.suggestion.regionNames.fullName)
            attributes.put(CHECKOUT_START_HOTEL_HOTEL_NAME, hotelCreateTripResponse.newHotelProductResponse.getHotelName())
            attributes.put(CHECKOUT_START_HOTEL_NUMBER_OF_ADULTS, hotelSearchParams.adults)
            attributes.put(CHECKOUT_START_HOTEL_CHECK_IN_DATE, hotelSearchParams.checkIn.toDate())
            attributes.put(CHECKOUT_START_HOTEL_LENGTH_OF_STAY, JodaUtils.daysBetween(hotelSearchParams.checkIn, hotelSearchParams.checkOut))
            setAttributes(attributes, CHECKOUT_START_HOTEL)
        }
    }

    fun trackHotelConfirmation(hotelCheckoutResponse: HotelCheckoutResponse, hotelSearchParams: HotelSearchParams) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = JSONObject()
            attributes.put(CONFIRMATION_HOTEL_DESTINATION, hotelSearchParams.suggestion.regionNames.fullName)
            attributes.put(CONFIRMATION_HOTEL_HOTEL_NAME, hotelCheckoutResponse.checkoutResponse.productResponse.hotelName)
            attributes.put(CONFIRMATION_HOTEL_NUMBER_OF_ADULTS, hotelSearchParams.adults)
            attributes.put(CONFIRMATION_HOTEL_CHECK_IN_DATE, hotelSearchParams.checkIn.toDate())
            attributes.put(CONFIRMATION_HOTEL_LENGTH_OF_STAY, JodaUtils.daysBetween(hotelSearchParams.checkIn, hotelSearchParams.checkOut))
            setAttributes(attributes, CONFIRMATION_HOTEL)
        }
    }

    fun trackLxConfirmation(activityTitle: String, activityDate: String) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = JSONObject()
            attributes.put(CONFIRMATION_LX_ACTIVITY_NAME, activityTitle)
            attributes.put(CONFIRMATION_LX_DATE_OF_ACTIVITY, ApiDateUtils.yyyyMMddHHmmssToLocalDate(activityDate).toDate())
            setAttributes(attributes, CONFIRMATION_LX)
        }
    }

    fun trackPackagesConfirmation(packageParams: PackageSearchParams) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = JSONObject()
            attributes.put(CONFIRMATION_PKG_DESTINATION, packageParams.destination?.regionNames?.fullName)
            attributes.put(CONFIRMATION_PKG_DEPARTURE_DATE, packageParams.startDate.toDate())
            attributes.put(CONFIRMATION_PKG_LENGTH_OF_STAY, Days.daysBetween(packageParams.startDate, packageParams.endDate).days)
            setAttributes(attributes, CONFIRMATION_PKG)
        }
    }

    fun trackRailConfirmation(railCheckoutResponse: RailCheckoutResponse) {
        if (isFeatureToggledOn() && initialized) {
            val attributes = JSONObject()
            val railLeg = railCheckoutResponse.railDomainProduct.railOffer.railProductList.first()?.legOptionList?.first()
            attributes.put(CONFIRMATION_RAIL_DESTINATION, railLeg?.arrivalStation?.stationDisplayName + ", " + railLeg?.arrivalStation?.stationCity)
            attributes.put(CONFIRMATION_RAIL_DEPARTURE_DATE, railLeg?.departureDateTime?.toDateTime()?.toDate())
            setAttributes(attributes, CONFIRMATION_RAIL)
        }
    }

    private fun isFeatureToggledOn(): Boolean = ProductFlavorFeatureConfiguration.getInstance().isCarnivalEnabled

    open fun setAttributes(attributes: JSONObject, eventName: String) {
        Carnival.logEvent(eventName)
        Carnival.setAttributes(AttributeMap(attributes), object : Carnival.AttributesHandler {
            override fun onSuccess() {
                Log.d(TAG, "Carnival attributes sent successfully.")
                saveAttributes(attributes)
            }

            override fun onFailure(error: Error) {
                Log.d(TAG, error.message)
            }
        })
    }

    open fun saveAttributes(attributes: JSONObject) {
        persistenceProvider.put(attributes)
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
                        .setSmallIcon(R.drawable.ic_stat)
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
