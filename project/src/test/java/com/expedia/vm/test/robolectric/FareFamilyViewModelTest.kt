package com.expedia.vm.test.robolectric

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.vm.FareFamilyViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FareFamilyViewModelTest {
    private enum class FlightType { DOMESTIC, INTERNATIONAL }

    lateinit private var sut: FareFamilyViewModel

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.NewLaunchTheme)

        SettingUtils.save(activity, R.string.preference_fare_family_flight_summary, true)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFareFamilyFlightSummary)
        sut = FareFamilyViewModel(activity)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testFareFamilyCardViewStringsForFreshCreateTrip(){
        val deltaPriceSubscriber = TestSubscriber<String>()
        val selectedClassSubscriber = TestSubscriber<String>()
        val fareFamilyTitleSubscriber = TestSubscriber<String>()
        val fromLabelVisibilitySubscriber = TestSubscriber<Boolean>()

        sut.deltaPriceObservable.subscribe(deltaPriceSubscriber)
        sut.selectedClassObservable.subscribe(selectedClassSubscriber)
        sut.fromLabelVisibility.subscribe(fromLabelVisibilitySubscriber)
        sut.fareFamilyTitleObservable.subscribe(fareFamilyTitleSubscriber)
        sut.tripObservable.onNext(tripResponseWithFareFamilyAvailable())
        assertEquals("+$42.00",deltaPriceSubscriber.onNextEvents[0])
        assertEquals("Economy",selectedClassSubscriber.onNextEvents[0])
        assertEquals("Upgrade your flights",fareFamilyTitleSubscriber.onNextEvents[0])
        assertTrue(fromLabelVisibilitySubscriber.onNextEvents[0])
    }

    @Test
    fun testFareFamilyCardViewStringsAfterSelectingFareFamily(){
        val deltaPriceSubscriber = TestSubscriber<String>()
        val selectedClassSubscriber = TestSubscriber<String>()
        val fareFamilyTitleSubscriber = TestSubscriber<String>()
        val fromLabelVisibilitySubscriber = TestSubscriber<Boolean>()

        sut.deltaPriceObservable.subscribe(deltaPriceSubscriber)
        sut.selectedClassObservable.subscribe(selectedClassSubscriber)
        sut.fromLabelVisibility.subscribe(fromLabelVisibilitySubscriber)
        sut.fareFamilyTitleObservable.subscribe(fareFamilyTitleSubscriber)
        sut.tripObservable.onNext(tripResponseWithFareFamilySelected())
        assertEquals("",deltaPriceSubscriber.onNextEvents[0])
        assertEquals("Economy",selectedClassSubscriber.onNextEvents[0])
        assertEquals("Change your cabin class",fareFamilyTitleSubscriber.onNextEvents[0])
        assertFalse(fromLabelVisibilitySubscriber.onNextEvents[0])
    }

    @Test
    fun fareFamilyWidgetVisiblility() {
        val widgetVisibilitySubscriber = TestSubscriber<Boolean>()
        sut.widgetVisibilityObservable.subscribe(widgetVisibilitySubscriber)

        sut.tripObservable.onNext(tripResponseWithoutFareFamilyAvailable())
        assertFalse(widgetVisibilitySubscriber.onNextEvents[0])

        sut.tripObservable.onNext(tripResponseWithFareFamilyAvailable())
        assertTrue(widgetVisibilitySubscriber.onNextEvents[1])
    }

    private fun tripResponseWithFareFamilySelected(): FlightCreateTripResponse {
        val trip = tripResponseWithFareFamilyAvailable()
        trip.isFareFamilyUpgraded = true
        return trip
    }

    private fun tripResponseWithFareFamilyAvailable(): FlightCreateTripResponse {
        val deltamoney = Money("42.00", "USD")
        deltamoney.formattedPrice = "$42.00"
        val fareFamilyDetails = FlightTripResponse.FareFamilyDetails("Economy", "Economy", "Economy",
                Money("210.00", "USD"), deltamoney, true, HashMap())
        val fareFamilyProduct = FlightTripResponse.FareFamilies("product-key", arrayOf(fareFamilyDetails))
        val trip = tripResponseWithoutFareFamilyAvailable()
        trip.fareFamilyList = fareFamilyProduct
        return trip
    }

    private fun tripResponseWithoutFareFamilyAvailable(): FlightCreateTripResponse {
        val offer = FlightTripDetails.FlightOffer()
        val seatClassAndBookingCode = FlightTripDetails().SeatClassAndBookingCode()
        seatClassAndBookingCode.seatClass = "coach"
        offer.offersSeatClassAndBookingCode = listOf(listOf(seatClassAndBookingCode))
        val details = FlightTripDetails()
        val leg = FlightLeg()
        leg.isBasicEconomy = false
        details.offer = offer
        details.legs = listOf(leg)
        val trip = FlightCreateTripResponse()
        trip.newTrip = TripDetails(null, null, tripId = "")
        trip.details = details
        trip.fareFamilyList  = FlightTripResponse.FareFamilies("product-key", emptyArray())
        return trip
    }
}
