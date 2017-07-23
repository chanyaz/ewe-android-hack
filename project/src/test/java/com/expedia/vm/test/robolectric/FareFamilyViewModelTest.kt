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
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.vm.FareFamilyViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import java.util.ArrayList
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
    fun testFareFamilyCardViewStringsForFreshCreateTrip(){
        val deltaPriceSubscriber = TestSubscriber<String>()
        val selectedClassSubscriber = TestSubscriber<CharSequence>()

        sut.deltaPriceObservable.subscribe(deltaPriceSubscriber)
        sut.selectedClassObservable.subscribe(selectedClassSubscriber)
        sut.tripObservable.onNext(tripResponseWithFareFamilyAvailable())
        assertEquals("$42.00",deltaPriceSubscriber.onNextEvents[0])
        assertEquals("Selected: Economy",selectedClassSubscriber.onNextEvents[0].toString())

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

    private fun tripResponseWithFareFamilyAvailable(): FlightCreateTripResponse {
        val fareFamilyProduct = FlightTripResponse.FareFamilies()
        val fareFamilyDetails = FlightTripResponse.FareFamilyDetails()
        fareFamilyDetails.fareFamilyName = "Economy"
        fareFamilyDetails.fareFamilyCode = "Economy"
        fareFamilyDetails.totalPrice = Money("210.00", "USD")
        val money = Money("42.00", "USD")
        money.formattedPrice = "$42.00"
        fareFamilyDetails.deltaTotalPrice = money
        fareFamilyProduct.fareFamilyDetails = arrayOf(fareFamilyDetails)
        val trip = tripResponseWithoutFareFamilyAvailable()
        trip.fareFamilies = fareFamilyProduct

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
        return trip
    }
}
