package com.expedia.bookings.test.widget.rail

import android.content.Context
import android.view.View
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.responses.RailCard
import com.expedia.bookings.data.rail.responses.RailProduct
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.rail.RailFareOptionView
import com.expedia.vm.rail.RailDetailsViewModel
import com.expedia.vm.rail.RailFareOptionViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class RailFareOptionViewTest {

    fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Test
    fun testRailFareOptionDisplayed() {
        val railFareOptionViewModel = RailFareOptionViewModel()

        val railFareOptionView = RailFareOptionView(getContext())
        railFareOptionView.viewModel = railFareOptionViewModel

        railFareOptionViewModel.offerFareSubject.onNext(getRailOffer())

        assertEquals("$10", railFareOptionView.priceView.text)
        assertEquals("Fare class", railFareOptionView.fareTitle.text)
        assertEquals("Fare Description", railFareOptionView.fareDescription.text)
        assertEquals(View.VISIBLE, railFareOptionView.railCardImage.visibility)
    }

    @Test
    fun testRailFareOptionClickEvents() {
        val railFareOptionViewModel = RailFareOptionViewModel()

        val testOfferClickedSubscriber = TestSubscriber<Unit>()
        val testShowAmenitiesClickedSubscriber = TestSubscriber<Unit>()
        val testShowFareClickedSubscriber = TestSubscriber<Unit>()
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