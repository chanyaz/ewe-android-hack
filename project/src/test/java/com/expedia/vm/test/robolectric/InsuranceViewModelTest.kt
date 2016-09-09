package com.expedia.vm.test.robolectric

import android.app.Activity
import android.text.Spanned
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
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class InsuranceViewModelTest {
    private enum class FlightType { DOMESTIC, INTERNATIONAL }

    lateinit private var sut: InsuranceViewModel

    @Before
    fun setup() {
        Robolectric.buildActivity(Activity::class.java).create().get().setTheme(R.style.NewLaunchTheme)

        val context = org.robolectric.RuntimeEnvironment.application
        sut = InsuranceViewModel(context, Mockito.mock(InsuranceServices::class.java))
    }

    @Test
    fun benefitsDifferBetweenDomesticAndInternational() {
        val benefitsSubscriber = TestSubscriber<Spanned>()
        sut.benefitsObservable.subscribe(benefitsSubscriber)

        sut.tripObservable.onNext(tripResponseWithInsuranceAvailableButNotSelected(FlightType.DOMESTIC))
        sut.tripObservable.onNext(tripResponseWithInsuranceAvailableButNotSelected(FlightType.INTERNATIONAL))

        benefitsSubscriber.assertValueCount(2)
        val domesticBenefits = benefitsSubscriber.onNextEvents[0]
        val internationalBenefits = benefitsSubscriber.onNextEvents[1]

        assertFalse(domesticBenefits.isNullOrEmpty())
        assertFalse(internationalBenefits.isNullOrEmpty())
        assertNotEquals(domesticBenefits, internationalBenefits)
    }

    @Test
    fun widgetIsNotVisibleWhenInsuranceIsUnavailable() {
        val widgetVisibilitySubscriber = TestSubscriber<Boolean>()
        sut.widgetVisibilityObservable.subscribe(widgetVisibilitySubscriber)

        sut.tripObservable.onNext(tripResponseWithoutInsuranceAvailable(FlightType.DOMESTIC))

        widgetVisibilitySubscriber.assertValueCount(1)
        val widgetIsVisible = widgetVisibilitySubscriber.onNextEvents[0]
        assertFalse(widgetIsVisible)
    }

    @Test
    fun widgetIsVisibleWhenInsuranceIsAvailable() {
        val widgetVisibilitySubscriber = TestSubscriber<Boolean>()
        sut.widgetVisibilityObservable.subscribe(widgetVisibilitySubscriber)

        sut.tripObservable.onNext(tripResponseWithInsuranceAvailableButNotSelected(FlightType.DOMESTIC))

        widgetVisibilitySubscriber.assertValueCount(1)
        val widgetIsVisible = widgetVisibilitySubscriber.onNextEvents[0]
        assertTrue(widgetIsVisible)
    }

    @Test
    fun widgetIsResetOnNewTrip() {
        var insuranceIsSelected: Boolean
        var toggleSwitchSubscriber: TestSubscriber<Boolean>

        toggleSwitchSubscriber = TestSubscriber<Boolean>()
        sut.programmaticToggleObservable.subscribe(toggleSwitchSubscriber)
        sut.tripObservable.onNext(tripResponseWithInsuranceAvailableAndSelected(FlightType.DOMESTIC))
        toggleSwitchSubscriber.assertValueCount(1)
        insuranceIsSelected = toggleSwitchSubscriber.onNextEvents[0]
        assertTrue(insuranceIsSelected)

        toggleSwitchSubscriber = TestSubscriber<Boolean>()
        sut.programmaticToggleObservable.subscribe(toggleSwitchSubscriber)
        sut.tripObservable.onNext(tripResponseWithInsuranceAvailableButNotSelected(FlightType.DOMESTIC))
        toggleSwitchSubscriber.assertValueCount(1)
        insuranceIsSelected = toggleSwitchSubscriber.onNextEvents[0]
        assertFalse(insuranceIsSelected)
    }

    private fun tripResponseWithInsuranceAvailableAndSelected(flightType: FlightType): FlightCreateTripResponse {
        val trip = tripResponseWithInsuranceAvailableButNotSelected(flightType)
        trip.getDetails().offer.selectedInsuranceProduct = trip.getDetails().offer.availableInsuranceProducts.firstOrNull()

        return trip
    }

    private fun tripResponseWithInsuranceAvailableButNotSelected(flightType: FlightType): FlightCreateTripResponse {
        val insuranceProduct = InsuranceProduct()
        insuranceProduct.terms = InsuranceSolicitationItem()
        insuranceProduct.terms.url = ""

        val trip = tripResponseWithoutInsuranceAvailable(flightType)
        trip.getDetails().offer.availableInsuranceProducts = listOf(insuranceProduct)

        return trip
    }

    private fun tripResponseWithoutInsuranceAvailable(flightType: FlightType): FlightCreateTripResponse {
        val offer = FlightTripDetails.FlightOffer()
        offer.availableInsuranceProducts = emptyList<InsuranceProduct>()
        offer.isInternational = (flightType == FlightType.INTERNATIONAL)

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
