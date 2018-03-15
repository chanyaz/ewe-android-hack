package com.expedia.bookings.tracking

import android.app.Application
import android.os.Bundle
import com.expedia.bookings.data.FlightTrip
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.lx.LXSearchResponse
import com.expedia.bookings.data.lx.LxSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.user.User
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.extensions.safePrint
import com.expedia.bookings.extensions.safePutInt
import com.expedia.bookings.extensions.safePutString
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.tracking.flight.FlightSearchTrackingData
import com.expedia.bookings.tracking.hotel.HotelSearchTrackingData
import com.expedia.bookings.utils.CollectionUtils
import com.expedia.bookings.utils.Ui
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.mobiata.android.Log
import com.mobiata.android.time.util.JodaUtils
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import java.math.BigDecimal
import java.util.Collections
import java.util.Comparator
import java.util.Currency

class FacebookEvents {

    companion object {
        private const val TAG = "FacebookTracking"
        private const val FB_PURCHASE_VALUE = "fb_purchase_value"
        private const val FB_PURCHASE_CURRENCY = "fb_purchase_currency"
        private const val FB_ORDER_ID = "fb_order_id"
        private const val VALUE_TO_SUM = "_valueToSum"
        private const val LOWEST_SEARCH_VALUE = "LowestSearch_Value"
        private const val BOOKING_VALUE = "Booking_Value"
        private const val ACTIVITY_VALUE = "Activity_Value"
        private const val NUM_PEOPLE = "Num_People"
        private const val NUM_CHILDREN = "Number_Children"
        private const val FB_CHECKIN_DATE = "fb_checkin_date"
        private const val FB_CHECKOUT_DATE = "fb_checkout_date"
        private const val FB_NUM_ADULTS = "fb_num_adults"
        private const val FB_NUM_CHILDREN = "fb_num_children"
        private const val FB_DEPARTING_DATE = "fb_departing_departure_date"
        private const val FB_RETURNING_DATE = "fb_returning_departure_date"
        private const val FB_ORIGIN_AIRPORT = "fb_origin_airport"
        private const val FB_DESTINATION_AIRPORT = "fb_destination_airport"

        @JvmField var userStateManager: UserStateManager? = null
        @JvmField var facebookLogger: AppEventsLogger? = null

        @JvmStatic
        fun init(app: Application) {
            if (ProductFlavorFeatureConfiguration.getInstance().isFacebookTrackingEnabled) {
                userStateManager = Ui.getApplication(app).appComponent().userStateManager()
                facebookLogger = AppEventsLogger.newLogger(app)
                AppEventsLogger.activateApp(app)
            }
        }

        private fun track(event: String, parameters: Bundle) {
            val keys = parameters.keySet()

            if (keys.size > 10) {
                Log.e(TAG, "$event passing too many parameters, max 10, tried ${keys.size}")
            }

            val nullKeys = keys.filter { parameters.get(it) == null }
            if (nullKeys.isNotEmpty()) {
                Log.e(TAG, "$event null values in bundle: ${nullKeys.joinToString(", ")}")
            }

            val badKeys = keys.filter { parameters.get(it) !is String && parameters.get(it) !is Int }
            if (badKeys.isNotEmpty()) {
                Log.e(TAG, "$event values other than string or integer found: ${badKeys.joinToString(", ")}")
            }

            for (key in parameters.keySet()) {
                val value = parameters.get(key)
                Log.d(TAG, " $key  : ${value?.toString()}")
            }

            facebookLogger?.logEvent(event, parameters)
        }
    }

    fun trackHotelV2Search(trackingData: HotelSearchTrackingData) {
        val location = getLocation(trackingData.city ?: "", trackingData.stateProvinceCode ?: "", trackingData.countryCode ?: "")
        val parameters = Bundle()

        addCommonHotelDATParams(parameters, trackingData, location)
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, location.city)
        parameters.safePutString(FB_PURCHASE_VALUE, trackingData.lowestHotelTotalPrice)
        parameters.safePutString(FB_PURCHASE_CURRENCY, trackingData.hotels.firstOrNull()?.rateCurrencyCode)
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, getListOfTopHotelIds(trackingData.hotels))

        track(AppEventsConstants.EVENT_NAME_SEARCHED, parameters)
    }

    fun trackHotelV2InfoSite(searchParams: com.expedia.bookings.data.hotels.HotelSearchParams, hotelOffersResponse: HotelOffersResponse) {
        val location: Location = getLocation(hotelOffersResponse.hotelCity,
                hotelOffersResponse.hotelStateProvince,
                hotelOffersResponse.hotelCountry)

        val parameters = Bundle()
        val chargeableRate = hotelOffersResponse.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo

        addCommonHotelDATParams(parameters, searchParams, location)
        parameters.safePutString(FB_PURCHASE_VALUE, chargeableRate?.averageRate.toString())
        parameters.safePutString(FB_PURCHASE_CURRENCY, chargeableRate?.currencyCode)
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, hotelOffersResponse.hotelId)

        track(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters)
    }

    fun trackHotelV2Checkout(hotelProductResponse: HotelCreateTripResponse.HotelProductResponse, searchParams: com.expedia.bookings.data.hotels.HotelSearchParams) {
        val location: Location = getLocation(hotelProductResponse.hotelCity,
                hotelProductResponse.hotelStateProvince,
                hotelProductResponse.hotelCountry)

        val chargeableRate = hotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo
        val parameters = Bundle()
        addCommonHotelDATParams(parameters, searchParams, location)

        parameters.safePutString(FB_PURCHASE_VALUE, chargeableRate.displayTotalPrice.formattedMoney)
        parameters.safePutString(FB_PURCHASE_CURRENCY, chargeableRate.currencyCode)
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, hotelProductResponse.hotelId)

        track(AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT, parameters)
    }

    fun trackHotelV2Confirmation(hotelCheckoutResponse: HotelCheckoutResponse) {

        val location: Location = getLocation(hotelCheckoutResponse.checkoutResponse.productResponse.hotelCity,
                hotelCheckoutResponse.checkoutResponse.productResponse.hotelStateProvince,
                hotelCheckoutResponse.checkoutResponse.productResponse.hotelCountry)

        val bookingValue: String? = hotelCheckoutResponse.totalCharges

        val parameters = Bundle()
        addCommonHotelDATParams(parameters, hotelCheckoutResponse, location)

        parameters.safePutString(FB_ORDER_ID, hotelCheckoutResponse.checkoutResponse.bookingResponse.travelRecordLocator)
        parameters.safePutString(FB_PURCHASE_VALUE, bookingValue)
        parameters.safePutString(VALUE_TO_SUM, bookingValue)
        parameters.safePutString(FB_PURCHASE_CURRENCY, hotelCheckoutResponse.currencyCode)
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, hotelCheckoutResponse.checkoutResponse.productResponse.hotelId)
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_CURRENCY, hotelCheckoutResponse.currencyCode)

        facebookLogger?.logPurchase(BigDecimal(hotelCheckoutResponse.totalCharges), Currency.getInstance(hotelCheckoutResponse.currencyCode), parameters)
    }

    fun trackFlightV2Search(searchTrackingData: FlightSearchTrackingData) {
        val destinationAirport = searchTrackingData.arrivalAirport?.gaiaId
        val arrivalAirport = searchTrackingData.departureAirport?.gaiaId
        val parameters = Bundle()
        val lastFlightSegment = searchTrackingData.flightLegList.firstOrNull()?.flightSegments?.size?.minus(1)
        val arrivalAirportAddress = lastFlightSegment?.let { searchTrackingData.flightLegList.firstOrNull()?.flightSegments?.get(lastFlightSegment)?.arrivalAirportAddress }
        val lowestValue = searchTrackingData.flightLegList.firstOrNull()?.packageOfferModel?.price?.packageTotalPrice?.amount?.toString()

        addCommonFlightV2Params(parameters, searchTrackingData.arrivalAirport, searchTrackingData.departureAirport, searchTrackingData.departureDate,
                searchTrackingData.returnDate, searchTrackingData.guests, searchTrackingData.children.size)
        addArrivalAirportAddress(parameters, arrivalAirportAddress)
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, "$arrivalAirport - $destinationAirport")
        parameters.safePutString(LOWEST_SEARCH_VALUE, lowestValue)

        addCommonFlightDATParams(parameters, searchTrackingData)
        parameters.safePutString(FB_PURCHASE_VALUE, lowestValue)
        parameters.safePutString(FB_PURCHASE_CURRENCY, searchTrackingData.flightLegList.firstOrNull()?.packageOfferModel?.price?.packageTotalPrice?.currencyCode)

        track(AppEventsConstants.EVENT_NAME_SEARCHED, parameters)
    }

    fun trackFilteredFlightV2Search(
        flightSearchParams: com.expedia.bookings.data.flights.FlightSearchParams,
        flightLegList: List<FlightLeg>
    ) {
        val destinationAirport = flightSearchParams.arrivalAirport.gaiaId
        val arrivalAirport = flightSearchParams.departureAirport.gaiaId
        val parameters = Bundle()

        addCommonFlightDATParams(parameters, flightSearchParams)
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, "$arrivalAirport - $destinationAirport")
        parameters.safePutString(FB_PURCHASE_VALUE, calculateLowestRateFlightsV2(flightLegList))
        parameters.safePutString(FB_PURCHASE_CURRENCY, flightLegList.firstOrNull()?.packageOfferModel?.price?.packageTotalPrice?.currencyCode)

        track(AppEventsConstants.EVENT_NAME_SEARCHED, parameters)
    }

    fun trackFlightV2Detail(
        flightSearchParams: com.expedia.bookings.data.flights.FlightSearchParams,
        flightCreateTripResponse: FlightCreateTripResponse
    ) {
        val parameters = Bundle()
        val totalPrice = flightCreateTripResponse.details.offer.totalPrice

        addCommonFlightDATParams(parameters, flightSearchParams)
        parameters.safePutString(FB_PURCHASE_VALUE, totalPrice.amount.toString())
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, flightCreateTripResponse.details.legs.firstOrNull()?.segments?.firstOrNull()?.airlineCode)
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_CURRENCY, totalPrice.currencyCode)
        parameters.safePutString(FB_PURCHASE_CURRENCY, totalPrice.currencyCode)

        track(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters)
    }

    fun trackFlightV2Checkout(flightCreateTripResponse: FlightCreateTripResponse, flightSearchParams: com.expedia.bookings.data.flights.FlightSearchParams) {
        val parameters = Bundle()
        val currencyCode = flightCreateTripResponse.details.offer?.totalPrice?.currencyCode

        addCommonFlightDATParams(parameters, flightSearchParams)
        parameters.safePutString(FB_PURCHASE_VALUE, flightCreateTripResponse.details.offer?.totalPrice?.amount?.toString())
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, flightCreateTripResponse.details.legs.firstOrNull()?.segments?.firstOrNull()?.airlineCode)
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_CURRENCY, currencyCode)
        parameters.safePutString(FB_PURCHASE_CURRENCY, currencyCode)

        track(AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT, parameters)
    }

    fun trackFlightV2Confirmation(flightCheckoutResponse: FlightCheckoutResponse, flightSearchParams: com.expedia.bookings.data.flights.FlightSearchParams) {
        val flightTripDetails = flightCheckoutResponse.getFirstFlightTripDetails()
        val flightLeg = flightTripDetails.legs.firstOrNull()
        val airLineCode = flightLeg?.segments?.firstOrNull()?.airlineCode
        val parameters = Bundle()
        val totalCharges = flightCheckoutResponse.totalChargesPrice

        addCommonFlightDATParams(parameters, flightSearchParams)
        parameters.safePutString(FB_ORDER_ID, flightCheckoutResponse.orderId)
        parameters.safePutString(FB_PURCHASE_VALUE, totalCharges?.amount.toString())
        parameters.safePutString(VALUE_TO_SUM, totalCharges?.amount.toString())
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, airLineCode)
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_CURRENCY, totalCharges?.currencyCode)
        parameters.safePutString(FB_PURCHASE_CURRENCY, totalCharges?.currencyCode)

        facebookLogger?.logPurchase(totalCharges?.amount, Currency.getInstance(totalCharges?.currencyCode), parameters)
    }

    fun trackLXSearch(searchParams: LxSearchParams, lxSearchResponse: LXSearchResponse) {
        val parameters = Bundle()
        val startDate = searchParams.startDate

        addCommonLXParams(parameters, startDate, lxSearchResponse.regionId, lxSearchResponse.destination)
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, searchParams.location)

        if (CollectionUtils.isNotEmpty(lxSearchResponse.activities)) {
            parameters.safePutString(LOWEST_SEARCH_VALUE, lxSearchResponse.lowestPriceActivity
                    .price.getAmount().toString())
        }

        track(AppEventsConstants.EVENT_NAME_SEARCHED, parameters)
    }

    fun trackLXDetail(
        activityId: String,
        destination: String,
        startDate: LocalDate,
        regionId: String,
        currencyCode: String,
        activityValue: String
    ) {
        val parameters = Bundle()

        addCommonLXParams(parameters, startDate, regionId, destination)
        parameters.safePutString(ACTIVITY_VALUE, activityValue)
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_CURRENCY, currencyCode)
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, activityId)

        track(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters)
    }

    fun trackLXCheckout(
        activityId: String,
        lxActivityLocation: String,
        startDate: LocalDate,
        regionId: String,
        totalPrice: Money,
        ticketCount: Int,
        childTicketCount: Int
    ) {
        val parameters = Bundle()

        addCommonLXParams(parameters, startDate, regionId, lxActivityLocation)
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_CURRENCY, totalPrice.currencyCode)
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, activityId)
        parameters.safePutString(BOOKING_VALUE, totalPrice.getAmount().toString())
        parameters.safePutInt(NUM_PEOPLE, ticketCount)
        parameters.safePutInt(NUM_CHILDREN, childTicketCount)

        track(AppEventsConstants.EVENT_NAME_ADDED_TO_CART, parameters)
    }

    fun trackLXConfirmation(
        activityId: String,
        lxActivityLocation: String,
        startDate: LocalDate,
        regionId: String,
        totalPrice: Money,
        ticketCount: Int,
        childTicketCount: Int
    ) {
        val parameters = Bundle()

        addCommonLXParams(parameters, startDate, regionId, lxActivityLocation)
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_CURRENCY, totalPrice.currencyCode)
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, activityId)
        parameters.safePutString(BOOKING_VALUE, totalPrice.getAmount().toString())
        parameters.safePutInt(NUM_PEOPLE, ticketCount)
        parameters.safePutInt(NUM_CHILDREN, childTicketCount)
        facebookLogger?.logPurchase(totalPrice.getAmount(), Currency.getInstance(totalPrice.currencyCode), parameters)
    }

    private fun getBookingWindow(time: LocalDate?): Int {
        if (time != null) {
            return JodaUtils.daysBetween(LocalDate.now(), time)
        } else {
            return 0
        }
    }

    fun calculateLowestRateFlights(flightTrips: List<FlightTrip>): String {
        if (flightTrips.isEmpty()) {
            return ""
        }
        var minAmount = flightTrips.first().totalPrice?.getAmount()
        for (trip in flightTrips) {
            val amount = trip.totalPrice.getAmount()
            if (amount < minAmount) {
                minAmount = amount
            }
        }
        return minAmount.toString()
    }

    private fun calculateLowestRateFlightsV2(flightLegList: List<FlightLeg>): String? {
        if (flightLegList.isEmpty()) {
            return ""
        }
        Collections.sort(flightLegList, priceComparator)
        return flightLegList.firstOrNull()?.packageOfferModel?.price?.packageTotalPrice?.amount?.toString()
    }

    private fun getLoyaltyTier(user: User?): String {
        val loyaltyTierNotAvailable = "N/A"
        if (user?.loyaltyMembershipInformation?.loyaltyMembershipTier != LoyaltyMembershipTier.NONE) {
            return user?.loyaltyMembershipInformation?.loyaltyMembershipTier?.toApiValue() ?: loyaltyTierNotAvailable
        }
        return loyaltyTierNotAvailable
    }

    private fun addCommonHotelDATParams(parameters: Bundle, searchParams: com.expedia.bookings.data.hotels.HotelSearchParams, location: Location) {
        addGenericHotelDATParams(parameters)
        addCommonHotelDATRegionParams(parameters, location)
        addCommonHotelDATSearchParams(parameters, searchParams.checkIn, searchParams.checkOut, searchParams.adults, searchParams.children.size)
    }

    private fun addCommonHotelDATParams(parameters: Bundle, searchParams: HotelSearchTrackingData, location: Location) {
        addGenericHotelDATParams(parameters)
        addCommonHotelDATRegionParams(parameters, location)
        addCommonHotelDATSearchParams(parameters, searchParams.checkInDate, searchParams.checkoutDate, searchParams.numberOfAdults, searchParams.numberOfChildren)
    }

    private fun addCommonHotelDATParams(parameters: Bundle, searchParams: HotelCheckoutResponse, location: Location) {
        val checkInDate = LocalDate.parse(searchParams.checkoutResponse.productResponse.checkInDate)
        val checkOutDate = LocalDate.parse(searchParams.checkoutResponse.productResponse.checkOutDate)

        addGenericHotelDATParams(parameters)
        addCommonHotelDATRegionParams(parameters, location)
        addCommonHotelDATSearchParams(parameters, checkInDate, checkOutDate)
    }

    private fun addGenericHotelDATParams(parameters: Bundle) {
        parameters.safePutString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "[\"product\",\"hotel\"]")
        parameters.safePutString("LOB", "Hotel")
    }

    private fun addCommonHotelDATSearchParams(
        parameters: Bundle,
        checkIn: LocalDate?,
        checkOut: LocalDate?,
        adults: Int,
        numberOfChildren: Int
    ) {
        val dtf = ISODateTimeFormat.date()

        parameters.safePutString(FB_CHECKIN_DATE, dtf.safePrint(checkIn))
        parameters.safePutString(FB_CHECKOUT_DATE, dtf.safePrint(checkOut))
        parameters.safePutInt(FB_NUM_ADULTS, adults)
        parameters.safePutInt(FB_NUM_CHILDREN, numberOfChildren)
    }

    private fun addCommonHotelDATSearchParams(parameters: Bundle, checkIn: LocalDate, checkOut: LocalDate) {
        val dtf = ISODateTimeFormat.date()

        parameters.safePutString(FB_CHECKIN_DATE, dtf.safePrint(checkIn))
        parameters.safePutString(FB_CHECKOUT_DATE, dtf.safePrint(checkOut))
    }

    private fun addCommonHotelDATRegionParams(parameters: Bundle, location: Location) {
        parameters.safePutString("fb_city", location.city)
        parameters.safePutString("fb_region", location.stateCode)
        parameters.safePutString("fb_country", location.countryCode)
    }

    private fun addCommonFlightDATParams(parameters: Bundle, flightParams: FlightSearchTrackingData) {
        val dtf = ISODateTimeFormat.date()

        parameters.safePutString("fb_content_type", "[\"product\",\"flight\"]")
        parameters.safePutString("LOB", "Flight")
        parameters.safePutString(FB_DEPARTING_DATE, dtf.safePrint(flightParams.departureDate))
        parameters.safePutString(FB_RETURNING_DATE, dtf.safePrint(flightParams.returnDate))
        parameters.safePutString(FB_ORIGIN_AIRPORT, flightParams.departureAirport?.hierarchyInfo?.airport?.airportCode ?: "")
        parameters.safePutString(FB_DESTINATION_AIRPORT, flightParams.arrivalAirport?.hierarchyInfo?.airport?.airportCode ?: "")
        parameters.safePutInt(FB_NUM_ADULTS, flightParams.adults)
        parameters.safePutInt(FB_NUM_CHILDREN, flightParams.children.size)
    }

    private fun addCommonFlightDATParams(parameters: Bundle, flightParams: com.expedia.bookings.data.flights.FlightSearchParams) {
        val dtf = ISODateTimeFormat.date()

        parameters.safePutString("fb_content_type", "[\"product\",\"flight\"]")
        parameters.safePutString("LOB", "Flight")
        parameters.safePutString(FB_DEPARTING_DATE, dtf.safePrint(flightParams.departureDate))
        parameters.safePutString(FB_RETURNING_DATE, dtf.safePrint(flightParams.returnDate))
        parameters.safePutString(FB_ORIGIN_AIRPORT, flightParams.departureAirport.hierarchyInfo?.airport?.airportCode ?: "")
        parameters.safePutString(FB_DESTINATION_AIRPORT, flightParams.arrivalAirport.hierarchyInfo?.airport?.airportCode ?: "")
        parameters.safePutInt(FB_NUM_ADULTS, flightParams.adults)
        parameters.safePutInt(FB_NUM_CHILDREN, flightParams.children.size)
    }

    private fun addCommonFlightV2Params(
        parameters: Bundle,
        arrivalAirport: SuggestionV4?,
        departureAirport: SuggestionV4?,
        departureDate: LocalDate?,
        returnDate: LocalDate?,
        guests: Int,
        childrenNo: Int
    ) {
        val dtf = ISODateTimeFormat.date()
        val destinationId = arrivalAirport?.gaiaId ?: ""
        parameters.safePutString("region_id", destinationId)
        parameters.safePutString("destination_name", destinationId)
        parameters.safePutString("LOB", "Flight")
        parameters.safePutString("Start_Date", dtf.safePrint(departureDate))
        parameters.safePutString("End_Date", dtf.safePrint(returnDate))

        parameters.safePutInt("Booking_Window", getBookingWindow(departureDate))
        parameters.safePutString("FlightOrigin_AirportCode", departureAirport?.gaiaId)
        parameters.safePutString("FlightDestination_AirportCode", destinationId)
        parameters.safePutInt(NUM_PEOPLE, guests)
        parameters.safePutInt(NUM_CHILDREN, childrenNo)
    }

    private fun addArrivalAirportAddress(parameters: Bundle, arrivalAirportAddress: FlightLeg.FlightSegment.AirportAddress?) {
        if (arrivalAirportAddress != null) {
            parameters.safePutString("destination_city", arrivalAirportAddress.city)
            parameters.safePutString("destination_state", arrivalAirportAddress.state)
            parameters.safePutString("destination_country", arrivalAirportAddress.country)
        }
    }

    private fun addCommonLXParams(parameters: Bundle, startDate: LocalDate, regionId: String?, location: String?) {
        val dtf = ISODateTimeFormat.date()
        parameters.safePutString("LOB", "Activity")
        parameters.safePutString("region_id", regionId)
        parameters.safePutString("destination_name", location)
        parameters.safePutString("Start_Date", dtf.safePrint(startDate))
        parameters.safePutInt("Booking_Window", getBookingWindow(startDate))

        if (userStateManager != null) {
            parameters.safePutInt("Logged_in_Status", encodeBoolean(userStateManager?.isUserAuthenticated() ?: false))
        }

        val user = userStateManager?.userSource?.user

        parameters.safePutString("Reward_Status", getLoyaltyTier(user))
        parameters.safePutString("POS", PointOfSale.getPointOfSale().twoLetterCountryCode)
    }

    private fun getListOfTopHotelIds(hotels: List<Hotel>, top: Int = 5): String {
        var idList = "["
        val topHotels = hotels.take(top)

        for (hotel in topHotels) {
            idList += "\\\"" + hotel.hotelId + "\\\""

            if (hotel.hotelId != topHotels.last().hotelId) {
                idList += ","
            }
        }

        idList += "]"

        if (idList.count() > 100) {
            return getListOfTopHotelIds(hotels, top - 1)
        }

        return idList
    }

    private fun encodeBoolean(boolean: Boolean): Int {
        return if (boolean) 1 else 0
    }

    private fun getLocation(hotelCity: String, hotelStateProvince: String, hotelCountry: String): Location {
        val location = Location()
        location.city = hotelCity
        location.stateCode = hotelStateProvince
        location.countryCode = hotelCountry
        return location
    }

    private val priceComparator = Comparator<FlightLeg> { flightLeg1, flightLeg2 ->
        flightLeg1.packageOfferModel.price.packageTotalPrice.amount.compareTo(flightLeg2.packageOfferModel.price.packageTotalPrice.amount)
    }
}
