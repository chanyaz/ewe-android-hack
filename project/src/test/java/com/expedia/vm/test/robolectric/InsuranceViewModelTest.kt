package com.expedia.vm.test.robolectric

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.insurance.InsuranceProduct
import com.expedia.bookings.data.insurance.InsuranceSolicitationItem
import com.expedia.bookings.services.InsuranceServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.InsuranceViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class InsuranceViewModelTest {
    lateinit private var sut: InsuranceViewModel

    @Before
    fun setup() {
        Robolectric.buildActivity(Activity::class.java).create().get().setTheme(R.style.NewLaunchTheme)

        val context = org.robolectric.RuntimeEnvironment.application
        sut = InsuranceViewModel(context, Mockito.mock(InsuranceServices::class.java))
    }

    @Test
    fun insuranceWidgetIsNotVisibleWhenInsuranceIsUnavailable() {
        val widgetVisibilitySubscriber = TestSubscriber<Boolean>()
        sut.widgetVisibilityObservable.subscribe(widgetVisibilitySubscriber)

        sut.tripObservable.onNext(tripResponseWithoutInsuranceAvailable())

        widgetVisibilitySubscriber.assertValueCount(1)
        val widgetIsVisible = widgetVisibilitySubscriber.onNextEvents[0]
        assertFalse(widgetIsVisible)
    }

    @Test
    fun insuranceWidgetIsVisibleWhenInsuranceIsAvailable() {
        val widgetVisibilitySubscriber = TestSubscriber<Boolean>()
        sut.widgetVisibilityObservable.subscribe(widgetVisibilitySubscriber)

        sut.tripObservable.onNext(tripResponseWithInsuranceAvailableButNotSelected())

        widgetVisibilitySubscriber.assertValueCount(1)
        val widgetIsVisible = widgetVisibilitySubscriber.onNextEvents[0]
        assertTrue(widgetIsVisible)
    }

    @Test
    fun insuranceWidgetIsResetOnNewTrip() {
        var insuranceIsSelected: Boolean
        var toggleSwitchSubscriber: TestSubscriber<Boolean>

        toggleSwitchSubscriber = TestSubscriber<Boolean>()
        sut.programmaticToggleObservable.subscribe(toggleSwitchSubscriber)
        sut.tripObservable.onNext(tripResponseWithInsuranceAvailableAndSelected())
        toggleSwitchSubscriber.assertValueCount(1)
        insuranceIsSelected = toggleSwitchSubscriber.onNextEvents[0]
        assertTrue(insuranceIsSelected)

        toggleSwitchSubscriber = TestSubscriber<Boolean>()
        sut.programmaticToggleObservable.subscribe(toggleSwitchSubscriber)
        sut.tripObservable.onNext(tripResponseWithInsuranceAvailableButNotSelected())
        toggleSwitchSubscriber.assertValueCount(1)
        insuranceIsSelected = toggleSwitchSubscriber.onNextEvents[0]
        assertFalse(insuranceIsSelected)
    }

    private fun tripResponseWithInsuranceAvailableAndSelected(): FlightCreateTripResponse {
        val trip = tripResponseWithInsuranceAvailableButNotSelected()
        trip.details.offer.selectedInsuranceProduct = trip.details.offer.availableInsuranceProducts.firstOrNull()

        return trip
    }

    private fun tripResponseWithInsuranceAvailableButNotSelected(): FlightCreateTripResponse {
        val insuranceProduct = InsuranceProduct()
        insuranceProduct.terms = InsuranceSolicitationItem()
        insuranceProduct.terms.url = ""

        val trip = tripResponseWithoutInsuranceAvailable()
        trip.details.offer.availableInsuranceProducts = listOf(insuranceProduct)

        return trip
    }

    private fun tripResponseWithoutInsuranceAvailable(): FlightCreateTripResponse {
        val offer = FlightTripDetails.FlightOffer()
        offer.availableInsuranceProducts = emptyList<InsuranceProduct>()

        val details = FlightTripDetails()
        details.offer = offer

        val trip = FlightCreateTripResponse()
        trip.newTrip = TripDetails(null, null, tripId = "")

        val tripDetailsField = trip.javaClass.getDeclaredField("details") // using reflection as field is private
        tripDetailsField.isAccessible = true
        tripDetailsField.set(trip, details)

        return trip
    }
}
