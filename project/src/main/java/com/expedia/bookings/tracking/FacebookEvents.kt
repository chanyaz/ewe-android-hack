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
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.Property
import com.expedia.bookings.data.Rate
import com.expedia.bookings.data.trips.TripBucketItemFlight
import com.expedia.bookings.data.trips.TripBucketItemHotel
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
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.services.HotelCheckoutResponse
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

class FacebookEvents() {
    companion object {
        fun activateAppIfEnabledInConfig(context: Context) {
            if (ProductFlavorFeatureConfiguration.getInstance().isFacebookTrackingEnabled) {
                AppEventsLogger.activateApp(context)
            }
        }

        fun deactivateAppIfEnabledInConfig(context: Context) {
            if (ProductFlavorFeatureConfiguration.getInstance().isFacebookTrackingEnabled) {
                AppEventsLogger.deactivateApp(context)
            }
        }

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
    }

    fun trackHotelSearch(search: HotelSearch) {
        val searchParams: HotelSearchParams? = search.searchParams
        val response: HotelSearchResponse? = search.searchResponse
        val location: Location? = response?.properties?.firstOrNull()?.location
        val properties: List<Property>? = response?.getFilteredAndSortedProperties(searchParams)

        if (searchParams != null && location != null && properties != null) {
            val parameters = Bundle()
            addCommonHotelParams(parameters, searchParams, location)
            parameters.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, location.city ?: "")
            parameters.putString("LowestSearch_Value", calculateLowestRateHotels(properties)?.displayPrice?.getAmount()?.toString() ?: "")
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
        val searchParams: HotelSearchParams? = search.searchParams
        val location: Location? = search.searchResponse?.properties?.firstOrNull()?.location
        val selectedProperty: Property? = search.selectedProperty

        if (searchParams != null && location != null && selectedProperty != null) {
            val parameters = Bundle()
            addCommonHotelParams(parameters, searchParams, location)
            parameters.putString("Room_Value", getLowestRate(selectedProperty)?.displayPrice?.getAmount()?.toString() ?: "")
            parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, getLowestRate(selectedProperty)?.displayPrice?.currencyCode ?: "")
            parameters.putInt("Num_Rooms", 1)
            parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, selectedProperty.propertyId ?: "")
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
        val searchParams: HotelSearchParams? = hotel.hotelSearch?.searchParams
        val property: Property? = hotel.hotelSearch?.selectedProperty
        val location: Location? = property?.location
        val bookingValue: String? = rate.totalAmountAfterTax?.getAmount().toString()

        if (searchParams != null && property != null && location != null) {
            val parameters = Bundle()
            addCommonHotelParams(parameters, searchParams, location)
            parameters.putInt("Num_Rooms", 1)
            parameters.putString("Booking_Value", bookingValue ?: "")
            parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, property.propertyId ?: "")
            parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, getLowestRate(property)?.displayPrice?.currencyCode ?: "")
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
        val searchParams: HotelSearchParams? = hotel.hotelSearch?.searchParams
        val property: Property? = hotel.hotelSearch?.selectedProperty
        val location: Location? = property?.location
        val bookingValue: String? = rate.totalAmountAfterTax?.getAmount().toString()

        if (searchParams != null && property != null && location != null) {
            val parameters = Bundle()
            addCommonHotelParams(parameters, searchParams, location)
            parameters.putString("Booking_Value", bookingValue ?: "")
            parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, property.propertyId ?: "")
            parameters.putInt("Num_Rooms", 1)
            parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, getLowestRate(property)?.displayPrice?.currencyCode ?: "")
            parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")
            facebookLogger?.logPurchase(rate.totalAmountAfterTax?.amount ?: BigDecimal(0), Currency.getInstance(rate.totalAmountAfterTax?.currencyCode));
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
        val searchParams = search.searchParams
        val location = searchParams.arrivalLocation
        val destinationAirport = searchParams.arrivalLocation.destinationId
        val arrivalAirport = searchParams.departureLocation.destinationId
        val parameters = Bundle()
        addCommonFlightParams(parameters, searchParams, location)
        parameters.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, "$arrivalAirport - $destinationAirport")
        parameters.putString("LowestSearch_Value", calculateLowestRateFlights(search.searchResponse.trips))

        track(AppEventsConstants.EVENT_NAME_SEARCHED, parameters)
    }

    fun trackFilteredFlightSearch(search: FlightSearch, legNumber: Int) {
        val searchParams = search.searchParams
        val location = searchParams.arrivalLocation
        val destinationAirport = searchParams.arrivalLocation.destinationId
        val arrivalAirport = searchParams.departureLocation.destinationId
        val parameters = Bundle()
        val trips = search.FlightTripQuery(legNumber).trips;
        addCommonFlightParams(parameters, searchParams, location)
        parameters.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, "$arrivalAirport - $destinationAirport")
        parameters.putString("LowestSearch_Value", calculateLowestRateFlights(trips))

        track(AppEventsConstants.EVENT_NAME_SEARCHED, parameters)
    }

    fun trackFlightDetail(search: FlightSearch) {
        val searchParams = search.searchParams
        val location = searchParams.arrivalLocation
        val flightTrip = search.selectedFlightTrip
        val money = flightTrip.totalFare

        val parameters = Bundle()
        addCommonFlightParams(parameters, searchParams, location)
        parameters.putString("Flight_Value", money.getAmount().toString())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, flightTrip.legs.get(0).firstAirlineCode)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, money.currency)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")

        track(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters)
    }

    fun trackFlightCheckout(flight: TripBucketItemFlight) {
        val searchParams = flight.flightSearchParams
        val location = searchParams.arrivalLocation
        val flightTrip = flight.flightTrip
        val money = flightTrip.totalFare

        val parameters = Bundle()
        addCommonFlightParams(parameters, searchParams, location)
        parameters.putString("Booking_Value", money.getAmount().toString())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, flightTrip.legs.get(0).firstAirlineCode)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, money.currency)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")

        track(AppEventsConstants.EVENT_NAME_ADDED_TO_CART, parameters)
    }

    fun trackFlightConfirmation(flight: TripBucketItemFlight) {
        val searchParams = flight.flightSearchParams
        val location = searchParams.arrivalLocation
        val flightTrip = flight.flightTrip
        val money = flightTrip.totalFare

        val parameters = Bundle()
        addCommonFlightParams(parameters, searchParams, location)
        parameters.putString("Booking_Value", money.getAmount().toString())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, flightTrip.legs.get(0).firstAirlineCode)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, money.currency)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")
        facebookLogger?.logPurchase(money.amount, Currency.getInstance(money.currencyCode));
        track(AppEventsConstants.EVENT_NAME_PURCHASED, parameters)
    }

    fun trackCarSearch(search: CarSearchParams, carSearch: CarSearch) {
        val searchCarOffer = carSearch.lowestTotalPriceOffer
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
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, searchCarFare.rate.currency)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")

        track(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters)
    }

    fun trackCarCheckout(offer: CreateTripCarOffer) {
        var parameters = Bundle()
        val startDate = offer.pickupTime.toLocalDate()
        val endDate = offer.dropOffTime.toLocalDate()
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
        val startDate = carOffer.pickupTime.toLocalDate()
        val endDate = carOffer.dropOffTime.toLocalDate()
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
            parameters.putString("LowestSearch_Value", lxSearchResponse.lowestPriceActivity
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

        var minPropertyRate = properties.get(0).lowestRate

        for (property in properties) {
            var propertyRate = property.lowestRate
            if (propertyRate == null || minPropertyRate == null)
                continue
            else if (propertyRate.displayPrice.getAmount() < minPropertyRate.displayPrice.getAmount()) {
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

    fun calculateLowestRateFlights(flightTrips: List<FlightTrip>): String {
        if (flightTrips.size == 0) {
            return "";
        }
        var minAmount = flightTrips.get(0).totalFare.getAmount()
        for (trip in flightTrips) {
            var amount = trip.totalFare.getAmount()
            if (amount < minAmount) {
                minAmount = amount
            }
        }
        return minAmount.toString()
    }

    private fun getLoyaltyTier(user: User?): String {
        var loyaltyTier = "N/A"
        if (user != null && user.primaryTraveler.loyaltyMembershipTier != LoyaltyMembershipTier.NONE) {
            loyaltyTier = Db.getUser().primaryTraveler.loyaltyMembershipTier.toApiValue()
        }
        return loyaltyTier
    }

    private fun addCommonHotelParams(parameters: Bundle, searchParams: HotelSearchParams, location: Location) {
        val dtf = ISODateTimeFormat.date()
        parameters.putString("LOB", "Hotel")
        val regionId = searchParams.regionId
        val formattedAddressCityState = StrUtils.formatAddressCityState(location) ?: ""
        parameters.putString("region_id", regionId ?: "")
        addCommonLocationEvents(parameters, location)

        parameters.putString("destination_name", formattedAddressCityState)
        parameters.putString("Checkin_Date", dtf.print(searchParams.checkInDate))
        parameters.putString("Checkout_Date", dtf.print(searchParams.checkOutDate))
        parameters.putInt("Booking_Window", getBookingWindow(searchParams.checkInDate))
        parameters.putInt("Num_People", searchParams.numTravelers)
        parameters.putInt("Number_Children", searchParams.numChildren)
        parameters.putInt("Number_Nights", searchParams.stayDuration)
        if (facebookContext != null) {
            parameters.putInt("Logged_in_Status", encodeBoolean(User.isLoggedIn(facebookContext)))
        }
        parameters.putString("Reward_Status", getLoyaltyTier(Db.getUser()))
        parameters.putString("POS", PointOfSale.getPointOfSale().twoLetterCountryCode)
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
        parameters.putInt("Num_People", searchParams.guests)
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

        val destinationId = searchParams.arrivalLocation.destinationId ?: ""
        parameters.putString("region_id", destinationId)
        parameters.putString("destination_name", destinationId)
        parameters.putString("LOB", "Flight")
        addCommonLocationEvents(parameters, location)
        parameters.putString("Start_Date", dtf.print(searchParams.departureDate))
        parameters.putString("End_Date", if (searchParams.returnDate != null) dtf.print(searchParams.returnDate) else "")

        parameters.putInt("Booking_Window", getBookingWindow(searchParams.departureDate))
        parameters.putString("FlightOrigin_AirportCode", searchParams.departureLocation.destinationId)
        parameters.putString("FlightDestination_AirportCode", destinationId)
        parameters.putInt("Num_People", searchParams.numTravelers)
        parameters.putInt("Number_Children", searchParams.numChildren)

        if (facebookContext != null) {
            parameters.putInt("Logged_in_Status", encodeBoolean(User.isLoggedIn(facebookContext)))
        }
        parameters.putString("Reward_Status", getLoyaltyTier(Db.getUser()))
        parameters.putString("POS", PointOfSale.getPointOfSale().twoLetterCountryCode)
    }

    private fun addCommonLocationEvents(parameters: Bundle, location: Location) {
        parameters.putString("destination_city", location.city ?: "")
        parameters.putString("destination_state", location.stateCode ?: "")
        parameters.putString("destination_country", location.countryCode ?: "")
    }

    /**
     * Null safe getter for lowestRate() call. See defect #4908 for more
     */
    private fun getLowestRate(property: Property): Rate? {
        val propertyLowestRate: Rate? = property.lowestRate // yes, this can be null (#4908)
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
        parameters.putString("POS", PointOfSale.getPointOfSale().twoLetterCountryCode)
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
        parameters.putString("POS", PointOfSale.getPointOfSale().twoLetterCountryCode)

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