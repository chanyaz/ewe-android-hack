package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailCard
import com.expedia.bookings.data.rail.responses.RailProduct
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.vm.rail.RailFareOptionViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class RailFareOptionViewModelTest {
    val context = RuntimeEnvironment.application

    @Test
    fun testOneWayFareOptionDetails() {
        val railFareOptionViewModel = RailFareOptionViewModel(context, false)

        val testPriceSubscriber = TestSubscriber<String>()
        val testFareTitleSubscriber = TestSubscriber<String>()
        val testFareDescriptionSubscriber = TestSubscriber<String>()
        val testRailCardAppliedSubscriber = TestSubscriber<Boolean>()

        railFareOptionViewModel.priceObservable.subscribe(testPriceSubscriber)
        railFareOptionViewModel.fareTitleObservable.subscribe(testFareTitleSubscriber)
        railFareOptionViewModel.fareDescriptionObservable.subscribe(testFareDescriptionSubscriber)
        railFareOptionViewModel.railCardAppliedObservable.subscribe(testRailCardAppliedSubscriber)

        railFareOptionViewModel.offerFareSubject.onNext(getRailOffer())
        railFareOptionViewModel.inboundLegCheapestPriceSubject.onNext(null)
        assertEquals("$10", testPriceSubscriber.onNextEvents[0])
        assertEquals("Fare class", testFareTitleSubscriber.onNextEvents[0])
        assertEquals("Fare Description", testFareDescriptionSubscriber.onNextEvents[0])
        assertTrue(testRailCardAppliedSubscriber.onNextEvents[0])
    }

    @Test
    fun testRoundTripOutboundTotalFareOptionDetails() {
        val railFareOptionViewModel = RailFareOptionViewModel(context, false)

        val testPriceSubscriber = TestSubscriber<String>()
        railFareOptionViewModel.priceObservable.subscribe(testPriceSubscriber)

        railFareOptionViewModel.offerFareSubject.onNext(getRailOffer())
        railFareOptionViewModel.inboundLegCheapestPriceSubject.onNext(Money("15", "USD"))
        assertEquals("$25", testPriceSubscriber.onNextEvents[0])
    }

    @Test
    fun testRoundTripInboundDeltaPriceFareOptionDetails() {
        val railFareOptionViewModel = RailFareOptionViewModel(context, true)

        val testPriceSubscriber = TestSubscriber<String>()
        railFareOptionViewModel.priceObservable.subscribe(testPriceSubscriber)

        railFareOptionViewModel.offerFareSubject.onNext(getRailOffer())
        railFareOptionViewModel.inboundLegCheapestPriceSubject.onNext(Money("5", "USD"))
        assertEquals("+$5", testPriceSubscriber.onNextEvents[0])
    }

    @Test
    fun testFareOptionClickEvents() {
        val railFareOptionViewModel = RailFareOptionViewModel(context, false)

        val testOfferSelectedSubscriber = TestSubscriber<RailSearchResponse.RailOffer>()
        val testShowAmenitiesSelectedSubscriber = TestSubscriber<RailSearchResponse.RailOffer>()
        val testShowFareSelectedSubscriber = TestSubscriber<RailSearchResponse.RailOffer>()

        railFareOptionViewModel.offerSelectedObservable.subscribe(testOfferSelectedSubscriber)
        railFareOptionViewModel.amenitiesSelectedObservable.subscribe(testShowAmenitiesSelectedSubscriber)
        railFareOptionViewModel.fareDetailsSelectedObservable.subscribe(testShowFareSelectedSubscriber)

        railFareOptionViewModel.offerFareSubject.onNext(getRailOffer())
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
        railProduct.aggregatedCarrierFareClassDisplayName = "Fare class"
        railProduct.aggregatedFareDescription = "Fare Description"
        val fareQualifierList = listOf(RailCard("", "", ""))
        railProduct.fareQualifierList = fareQualifierList
        railOffer.railProductList = listOf(railProduct)
        return railOffer
    }
}