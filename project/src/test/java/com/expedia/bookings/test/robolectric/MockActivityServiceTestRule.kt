package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.lx.ActivityDetailsResponse
import com.expedia.bookings.data.lx.LXCheckoutParams
import com.expedia.bookings.data.lx.LXCheckoutResponse
import com.expedia.bookings.services.LxServices
import com.expedia.bookings.testrule.ServicesRule
import org.joda.time.LocalDate
import rx.observers.TestSubscriber

class MockActivityServiceTestRule : ServicesRule<LxServices>(LxServices::class.java) {

    lateinit private var lxCheckoutParams: LXCheckoutParams

    fun getHappyOffersResponse(): ActivityDetailsResponse {
        return getActivityOffersResponse("happy")
    }

    private fun getActivityOffersResponse(activityId: String): ActivityDetailsResponse {
        var observer = TestSubscriber<ActivityDetailsResponse>()

        services?.lxDetails(activityId, activityId, LocalDate.now().plusDays(4),
                LocalDate.now().plusDays(6), observer)
        observer.awaitTerminalEvent()
        return observer.onNextEvents.get(0)
    }

    fun getCheckoutError(errorType: String) : ApiError {
        var observer = TestSubscriber <LXCheckoutResponse>()
        setCheckoutParams(errorType)
        services?.lxCheckout(lxCheckoutParams, observer)
        observer.awaitTerminalEvent()
        observer.assertNotCompleted()
        return observer.onErrorEvents.get(0) as ApiError
    }

    fun getCheckoutResponseForPriceChange(errorType: String) : LXCheckoutResponse {
        var observer = TestSubscriber <LXCheckoutResponse>()
        setCheckoutParams(errorType)
        services?.lxCheckout(lxCheckoutParams, observer)
        observer.awaitTerminalEvent()
        observer.assertCompleted()
        return observer.onNextEvents.get(0)
    }

    fun setCheckoutParams(errorType: String){
        lxCheckoutParams = LXCheckoutParams()
        lxCheckoutParams.firstName = errorType
        lxCheckoutParams.lastName = "Test"
        lxCheckoutParams.cvv = "111"
        lxCheckoutParams.expectedTotalFare = "180.00"
        lxCheckoutParams.phone = "456-4567"
        lxCheckoutParams.email = "qa-ehcc@mobiata.com"
        lxCheckoutParams.tripId = "happypath_trip_id"
        lxCheckoutParams.expectedFareCurrencyCode = "USD"
        lxCheckoutParams.phoneCountryCode = "1"

    }

    fun getCheckoutParams(): LXCheckoutParams {
        return lxCheckoutParams
    }
}