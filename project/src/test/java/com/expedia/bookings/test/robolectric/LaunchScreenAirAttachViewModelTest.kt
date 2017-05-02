package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.AirAttach
import com.expedia.bookings.data.FlightLeg
import com.expedia.bookings.data.HotelSearchParams
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.vm.launch.LaunchScreenAirAttachViewModel
import com.mobiata.flightlib.data.Flight
import com.mobiata.flightlib.data.Waypoint
import org.joda.time.LocalDateTime
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class LaunchScreenAirAttachViewModelTest {

    lateinit private var sut: LaunchScreenAirAttachViewModel
    lateinit private var flightTrip: Trip
    lateinit private var view: View
    lateinit private var context: Context

    val firstLineSubsciber = TestSubscriber<String>()
    val secondLineSubsciber = TestSubscriber<String>()
    val offerExpiresSubsciber = TestSubscriber<String>()

    @Before
    fun before() {
        context = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(context).defaultTripComponents()
        view = LayoutInflater.from(context).inflate(R.layout.launch_screen_air_attach_card, null)
    }

    private fun createSystemUnderTest(hotelSearchParams: HotelSearchParams, cityName: String, epochSecs: Long = 200L) {
        flightTrip = createFlightTrip(epochSecs)
        sut = LaunchScreenAirAttachViewModel(context, view, flightTrip, hotelSearchParams, cityName)
    }

    @Test
    fun testAirAttachVariant1Displayed() {
        enableABTest()
        AbacusTestUtils.bucketTestWithVariant(13345, 1)

        val expirationEpochSec = LocalDateTime.now().plusDays(20).plusHours(8).toDateTime().millis / 1000
        val contentDesc = "Offer expires in 20 days Up to 55% off San Francisco Hotels Save on a hotel because you booked a flight  Button"

        createSystemUnderTest(createHotelSearchParams(), "San Francisco", expirationEpochSec)
        airAttachMessageSubscribe()

        assertEquals("Up to 55% off San Francisco Hotels", sut.firstLineObserver.value)
        assertEquals("Save on a hotel because you booked a flight", sut.secondLineObserver.value)
        assertEquals("Offer expires in 20 days", sut.offerExpiresObserver.value)
        assertEquals(contentDesc, view.contentDescription.toString())
    }

    @Test
    fun testAirAttachVariant2Displayed() {
        enableABTest()
        AbacusTestUtils.bucketTestWithVariant(13345, 2)

        val expirationEpochSec = LocalDateTime.now().plusHours(5).toDateTime().millis / 1000
        val contentDesc = "Offer expires in 4 hours Because You Booked a Flight Save up to 55% off San Francisco Hotels  Button"

        createSystemUnderTest(createHotelSearchParams(), "San Francisco", expirationEpochSec)
        airAttachMessageSubscribe()

        assertEquals("Because You Booked a Flight", sut.firstLineObserver.value)
        assertEquals("Save up to 55% off San Francisco Hotels", sut.secondLineObserver.value)
        assertEquals("Offer expires in 4 hours", sut.offerExpiresObserver.value)
        assertEquals(contentDesc, view.contentDescription.toString())
    }

    @Test
    fun testAirAttachExpiresSoon() {
        enableABTest()
        AbacusTestUtils.bucketTestWithVariant(13345, 1)

        val expirationEpochSec = LocalDateTime.now().toDateTime().millis / 1000
        val contentDesc = "Offer expires soon Up to 55% off San Francisco Hotels Save on a hotel because you booked a flight  Button"

        createSystemUnderTest(createHotelSearchParams(), "San Francisco", expirationEpochSec)
        airAttachMessageSubscribe()

        assertEquals("Offer expires soon", sut.offerExpiresObserver.value)
        assertEquals(contentDesc, view.contentDescription.toString())
    }

    private fun airAttachMessageSubscribe() {
        sut.firstLineObserver.subscribe(firstLineSubsciber)
        sut.secondLineObserver.subscribe(secondLineSubsciber)
        sut.offerExpiresObserver.subscribe(offerExpiresSubsciber)
    }

    private fun enableABTest() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppShowAirAttachMessageOnLaunchScreen)
    }

    private fun createHotelSearchParams(): HotelSearchParams {
        val leg = FlightLeg()
        val segment = Flight()
        leg.addSegment(segment)
        val waypoint = Waypoint(2)
        segment.destinationWaypoint = waypoint
        waypoint.mAirportCode = "SFO"
        val params = HotelSearchParams.fromFlightParams("1234", leg, null, 1, null);

        return params
    }

    private fun createAirAttach(expirationEpochSec: Long): AirAttach {
        val jsonObj = JSONObject()
        val offerExpiresObj = JSONObject()

        offerExpiresObj.put("epochSeconds", expirationEpochSec)
        offerExpiresObj.put("timeZoneOffsetSeconds", -28800)
        jsonObj.put("airAttachQualified", true)
        jsonObj.put("offerExpiresTime", offerExpiresObj)

        val airAttach = AirAttach(jsonObj)
        val toJson = airAttach.toJson()
        val airAttachFromJson = AirAttach()
        airAttachFromJson.fromJson(toJson)

        return airAttach
    }

    private fun createFlightTrip(expirationEpochSec: Long): Trip {
        flightTrip = Mockito.mock(Trip::class.java)
        val airAttach = createAirAttach(expirationEpochSec)
        Mockito.`when`(flightTrip.airAttach).thenReturn(airAttach)

        return flightTrip
    }
}
