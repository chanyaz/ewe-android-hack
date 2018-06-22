package com.expedia.bookings.marketing.carnival

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.util.Log
import com.carnival.sdk.AttributeMap
import com.carnival.sdk.Carnival
import com.carnival.sdk.Carnival.CarnivalHandler
import com.carnival.sdk.CarnivalImpressionType
import com.carnival.sdk.ContentIntentBuilder
import com.carnival.sdk.NotificationConfig
import com.carnival.sdk.NotificationReceivedListener
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.pos.PointOfSale
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
import com.expedia.bookings.marketing.carnival.model.CarnivalMessage
import com.expedia.bookings.marketing.carnival.model.CarnivalNotificationConstants.KEY_NOTIFICATION_PROVIDER
import com.expedia.bookings.marketing.carnival.model.CarnivalNotificationConstants.KEY_NOTIFICATION_PROVIDER_VALUE
import com.expedia.bookings.marketing.carnival.model.CarnivalNotificationConstants.KEY_PAYLOAD_ALERT
import com.expedia.bookings.marketing.carnival.model.CarnivalNotificationConstants.KEY_PAYLOAD_DEEPLINK
import com.expedia.bookings.marketing.carnival.model.CarnivalNotificationConstants.KEY_PAYLOAD_MARKETING
import com.expedia.bookings.marketing.carnival.model.CarnivalNotificationConstants.KEY_PAYLOAD_TITLE
import com.expedia.bookings.marketing.carnival.model.CarnivalNotificationTypeConstants
import com.expedia.bookings.marketing.carnival.persistence.CarnivalPersistenceProvider
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.ApiDateUtils
import com.expedia.bookings.utils.JodaUtils
import com.squareup.phrase.Phrase
import com.squareup.picasso.Picasso
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.util.regex.Pattern

open class CarnivalUtils {

    companion object {
        private val tag = "CarnivalTracker"
        private val olacid = "olacid"
        private val parameterizedPattern = Pattern.compile("\\{\\{(.+)\\}\\}")
        private val carnivalDateFormat = "EEE MMM dd HH:mm:ss z yyyy"
        private val unwantedCharacters = Regex("[{}®]")
        private lateinit var appContext: Context
        private var carnivalUtils: CarnivalUtils? = null
        private var initialized = false
        internal var messageQueue = mutableListOf<CarnivalMessage>()
        private lateinit var persistenceProvider: CarnivalPersistenceProvider
        internal var supportFragmentManager: FragmentManager? = null

        @JvmStatic
        @Synchronized
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
        if (isCarnivalEnabled()) {
            initialized = true

            Carnival.setOnInAppNotificationDisplayListener { message ->
                val carnivalMessage = CarnivalMessage(message.imageURL, message.title, message.attributes, message.text)
                carnivalMessage.messageData = message

                if (!message.imageURL.isNullOrEmpty()) {
                    Picasso.with(appContext).load(message.imageURL).fetch(object : com.squareup.picasso.Callback {
                        override fun onSuccess() = createInAppNotification(supportFragmentManager, carnivalMessage)

                        override fun onError() {
                            Log.d("Carnival Failure", "Image load failed for in-app messaging.")
                        }
                    })
                } else {
                    createInAppNotification(supportFragmentManager, carnivalMessage)
                }
                false
            }
            Carnival.startEngine(appContext, appContext.getString(R.string.carnival_sdk_key))

            Carnival.addNotificationReceivedListener(CustomCarnivalListener())
        }
    }

    fun setupListener(fragManager: FragmentManager) {
        if (isCarnivalEnabled() && initialized) {
            supportFragmentManager = fragManager
            checkForStaleMessages()
        }
    }

    fun checkForStaleMessages() {
        if (messageQueue.any()) {
            buildDialog(messageQueue.first())
            messageQueue.clear()
        }
    }

    fun createInAppNotification(supportFragmentManager: FragmentManager?, carnivalMessage: CarnivalMessage) {
        if (supportFragmentManager != null) {
            buildDialog(carnivalMessage)
        } else {
            messageQueue.add(carnivalMessage)
        }
    }

    open fun buildDialog(carnivalMessage: CarnivalMessage) {
        if (supportFragmentManager != null) {
            val args = Bundle()
            args.putParcelable("carnival_message", carnivalMessage)
            val inAppDialogFragment = InAppNotificationDialogFragment()
            inAppDialogFragment.arguments = args

            val transaction = supportFragmentManager!!.beginTransaction()
            transaction.add(inAppDialogFragment, "fragment_dialog_in_app_notification")
            transaction.commitAllowingStateLoss()

            Carnival.registerMessageImpression(CarnivalImpressionType.IMPRESSION_TYPE_IN_APP_VIEW, carnivalMessage.messageData)
            Carnival.setMessageRead(carnivalMessage.messageData, null)
        }
    }

    fun trackLaunch(isLocationEnabled: Boolean, isSignedIn: Boolean, traveler: Traveler?, bookedProducts: MutableCollection<Trip>, loyaltyTier: LoyaltyMembershipTier?, latitude: Double?, longitude: Double?, posUrl: String) {
        if (isCarnivalEnabled() && initialized) {
            val attributes = AttributeMap()
            val coordinates = latitude.toString() + ", " + longitude.toString()
            val bookedTrips = bookedProducts
                    .filter { it.tripComponents.any() }
                    .map { it.tripComponents.first()?.type.toString() }
                    .toSet()

            attributes.putBoolean(APP_OPEN_LAUNCH_RELAUNCH_LOCATION_ENABLED, isLocationEnabled)
            traveler?.tuid?.toInt()?.let { attributes.putInt(APP_OPEN_LAUNCH_RELAUNCH_USERID, it) }
            attributes.putString(APP_OPEN_LAUNCH_RELAUNCH_USER_EMAIL, traveler?.email)
            attributes.putBoolean(APP_OPEN_LAUNCH_RELAUNCH_SIGN_IN, isSignedIn)
            attributes.putStringArray(APP_OPEN_LAUNCH_RELAUNCH_BOOKED_PRODUCT, ArrayList(bookedTrips.distinct()))
            attributes.putString(APP_OPEN_LAUNCH_RELAUNCH_LOYALTY_TIER, loyaltyTier?.toApiValue())
            attributes.putString(APP_OPEN_LAUNCH_RELAUNCH_LAST_LOCATION, coordinates)
            attributes.putStringArray(APP_OPEN_LAUNCH_RELAUNCH_NOTIFICATION_TYPE, arrayListOf(CarnivalNotificationTypeConstants.MKTG, CarnivalNotificationTypeConstants.SERV, CarnivalNotificationTypeConstants.PROMO)) //by default give them all types until the control is created to set these values
            attributes.putString(APP_OPEN_LAUNCH_RELAUNCH_POS, posUrl)
            setAttributes(attributes, APP_OPEN_LAUNCH_RELAUNCH)
        }
    }

    fun trackFlightSearch(destination: String?, adults: Int, departure_date: LocalDate) {
        if (isCarnivalEnabled() && initialized) {
            val attributes = AttributeMap()
            attributes.putString(SEARCH_FLIGHT_DESTINATION, destination)
            attributes.putInt(SEARCH_FLIGHT_NUMBER_OF_ADULTS, adults)
            attributes.putDate(SEARCH_FLIGHT_DEPARTURE_DATE, departure_date.toDate())
            setAttributes(attributes, SEARCH_FLIGHT)
        }
    }

    fun trackFlightCheckoutStart(destination: String?, adults: Int, departure_date: LocalDate, outboundFlight: FlightLeg?, inboundFlight: FlightLeg?, isRoundTrip: Boolean) {
        if (isCarnivalEnabled() && initialized) {
            val attributes = AttributeMap()
            attributes.putString(CHECKOUT_START_FLIGHT_DESTINATION, destination)
            attributes.putStringArray(CHECKOUT_START_FLIGHT_AIRLINE, getAllAirlinesInTrip(outboundFlight, inboundFlight, isRoundTrip))
            attributes.putStringArray(CHECKOUT_START_FLIGHT_FLIGHT_NUMBER, getAllFlightNumbersInTrip(outboundFlight, inboundFlight, isRoundTrip))
            attributes.putInt(CHECKOUT_START_FLIGHT_NUMBER_OF_ADULTS, adults)
            attributes.putDate(CHECKOUT_START_FLIGHT_DEPARTURE_DATE, departure_date.toDate())
            attributes.putString(CHECKOUT_START_FLIGHT_LENGTH_OF_FLIGHT, calculateTotalTravelTime(outboundFlight, inboundFlight, isRoundTrip))
            setAttributes(attributes, CHECKOUT_START_FLIGHT)
        }
    }

    fun trackFlightCheckoutConfirmation(destination: String?, adults: Int, departure_date: LocalDate, outboundFlight: FlightLeg?, inboundFlight: FlightLeg?, isRoundTrip: Boolean) {
        if (isCarnivalEnabled() && initialized) {
            val attributes = AttributeMap()
            attributes.putString(CONFIRMATION_FLIGHT_DESTINATION, destination)
            attributes.putStringArray(CONFIRMATION_FLIGHT_AIRLINE, getAllAirlinesInTrip(outboundFlight, inboundFlight, isRoundTrip))
            attributes.putStringArray(CONFIRMATION_FLIGHT_FLIGHT_NUMBER, getAllFlightNumbersInTrip(outboundFlight, inboundFlight, isRoundTrip))
            attributes.putInt(CONFIRMATION_FLIGHT_NUMBER_OF_ADULTS, adults)
            attributes.putDate(CONFIRMATION_FLIGHT_DEPARTURE_DATE, departure_date.toDate())
            attributes.putString(CONFIRMATION_FLIGHT_LENGTH_OF_FLIGHT, calculateTotalTravelTime(outboundFlight, inboundFlight, isRoundTrip))
            setAttributes(attributes, CONFIRMATION_FLIGHT)
        }
    }

    fun trackHotelSearch(searchParams: HotelSearchParams) {
        if (isCarnivalEnabled() && initialized) {
            val attributes = AttributeMap()
            attributes.putString(SEARCH_HOTEL_DESTINATION, searchParams.suggestion.regionNames.fullName ?: searchParams.suggestion.regionNames.displayName)
            attributes.putInt(SEARCH_HOTEL_NUMBER_OF_ADULTS, searchParams.adults)
            attributes.putDate(SEARCH_HOTEL_CHECK_IN_DATE, searchParams.checkIn.toDate())
            attributes.putInt(SEARCH_HOTEL_LENGTH_OF_STAY, JodaUtils.daysBetween(searchParams.checkIn, searchParams.checkOut))
            setAttributes(attributes, SEARCH_HOTEL)
        }
    }

    fun trackHotelInfoSite(hotelOffersResponse: HotelOffersResponse, searchParams: HotelSearchParams) {
        if (isCarnivalEnabled() && initialized) {
            val attributes = AttributeMap()
            attributes.putString(PRODUCT_VIEW_HOTEL_DESTINATION, searchParams.suggestion.regionNames.fullName ?: searchParams.suggestion.regionNames.displayName)
            attributes.putString(PRODUCT_VIEW_HOTEL_HOTEL_NAME, hotelOffersResponse.hotelName)
            attributes.putInt(PRODUCT_VIEW_HOTEL_NUMBER_OF_ADULTS, searchParams.adults)
            attributes.putDate(PRODUCT_VIEW_HOTEL_CHECK_IN_DATE, searchParams.checkIn.toDate())
            attributes.putInt(PRODUCT_VIEW_HOTEL_LENGTH_OF_STAY, JodaUtils.daysBetween(searchParams.checkIn, searchParams.checkOut))
            setAttributes(attributes, PRODUCT_VIEW_HOTEL)
        }
    }

    fun trackHotelCheckoutStart(hotelCreateTripResponse: HotelCreateTripResponse, hotelSearchParams: HotelSearchParams) {
        if (isCarnivalEnabled() && initialized) {
            val attributes = AttributeMap()
            attributes.putString(CHECKOUT_START_HOTEL_DESTINATION, hotelSearchParams.suggestion.regionNames.fullName ?: hotelSearchParams.suggestion.regionNames.displayName)
            attributes.putString(CHECKOUT_START_HOTEL_HOTEL_NAME, hotelCreateTripResponse.newHotelProductResponse.getHotelName())
            attributes.putInt(CHECKOUT_START_HOTEL_NUMBER_OF_ADULTS, hotelSearchParams.adults)
            attributes.putDate(CHECKOUT_START_HOTEL_CHECK_IN_DATE, hotelSearchParams.checkIn.toDate())
            attributes.putInt(CHECKOUT_START_HOTEL_LENGTH_OF_STAY, JodaUtils.daysBetween(hotelSearchParams.checkIn, hotelSearchParams.checkOut))
            setAttributes(attributes, CHECKOUT_START_HOTEL)
        }
    }

    fun trackHotelConfirmation(hotelCheckoutResponse: HotelCheckoutResponse, hotelSearchParams: HotelSearchParams) {
        if (isCarnivalEnabled() && initialized) {
            val attributes = AttributeMap()
            attributes.putString(CONFIRMATION_HOTEL_DESTINATION, hotelSearchParams.suggestion.regionNames.fullName ?: hotelSearchParams.suggestion.regionNames.displayName)
            attributes.putString(CONFIRMATION_HOTEL_HOTEL_NAME, hotelCheckoutResponse.checkoutResponse.productResponse.hotelName)
            attributes.putInt(CONFIRMATION_HOTEL_NUMBER_OF_ADULTS, hotelSearchParams.adults)
            attributes.putDate(CONFIRMATION_HOTEL_CHECK_IN_DATE, hotelSearchParams.checkIn.toDate())
            attributes.putInt(CONFIRMATION_HOTEL_LENGTH_OF_STAY, JodaUtils.daysBetween(hotelSearchParams.checkIn, hotelSearchParams.checkOut))
            setAttributes(attributes, CONFIRMATION_HOTEL)
        }
    }

    fun trackLxConfirmation(activityTitle: String, activityDate: String) {
        if (isCarnivalEnabled() && initialized) {
            val attributes = AttributeMap()
            attributes.putString(CONFIRMATION_LX_ACTIVITY_NAME, activityTitle)
            attributes.putDate(CONFIRMATION_LX_DATE_OF_ACTIVITY, ApiDateUtils.yyyyMMddHHmmssToLocalDate(activityDate).toDate())
            setAttributes(attributes, CONFIRMATION_LX)
        }
    }

    fun trackPackagesConfirmation(packageParams: PackageSearchParams) {
        if (isCarnivalEnabled() && initialized) {
            val attributes = AttributeMap()
            attributes.putString(CONFIRMATION_PKG_DESTINATION, packageParams.destination?.regionNames?.fullName)
            attributes.putDate(CONFIRMATION_PKG_DEPARTURE_DATE, packageParams.startDate.toDate())
            attributes.putInt(CONFIRMATION_PKG_LENGTH_OF_STAY, Days.daysBetween(packageParams.startDate, packageParams.endDate).days)
            setAttributes(attributes, CONFIRMATION_PKG)
        }
    }

    fun trackRailConfirmation(railCheckoutResponse: RailCheckoutResponse) {
        if (isCarnivalEnabled() && initialized) {
            val attributes = AttributeMap()
            val railLeg = railCheckoutResponse.railDomainProduct.railOffer.railProductList.first()?.legOptionList?.first()
            attributes.putString(CONFIRMATION_RAIL_DESTINATION, railLeg?.arrivalStation?.stationDisplayName + ", " + railLeg?.arrivalStation?.stationCity)
            attributes.putDate(CONFIRMATION_RAIL_DEPARTURE_DATE, railLeg?.departureDateTime?.toDateTime()?.toDate())
            setAttributes(attributes, CONFIRMATION_RAIL)
        }
    }

    private fun isCarnivalEnabled(): Boolean = ProductFlavorFeatureConfiguration.getInstance().isCarnivalEnabled

    open fun setAttributes(attributes: AttributeMap, eventName: String) {
        Carnival.logEvent(eventName)
        Carnival.setAttributes(attributes, object : Carnival.AttributesHandler {
            override fun onSuccess() {
                Log.d(tag, "Carnival attributes sent successfully.")
                saveAttributes(attributes)
            }

            override fun onFailure(error: Error) {
                Log.d(tag, error.message)
            }
        })
    }

    open fun saveAttributes(attributes: AttributeMap) {
        persistenceProvider.put(attributes)
    }

    open fun setUserInfo(userId: String?, userEmail: String?) {
        if (isCarnivalEnabled() && initialized) {
            Carnival.setUserId(userId, object : CarnivalHandler<Void> {
                override fun onSuccess(value: Void) {
                    Log.d(tag, "Carnival UserId set successfully.")
                }

                override fun onFailure(error: Error) {
                    Log.d(tag, error.message)
                }
            })

            Carnival.setUserEmail(userEmail, object : CarnivalHandler<Void> {
                override fun onSuccess(value: Void) {
                    Log.d(tag, "Carnival User Email set successfully.")
                }

                override fun onFailure(error: Error) {
                    Log.d(tag, error.message)
                }
            })
        }
    }

    fun toggleNotifications(enableNotifications: Boolean) {
        if (isCarnivalEnabled() && initialized) {
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

    fun trackCarnivalPush(context: Context, deeplink: Uri, bundle: Bundle) {
        val marketingCode = bundle.getString(KEY_PAYLOAD_MARKETING)
        val marketingOLAcidFromUri = deeplink.getQueryParameter(olacid)
        if (!marketingCode.isNullOrEmpty()) {
            OmnitureTracking.trackCarnivalPushNotificationTap(marketingCode)
            return
        }
        if (marketingOLAcidFromUri.isNullOrEmpty()) {
            val brand = ProductFlavorFeatureConfiguration.getInstance().getPOSSpecificBrandName(context)
            val countryCode = PointOfSale.getPointOfSale().twoLetterCountryCode
            val defaultOLAcid = Phrase.from(context.resources, R.string.carnival_default_olacid_TEMPLATE).put("brand", brand).put("pos", countryCode).format().toString().toUpperCase()
            OmnitureTracking.trackCarnivalPushNotificationTap(defaultOLAcid)
        } else {
            OmnitureTracking.trackCarnivalPushNotificationTap(marketingOLAcidFromUri)
        }
    }

    fun createParameterizedDeeplinkWithStoredValues(data: Uri): Uri {
        var stringUri = data.toString()
        val listOfParamValuesToFill = mutableListOf<String>()

        data.queryParameterNames.forEach { param ->
            val paramValue = data.getQueryParameter(param)

            if (parameterizedPattern.matcher(paramValue).matches()) {
                listOfParamValuesToFill.add(paramValue)
            }
        }

        for (paramValue in listOfParamValuesToFill) {
            val storedCarnivalValue = CarnivalUtils.persistenceProvider.get(stripUnwantedCharacters(paramValue)).toString()

            if (storedCarnivalValue.isEmpty() || storedCarnivalValue == "null") {
                Log.d(tag, "Deeplink parameter was stored as NULL, safeguarding against bad deeplink.")
                return Uri.parse(appContext.getString(R.string.deeplink_home))
            } else {
                val dateValue = getCarnivalDate(storedCarnivalValue)

                stringUri = if (dateValue != null) {
                    stringUri.replace(paramValue, stripUnwantedCharacters(dateValue.toString()))
                } else {
                    stringUri.replace(paramValue, stripUnwantedCharacters(storedCarnivalValue))
                }
            }
        }
        return Uri.parse(stringUri)
    }

    private fun stripUnwantedCharacters(stringToStrip: String): String {
        return stringToStrip.replace(unwantedCharacters, "")
    }

    private fun getCarnivalDate(inDate: String): LocalDate? {
        return try {
            LocalDate.parse(inDate, DateTimeFormat.forPattern(carnivalDateFormat))
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    class CustomCarnivalListener : NotificationReceivedListener {
        override fun onNotificationReceived(context: Context, bundle: Bundle) {
            if (isNotificationFromCarnival(bundle)) {
                setupCarnivalConfig()
            }
        }

        private fun setupCarnivalConfig() {
            val notificationExtender = CarnivalNotificationExtender()
            val notificationConfig = NotificationConfig()
            notificationConfig.addNotificationExtender(notificationExtender)
            notificationConfig.setContentIntentBuilder(CarnivalContentIntentBuilder())
            Carnival.setNotificationConfig(notificationConfig)
        }

        fun isNotificationFromCarnival(bundle: Bundle): Boolean {
            return bundle.containsKey(KEY_NOTIFICATION_PROVIDER) && bundle.getString(KEY_NOTIFICATION_PROVIDER) == KEY_NOTIFICATION_PROVIDER_VALUE
        }
    }

    class CarnivalNotificationExtender : NotificationCompat.Extender {
        override fun extend(builder: NotificationCompat.Builder): NotificationCompat.Builder {
            val bundle = builder.extras
            builder.setContentTitle(bundle.getString(KEY_PAYLOAD_TITLE))
            builder.setContentText(bundle.getString(KEY_PAYLOAD_ALERT))
            builder.setSmallIcon(R.drawable.ic_stat)
            builder.setAutoCancel(true)
            return builder
        }
    }

    class CarnivalContentIntentBuilder : ContentIntentBuilder {

        override fun build(context: Context, bundle: Bundle?): PendingIntent? {
            return createPendingIntent(context, bundle)
        }

        private fun createPendingIntent(context: Context, bundle: Bundle?): PendingIntent? {
            val pendingIntent: PendingIntent?
            val deeplink = bundle?.getString(KEY_PAYLOAD_DEEPLINK)

            bundle?.putBoolean(KEY_NOTIFICATION_PROVIDER_VALUE, true)

            val intent = Intent()
                    .setAction(Intent.ACTION_VIEW)
                    .setData(android.net.Uri.parse(if (deeplink.isNullOrEmpty()) {
                        StringBuilder().append(Phrase.from(context, R.string.deeplink_all_brand_home_TEMPLATE)
                                .put("brand_scheme", BuildConfig.DEEPLINK_SCHEME)
                                .format()).toString()
                    } else {
                        deeplink
                    }))
                    .addFlags(Intent.FLAG_FROM_BACKGROUND)

            bundle?.let { intent.putExtras(it) }

            val stackBuilder = TaskStackBuilder.create(context).addNextIntent(intent)
            pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            return pendingIntent
        }
    }
}
