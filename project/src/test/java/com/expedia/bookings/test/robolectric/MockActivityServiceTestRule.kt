package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.lx.ActivityDetailsResponse
import com.expedia.bookings.data.lx.LXCheckoutParams
import com.expedia.bookings.data.lx.LXCheckoutResponse
import com.expedia.bookings.services.LxServices
import com.expedia.bookings.testrule.ServicesRule
import org.joda.time.LocalDate
import com.expedia.bookings.services.TestObserver

class MockActivityServiceTestRule : ServicesRule<LxServices>(LxServices::class.java) {

    lateinit private var lxCheckoutParams: LXCheckoutParams

    fun getHappyOffersResponse(): ActivityDetailsResponse {
        return getActivityOffersResponse("happy")
    }

    private fun getActivityOffersResponse(activityId: String): ActivityDetailsResponse {
<<<<<<< HEAD
        val observer = TestObserver<ActivityDetailsResponse>()
=======
        var observer = TestObserver<ActivityDetailsResponse>()
>>>>>>> 5abc89409b... WIP

        services?.lxDetails(activityId, activityId, LocalDate.now().plusDays(4),
                LocalDate.now().plusDays(6), observer)
        observer.awaitTerminalEvent()
        return observer.onNextEvents[0]
    }

    fun getCheckoutError(errorType: String) : ApiError {
<<<<<<< HEAD
        val observer = TestSubscriber <LXCheckoutResponse>()
        setCheckoutParams(errorType)
        services?.lxCheckout(lxCheckoutParams, observer)
        observer.awaitTerminalEvent()
        observer.assertNotCompleted()
        return observer.onErrorEvents[0] as ApiError
    }

    fun getCheckoutResponseForPriceChange(errorType: String) : LXCheckoutResponse {
        val observer = TestSubscriber <LXCheckoutResponse>()
        setCheckoutParams(errorType)
        services?.lxCheckout(lxCheckoutParams, observer)
        observer.awaitTerminalEvent()
        observer.assertCompleted()
        return observer.onNextEvents[0]
=======
        var observer = TestObserver <LXCheckoutResponse>()
        setCheckoutParams(errorType)
        services?.lxCheckout(lxCheckoutParams, observer)
        observer.awaitTerminalEvent()
        observer.assertNotComplete()
        return observer.onErrorEvents.get(0) as ApiError
    }

    fun getCheckoutResponseForPriceChange(errorType: String) : LXCheckoutResponse {
        var observer = TestObserver <LXCheckoutResponse>()
        setCheckoutParams(errorType)
        services?.lxCheckout(lxCheckoutParams, observer)
        observer.awaitTerminalEvent()
        observer.assertComplete()
        return observer.onNextEvents.get(0)
>>>>>>> 5abc89409b... WIP
    }


    fun setCheckoutParams(errorType: String, isRequiredCVV: Boolean = true){
        lxCheckoutParams = LXCheckoutParams()
        lxCheckoutParams.firstName = errorType
        lxCheckoutParams.lastName = "Test"
        if (isRequiredCVV)
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