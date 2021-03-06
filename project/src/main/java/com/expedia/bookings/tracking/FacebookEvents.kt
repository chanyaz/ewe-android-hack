package com.expedia.bookings.tracking

import android.content.Context
import android.os.Bundle
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightSearch
import com.expedia.bookings.data.FlightSearchParams
import com.expedia.bookings.data.FlightTrip
import com.expedia.bookings.data.HotelSearch
import com.expedia.bookings.data.HotelSearchParams
import com.expedia.bookings.data.HotelSearchResponse
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.Property
import com.expedia.bookings.data.Rate
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TripBucketItemFlight
import com.expedia.bookings.data.TripBucketItemHotel
import com.expedia.bookings.data.User
import com.expedia.bookings.data.cars.CarCheckoutResponse
import com.expedia.bookings.data.cars.CarLocation
import com.expedia.bookings.data.cars.CarSearch
import com.expedia.bookings.data.cars.CarSearchParams
import com.expedia.bookings.data.cars.CreateTripCarOffer
import com.expedia.bookings.data.cars.RateTerm
import com.expedia.bookings.data.cars.SearchCarOffer
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.lx.LXSearchParams
import com.expedia.bookings.data.lx.LXSearchResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.utils.CollectionUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.mobiata.android.Log
import com.mobiata.android.time.util.JodaUtils
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import java.math.BigDecimal
import java.util.Currency

private val TAG = "FacebookTracking"
@JvmField var facebookContext: Context? = null
@JvmField var facebookLogger: AppEventsLogger? = null

private fun track(event: String, parameters: Bundle) {
    val keys = parameters.keySet()

    if (keys.size > 10) {
        Log.e(TAG, "${event} passing too many parameters, max 10, tried ${keys.size}")
    }

    val nullKeys = keys.filter { parameters.get(it) == null }
    if (nullKeys.size > 0) {
        Log.e(TAG, "${event} null values in bundle: ${nullKeys.joinToString(", ")}")
    }

    val badKeys = keys.filter { parameters.get(it) !is String && parameters.get(it) !is Int }
    if (badKeys.size > 0) {
        Log.e(TAG, "${event} values other than string or integer found: ${badKeys.joinToString(", ")}")
    }

    for (key in parameters.keySet()) {
        val value = parameters.get(key);
        Log.d(TAG, " $key  : ${value?.toString()}");
    }

    facebookLogger?.logEvent(event, parameters)
}

class FacebookEvents() {
    companion object {
        public fun activateAppIfEnabledInConfig(context: Context) {
            if (ProductFlavorFeatureConfiguration.getInstance().isFacebookTrackingEnabled()) {
                AppEventsLogger.activateApp(context)
            }
        }

        public fun deactivateAppIfEnabledInConfig(context: Context) {
            if (ProductFlavorFeatureConfiguration.getInstance().isFacebookTrackingEnabled()) {
                AppEventsLogger.deactivateApp(context)
            }
        }
    }

    fun trackHotelSearch(search: HotelSearch) {
        val searchParams: HotelSearchParams? = search.getSearchParams()
        val response: HotelSearchResponse? = search.getSearchResponse()
        val location: Location? = response?.getProperties()?.firstOrNull()?.getLocation()
        val properties: List<Property>? = response?.getFilteredAndSortedProperties(searchParams)

        if (searchParams != null && location != null && properties != null) {
            val parameters = Bundle()
            addCommonHotelParams(parameters, searchParams, location)
            parameters.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, location.getCity() ?: "")
            parameters.putString("LowestSearch_Value", calculateLowestRateHotels(properties)?.getDisplayPrice()?.getAmount()?.toString() ?: "")
            parameters.putInt("Num_Rooms", 1)

            track(AppEventsConstants.EVENT_NAME_SEARCHED, parameters)
        }
    }

    fun trackHotelV2Search(searchParams: com.expedia.bookings.data.hotels.HotelSearchParams, searchResponse: com.expedia.bookings.data.hotels.HotelSearchResponse) {

        val location: Location = getLocation(searchResponse.hotelList.get(0).city,
                searchResponse.hotelList.get(0).stateProvinceCode,
                searchResponse.hotelList.get(0).countryCode)
        val hotelList: List<Hotel> = searchResponse.hotelList
        val parameters = Bundle()
        addCommonHotelV2Params(parameters, searchParams, searchResponse.searchRegionId ?: "", location)
        parameters.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, location.city ?: "")
        parameters.putString("LowestSearch_Value", calculateLowestRateV2Hotels(hotelList)?.displayTotalPrice?.getAmount()?.toString() ?: "")
        parameters.putInt("Num_Rooms", 1)

        track(AppEventsConstants.EVENT_NAME_SEARCHED, parameters)
    }

    fun trackHotelInfoSite(search: HotelSearch) {
        val searchParams: HotelSearchParams? = search.getSearchParams()
        val location: Location? = search.getSearchResponse()?.getProperties()?.firstOrNull()?.getLocation()
        val selectedProperty: Property? = search.getSelectedProperty()

        if (searchParams != null && location != null && selectedProperty != null) {
            val parameters = Bundle()
            addCommonHotelParams(parameters, searchParams, location)
            parameters.putString("Room_Value", getLowestRate(selectedProperty)?.getDisplayPrice()?.getAmount()?.toString() ?: "")
            parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, getLowestRate(selectedProperty)?.getDisplayPrice()?.currencyCode ?: "")
            parameters.putInt("Num_Rooms", 1)
            parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, selectedProperty.getPropertyId() ?: "")
            parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")

            track(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters)
        }
    }

    fun trackHotelV2InfoSite(searchParams: com.expedia.bookings.data.hotels.HotelSearchParams, hotelOffersResponse: HotelOffersResponse) {
        val location: Location = getLocation(hotelOffersResponse.hotelCity,
                hotelOffersResponse.hotelStateProvince,
                hotelOffersResponse.hotelCountry)

        val parameters = Bundle()
        addCommonHotelV2Params(parameters, searchParams, hotelOffersResponse.locationId ?: "", location)

        val dailyPrice: String? = hotelOffersResponse.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo?.averageRate.toString()
        val currencyCode: String? = hotelOffersResponse.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo?.currencyCode

        parameters.putString("Room_Value", dailyPrice ?: "")
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currencyCode ?: "")
        parameters.putInt("Num_Rooms", 1)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, hotelOffersResponse.locationId)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")

        track(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters)

    }

    fun trackHotelCheckout(hotel: TripBucketItemHotel, rate: Rate) {
        val searchParams: HotelSearchParams? = hotel.getHotelSearch()?.getSearchParams()
        val property: Property? = hotel.getHotelSearch()?.getSelectedProperty()
        val location: Location? = property?.getLocation()
        val bookingValue: String? = rate.getTotalAmountAfterTax()?.getAmount().toString()

        if (searchParams != null && property != null && location != null) {
            val parameters = Bundle()
            addCommonHotelParams(parameters, searchParams, location)
            parameters.putInt("Num_Rooms", 1)
            parameters.putString("Booking_Value", bookingValue ?: "")
            parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, property.getPropertyId() ?: "")
            parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, getLowestRate(property)?.getDisplayPrice()?.currencyCode ?: "")
            parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")

            track(AppEventsConstants.EVENT_NAME_ADDED_TO_CART, parameters)
        }
    }

    fun trackHotelV2Checkout(hotelProductResponse: HotelCreateTripResponse.HotelProductResponse, searchParams: com.expedia.bookings.data.hotels.HotelSearchParams) {
        val location: Location = getLocation(hotelProductResponse.hotelCity,
                hotelProductResponse.hotelStateProvince,
                hotelProductResponse.hotelCountry)

        val bookingValue: String? = hotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.displayTotalPrice.formattedMoney
        val currencyCode: String? = hotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.currencyCode

        val parameters = Bundle()
        addCommonHotelV2Params(parameters, searchParams, hotelProductResponse.regionId ?: "", location)
        parameters.putInt("Num_Rooms", 1)
        parameters.putString("Booking_Value", bookingValue ?: "")
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, hotelProductResponse.hotelId ?: "")
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currencyCode ?: "")
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")

        track(AppEventsConstants.EVENT_NAME_ADDED_TO_CART, parameters)

    }

    fun trackHotelConfirmation(hotel: TripBucketItemHotel, rate: Rate) {
        val searchParams: HotelSearchParams? = hotel.getHotelSearch()?.getSearchParams()
        val property: Property? = hotel.getHotelSearch()?.getSelectedProperty()
        val location: Location? = property?.getLocation()
        val bookingValue: String? = rate.getTotalAmountAfterTax()?.getAmount().toString()

        if (searchParams != null && property != null && location != null) {
            val parameters = Bundle()
            addCommonHotelParams(parameters, searchParams, location)
            parameters.putString("Booking_Value", bookingValue ?: "")
            parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, property.getPropertyId() ?: "")
            parameters.putInt("Num_Rooms", 1)
            parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, getLowestRate(property)?.getDisplayPrice()?.currencyCode ?: "")
            parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")
            facebookLogger?.logPurchase(rate.getTotalAmountAfterTax()?.amount ?: BigDecimal(0), Currency.getInstance(rate.getTotalAmountAfterTax()?.currencyCode));
        }
    }

    fun trackHotelV2Confirmation(hotelCheckoutResponse: HotelCheckoutResponse) {

        val location: Location = getLocation(hotelCheckoutResponse.checkoutResponse.productResponse.hotelCity,
                hotelCheckoutResponse.checkoutResponse.productResponse.hotelStateProvince,
                hotelCheckoutResponse.checkoutResponse.productResponse.hotelCountry)

        val bookingValue: String? = hotelCheckoutResponse.totalCharges

        val parameters = Bundle()
        addCommonHotelV2Params(parameters, hotelCheckoutResponse, location)
        parameters.putString("Booking_Value", bookingValue ?: "")
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, hotelCheckoutResponse.checkoutResponse.productResponse.hotelId ?: "")
        parameters.putInt("Num_Rooms", 1)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, hotelCheckoutResponse.currencyCode ?: "")
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")
        facebookLogger?.logPurchase(BigDecimal(hotelCheckoutResponse.totalCharges), Currency.getInstance(hotelCheckoutResponse.currencyCode));
        track(AppEventsConstants.EVENT_NAME_PURCHASED, parameters)
    }

    fun trackFlightSearch(search: FlightSearch) {
        val searchParams = search.getSearchParams()
        val location = searchParams.getArrivalLocation()
        val destinationAirport = searchParams.getArrivalLocation().getDestinationId()
        val arrivalAirport = searchParams.getDepartureLocation().getDestinationId()
        val parameters = Bundle()
        addCommonFlightParams(parameters, searchParams, location)
        parameters.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, "$arrivalAirport - $destinationAirport")
        parameters.putString("LowestSearch_Value", calculateLowestRateFlights(search.getSearchResponse().getTrips()))

        track(AppEventsConstants.EVENT_NAME_SEARCHED, parameters)
    }

    fun trackFilteredFlightSearch(search: FlightSearch, legNumber: Int) {
        val searchParams = search.getSearchParams()
        val location = searchParams.getArrivalLocation()
        val destinationAirport = searchParams.getArrivalLocation().getDestinationId()
        val arrivalAirport = searchParams.getDepartureLocation().getDestinationId()
        val parameters = Bundle()
        val trips = search.FlightTripQuery(legNumber).getTrips();
        addCommonFlightParams(parameters, searchParams, location)
        parameters.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, "$arrivalAirport - $destinationAirport")
        parameters.putString("LowestSearch_Value", calculateLowestRateFlights(trips))

        track(AppEventsConstants.EVENT_NAME_SEARCHED, parameters)
    }

    fun trackFlightDetail(search: FlightSearch) {
        val searchParams = search.getSearchParams()
        val location = searchParams.getArrivalLocation()
        val flightTrip = search.getSelectedFlightTrip()
        val money = flightTrip.getTotalFare()

        val parameters = Bundle()
        addCommonFlightParams(parameters, searchParams, location)
        parameters.putString("Flight_Value", money.getAmount().toString())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, flightTrip.getLegs().get(0).getFirstAirlineCode())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, money.getCurrency())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")

        track(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters)
    }

    fun trackFlightCheckout(flight: TripBucketItemFlight) {
        val searchParams = flight.getFlightSearchParams()
        val location = searchParams.getArrivalLocation()
        val flightTrip = flight.getFlightTrip()
        val money = flightTrip.getTotalFare()

        val parameters = Bundle()
        addCommonFlightParams(parameters, searchParams, location)
        parameters.putString("Booking_Value", money.getAmount().toString())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, flightTrip.getLegs().get(0).getFirstAirlineCode())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, money.getCurrency())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")

        track(AppEventsConstants.EVENT_NAME_ADDED_TO_CART, parameters)
    }

    fun trackFlightConfirmation(flight: TripBucketItemFlight) {
        val searchParams = flight.getFlightSearchParams()
        val location = searchParams.getArrivalLocation()
        val flightTrip = flight.getFlightTrip()
        val money = flightTrip.getTotalFare()

        val parameters = Bundle()
        addCommonFlightParams(parameters, searchParams, location)
        parameters.putString("Booking_Value", money.getAmount().toString())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, flightTrip.getLegs().get(0).getFirstAirlineCode())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, money.getCurrency())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")
        facebookLogger?.logPurchase(money.amount, Currency.getInstance(money.currencyCode));
        track(AppEventsConstants.EVENT_NAME_PURCHASED, parameters)
    }

    fun trackCarSearch(search: CarSearchParams, carSearch: CarSearch) {
        val searchCarOffer = carSearch.getLowestTotalPriceOffer()
        val startDate = search.startDateTime.toLocalDate()
        val endDate = search.endDateTime.toLocalDate()
        val location = searchCarOffer.pickUpLocation
        val originDescription = search.originDescription
        val parameters = Bundle()

        addCommonCarParams(parameters, startDate, endDate, location)

        if (Strings.isNotEmpty(originDescription)) {
            parameters.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, originDescription)
            parameters.putString("Pickup_Location", originDescription)
            parameters.putString("Dropoff_Location", originDescription)
        }
        parameters.putString("LowestSearch_Value", searchCarOffer.fare.total.getAmount().toString())

        track(AppEventsConstants.EVENT_NAME_SEARCHED, parameters)
    }

    fun trackCarDetail(search: CarSearchParams, searchCarOffer: SearchCarOffer) {
        var parameters = Bundle()
        val startDate = search.startDateTime.toLocalDate()
        val endDate = search.endDateTime.toLocalDate()
        val location = searchCarOffer.pickUpLocation

        addCommonCarParams(parameters, startDate, endDate, location)
        val searchCarFare = searchCarOffer.fare
        parameters.putString("Car_Value", if (searchCarFare.rateTerm.equals(RateTerm.UNKNOWN))
            searchCarFare.total.getAmount().toString() else searchCarFare.rate.getAmount().toString())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, searchCarFare.rate.getCurrency())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")

        track(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters)
    }

    fun trackCarCheckout(offer: CreateTripCarOffer) {
        var parameters = Bundle()
        val startDate = offer.getPickupTime().toLocalDate()
        val endDate = offer.getDropOffTime().toLocalDate()
        val location = offer.pickUpLocation

        addCommonCarParams(parameters, startDate, endDate, location)
        parameters.putString("Booking_Value", offer.detailedFare.grandTotal.getAmount().toString())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, offer.detailedFare.grandTotal.currencyCode)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")

        track(AppEventsConstants.EVENT_NAME_ADDED_TO_CART, parameters)
    }

    fun trackCarConfirmation(offer: CarCheckoutResponse) {
        var parameters = Bundle()
        val carOffer = offer.newCarProduct
        val startDate = carOffer.getPickupTime().toLocalDate()
        val endDate = carOffer.getDropOffTime().toLocalDate()
        val location = carOffer.pickUpLocation
        val grandTotal = carOffer.detailedFare.grandTotal

        addCommonCarParams(parameters, startDate, endDate, location)
        parameters.putString("Booking_Value", grandTotal.getAmount().toString())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, grandTotal.currencyCode)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")
        facebookLogger?.logPurchase(grandTotal.getAmount(), Currency.getInstance(grandTotal.currencyCode));
        track(AppEventsConstants.EVENT_NAME_PURCHASED, parameters)
    }

    fun trackLXSearch(searchParams: LXSearchParams, lxSearchResponse: LXSearchResponse) {
        var parameters = Bundle()
        val startDate = searchParams.startDate

        addCommonLXParams(parameters, startDate, lxSearchResponse.regionId, lxSearchResponse.destination)
        parameters.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, searchParams.location ?: "")

        if (CollectionUtils.isNotEmpty(lxSearchResponse.activities)) {
            parameters.putString("LowestSearch_Value", lxSearchResponse.getLowestPriceActivity()
                    .price.getAmount().toString())
        }

        track(AppEventsConstants.EVENT_NAME_SEARCHED, parameters)
    }

    fun trackLXDetail(activityId: String, destination: String, startDate: LocalDate, regionId: String,
                      currencyCode: String, activityValue: String) {
        var parameters = Bundle()

        addCommonLXParams(parameters, startDate, regionId, destination)
        parameters.putString("Activity_Value", activityValue)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currencyCode)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, activityId)

        track(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters)
    }

    fun trackLXCheckout(activityId: String, lxActivityLocation: String, startDate: LocalDate, regionId: String,
                        totalPrice: Money, ticketCount: Int, childTicketCount: Int) {
        var parameters = Bundle()

        addCommonLXParams(parameters, startDate, regionId, lxActivityLocation)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, totalPrice.currencyCode)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, activityId)
        parameters.putString("Booking_Value", totalPrice.getAmount().toString())
        parameters.putInt("Num_People", ticketCount)
        parameters.putInt("Number_Children", childTicketCount)

        track(AppEventsConstants.EVENT_NAME_ADDED_TO_CART, parameters)
    }

    fun trackLXConfirmation(activityId: String, lxActivityLocation: String, startDate: LocalDate, regionId: String,
                            totalPrice: Money, ticketCount: Int, childTicketCount: Int) {
        var parameters = Bundle()

        addCommonLXParams(parameters, startDate, regionId, lxActivityLocation)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, totalPrice.currencyCode)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, activityId)
        parameters.putString("Booking_Value", totalPrice.getAmount().toString())
        parameters.putInt("Num_People", ticketCount)
        parameters.putInt("Number_Children", childTicketCount)
        facebookLogger?.logPurchase(totalPrice.getAmount(), Currency.getInstance(totalPrice.currencyCode));
        track(AppEventsConstants.EVENT_NAME_PURCHASED, parameters)
    }

    private fun getBookingWindow(time: LocalDate): Int {
        return JodaUtils.daysBetween(LocalDate.now(), time)
    }

    private fun calculateLowestRateHotels(properties: List<Property>): Rate? {
        if (properties.size == 0) return null

        var minPropertyRate = properties.get(0).getLowestRate()

        for (property in properties) {
            var propertyRate = property.getLowestRate()
            if (propertyRate == null || minPropertyRate == null)
                continue
            else if (propertyRate.getDisplayPrice().getAmount() < minPropertyRate.getDisplayPrice().getAmount()) {
                minPropertyRate = propertyRate
            }
        }
        return minPropertyRate
    }

    private fun calculateLowestRateV2Hotels(properties: List<Hotel>): HotelRate? {
        if (properties.size == 0) return null

        var minPropertyRate = properties.get(0).lowRateInfo

        for (property in properties) {
            var propertyRate = property.lowRateInfo
            if (propertyRate == null || minPropertyRate == null)
                continue
            else if (propertyRate.displayTotalPrice.getAmount() < minPropertyRate.displayTotalPrice.getAmount()) {
                minPropertyRate = propertyRate
            }
        }
        return minPropertyRate
    }

    public fun calculateLowestRateFlights(flightTrips: List<FlightTrip>): String {
        if (flightTrips.size == 0) {
            return "";
        }
        var minAmount = flightTrips.get(0).getTotalFare().getAmount()
        for (trip in flightTrips) {
            var amount = trip.getTotalFare().getAmount()
            if (amount < minAmount) {
                minAmount = amount
            }
        }
        return minAmount.toString()
    }

    private fun getLoyaltyTier(user: User?): String {
        var loyaltyTier = "N/A"
        if (user != null && user.getPrimaryTraveler().getLoyaltyMembershipTier() != Traveler.LoyaltyMembershipTier.NONE) {
            loyaltyTier = Db.getUser().getPrimaryTraveler().getLoyaltyMembershipTier().name
        }
        return loyaltyTier
    }

    private fun addCommonHotelParams(parameters: Bundle, searchParams: HotelSearchParams, location: Location) {
        val dtf = ISODateTimeFormat.date()
        parameters.putString("LOB", "Hotel")
        val regionId = searchParams.getRegionId()
        val formattedAddressCityState = StrUtils.formatAddressCityState(location) ?: ""
        parameters.putString("region_id", regionId ?: "")
        addCommonLocationEvents(parameters, location)

        parameters.putString("destination_name", formattedAddressCityState)
        parameters.putString("Checkin_Date", dtf.print(searchParams.getCheckInDate()))
        parameters.putString("Checkout_Date", dtf.print(searchParams.getCheckOutDate()))
        parameters.putInt("Booking_Window", getBookingWindow(searchParams.getCheckInDate()))
        parameters.putInt("Num_People", searchParams.getNumTravelers())
        parameters.putInt("Number_Children", searchParams.getNumChildren())
        parameters.putInt("Number_Nights", searchParams.getStayDuration())
        if (facebookContext != null) {
            parameters.putInt("Logged_in_Status", encodeBoolean(User.isLoggedIn(facebookContext)))
        }
        parameters.putString("Reward_Status", getLoyaltyTier(Db.getUser()))
        parameters.putString("POS", PointOfSale.getPointOfSale().getTwoLetterCountryCode())
    }

    private fun addCommonHotelV2Params(parameters: Bundle, searchParams: com.expedia.bookings.data.hotels.HotelSearchParams, regionId: String, location: Location) {
        val dtf = ISODateTimeFormat.date()
        parameters.putString("LOB", "Hotel")
        val formattedAddressCityState = StrUtils.formatAddressCityState(location) ?: ""
        val numOfNight = JodaUtils.daysBetween(searchParams.checkIn, searchParams.checkOut)
        parameters.putString("region_id", regionId)
        addCommonLocationEvents(parameters, location)

        parameters.putString("destination_name", formattedAddressCityState)
        parameters.putString("Checkin_Date", dtf.print(searchParams.checkIn))
        parameters.putString("Checkout_Date", dtf.print(searchParams.checkOut))
        parameters.putInt("Booking_Window", getBookingWindow(searchParams.checkIn))
        parameters.putInt("Num_People", searchParams.guests())
        parameters.putInt("Number_Children", searchParams.children.size)
        parameters.putInt("Number_Nights", numOfNight)
        if (facebookContext != null) {
            parameters.putInt("Logged_in_Status", encodeBoolean(User.isLoggedIn(facebookContext)))
        }
        parameters.putString("Reward_Status", getLoyaltyTier(Db.getUser()))
        parameters.putString("POS", PointOfSale.getPointOfSale().twoLetterCountryCode)
    }

    private fun addCommonHotelV2Params(parameters: Bundle, hotelCheckoutResponse: HotelCheckoutResponse, location: Location) {
        val dtf = ISODateTimeFormat.date()
        parameters.putString("LOB", "Hotel")

        val regionId = hotelCheckoutResponse.checkoutResponse.productResponse.regionId ?: ""
        val formattedAddressCityState = StrUtils.formatAddressCityState(location) ?: ""
        val checkInDate = LocalDate(hotelCheckoutResponse.checkoutResponse.productResponse.checkInDate)
        val checkOutDate = LocalDate(hotelCheckoutResponse.checkoutResponse.productResponse.checkOutDate)
        val numOfNight = JodaUtils.daysBetween(checkInDate, checkOutDate)
        parameters.putString("region_id", regionId)
        addCommonLocationEvents(parameters, location)

        parameters.putString("destination_name", formattedAddressCityState)
        parameters.putString("Checkin_Date", dtf.print(checkInDate))
        parameters.putString("Checkout_Date", dtf.print(checkOutDate))
        parameters.putInt("Booking_Window", getBookingWindow(checkInDate))

        //ToDo API don't return number of child guest and total guests

        parameters.putInt("Number_Nights", numOfNight)
        if (facebookContext != null) {
            parameters.putInt("Logged_in_Status", encodeBoolean(User.isLoggedIn(facebookContext)))
        }
        parameters.putString("Reward_Status", getLoyaltyTier(Db.getUser()))
        parameters.putString("POS", PointOfSale.getPointOfSale().twoLetterCountryCode)
    }


    private fun addCommonFlightParams(parameters: Bundle, searchParams: FlightSearchParams, location: Location) {
        val dtf = ISODateTimeFormat.date()

        val destinationId = searchParams.getArrivalLocation().getDestinationId() ?: ""
        parameters.putString("region_id", destinationId)
        parameters.putString("destination_name", destinationId)
        parameters.putString("LOB", "Flight")
        addCommonLocationEvents(parameters, location)
        parameters.putString("Start_Date", dtf.print(searchParams.getDepartureDate()))
        parameters.putString("End_Date", if (searchParams.getReturnDate() != null) dtf.print(searchParams.getReturnDate()) else "")

        parameters.putInt("Booking_Window", getBookingWindow(searchParams.getDepartureDate()))
        parameters.putString("FlightOrigin_AirportCode", searchParams.getDepartureLocation().getDestinationId())
        parameters.putString("FlightDestination_AirportCode", destinationId)
        parameters.putInt("Num_People", searchParams.getNumTravelers())
        parameters.putInt("Number_Children", searchParams.getNumChildren())

        if (facebookContext != null) {
            parameters.putInt("Logged_in_Status", encodeBoolean(User.isLoggedIn(facebookContext)))
        }
        parameters.putString("Reward_Status", getLoyaltyTier(Db.getUser()))
        parameters.putString("POS", PointOfSale.getPointOfSale().getTwoLetterCountryCode())
    }

    private fun addCommonLocationEvents(parameters: Bundle, location: Location) {
        parameters.putString("destination_city", location.getCity() ?: "")
        parameters.putString("destination_state", location.getStateCode() ?: "")
        parameters.putString("destination_country", location.getCountryCode() ?: "")
    }

    /**
     * Null safe getter for lowestRate() call. See defect #4908 for more
     */
    private fun getLowestRate(property: Property): Rate? {
        val propertyLowestRate: Rate? = property.getLowestRate() // yes, this can be null (#4908)
        return propertyLowestRate
    }

    private fun addCommonCarParams(parameters: Bundle, startDate: LocalDate, endDate: LocalDate, pickUpLocation: CarLocation) {
        val dtf = ISODateTimeFormat.date()
        val locationCode = pickUpLocation.locationDescription
        val regionId = pickUpLocation.regionId
        val cityName = pickUpLocation.cityName
        val provinceStateName = pickUpLocation.provinceStateName
        val countryCode = pickUpLocation.countryCode

        parameters.putString("LOB", "Car")
        parameters.putString("region_id", regionId ?: "")
        parameters.putString("destination_city", cityName ?: "")
        parameters.putString("destination_state", provinceStateName ?: "")
        parameters.putString("destination_country", countryCode ?: "")
        parameters.putString("destination_name", locationCode ?: "")
        parameters.putString("Start_Date", dtf.print(startDate))
        parameters.putString("End_Date", dtf.print(endDate))
        parameters.putInt("Booking_Window", getBookingWindow(startDate))

        if (facebookContext != null) {
            parameters.putInt("Logged_in_Status", encodeBoolean(User.isLoggedIn(facebookContext)))
        }
        parameters.putString("Reward_Status", getLoyaltyTier(Db.getUser()))
        parameters.putString("POS", PointOfSale.getPointOfSale().getTwoLetterCountryCode())
    }

    private fun addCommonLXParams(parameters: Bundle, startDate: LocalDate, regionId: String?, location: String?) {
        val dtf = ISODateTimeFormat.date()
        parameters.putString("LOB", "Activity")
        parameters.putString("region_id", regionId ?: "")
        parameters.putString("destination_name", location ?: "")
        parameters.putString("Start_Date", dtf.print(startDate))
        parameters.putInt("Booking_Window", getBookingWindow(startDate))

        if (facebookContext != null) {
            parameters.putInt("Logged_in_Status", encodeBoolean(User.isLoggedIn(facebookContext)))
        }
        parameters.putString("Reward_Status", getLoyaltyTier(Db.getUser()))
        parameters.putString("POS", PointOfSale.getPointOfSale().getTwoLetterCountryCode())

    }

    private fun encodeBoolean(boolean: Boolean): Int {
        return if (boolean) 1 else 0
    }

    private fun getLocation(hotelCity: String, hotelStateProvince: String, hotelCountry: String): Location {
        val location: Location = Location()
        location.city = hotelCity
        location.stateCode = hotelStateProvince
        location.countryCode = hotelCountry
        return location

    }
}