package com.expedia.bookings.tracking

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightSearch
import com.expedia.bookings.data.FlightSearchParams
import com.expedia.bookings.data.FlightTrip
import com.expedia.bookings.data.HotelSearch
import com.expedia.bookings.data.HotelSearchParams
import com.expedia.bookings.data.Location
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
import com.expedia.bookings.data.cars.CategorizedCarOffers
import com.expedia.bookings.data.cars.CreateTripCarOffer
import com.expedia.bookings.data.cars.RateTerm
import com.expedia.bookings.data.cars.SearchCarOffer
import com.expedia.bookings.data.lx.LXActivity
import com.expedia.bookings.data.lx.LXSearchParams
import com.expedia.bookings.data.lx.LXSearchResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.utils.CollectionUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.facebook.AppEventsConstants
import com.facebook.AppEventsLogger
import com.mobiata.android.Log
import com.mobiata.android.time.util.JodaUtils
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.List

val TAG = "FacebookTracking"
var context: Context? = null
var facebookLogger: AppEventsLogger? = null

fun track(event: String, parameters: Bundle) {
    val keys = parameters.keySet()

    if (keys.size() > 10) {
        Log.e(TAG, "${event} passing too many parameters, max 10, tried ${keys.size()}")
    }

    val nullKeys = keys.filter { parameters.get(it) == null }
    if (nullKeys.size() > 0) {
        Log.e(TAG, "${event} null values in bundle: ${nullKeys.join(", ")}")
    }

    val badKeys = keys.filter { parameters.get(it) !is String && parameters.get(it) !is Int }
    if (badKeys.size() > 0) {
        Log.e(TAG, "${event} values other than string or integer found: ${badKeys.join(", ")}")
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
        val searchParams = search.getSearchParams()
        val location = search.getSearchResponse().getProperty(0).getLocation()
        val properties = search.getSearchResponse().getFilteredAndSortedProperties(Db.getHotelSearch().getSearchParams())

        val parameters = Bundle()
        addCommonHotelParams(parameters, searchParams, location)
        parameters.putString("Search_String", location.getCity() ?: "")
        parameters.putString("LowestSearch_Value", calculateLowestRateHotels(properties)?.getDisplayPrice()?.getAmount().toString() ?: "")
        parameters.putInt("Num_Rooms", 1)

        track(AppEventsConstants.EVENT_NAME_SEARCHED, parameters)
    }

    fun trackHotelInfosite(search: HotelSearch) {
        val searchParams = search.getSearchParams()
        val location = search.getSearchResponse().getProperty(0).getLocation()
        val property = search.getSelectedProperty()

        val parameters = Bundle()
        addCommonHotelParams(parameters, searchParams, location)
        parameters.putString("Room_Value", getLowestRate(property)?.getDisplayPrice()?.getAmount().toString() ?: "")
        parameters.putString("Currency", getLowestRate(property)?.getDisplayPrice()?.currencyCode ?: "")
        parameters.putInt("Num_Rooms", 1)
        parameters.putString("Content_ID", property.getPropertyId())
        parameters.putString("ContentType", "product")

        track(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters)
    }

    fun trackHotelCheckout(hotel: TripBucketItemHotel, rate: Rate) {
        val searchParams = hotel.getHotelSearch().getSearchParams()
        val property = hotel.getProperty()
        val location = property.getLocation()

        val parameters = Bundle()
        addCommonHotelParams(parameters, searchParams, location)
        parameters.putInt("Num_Rooms", 1)
        parameters.putString("Booking_Value", rate.getTotalAmountAfterTax().getAmount().toString())
        parameters.putString("Content_ID", property.getPropertyId())
        parameters.putString("Currency", getLowestRate(property)?.getDisplayPrice()?.currencyCode ?: "")
        parameters.putString("ContentType", "product")

        track(AppEventsConstants.EVENT_NAME_ADDED_TO_CART, parameters)
    }

    fun trackHotelConfirmation(hotel: TripBucketItemHotel, rate: Rate) {
        val searchParams = hotel.getHotelSearch().getSearchParams()
        val property = hotel.getProperty()
        val location = property.getLocation()

        val parameters = Bundle()
        addCommonHotelParams(parameters, searchParams, location)
        parameters.putString("Booking_Value", rate.getTotalAmountAfterTax().getAmount().toString())
        parameters.putString("Content_ID", property.getPropertyId())
        parameters.putInt("Num_Rooms", 1)
        parameters.putString("Currency", getLowestRate(property)?.getDisplayPrice()?.currencyCode ?: "")
        parameters.putString("ContentType", "product")

        track(AppEventsConstants.EVENT_NAME_PURCHASED, parameters)
    }

    fun trackFlightSearch(search: FlightSearch) {
        val searchParams = search.getSearchParams()
        val location = searchParams.getArrivalLocation()
        val destinationAirport = searchParams.getArrivalLocation().getDestinationId()
        val arrivalAirport = searchParams.getDepartureLocation().getDestinationId()
        val parameters = Bundle()
        addCommonFlightParams(parameters, searchParams, location)
        parameters.putString("Search_String", "$arrivalAirport - $destinationAirport")
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
        parameters.putString("Search_String", "$arrivalAirport - $destinationAirport")
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
        parameters.putString("Content_ID", flightTrip.getLegs().get(0).getFirstAirlineCode())
        parameters.putString("Currency", money.getCurrency())
        parameters.putString("ContentType", "product")

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
        parameters.putString("Content_ID", flightTrip.getLegs().get(0).getFirstAirlineCode())
        parameters.putString("Currency", money.getCurrency())
        parameters.putString("ContentType", "product")

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
        parameters.putString("Content_ID", flightTrip.getLegs().get(0).getFirstAirlineCode())
        parameters.putString("Currency", money.getCurrency())
        parameters.putString("ContentType", "product")

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
            parameters.putString("Search_String", originDescription)
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
        parameters.putString("Currency", searchCarFare.rate.getCurrency())
        parameters.putString("ContentType", "product")

        track(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters)
    }

    fun trackCarCheckout(offer: CreateTripCarOffer) {
        var parameters = Bundle()
        val startDate = offer.getPickupTime().toLocalDate()
        val endDate = offer.getDropOffTime().toLocalDate()
        val location = offer.pickUpLocation

        addCommonCarParams(parameters, startDate, endDate, location)
        parameters.putString("Booking_Value", offer.detailedFare.grandTotal.getAmount().toString())
        parameters.putString("Currency", offer.detailedFare.grandTotal.currencyCode)
        parameters.putString("ContentType", "product")

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
        parameters.putString("Currency", grandTotal.currencyCode)
        parameters.putString("ContentType", "product")

        track(AppEventsConstants.EVENT_NAME_PURCHASED, parameters)
    }

    fun trackLXSearch(searchParams: LXSearchParams, lxSearchResponse: LXSearchResponse) {
        var parameters = Bundle()
        val startDate = searchParams.startDate

        addCommonLXParams(parameters, startDate, lxSearchResponse.regionId, lxSearchResponse.destination)
        parameters.putString("Search_String", searchParams.location ?: "")

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
        parameters.putString("Currency", currencyCode)
        parameters.putString("ContentType", "product")
        parameters.putString("Content_ID", activityId)

        track(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters)
    }

    fun trackLXCheckout(activityId: String, lxActivityLocation: String, startDate: LocalDate, regionId: String,
                        totalPrice: Money, ticketCount: Int, childTicketCount: Int) {
        var parameters = Bundle()

        addCommonLXParams(parameters, startDate, regionId, lxActivityLocation)
        parameters.putString("Currency", totalPrice.currencyCode)
        parameters.putString("ContentType", "product")
        parameters.putString("Content_ID", activityId)
        parameters.putString("Booking_Value", totalPrice.getAmount().toString())
        parameters.putInt("Num_People", ticketCount)
        parameters.putInt("Number_Children", childTicketCount)

        track(AppEventsConstants.EVENT_NAME_ADDED_TO_CART, parameters)
    }

    fun trackLXConfirmation(activityId: String, lxActivityLocation: String, startDate: LocalDate, regionId: String,
                            totalPrice: Money, ticketCount: Int, childTicketCount: Int) {
        var parameters = Bundle()

        addCommonLXParams(parameters, startDate, regionId, lxActivityLocation)
        parameters.putString("Currency", totalPrice.currencyCode)
        parameters.putString("ContentType", "product")
        parameters.putString("Content_ID", activityId)
        parameters.putString("Booking_Value", totalPrice.getAmount().toString())
        parameters.putInt("Num_People", ticketCount)
        parameters.putInt("Number_Children", childTicketCount)

        track(AppEventsConstants.EVENT_NAME_PURCHASED, parameters)
    }
}

fun getBookingWindow(time: LocalDate): Int {
    return JodaUtils.daysBetween(LocalDate.now(), time)
}

fun calculateLowestRateHotels(properties: List<Property>): Rate? {
    if (properties.size() == 0) return null

    var minPropertyRate = properties.get(0).getLowestRate()
    for (property in properties) {
        var propertyRate = property.getLowestRate()
        if (propertyRate == null)
            continue
        else {
            if (propertyRate.getDisplayPrice().getAmount() < minPropertyRate.getDisplayPrice().getAmount()) {
                minPropertyRate = propertyRate
            }
        }
    }
    return minPropertyRate
}

fun calculateLowestRateFlights(flightTrips: List<FlightTrip>): String {
    var minAmount = flightTrips.get(0).getTotalFare().getAmount()
    for (trip in flightTrips) {
        var amount = trip.getTotalFare().getAmount()
        if (amount < minAmount) {
            minAmount = amount
        }
    }
    return minAmount.toString()
}

fun getLoyaltyTier(user: User?): String {
    var loyaltyTier = "N/A"
    if (user != null && user.getPrimaryTraveler().getLoyaltyMembershipTier() != Traveler.LoyaltyMembershipTier.NONE) {
        loyaltyTier = Db.getUser().getPrimaryTraveler().getLoyaltyMembershipTier().name()
    }
    return loyaltyTier
}

fun addCommonHotelParams(parameters: Bundle, searchParams: HotelSearchParams, location: Location) {
    val dtf = ISODateTimeFormat.date()
    parameters.putString("LOB", "Hotel")
    val regionId = searchParams.getRegionId()
    val formattedAddressCityState = StrUtils.formatAddressCityState(location)
    parameters.putString("region_id", regionId ?: "")
    addCommonLocationEvents(parameters, location)

    parameters.putString("destination_name", formattedAddressCityState ?: "")
    parameters.putString("Checkin_Date", dtf.print(searchParams.getCheckInDate()))
    parameters.putString("Checkout_Date", dtf.print(searchParams.getCheckOutDate()))
    parameters.putInt("Booking_Window", getBookingWindow(searchParams.getCheckInDate()))
    parameters.putInt("Num_People", searchParams.getNumTravelers())
    parameters.putInt("Number_Children", searchParams.getNumChildren())
    parameters.putInt("Number_Nights", searchParams.getStayDuration())
    if (context != null) {
        parameters.putInt("Logged_in_Status", encodeBoolean(User.isLoggedIn(context)))
    }
    parameters.putString("Reward_Status", getLoyaltyTier(Db.getUser()))
    parameters.putString("POS", PointOfSale.getPointOfSale().getTwoLetterCountryCode())
}

fun addCommonFlightParams(parameters: Bundle, searchParams: FlightSearchParams, location: Location) {
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

    if (context != null) {
        parameters.putInt("Logged_in_Status", encodeBoolean(User.isLoggedIn(context)))
    }
    parameters.putString("Reward_Status", getLoyaltyTier(Db.getUser()))
    parameters.putString("POS", PointOfSale.getPointOfSale().getTwoLetterCountryCode())
}

fun addCommonLocationEvents(parameters: Bundle, location: Location) {
    val city = location.getCity()
    val stateCode = location.getStateCode()
    val countryCode = location.getCountryCode()
    parameters.putString("destination_city", city ?: "")
    parameters.putString("destination_state", stateCode ?: "")
    parameters.putString("destination_country", countryCode ?: "")
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

    if (context != null) {
        parameters.putInt("Logged_in_Status", encodeBoolean(User.isLoggedIn(context)))
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

    if (context != null) {
        parameters.putInt("Logged_in_Status", encodeBoolean(User.isLoggedIn(context)))
    }
    parameters.putString("Reward_Status", getLoyaltyTier(Db.getUser()))
    parameters.putString("POS", PointOfSale.getPointOfSale().getTwoLetterCountryCode())

}

private fun encodeBoolean(boolean: Boolean): Int {
    return if (boolean) 1 else 0
}
