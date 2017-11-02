package com.expedia.bookings.test.widget.rail

import android.content.Context
import android.view.View
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailCard
import com.expedia.bookings.data.rail.responses.RailOffer
import com.expedia.bookings.data.rail.responses.RailProduct
import com.expedia.bookings.rail.widget.RailFareOptionView
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.util.Optional
import com.expedia.vm.rail.RailFareOptionViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class RailFareOptionViewTest {
    val expectedFareTitle = "Standard Fare class"

    fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Test
    fun testOneWayRailFareOptionDisplayed() {
        val railFareOptionViewModel = RailFareOptionViewModel(getContext(), false)

        val railFareOptionView = RailFareOptionView(getContext())
        railFareOptionView.viewModel = railFareOptionViewModel

        railFareOptionViewModel.offerFareSubject.onNext(getRailOffer())
        railFareOptionViewModel.inboundLegCheapestPriceSubject.onNext(Optional(null))

        assertEquals("$10", railFareOptionView.priceView.text)
        assertEquals(expectedFareTitle, railFareOptionView.fareTitle.text)
        assertEquals("Fare Description", railFareOptionView.fareDescription.text)
        assertEquals(View.VISIBLE, railFareOptionView.railCardImage.visibility)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testRoundTripRailFareOptionDisplayed() {
        val railFareOptionViewModel = RailFareOptionViewModel(getContext(), false)

        val railFareOptionView = RailFareOptionView(getContext())
        railFareOptionView.viewModel = railFareOptionViewModel

        railFareOptionViewModel.offerFareSubject.onNext(getRailOffer())
        railFareOptionViewModel.inboundLegCheapestPriceSubject.onNext(Optional(Money("15", "USD")))

        assertEquals("$25", railFareOptionView.priceView.text)
        assertEquals(expectedFareTitle, railFareOptionView.fareTitle.text)
        assertEquals("Fare Description", railFareOptionView.fareDescription.text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testRoundTripDeltaPriceFareOptionDisplayed() {
        val railFareOptionViewModel = RailFareOptionViewModel(getContext(), true)

        val railFareOptionView = RailFareOptionView(getContext())
        railFareOptionView.viewModel = railFareOptionViewModel

        railFareOptionViewModel.offerFareSubject.onNext(getRailOffer())
        railFareOptionViewModel.inboundLegCheapestPriceSubject.onNext(Optional(Money("5", "USD")))

        assertEquals("+$5", railFareOptionView.priceView.text)
        assertEquals(expectedFareTitle, railFareOptionView.fareTitle.text)
        assertEquals("Fare Description", railFareOptionView.fareDescription.text)
    }

    @Test
    fun testRailFareOptionClickEvents() {
        val railFareOptionViewModel = RailFareOptionViewModel(getContext(), false)

        val testOfferClickedSubscriber = TestObserver<Unit>()
        val testShowAmenitiesClickedSubscriber = TestObserver<Unit>()
        val testShowFareClickedSubscriber = TestObserver<Unit>()
        railFareOptionViewModel.offerSelectButtonClicked.subscribe(testOfferClickedSubscriber)
        railFareOptionViewModel.showAmenitiesForFareClicked.subscribe(testShowAmenitiesClickedSubscriber)
        railFareOptionViewModel.showFareRulesForFareClicked.subscribe(testShowFareClickedSubscriber)

        val railFareOptionView = RailFareOptionView(getContext())
        railFareOptionView.viewModel = railFareOptionViewModel

        railFareOptionViewModel.offerFareSubject.onNext(getRailOffer())
        railFareOptionView.fareTitle.performClick()
        railFareOptionView.amenitiesButton.performClick()
        railFareOptionView.selectButton.performClick()

        testOfferClickedSubscriber.assertValueCount(1)
        testShowAmenitiesClickedSubscriber.assertValueCount(1)
        testShowFareClickedSubscriber.assertValueCount(1)
    }

    private fun getRailOffer(): RailOffer {
        val railOffer = RailOffer()
        railOffer.totalPrice = Money(10, "USD")
        railOffer.totalPrice.formattedPrice = "$10"
        val railProduct = RailProduct()
        railProduct.aggregatedCarrierServiceClassDisplayName = "Standard"
        railProduct.aggregatedCarrierFareClassDisplayName = "Fare class"
        railProduct.aggregatedFareDescription = "Fare Description"
        val fareQualifierList = listOf(RailCard("", "", ""))
        railProduct.fareQualifierList = fareQualifierList
        railOffer.railProductList = listOf(railProduct)
        return railOffer
    }
}