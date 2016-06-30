package com.expedia.bookings.test

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.cars.CarCheckoutParams
import com.expedia.bookings.data.cars.CarCheckoutResponse
import com.expedia.bookings.data.cars.CarCreateTripResponse
import com.expedia.bookings.data.cars.CreateTripCarOffer
import com.expedia.bookings.services.CarServices
import com.expedia.bookings.testrule.ServicesRule
import rx.observers.TestSubscriber

class MockCarServiceTestRule : ServicesRule<CarServices>(CarServices::class.java) {

    fun getHappyCreateTripResponse(): CarCreateTripResponse {
        return getCreateTripResponse("happy")
    }

    private fun getCreateTripResponse(responseFileName: String): CarCreateTripResponse {
        val productKey = responseFileName
        val observer = TestSubscriber<CarCreateTripResponse>()

        services?.createTrip(productKey, Money(), false, observer)
        observer.awaitTerminalEvent()
        observer.assertCompleted()
        return observer.onNextEvents[0]
    }

    fun getCheckoutTripResponse(carCheckoutParams: CarCheckoutParams): CarCheckoutResponse {
        val observer = TestSubscriber<CarCheckoutResponse>()
        val createTripCarOffer = CreateTripCarOffer()

        services?.checkout(createTripCarOffer, carCheckoutParams, observer)
        observer.awaitTerminalEvent()
        observer.assertCompleted()
        return observer.onNextEvents[0]
    }

}
