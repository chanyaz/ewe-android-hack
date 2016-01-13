package com.expedia.bookings.test

import com.expedia.bookings.data.hotels.HotelCheckoutParams
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.PaymentModel
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.services.HotelServices
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import retrofit.RestAdapter
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.io.File

public class MockHotelServiceTestRule : TestRule {

    val server: MockWebServer = MockWebServer()
    lateinit var service: HotelServices

    override fun apply(base: Statement?, description: Description?): Statement? {
        server.setDispatcher(expediaDispatcher())
        val createHotelService = object: Statement() {
            override fun evaluate() {
                service = HotelServices("http://localhost:" + server.port, OkHttpClient(), MockInterceptor(), Schedulers.immediate(), Schedulers.immediate(), RestAdapter.LogLevel.FULL)
                base?.evaluate()
            }
        }
        return server.apply(createHotelService, description)
    }

    fun getPriceChangeDownCreateTripResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("hotel_price_change_down")
    }

    fun getPriceChangeUpCreateTripResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("hotel_price_change_up")
    }

    fun getPayLaterOfferCreateTripResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("pay_later_offer")
    }

    fun getHappyCreateTripResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("happypath_0")
    }

    fun getLoggedInUserWithRedeemablePointsCreateTripResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("logged_in_user_with_redeemable_points")
    }

    fun getLoggedInUserWithNonRedeemeblePointsCreateTripResponse(): HotelCreateTripResponse {
        return getCreateTripResponse("logged_in_user_with_non_redeemable_points")
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
        val hotelSearchParams = HotelSearchParams(SuggestionV4(), LocalDate(), LocalDate(), 1, emptyList())
        val observer = TestSubscriber<HotelOffersResponse>()
        service.offers(hotelSearchParams, responseFileName, observer)
        observer.awaitTerminalEvent()
        observer.assertCompleted()
        return observer.onNextEvents.get(0)
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

    private fun getCreateTripResponse(responseFileName: String): HotelCreateTripResponse {
        val productKey = responseFileName
        val observer = TestSubscriber<HotelCreateTripResponse>()
        service.createTrip(HotelCreateTripParams(productKey, false, 1, emptyList()), observer)
        observer.awaitTerminalEvent()
        observer.assertCompleted()
        return observer.onNextEvents.get(0)
    }

    private fun getCheckoutTripResponse(responseFileName: String): HotelCheckoutResponse {
        val tripId = responseFileName
        val observer = TestSubscriber<HotelCheckoutResponse>()
        val checkoutParams = HotelCheckoutParams()
        checkoutParams.tripId = tripId
        checkoutParams.expectedTotalFare = "42.00"
        checkoutParams.tealeafTransactionId = "tealeafHotel:" + checkoutParams.tripId
        service.checkout(checkoutParams, observer)
        observer.awaitTerminalEvent()
        observer.assertCompleted()
        return observer.onNextEvents.get(0)
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
        var observer = TestSubscriber<HotelOffersResponse>()
        val hotelSearchParams = HotelSearchParams(SuggestionV4(), LocalDate.now().plusDays(4), LocalDate.now().plusDays(6), 2, listOf(2, 4))

        service.offers(hotelSearchParams, responseFileName, observer)
        observer.awaitTerminalEvent()
        return observer.onNextEvents.get(0)
    }

    private fun expediaDispatcher(): ExpediaDispatcher {
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        return ExpediaDispatcher(opener)
    }
}
