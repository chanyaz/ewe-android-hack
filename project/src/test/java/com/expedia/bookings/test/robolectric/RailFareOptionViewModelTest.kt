package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailProduct
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.vm.rail.RailDetailsViewModel
import com.expedia.vm.rail.RailFareOptionViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class RailFareOptionViewModelTest {

    @Test
    fun testFareOptionDetails() {
        val railFareOptionViewModel = RailFareOptionViewModel()

        val testPriceSubscriber = TestSubscriber<String>()
        val testFareTitleSubscriber = TestSubscriber<String>()
        val testFareDescriptionSubscriber = TestSubscriber<String>()
        railFareOptionViewModel.priceObservable.subscribe(testPriceSubscriber)
        railFareOptionViewModel.fareTitleObservable.subscribe(testFareTitleSubscriber)
        railFareOptionViewModel.fareDescriptionObservable.subscribe(testFareDescriptionSubscriber)

        railFareOptionViewModel.offerFare.onNext(getRailOffer())
        assertEquals("$10", testPriceSubscriber.onNextEvents[0])
        assertEquals("Service class", testFareTitleSubscriber.onNextEvents[0])
        assertEquals("Fare Description", testFareDescriptionSubscriber.onNextEvents[0])
    }

    @Test
    fun testFareOptionClickEvents() {
        val railFareOptionViewModel = RailFareOptionViewModel()

        val testOfferSelectedSubscriber = TestSubscriber<RailSearchResponse.RailOffer>()
        val testShowAmenitiesSelectedSubscriber = TestSubscriber<RailSearchResponse.RailOffer>()
        val testShowFareSelectedSubscriber = TestSubscriber<RailSearchResponse.RailOffer>()

        railFareOptionViewModel.offerSelected.subscribe(testOfferSelectedSubscriber)
        railFareOptionViewModel.showAmenitiesDetails.subscribe(testShowAmenitiesSelectedSubscriber)
        railFareOptionViewModel.showFareDetails.subscribe(testShowFareSelectedSubscriber)

        railFareOptionViewModel.offerFare.onNext(getRailOffer())
        railFareOptionViewModel.offerSelectButtonClicked.onNext(Unit)
        railFareOptionViewModel.showAmenitiesForFareClicked.onNext(Unit)
        railFareOptionViewModel.showFareRulesForFareClicked.onNext(Unit)

        testOfferSelectedSubscriber.assertValueCount(1)
        testShowAmenitiesSelectedSubscriber.assertValueCount(1)
        testShowFareSelectedSubscriber.assertValueCount(1)
    }

    private fun getRailOffer(): RailSearchResponse.RailOffer {
        val railOffer = RailSearchResponse.RailOffer()
        railOffer.totalPrice = Money(10, "USD")
        railOffer.totalPrice.formattedPrice = "$10"
        val railProduct = RailProduct()
        railProduct.aggregatedCarrierServiceClassDisplayName = "Service class"
        railProduct.aggregatedFareDescription = "Fare Description"
        railOffer.railProductList = listOf(railProduct)
        return railOffer
    }

}