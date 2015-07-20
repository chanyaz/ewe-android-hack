package com.expedia.bookings.tracking;

import android.content.Context
import android.os.Bundle
import com.expedia.bookings.data.*
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.StrUtils
import com.facebook.AppEventsConstants
import com.facebook.AppEventsLogger
import com.mobiata.android.time.util.JodaUtils
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.properties.Delegates

class FacebookEvents(val context: Context?) {
    val logger: AppEventsLogger? by Delegates.lazy {
        if (context != null) {
            AppEventsLogger.newLogger(context)
        }
        null
    }

    fun trackHotelSearch(search: HotelSearch) {
        val searchParams = search.getSearchParams()
        val location = search.getSearchResponse().getProperty(0).getLocation()

        var parameters = Bundle();
        addCommonHotelParams(parameters, searchParams, location)
        parameters.putString("Search_String", searchParams.getQuery())
        parameters.putString("AvgSearch_Value", calculateAverageRateHotels(search.getSearchResponse().getProperties()));
        parameters.putInt("Num_Rooms", search.getSearchResponse().getProperties().size())

        logger?.logEvent(AppEventsConstants.EVENT_NAME_SEARCHED, parameters);
    }

    fun trackHotelInfosite(search: HotelSearch) {
        val searchParams = search.getSearchParams()
        val location = search.getSearchResponse().getProperty(0).getLocation()
        val property = search.getSelectedProperty()

        var parameters = Bundle();
        addCommonHotelParams(parameters, searchParams, location)
        parameters.putString("Room_Value", property.getLowestRate().getAverageRate().getFormattedMoney());
        parameters.putInt("Num_Rooms", search.getSearchResponse().getProperties().size())
        parameters.putString("Content_ID", property.getPropertyId());
        parameters.putString("Currency", property.getLowestRate().getAverageRate().currencyCode);
        parameters.putString("ContentType", "product");

        logger?.logEvent(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters);
    }

    fun trackHotelCheckout(hotel: TripBucketItemHotel, rate: Rate) {
        val searchParams = hotel.getHotelSearch().getSearchParams()
        val property = hotel.getProperty()
        val location = property.getLocation()

        var parameters = Bundle();
        addCommonHotelParams(parameters, searchParams, location)
        parameters.putString("Booking_Value", rate.getTotalAmountAfterTax().getFormattedMoney());
        parameters.putString("Content_ID", property.getPropertyId());
        parameters.putString("Currency", property.getLowestRate().getAverageRate().currencyCode);
        parameters.putString("ContentType", "product");

        logger?.logEvent(AppEventsConstants.EVENT_NAME_ADDED_TO_CART, parameters);
    }

    fun trackHotelConfirmation(hotel: TripBucketItemHotel, rate: Rate) {
        val searchParams = hotel.getHotelSearch().getSearchParams()
        val property = hotel.getProperty()
        val location = property.getLocation()

        var parameters = Bundle();
        addCommonHotelParams(parameters, searchParams, location)
        parameters.putString("Booking_Value", rate.getTotalAmountAfterTax().getFormattedMoney());
        parameters.putString("Content_ID", property.getPropertyId());
        parameters.putString("Currency", property.getLowestRate().getAverageRate().currencyCode);
        parameters.putString("ContentType", "product");

        logger?.logEvent(AppEventsConstants.EVENT_NAME_PURCHASED, parameters);
    }

    fun trackFlightSearch(search: FlightSearch) {
        val searchParams = search.getSearchParams()
        val location = searchParams.getArrivalLocation()
        val destinationAirport = searchParams.getArrivalLocation().getDestinationId()

        var parameters = Bundle();
        addCommonFlightParams(parameters, searchParams, location)
        parameters.putString("Search_String", destinationAirport)
        parameters.putString("AvgSearch_Value", calculateAverageRateFlights(search.getSearchResponse().getTrips()));

        logger?.logEvent(AppEventsConstants.EVENT_NAME_SEARCHED, parameters);
    }

    fun trackFlightDetail(search: FlightSearch, flightTrip: FlightTrip) {
        val searchParams = search.getSearchParams()
        val location = searchParams.getArrivalLocation()
        val money = flightTrip.getTotalFare()

        var parameters = Bundle();
        addCommonFlightParams(parameters, searchParams, location)
        parameters.putString("Flight_Value", money.getFormattedMoney());
        parameters.putString("Content_ID", flightTrip.getLegs().get(0).getFirstAirlineCode());
        parameters.putString("Currency", money.getCurrency());
        parameters.putString("ContentType", "product");

        logger?.logEvent(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters);
    }

    fun trackFlightCheckout(flight: TripBucketItemFlight) {
        val searchParams = flight.getFlightSearchParams()
        val location = searchParams.getArrivalLocation()
        val flightTrip = flight.getFlightTrip()
        val money = flightTrip.getTotalFare()

        var parameters = Bundle();
        addCommonFlightParams(parameters, searchParams, location)
        parameters.putString("Booking_Value", money.getFormattedMoney());
        parameters.putString("Content_ID", flightTrip.getLegs().get(0).getFirstAirlineCode());
        parameters.putString("Currency", money.getCurrency());
        parameters.putString("ContentType", "product");

        logger?.logEvent(AppEventsConstants.EVENT_NAME_ADDED_TO_CART, parameters);
    }

    fun trackFlightConfirmation(flight: TripBucketItemFlight) {
        val searchParams = flight.getFlightSearchParams()
        val location = searchParams.getArrivalLocation()
        val flightTrip = flight.getFlightTrip()
        val money = flightTrip.getTotalFare()

        var parameters = Bundle();
        addCommonFlightParams(parameters, searchParams, location)
        parameters.putString("Booking_Value", money.getFormattedMoney());
        parameters.putString("Content_ID", flightTrip.getLegs().get(0).getFirstAirlineCode());
        parameters.putString("Currency", money.getCurrency());
        parameters.putString("ContentType", "product");

        logger?.logEvent(AppEventsConstants.EVENT_NAME_PURCHASED, parameters);
    }

    private fun getBookingWindow(time: LocalDate): Int {
        return JodaUtils.daysBetween(LocalDate.now(), time)
    }

    private fun calculateAverageRateHotels(properties: List<Property>): String {
        var totalPrice = BigDecimal.ZERO
        for (property in properties) {
            totalPrice = totalPrice.add(property.getLowestRate().getAverageRate().amount)
        }
        return totalPrice.divide(BigDecimal(properties.size()), 2, RoundingMode.HALF_UP).toString()
    }

    private fun calculateAverageRateFlights(flightTrips: List<FlightTrip>): String {
        var totalPrice = BigDecimal.ZERO
        for (trip in flightTrips) {
            totalPrice = totalPrice.add(trip.getTotalFare().amount)
        }
        return totalPrice.divide(BigDecimal(flightTrips.size()), 2, RoundingMode.HALF_UP).toString()
    }

    private fun getLoyaltyTier(user: User?): String {
        var loyaltyTier = "N/A"
        if (user != null && user.getPrimaryTraveler().getLoyaltyMembershipTier() != Traveler.LoyaltyMembershipTier.NONE) {
            loyaltyTier = Db.getUser().getPrimaryTraveler().getLoyaltyMembershipTier().name()
        }
        return loyaltyTier
    }

    private fun addCommonHotelParams(parameters: Bundle, searchParams: HotelSearchParams, location: Location) {
        val dtf = ISODateTimeFormat.date()
        parameters.putString("LOB", "Hotels")
        parameters.putString("region_id", searchParams.getRegionId())
        addCommonLocationEvents(parameters, location)
        parameters.putString("destination_name", StrUtils.formatAddressCityState(location))
        parameters.putString("Checkin_Date", dtf.print(searchParams.getCheckInDate()))
        parameters.putString("Checkout_Date", dtf.print(searchParams.getCheckOutDate()))
        parameters.putInt("Booking_Window", getBookingWindow(searchParams.getCheckInDate()))

        parameters.putInt("Num_People", searchParams.getNumTravelers())
        parameters.putInt("Number_Children", searchParams.getNumChildren())
        parameters.putInt("Number_Nights", searchParams.getStayDuration())
        parameters.putBoolean("Logged_in_Status", User.isLoggedIn(context))
        parameters.putString("Reward_Status", getLoyaltyTier(Db.getUser()));
        parameters.putString("POS", PointOfSale.getPointOfSale().getTwoLetterCountryCode());
    }

    private fun addCommonFlightParams(parameters: Bundle, searchParams: FlightSearchParams, location: Location) {
        val dtf = ISODateTimeFormat.date()
        parameters.putString("LOB", "Flight")
        parameters.putString("region_id", searchParams.getArrivalLocation().getDestinationId())
        addCommonLocationEvents(parameters, location)
        parameters.putString("destination_name", searchParams.getArrivalLocation().getDestinationId())
        parameters.putString("Start_Date", dtf.print(searchParams.getDepartureDate()))
        if (searchParams.getReturnDate() != null) {
            parameters.putString("End_Date", dtf.print(searchParams.getReturnDate()))
        }
        parameters.putInt("Booking_Window", getBookingWindow(searchParams.getDepartureDate()))
        parameters.putString("FlightOrigin_AirportCode", searchParams.getDepartureLocation().getDestinationId())
        parameters.putString("FlightDestination_AirportCode", searchParams.getArrivalLocation().getDestinationId())
        parameters.putInt("Num_People", searchParams.getNumTravelers())
        parameters.putInt("Number_Children", searchParams.getNumChildren())

        parameters.putBoolean("Logged_in_Status", User.isLoggedIn(context))
        parameters.putString("Reward_Status", getLoyaltyTier(Db.getUser()));
        parameters.putString("POS", PointOfSale.getPointOfSale().getTwoLetterCountryCode());
    }

    private fun addCommonLocationEvents(parameters: Bundle, location: Location) {
        parameters.putString("destination_city", location.getCity())
        parameters.putString("destination_state", location.getStateCode())
        parameters.putString("destination_country", location.getCountryCode())
    }
}
