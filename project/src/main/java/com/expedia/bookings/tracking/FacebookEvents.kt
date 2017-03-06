package com.expedia.bookings.tracking

import android.app.Activity
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
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.User
import com.expedia.bookings.data.cars.CarCheckoutResponse
import com.expedia.bookings.data.cars.CarLocation
import com.expedia.bookings.data.cars.CarSearch
import com.expedia.bookings.data.cars.CarSearchParam
import com.expedia.bookings.data.cars.CreateTripCarOffer
import com.expedia.bookings.data.cars.RateTerm
import com.expedia.bookings.data.cars.SearchCarOffer
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.lx.LXSearchResponse
import com.expedia.bookings.data.lx.LxSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.TripBucketItemFlight
import com.expedia.bookings.data.trips.TripBucketItemHotel
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.tracking.flight.FlightSearchTrackingData
import com.expedia.bookings.tracking.hotel.HotelSearchTrackingData
import com.expedia.bookings.utils.CollectionUtils
import com.expedia.bookings.utils.Strings
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

private val TAG = "FacebookTracking"
@JvmField var facebookContext: Context? = null
@JvmField var facebookLogger: AppEventsLogger? = null
private const val FB_PURCHASE_VALUE = "fb_purchase_value"
private const val FB_PURCHASE_CURRENCY = "fb_purchase_currency"
private const val FB_ORDER_ID = "fb_order_id"
private const val VALUE_TO_SUM = "_valueToSum"
private const val LOWEST_SEARCH_VALUE = "LowestSearch_Value"
private const val PICKUP_LOCATION = "Pickup_Location"
private const val DROPOFF_LOCATION = "Dropoff_Location"
private const val CAR_VALUE = "Car_Value"
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

class FacebookEvents {
    companion object {
        fun activateAppIfEnabledInConfig(context: Context) {
            if (ProductFlavorFeatureConfiguration.getInstance().isFacebookTrackingEnabled) {
                AppEventsLogger.activateApp((context as Activity).application)
            }
        }

        private fun track(event: String, parameters: Bundle) {
            val keys = parameters.keySet()

            if (keys.size > 10) {
                Log.e(TAG, "${event} passing too many parameters, max 10, tried ${keys.size}")
            }

            val nullKeys = keys.filter { parameters.get(it) == null }
            if (nullKeys.isNotEmpty()) {
                Log.e(TAG, "${event} null values in bundle: ${nullKeys.joinToString(", ")}")
            }

            val badKeys = keys.filter { parameters.get(it) !is String && parameters.get(it) !is Int }
            if (badKeys.isNotEmpty()) {
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

            addCommonHotelDATParams(parameters, searchParams, location)
            parameters.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, location.city ?: "")
            parameters.putString(FB_PURCHASE_VALUE, calculateLowestRateHotels(properties)?.displayPrice?.getAmount()?.toString() ?: "")
            parameters.putString(FB_PURCHASE_CURRENCY, properties.first().lowestRate!!.displayPrice.currencyCode ?: "")
            parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, getListOfTopPropertyIds(properties))

            track(AppEventsConstants.EVENT_NAME_SEARCHED, parameters)
        }
    }

    fun trackHotelV2Search(trackingData: HotelSearchTrackingData) {
        val location = getLocation(trackingData.city ?: "", trackingData.stateProvinceCode ?: "", trackingData.countryCode ?: "")
        val parameters = Bundle()

        addCommonHotelDATParams(parameters, trackingData, location)
        parameters.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, location.city ?: "")
        parameters.putString(FB_PURCHASE_VALUE, trackingData.lowestHotelTotalPrice ?: "")
        parameters.putString(FB_PURCHASE_CURRENCY, trackingData.hotels.first().rateCurrencyCode ?: "")
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, getListOfTopHotelIds(trackingData.hotels))

        track(AppEventsConstants.EVENT_NAME_SEARCHED, parameters)
    }

    fun trackHotelInfoSite(search: HotelSearch) {
        val searchParams: HotelSearchParams? = search.searchParams
        val location: Location? = search.searchResponse?.properties?.firstOrNull()?.location
        val selectedProperty: Property? = search.selectedProperty

        if (searchParams != null && location != null && selectedProperty != null) {
            val parameters = Bundle()
            val price = getLowestRate(selectedProperty)?.displayPrice
            addCommonHotelDATParams(parameters, searchParams, location)

            parameters.putString(FB_PURCHASE_VALUE, price?.getAmount()?.toString() ?: "")
            parameters.putString(FB_PURCHASE_CURRENCY, price?.currencyCode ?: "")
            parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, selectedProperty.propertyId ?: "")

            track(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters)
        }
    }

    fun trackHotelV2InfoSite(searchParams: com.expedia.bookings.data.hotels.HotelSearchParams, hotelOffersResponse: HotelOffersResponse) {
        val location: Location = getLocation(hotelOffersResponse.hotelCity,
                hotelOffersResponse.hotelStateProvince,
                hotelOffersResponse.hotelCountry)

        val parameters = Bundle()
        val chargeableRate = hotelOffersResponse.hotelRoomResponse?.firstOrNull()?.rateInfo?.chargeableRateInfo

        addCommonHotelDATParams(parameters, searchParams, location)
        parameters.putString(FB_PURCHASE_VALUE, chargeableRate?.averageRate.toString())
        parameters.putString(FB_PURCHASE_CURRENCY, chargeableRate?.currencyCode ?: "")
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, hotelOffersResponse.hotelId)

        track(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters)
    }

    fun trackHotelCheckout(hotel: TripBucketItemHotel, rate: Rate) {
        val searchParams: HotelSearchParams? = hotel.hotelSearch?.searchParams
        val property: Property? = hotel.hotelSearch?.selectedProperty
        val location: Location? = property?.location
        val bookingValue: String? = rate.totalAmountAfterTax?.getAmount().toString()

        if (searchParams != null && property != null && location != null) {
            val parameters = Bundle()
            addCommonHotelDATParams(parameters, searchParams, location)

            parameters.putString(FB_PURCHASE_VALUE, bookingValue ?: "")
            parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, property.propertyId ?: "")
            parameters.putString(FB_PURCHASE_CURRENCY, getLowestRate(property)?.displayPrice?.currencyCode ?: "")

            track(AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT, parameters)
        }
    }

    fun trackHotelV2Checkout(hotelProductResponse: HotelCreateTripResponse.HotelProductResponse, searchParams: com.expedia.bookings.data.hotels.HotelSearchParams) {
        val location: Location = getLocation(hotelProductResponse.hotelCity,
                hotelProductResponse.hotelStateProvince,
                hotelProductResponse.hotelCountry)

        val chargeableRate = hotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo
        val parameters = Bundle()
        addCommonHotelDATParams(parameters, searchParams, location)

        parameters.putString(FB_PURCHASE_VALUE, chargeableRate.displayTotalPrice.formattedMoney ?: "")
        parameters.putString(FB_PURCHASE_CURRENCY, chargeableRate.currencyCode ?: "")
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, hotelProductResponse.hotelId ?: "")

        track(AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT, parameters)

    }

    fun trackHotelConfirmation(hotel: TripBucketItemHotel, rate: Rate) {
        val searchParams: HotelSearchParams? = hotel.hotelSearch?.searchParams
        val property: Property? = hotel.hotelSearch?.selectedProperty
        val location: Location? = property?.location
        val bookingValue: String? = rate.totalAmountAfterTax?.getAmount().toString()

        if (searchParams != null && property != null && location != null) {
            val parameters = Bundle()
            val currencyCode: String? = getLowestRate(property)?.displayPrice?.currencyCode ?: ""
            addCommonHotelDATParams(parameters, searchParams, location)

            parameters.putString(FB_ORDER_ID, hotel.bookingResponse.orderNumber ?: "")
            parameters.putString(FB_PURCHASE_VALUE, bookingValue ?: "")
            parameters.putString(VALUE_TO_SUM, bookingValue ?: "")
            parameters.putString(FB_PURCHASE_CURRENCY, currencyCode)
            parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, property.propertyId ?: "")
            parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currencyCode)

            facebookLogger?.logPurchase(rate.totalAmountAfterTax?.amount ?: BigDecimal(0), Currency.getInstance(rate.totalAmountAfterTax?.currencyCode));
        }
    }

    fun trackHotelV2Confirmation(hotelCheckoutResponse: HotelCheckoutResponse) {

        val location: Location = getLocation(hotelCheckoutResponse.checkoutResponse.productResponse.hotelCity,
                hotelCheckoutResponse.checkoutResponse.productResponse.hotelStateProvince,
                hotelCheckoutResponse.checkoutResponse.productResponse.hotelCountry)

        val bookingValue: String? = hotelCheckoutResponse.totalCharges

        val parameters = Bundle()
        addCommonHotelDATParams(parameters, hotelCheckoutResponse, location)

        parameters.putString(FB_ORDER_ID, hotelCheckoutResponse.checkoutResponse.bookingResponse.travelRecordLocator ?: "")
        parameters.putString(FB_PURCHASE_VALUE, bookingValue ?: "")
        parameters.putString(VALUE_TO_SUM, bookingValue ?: "")
        parameters.putString(FB_PURCHASE_CURRENCY, hotelCheckoutResponse.currencyCode ?: "")
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, hotelCheckoutResponse.checkoutResponse.productResponse.hotelId ?: "")
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, hotelCheckoutResponse.currencyCode ?: "")

        facebookLogger?.logPurchase(BigDecimal(hotelCheckoutResponse.totalCharges), Currency.getInstance(hotelCheckoutResponse.currencyCode), parameters)
    }

    fun trackFlightSearch(search: FlightSearch) {
        val searchParams = search.searchParams
        val destinationAirport = searchParams.arrivalLocation.destinationId
        val arrivalAirport = searchParams.departureLocation.destinationId
        val parameters = Bundle()

        addCommonFlightDATParams(parameters, searchParams)
        parameters.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, "$arrivalAirport - $destinationAirport")
        parameters.putString(FB_PURCHASE_VALUE, calculateLowestRateFlights(search.searchResponse.trips))
        parameters.putString(FB_PURCHASE_CURRENCY, search.searchResponse.trips.first()!!.totalPrice.currencyCode ?: "")

        track(AppEventsConstants.EVENT_NAME_SEARCHED, parameters)
    }

    fun trackFlightV2Search(searchTrackingData: FlightSearchTrackingData) {
        val destinationAirport = searchTrackingData.arrivalAirport?.gaiaId
        val arrivalAirport = searchTrackingData.departureAirport?.gaiaId
        val parameters = Bundle()
        val lastFlightSegment = searchTrackingData.flightLegList[0].flightSegments.size - 1
        val arrivalAirportAddress = searchTrackingData.flightLegList[0].flightSegments[lastFlightSegment].arrivalAirportAddress
        val lowestValue = searchTrackingData.flightLegList[0].packageOfferModel.price.packageTotalPrice.amount.toString()

        addCommonFlightV2Params(parameters, searchTrackingData.arrivalAirport, searchTrackingData.departureAirport, searchTrackingData.departureDate,
                searchTrackingData.returnDate, searchTrackingData.guests, searchTrackingData.children.size)
        addArrivalAirportAddress(parameters, arrivalAirportAddress)
        parameters.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, "$arrivalAirport - $destinationAirport")
        parameters.putString(LOWEST_SEARCH_VALUE, lowestValue)

        addCommonFlightDATParams(parameters,searchTrackingData)
        parameters.putString(FB_PURCHASE_VALUE, lowestValue)
        parameters.putString(FB_PURCHASE_CURRENCY, searchTrackingData.flightLegList[0].packageOfferModel.price.packageTotalPrice.currencyCode ?: "")

        track(AppEventsConstants.EVENT_NAME_SEARCHED, parameters)
    }

    fun trackFilteredFlightSearch(search: FlightSearch, legNumber: Int) {
        val searchParams = search.searchParams
        val destinationAirport = searchParams.arrivalLocation.destinationId
        val arrivalAirport = searchParams.departureLocation.destinationId
        val parameters = Bundle()
        val trips = search.FlightTripQuery(legNumber).trips

        addCommonFlightDATParams(parameters, searchParams)
        parameters.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, "$arrivalAirport - $destinationAirport")
        parameters.putString(FB_PURCHASE_VALUE, calculateLowestRateFlights(trips))
        parameters.putString(FB_PURCHASE_CURRENCY, trips.first()!!.totalPrice.currencyCode)

        track(AppEventsConstants.EVENT_NAME_SEARCHED, parameters)
    }

    fun trackFilteredFlightV2Search(flightSearchParams: com.expedia.bookings.data.flights.FlightSearchParams,
                                    flightLegList: List<FlightLeg>) {
        val destinationAirport = flightSearchParams.arrivalAirport.gaiaId
        val arrivalAirport = flightSearchParams.departureAirport.gaiaId
        val parameters = Bundle()

        addCommonFlightDATParams(parameters, flightSearchParams)
        parameters.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, "$arrivalAirport - $destinationAirport")
        parameters.putString(FB_PURCHASE_VALUE, calculateLowestRateFlightsV2(flightLegList))
        parameters.putString(FB_PURCHASE_CURRENCY, flightLegList[0].packageOfferModel.price.packageTotalPrice.currencyCode ?: "")

        track(AppEventsConstants.EVENT_NAME_SEARCHED, parameters)
    }

    fun trackFlightDetail(search: FlightSearch) {
        val searchParams = search.searchParams
        val flightTrip = search.selectedFlightTrip
        val money = flightTrip.totalPrice
        val parameters = Bundle()

        addCommonFlightDATParams(parameters, searchParams)
        parameters.putString(FB_PURCHASE_VALUE, money.getAmount().toString())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, flightTrip.legs[0].firstAirlineCode)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, money.currency)
        parameters.putString(FB_PURCHASE_CURRENCY, money.currency)

        track(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters)
    }

    fun trackFlightV2Detail(flightSearchParams: com.expedia.bookings.data.flights.FlightSearchParams,
                            flightCreateTripResponse: FlightCreateTripResponse) {
        val parameters = Bundle()
        val totalPrice = flightCreateTripResponse.details.offer.totalPrice

        addCommonFlightDATParams(parameters, flightSearchParams)
        parameters.putString(FB_PURCHASE_VALUE, totalPrice.amount.toString())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, flightCreateTripResponse.details.legs[0].segments[0].airlineCode)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, totalPrice.currencyCode)
        parameters.putString(FB_PURCHASE_CURRENCY, totalPrice.currencyCode)

        track(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters)
    }

    fun trackFlightCheckout(flight: TripBucketItemFlight) {
        val searchParams = flight.flightSearchParams
        val flightTrip = flight.flightTrip
        val money = flightTrip.totalPrice
        val parameters = Bundle()

        addCommonFlightDATParams(parameters, searchParams)
        parameters.putString(FB_PURCHASE_VALUE, money.getAmount().toString())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, flightTrip.legs[0].firstAirlineCode)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, money.currency)
        parameters.putString(FB_PURCHASE_CURRENCY, money.currency)

        track(AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT, parameters)
    }

    fun trackFlightV2Checkout(flightCreateTripResponse: FlightCreateTripResponse, flightSearchParams: com.expedia.bookings.data.flights.FlightSearchParams) {
        val parameters = Bundle()
        val currencyCode = flightCreateTripResponse.details.offer.totalPrice.currencyCode

        addCommonFlightDATParams(parameters, flightSearchParams)
        parameters.putString(FB_PURCHASE_VALUE, flightCreateTripResponse.details.offer.totalPrice.amount.toString())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, flightCreateTripResponse.details.legs[0].segments[0].airlineCode)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currencyCode)
        parameters.putString(FB_PURCHASE_CURRENCY, currencyCode)

        track(AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT, parameters)
    }

    fun trackFlightConfirmation(flight: TripBucketItemFlight) {
        val searchParams = flight.flightSearchParams
        val flightTrip = flight.flightTrip
        val money = flightTrip.totalPrice
        val parameters = Bundle()

        addCommonFlightDATParams(parameters, searchParams)
        parameters.putString(FB_ORDER_ID, flight.itineraryResponse.itinerary.travelRecordLocator ?: "")
        parameters.putString(FB_PURCHASE_VALUE, money.getAmount().toString())
        parameters.putString(VALUE_TO_SUM, money.getAmount().toString())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, flightTrip.legs[0].firstAirlineCode)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, money.currency)
        parameters.putString(FB_PURCHASE_CURRENCY, money.currency)

        facebookLogger?.logPurchase(money.amount, Currency.getInstance(money.currencyCode), parameters)
    }

    fun trackFlightV2Confirmation(flightCheckoutResponse: FlightCheckoutResponse, flightSearchParams: com.expedia.bookings.data.flights.FlightSearchParams) {
        val flightTripDetails = flightCheckoutResponse.getFirstFlightTripDetails()
        val flightLeg = flightTripDetails.legs[0]
        val airLineCode = flightLeg.segments[0].airlineCode
        val parameters = Bundle()
        val totalCharges = flightCheckoutResponse.totalChargesPrice

        addCommonFlightDATParams(parameters, flightSearchParams)
        parameters.putString(FB_ORDER_ID, flightCheckoutResponse.orderId ?: "")
        parameters.putString(FB_PURCHASE_VALUE, totalCharges?.amount.toString())
        parameters.putString(VALUE_TO_SUM, totalCharges?.amount.toString())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, airLineCode)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, totalCharges?.currencyCode)
        parameters.putString(FB_PURCHASE_CURRENCY, totalCharges?.currencyCode)

        facebookLogger?.logPurchase(totalCharges?.amount, Currency.getInstance(totalCharges?.currencyCode), parameters)
    }

    fun trackCarSearch(search: CarSearchParam, carSearch: CarSearch) {
        val searchCarOffer = carSearch.lowestTotalPriceOffer
        val startDate = search.startDateTime.toLocalDate()
        val endDate = search.endDateTime.toLocalDate()
        val location = searchCarOffer.pickUpLocation
        val originDescription = search.originDescription
        val parameters = Bundle()

        addCommonCarParams(parameters, startDate, endDate, location)

        if (Strings.isNotEmpty(originDescription)) {
            parameters.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, originDescription)
            parameters.putString(PICKUP_LOCATION, originDescription)
            parameters.putString(DROPOFF_LOCATION, originDescription)
        }
        parameters.putString(LOWEST_SEARCH_VALUE, searchCarOffer.fare.total.getAmount().toString())

        track(AppEventsConstants.EVENT_NAME_SEARCHED, parameters)
    }

    fun trackCarDetail(search: CarSearchParam, searchCarOffer: SearchCarOffer) {
        var parameters = Bundle()
        val startDate = search.startDateTime.toLocalDate()
        val endDate = search.endDateTime.toLocalDate()
        val location = searchCarOffer.pickUpLocation

        addCommonCarParams(parameters, startDate, endDate, location)
        val searchCarFare = searchCarOffer.fare
        parameters.putString(CAR_VALUE, if (searchCarFare.rateTerm.equals(RateTerm.UNKNOWN))
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
        parameters.putString(BOOKING_VALUE, offer.detailedFare.grandTotal.getAmount().toString())
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
        parameters.putString(BOOKING_VALUE, grandTotal.getAmount().toString())
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, grandTotal.currencyCode)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")
        facebookLogger?.logPurchase(grandTotal.getAmount(), Currency.getInstance(grandTotal.currencyCode), parameters)
    }

    fun trackLXSearch(searchParams: LxSearchParams, lxSearchResponse: LXSearchResponse) {
        var parameters = Bundle()
        val startDate = searchParams.startDate

        addCommonLXParams(parameters, startDate, lxSearchResponse.regionId, lxSearchResponse.destination)
        parameters.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, searchParams.location)

        if (CollectionUtils.isNotEmpty(lxSearchResponse.activities)) {
            parameters.putString(LOWEST_SEARCH_VALUE, lxSearchResponse.lowestPriceActivity
                    .price.getAmount().toString())
        }

        track(AppEventsConstants.EVENT_NAME_SEARCHED, parameters)
    }

    fun trackLXDetail(activityId: String, destination: String, startDate: LocalDate, regionId: String,
                      currencyCode: String, activityValue: String) {
        var parameters = Bundle()

        addCommonLXParams(parameters, startDate, regionId, destination)
        parameters.putString(ACTIVITY_VALUE, activityValue)
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
        parameters.putString(BOOKING_VALUE, totalPrice.getAmount().toString())
        parameters.putInt(NUM_PEOPLE, ticketCount)
        parameters.putInt(NUM_CHILDREN, childTicketCount)

        track(AppEventsConstants.EVENT_NAME_ADDED_TO_CART, parameters)
    }

    fun trackLXConfirmation(activityId: String, lxActivityLocation: String, startDate: LocalDate, regionId: String,
                            totalPrice: Money, ticketCount: Int, childTicketCount: Int) {
        var parameters = Bundle()

        addCommonLXParams(parameters, startDate, regionId, lxActivityLocation)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, totalPrice.currencyCode)
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product")
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, activityId)
        parameters.putString(BOOKING_VALUE, totalPrice.getAmount().toString())
        parameters.putInt(NUM_PEOPLE, ticketCount)
        parameters.putInt(NUM_CHILDREN, childTicketCount)
        facebookLogger?.logPurchase(totalPrice.getAmount(), Currency.getInstance(totalPrice.currencyCode), parameters)
    }

    private fun getBookingWindow(time: LocalDate): Int {
        return JodaUtils.daysBetween(LocalDate.now(), time)
    }

    private fun calculateLowestRateHotels(properties: List<Property>): Rate? {
        if (properties.isEmpty()) return null

        var minPropertyRate = properties[0].lowestRate

        for (property in properties) {
            val propertyRate = property.lowestRate
            if (propertyRate == null || minPropertyRate == null)
                continue
            else if (propertyRate.displayPrice.getAmount() < minPropertyRate.displayPrice.getAmount()) {
                minPropertyRate = propertyRate
            }
        }
        return minPropertyRate
    }

    fun calculateLowestRateFlights(flightTrips: List<FlightTrip>): String {
        if (flightTrips.isEmpty()) {
            return ""
        }
        var minAmount = flightTrips[0].totalPrice.getAmount()
        for (trip in flightTrips) {
            val amount = trip.totalPrice.getAmount()
            if (amount < minAmount) {
                minAmount = amount
            }
        }
        return minAmount.toString()
    }

    private fun calculateLowestRateFlightsV2(flightLegList: List<FlightLeg>): String {
        if (flightLegList.isEmpty()) {
            return ""
        }
        Collections.sort(flightLegList, priceComparator)
        return flightLegList[0].packageOfferModel.price.packageTotalPrice.amount.toString()
    }

    private fun getLoyaltyTier(user: User?): String {
        val loyaltyTierNotAvailable = "N/A"
        if (user?.primaryTraveler?.loyaltyMembershipTier != LoyaltyMembershipTier.NONE) {
            return user?.primaryTraveler?.loyaltyMembershipTier?.toApiValue() ?: loyaltyTierNotAvailable
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
        addCommonHotelDATSearchParams(parameters, searchParams.checkInDate!!, searchParams.checkoutDate!!, searchParams.numberOfAdults, searchParams.numberOfChildren)
    }

    private fun addCommonHotelDATParams(parameters: Bundle, searchParams: HotelCheckoutResponse, location: Location) {
        val checkInDate = LocalDate.parse(searchParams.checkoutResponse.productResponse.checkInDate)
        val checkOutDate = LocalDate.parse(searchParams.checkoutResponse.productResponse.checkOutDate)

        addGenericHotelDATParams(parameters)
        addCommonHotelDATRegionParams(parameters, location)
        addCommonHotelDATSearchParams(parameters, checkInDate, checkOutDate)
    }

    private fun addCommonHotelDATParams(parameters: Bundle, searchParams: HotelSearchParams, location: Location) {
        addGenericHotelDATParams(parameters)
        addCommonHotelDATRegionParams(parameters, location)
        addCommonHotelDATSearchParams(parameters, searchParams.checkInDate!!, searchParams.checkOutDate, searchParams.numAdults, searchParams.numChildren)
    }

    private fun addGenericHotelDATParams(parameters: Bundle) {
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "[\"product\",\"hotel\"]")
        parameters.putString("LOB", "Hotel")
    }

    private fun addCommonHotelDATSearchParams(parameters: Bundle, checkIn: LocalDate, checkOut: LocalDate,
                                              adults: Int, numberOfChildren: Int) {
        val dtf = ISODateTimeFormat.date()

        parameters.putString(FB_CHECKIN_DATE, dtf.print(checkIn))
        parameters.putString(FB_CHECKOUT_DATE, dtf.print(checkOut))
        parameters.putInt(FB_NUM_ADULTS, adults)
        parameters.putInt(FB_NUM_CHILDREN, numberOfChildren)
    }

    private fun addCommonHotelDATSearchParams(parameters: Bundle, checkIn: LocalDate, checkOut: LocalDate) {
        val dtf = ISODateTimeFormat.date()

        parameters.putString(FB_CHECKIN_DATE, dtf.print(checkIn))
        parameters.putString(FB_CHECKOUT_DATE, dtf.print(checkOut))
    }

    private fun addCommonHotelDATRegionParams(parameters: Bundle, location: Location) {
        parameters.putString("fb_city", location.city ?: "")
        parameters.putString("fb_region", location.stateCode ?: "")
        parameters.putString("fb_country", location.countryCode ?: "")
    }

    private fun addCommonFlightDATParams(parameters: Bundle, searchParams: FlightSearchParams) {
        val dtf = ISODateTimeFormat.date()
        val destinationId = searchParams.arrivalLocation.destinationId ?: ""

        parameters.putString("fb_content_type", "[\"product\",\"flight\"]")
        parameters.putString("LOB", "Flight")
        parameters.putString(FB_DEPARTING_DATE, dtf.print(searchParams.departureDate))
        parameters.putString(FB_RETURNING_DATE, if (searchParams.returnDate != null) dtf.print(searchParams.returnDate) else "")
        parameters.putString(FB_ORIGIN_AIRPORT, searchParams.departureLocation.destinationId)
        parameters.putString(FB_DESTINATION_AIRPORT, destinationId)
        parameters.putInt(FB_NUM_ADULTS, searchParams.numAdults)
        parameters.putInt(FB_NUM_CHILDREN, searchParams.numChildren)
    }

    private fun addCommonFlightDATParams(parameters: Bundle, flightParams: FlightSearchTrackingData){
        val dtf = ISODateTimeFormat.date()

        parameters.putString("fb_content_type", "[\"product\",\"flight\"]")
        parameters.putString("LOB", "Flight")
        parameters.putString(FB_DEPARTING_DATE, dtf.print(flightParams.departureDate))
        parameters.putString(FB_RETURNING_DATE, if (flightParams.returnDate != null) dtf.print(flightParams.returnDate) else "")
        parameters.putString(FB_ORIGIN_AIRPORT, flightParams.departureAirport?.hierarchyInfo?.airport?.airportCode ?:"")
        parameters.putString(FB_DESTINATION_AIRPORT, flightParams.arrivalAirport?.hierarchyInfo?.airport?.airportCode ?:"")
        parameters.putInt(FB_NUM_ADULTS, flightParams.adults)
        parameters.putInt(FB_NUM_CHILDREN, flightParams.children.size)
    }

    private fun addCommonFlightDATParams(parameters: Bundle, flightParams: com.expedia.bookings.data.flights.FlightSearchParams){
        val dtf = ISODateTimeFormat.date()

        parameters.putString("fb_content_type", "[\"product\",\"flight\"]")
        parameters.putString("LOB", "Flight")
        parameters.putString(FB_DEPARTING_DATE, dtf.print(flightParams.departureDate))
        parameters.putString(FB_RETURNING_DATE, if (flightParams.returnDate != null) dtf.print(flightParams.returnDate) else "")
        parameters.putString(FB_ORIGIN_AIRPORT, flightParams.departureAirport.hierarchyInfo?.airport?.airportCode ?:"")
        parameters.putString(FB_DESTINATION_AIRPORT, flightParams.arrivalAirport.hierarchyInfo?.airport?.airportCode ?:"")
        parameters.putInt(FB_NUM_ADULTS, flightParams.adults)
        parameters.putInt(FB_NUM_CHILDREN, flightParams.children.size)
    }

    private fun addCommonFlightV2Params(parameters: Bundle, arrivalAirport: SuggestionV4?,
                                        departureAirport: SuggestionV4?, departureDate: LocalDate?, returnDate: LocalDate?, guests: Int, childrenNo: Int) {
        val dtf = ISODateTimeFormat.date()
        val destinationId = arrivalAirport!!.gaiaId
        parameters.putString("region_id", destinationId)
        parameters.putString("destination_name", destinationId)
        parameters.putString("LOB", "Flight")
        parameters.putString("Start_Date", dtf.print(departureDate))
        parameters.putString("End_Date", if (returnDate != null) dtf.print(returnDate) else "")

        parameters.putInt("Booking_Window", getBookingWindow(departureDate!!))
        parameters.putString("FlightOrigin_AirportCode", departureAirport?.gaiaId)
        parameters.putString("FlightDestination_AirportCode", destinationId)
        parameters.putInt(NUM_PEOPLE, guests)
        parameters.putInt(NUM_CHILDREN, childrenNo)
    }

    private fun addArrivalAirportAddress(parameters: Bundle, arrivalAirportAddress: FlightLeg.FlightSegment.AirportAddress?) {
        if (arrivalAirportAddress != null) {
            parameters.putString("destination_city", arrivalAirportAddress.city)
            parameters.putString("destination_state", arrivalAirportAddress.state)
            parameters.putString("destination_country", arrivalAirportAddress.country)
        }
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

    private fun getListOfTopHotelIds(hotels: List<Hotel>, top: Int = 5) : String {
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
            return getListOfTopHotelIds(hotels, top-1)
        }

        return idList
    }

    private fun getListOfTopPropertyIds(hotels: List<Property>, top: Int = 5) : String {
        var idList = "["
        val topHotels = hotels.take(top)

        for (hotel in topHotels) {
            idList += "\\\"" + hotel.propertyId + "\\\""

            if (hotel.propertyId != topHotels.last().propertyId) {
                idList += ","
            }
        }

        idList += "]"

        if (idList.count() > 100) {
            return getListOfTopPropertyIds(hotels, top-1)
        }

        return idList
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

    private val priceComparator = Comparator<FlightLeg> { flightLeg1, flightLeg2 ->
        flightLeg1.packageOfferModel.price.packageTotalPrice.amount.compareTo(flightLeg2.packageOfferModel.price.packageTotalPrice.amount)
    }
}