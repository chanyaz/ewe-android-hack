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

class FacebookEvents(val context: Context) {
    val logger = AppEventsLogger.newLogger(context)

    fun trackHotelSearch(search: HotelSearch) {
        val searchParams = search.getSearchParams()
        val location = search.getSearchResponse().getProperty(0).getLocation()

        var parameters = Bundle();
        addCommonHotelParams(parameters, searchParams, location)
        parameters.putString("Search_String", searchParams.getQuery())
        parameters.putString("AvgSearch_Value", calculateAverageRate(search.getSearchResponse().getProperties()));
        parameters.putInt("Num_Rooms", search.getSearchResponse().getProperties().size())

        logger.logEvent(AppEventsConstants.EVENT_NAME_SEARCHED, parameters);
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

        logger.logEvent(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, parameters);
    }

    fun trackHotelCheckout(hotel: TripBucketItemHotel, rate : Rate) {
        val searchParams = hotel.getHotelSearch().getSearchParams()
        val property = hotel.getProperty()
        val location = property.getLocation()

        var parameters = Bundle();
        addCommonHotelParams(parameters, searchParams, location)
        parameters.putString("Booking_Value", rate.getTotalAmountAfterTax().getFormattedMoney());
        parameters.putString("Content_ID", property.getPropertyId());
        parameters.putString("Currency", property.getLowestRate().getAverageRate().currencyCode);
        parameters.putString("ContentType", "product");

        logger.logEvent(AppEventsConstants.EVENT_NAME_ADDED_TO_CART, parameters);
    }

    fun trackHotelConfirmation(hotel: TripBucketItemHotel, rate : Rate) {
        val searchParams = hotel.getHotelSearch().getSearchParams()
        val property = hotel.getProperty()
        val location = property.getLocation()

        var parameters = Bundle();
        addCommonHotelParams(parameters, searchParams, location)
        parameters.putString("Booking_Value", rate.getTotalAmountAfterTax().getFormattedMoney());
        parameters.putString("Content_ID", property.getPropertyId());
        parameters.putString("Currency", property.getLowestRate().getAverageRate().currencyCode);
        parameters.putString("ContentType", "product");

        logger.logEvent(AppEventsConstants.EVENT_NAME_PURCHASED, parameters);
    }

    private fun getBookingWindow(time: LocalDate): Int {
        return JodaUtils.daysBetween(LocalDate.now(), time)
    }

    private fun calculateAverageRate(properties: List<Property>): String {
        var totalPrice = BigDecimal.ZERO
        for (property in properties) {
            totalPrice = totalPrice.add(property.getLowestRate().getAverageRate().amount)
        }
        return totalPrice.divide(BigDecimal(properties.size()), 2, RoundingMode.HALF_UP).toString()
    }

    private fun getLoyaltyTier(user: User?): String {
        var loyaltyTier = "N/A"
        if (user != null && user.getPrimaryTraveler().getLoyaltyMembershipTier() != Traveler.LoyaltyMembershipTier.NONE) {
            loyaltyTier = Db.getUser().getPrimaryTraveler().getLoyaltyMembershipTier().name()
        }
        return loyaltyTier
    }

    private fun addCommonHotelParams(parameters : Bundle, searchParams : HotelSearchParams, location : Location) {
        val dtf = ISODateTimeFormat.date()
        parameters.putString("LOB", "Hotels")
        parameters.putString("region_id", searchParams.getRegionId())
        parameters.putString("destination_city", location.getCity())
        parameters.putString("destination_state", location.getStateCode())
        parameters.putString("destination_country", location.getCountryCode())
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

}
