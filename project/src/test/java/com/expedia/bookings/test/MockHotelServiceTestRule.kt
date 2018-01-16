package com.expedia.bookings.test

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelApplyCouponCodeParameters
import com.expedia.bookings.data.hotels.HotelCheckoutParamsMock
import com.expedia.bookings.data.hotels.HotelCheckoutV2Params
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.payment.MiscellaneousParams
import com.expedia.bookings.data.payment.PointsAndCurrency
import com.expedia.bookings.data.payment.PointsType
import com.expedia.bookings.data.payment.ProgramName
import com.expedia.bookings.data.payment.TripDetails
import com.expedia.bookings.data.payment.UserPreferencePointsDetails
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.testrule.ServicesRule
import org.joda.time.LocalDate
import rx.observers.TestSubscriber

class MockHotelServiceTestRule : ServicesRule<HotelServices>(HotelServices::class.java) {

    fun getPriceChangeDownCreateTripResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("hotel_price_change_down")
    }

    fun getPriceChangeUpCreateTripResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("hotel_price_change_up")
    }

    fun getPayLaterOfferCreateTripResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("pay_later_offer")
    }

    fun getHappyCreateTripEmailOptOutResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("hotel_email_opt_in_1")
    }

    fun getHappyCreateTripEmailOptInResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("hotel_email_opt_in_0")
    }

    fun getHappyCreateTripResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("happypath_0")
    }

    fun getHotelCouponCreateTripResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("create_trip_with_multiple_saved_coupon")
    }

    fun getHappyCreateTripResponseWithPropertyFee(): HotelCreateTripResponse {
        return getCreateTripResponse("happypath_2_night_stay_0")
    }

    fun getLoggedInUserWithRedeemablePointsLessThanTripTotalCreateTripResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("logged_in_user_with_redeemable_points_less_than_trip_total")
    }

    fun getLoggedInUserWithRedeemablePointsCreateTripResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("logged_in_user_with_redeemable_points")
    }

    fun getLoggedInUserWithRedeemableOrbucksCreateTripResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("logged_in_user_with_redeemable_orbucks")
    }

    fun getLoggedInUserWithNonRedeemablePointsCreateTripResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("logged_in_user_with_non_redeemable_points")
    }

    fun getHotelWithFeesAndIncludedTaxesResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("hotel_with_fees_and_included_taxes")
    }

    fun getHotelWithFeesPaidAtHotelResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("hotel_with_fees_paid_at_hotel")
    }

    fun getHotelWithGuestChargeResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("hotel_with_extra_guest_charge_and_discount")
    }

    fun getHotelWithTaxesIncludedResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("hotel_with_included_taxes")
    }

    fun getProductKeyExpiredResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("error_expired_product_key_createtrip")
    }

    fun getUnknownErrorResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("error_unknown_createtrip")
    }

    fun getHappyOfferResponse(): HotelOffersResponse {
        return getOfferResponse("happypath")
    }

    fun getRoomSoldOutCreateTripResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("error_room_unavailable_0")
    }

    fun getRoomSoldOutCheckoutResponse(): HotelCheckoutResponse {
        return getCheckoutTripResponse("error_room_unavailable_0")
    }

    private fun getOfferResponse(responseFileName: String): HotelOffersResponse {
        val suggestion = SuggestionV4()
        suggestion.coordinates = SuggestionV4.LatLng()
        val hotelSearchParams = HotelSearchParams.Builder(0, 0).destination(suggestion).startDate(LocalDate()).endDate(LocalDate()).adults(1).children(emptyList()).build() as HotelSearchParams
        val observer = TestSubscriber<HotelOffersResponse>()
        services?.offers(hotelSearchParams, responseFileName, observer)
        observer.awaitTerminalEvent()
        observer.assertCompleted()
        return observer.onNextEvents[0]
    }

    fun getHappyCheckoutResponse(): HotelCheckoutResponse {
        return getCheckoutTripResponse("happypath_0")
    }

    fun getUnknownErrorCheckoutResponse(): HotelCheckoutResponse {
        return getCheckoutTripResponse("error_checkout_unknown")
    }

    fun getInvalidTravelerInputCheckoutResponse(): HotelCheckoutResponse {
        return getCheckoutTripResponse("error_checkout_traveller_info")
    }

    fun getInvalidCardNumberInputCheckoutResponse(): HotelCheckoutResponse {
        return getCheckoutTripResponse("error_checkout_card")
    }

    fun getPaymentFailedCheckoutResponse(): HotelCheckoutResponse {
        return getCheckoutTripResponse("error_checkout_card_limit_exceeded_0")
    }

    fun getSessionTimeoutCheckoutResponse(): HotelCheckoutResponse {
        return getCheckoutTripResponse("error_checkout_session_timeout_0")
    }

    fun getTripAlreadyBookedCheckoutResponse(): HotelCheckoutResponse {
        return getCheckoutTripResponse("error_checkout_trip_already_booked")
    }

    fun getPriceChangeCheckoutResponse(): HotelCheckoutResponse {
        return getCheckoutTripResponse("hotel_price_change_checkout")
    }

    fun getPriceChangeWithUserPreferencesCheckoutResponse(): HotelCheckoutResponse {
        return getCheckoutTripResponse("hotel_price_change_with_user_preferences")
    }

    fun getApplyCouponResponseWithUserPreference(): HotelCreateTripResponse {
        return getApplyCouponResponse("hotel_coupon_with_user_points_preference")
    }

    private fun getCreateTripResponse(responseFileName: String): HotelCreateTripResponse {
        val productKey = responseFileName
        val observer = TestSubscriber<HotelCreateTripResponse>()
        services?.createTrip(HotelCreateTripParams(productKey, false, 1, emptyList()), true, observer)
        observer.awaitTerminalEvent()
        observer.assertCompleted()
        return observer.onNextEvents[0]
    }

    private fun getApplyCouponResponse(responseFileName: String): HotelCreateTripResponse {
        val observer = TestSubscriber<HotelCreateTripResponse>()
        val applyCouponParams = HotelApplyCouponCodeParameters.Builder()
                .couponCode(responseFileName).isFromNotSignedInToSignedIn(false).tripId("tripId").
                userPreferencePointsDetails(listOf(UserPreferencePointsDetails(ProgramName.ExpediaRewards, PointsAndCurrency(1000f, PointsType.BURN, Money("100", "USD")))))
                .build()

        services?.applyCoupon(applyCouponParams, true)!!.subscribe(observer)
        observer.awaitTerminalEvent()
        observer.assertCompleted()
        return observer.onNextEvents[0]
    }

    private fun getCheckoutTripResponse(responseFileName: String): HotelCheckoutResponse {
        val tripId = responseFileName
        val observer = TestSubscriber<HotelCheckoutResponse>()
        val tripDetails = TripDetails(tripId, "42.00", "USD", true)
        val miscParameters = MiscellaneousParams(true, "tealeafHotel:" + tripId, "expedia.app.android.phone:x.x.x")
        val checkoutParams = HotelCheckoutV2Params.Builder()
                .tripDetails(tripDetails)
                .checkoutInfo(HotelCheckoutParamsMock.checkoutInfo())
                .paymentInfo(HotelCheckoutParamsMock.paymentInfo())
                .traveler(HotelCheckoutParamsMock.traveler())
                .misc(miscParameters).build();
        services?.checkout(checkoutParams, observer)
        observer.awaitTerminalEvent()
        observer.assertCompleted()
        return observer.onNextEvents[0]
    }

    fun getRoomOffersNotAvailableHotelOffersResponse(): HotelOffersResponse {
        return getHotelOffersResponse("room_offers_not_available")
    }

    fun getAirAttachedHotelOffersResponse(): HotelOffersResponse {
        return getHotelOffersResponse("air_attached_hotel")
    }

    fun getHappyHotelOffersResponse(): HotelOffersResponse {
        return getHotelOffersResponse("happypath")
    }

    fun getZeroStarRatingHotelOffersResponse(): HotelOffersResponse {
        return getHotelOffersResponse("zero_star_rating")
    }

    private fun getHotelOffersResponse(responseFileName: String): HotelOffersResponse {
        val observer = TestSubscriber<HotelOffersResponse>()
        val suggestion = SuggestionV4()
        suggestion.coordinates = SuggestionV4.LatLng()
        val hotelSearchParams = HotelSearchParams.Builder(0, 0)
                .destination(suggestion)
                .startDate(LocalDate.now().plusDays(4))
                .endDate(LocalDate.now().plusDays(6))
                .adults(2)
                .children(listOf(2, 4)).build() as HotelSearchParams

        services?.offers(hotelSearchParams, responseFileName, observer)
        observer.awaitTerminalEvent()
        return observer.onNextEvents[0]
    }
}
