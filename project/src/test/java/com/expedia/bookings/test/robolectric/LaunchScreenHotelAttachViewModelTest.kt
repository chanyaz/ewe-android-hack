package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.AirAttach
import com.expedia.bookings.data.DeprecatedHotelSearchParams
import com.expedia.bookings.data.FlightLeg
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.utils.Ui
import com.expedia.vm.launch.LaunchScreenHotelAttachViewModel
import com.mobiata.flightlib.data.Flight
import com.mobiata.flightlib.data.Waypoint
import org.joda.time.LocalDateTime
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class LaunchScreenHotelAttachViewModelTest {

    private lateinit var sut: LaunchScreenHotelAttachViewModel
    private lateinit var flightTrip: Trip
    private lateinit var view: View
    private lateinit var context: Context

    val firstLineSubscriber = TestObserver<String>()
    val secondLineSubscriber = TestObserver<String>()
    val offerExpiresSubscriber = TestObserver<String>()

    @Before
    fun before() {
        context = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(context).defaultTripComponents()
        view = LayoutInflater.from(context).inflate(R.layout.launch_screen_hotel_mip_card, null)
    }

    private fun createSystemUnderTest(hotelSearchParams: DeprecatedHotelSearchParams, cityName: String, expiryDateTime: LocalDateTime) {
        flightTrip = createFlightTrip(expiryDateTime)
        sut = LaunchScreenHotelAttachViewModel(context, view, flightTrip, hotelSearchParams, cityName)
    }

    //@Test
    fun testAirAttachContentDisplayed() {
        val expiryDateTime = LocalDateTime.now().plusHours(5)
        val contentDesc = "Offer expires in 4 hours. Because You Booked a Flight. Save up to 55% off San Francisco Hotels. Button"

        createSystemUnderTest(createHotelSearchParams(), "San Francisco", expiryDateTime)
        airAttachMessageSubscribe()

        assertEquals("Because You Booked a Flight", sut.firstLineObserver.value)
        assertEquals("Save up to 55% off San Francisco Hotels", sut.secondLineObserver.value)
        assertEquals("Offer expires in 4 hours", sut.offerExpiresObserver.value)
        assertEquals(contentDesc, view.contentDescription.toString())
    }

    @Test
    fun testAirAttachExpiresSoon() {
        val expiryDateTime = LocalDateTime.now()
        val contentDesc = "Offer expires soon. Because You Booked a Flight. Save up to 55% off San Francisco Hotels. Button"

        createSystemUnderTest(createHotelSearchParams(), "San Francisco", expiryDateTime)
        airAttachMessageSubscribe()

        assertEquals("Offer expires soon", sut.offerExpiresObserver.value)
        assertEquals(contentDesc, view.contentDescription.toString())
    }

    private fun airAttachMessageSubscribe() {
        sut.firstLineObserver.subscribe(firstLineSubscriber)
        sut.secondLineObserver.subscribe(secondLineSubscriber)
        sut.offerExpiresObserver.subscribe(offerExpiresSubscriber)
    }

    private fun createHotelSearchParams(): DeprecatedHotelSearchParams {
        val leg = FlightLeg()
        val segment = Flight()
        leg.addSegment(segment)
        val waypoint = Waypoint(2)
        segment.destinationWaypoint = waypoint
        waypoint.mAirportCode = "SFO"
        val params = DeprecatedHotelSearchParams.fromFlightParams("1234", leg, null, 1, null)

        return params
    }

    private fun createAirAttach(expiryLocalDateTime: LocalDateTime): AirAttach {
        val jsonObj = JSONObject()
        val offerExpiresObj = JSONObject()
        val expiryDateTime = expiryLocalDateTime.toDateTime()

        offerExpiresObj.put("epochSeconds", expiryDateTime.millis / 1000)
        offerExpiresObj.put("timeZoneOffsetSeconds", expiryDateTime.getZone().getOffset(expiryDateTime) / 1000)
        jsonObj.put("airAttachQualified", true)
        jsonObj.put("offerExpiresTime", offerExpiresObj)

        val airAttach = AirAttach(jsonObj)
        val toJson = airAttach.toJson()
        val airAttachFromJson = AirAttach()
        airAttachFromJson.fromJson(toJson)

        return airAttach
    }

    private fun createFlightTrip(expiryDateTime: LocalDateTime): Trip {
        flightTrip = Mockito.mock(Trip::class.java)
        val airAttach = createAirAttach(expiryDateTime)
        Mockito.`when`(flightTrip.airAttach).thenReturn(airAttach)

        return flightTrip
    }
}
