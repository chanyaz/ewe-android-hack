package com.expedia.bookings.widget.itin

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightLeg
import com.expedia.bookings.data.FlightTrip
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.TicketingStatus
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.launch.activity.PhoneLaunchActivity
import com.expedia.bookings.server.TripParser
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.TextView
import com.mobiata.flightlib.data.Flight
import okio.Okio
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricRunner::class)
class FlightItinContentGeneratorTest {

    private lateinit var tripFlight: TripFlight
    private lateinit var flightDetailView: View
    private lateinit var sut: FlightItinContentGenerator
    private lateinit var itinCardData: ItinCardDataFlight
    private lateinit var activity: PhoneLaunchActivity

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().get()
        activity.setTheme(R.style.LaunchTheme)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun testFlightInsurance() {
        createSystemUnderTest()
        givenGoodFlightItinDetailView()

        val textView = flightDetailView.findViewById<View>(R.id.insurance_name) as TextView
        assertEquals(View.VISIBLE, textView.visibility)
        assertEquals("Travel Protection - Total Protection Plan", textView.text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun testFlightImageContentDescription() {
        createSystemUnderTest()
        itinCardData = ItinCardDataFlight(tripFlight, 0)
        sut = FlightItinContentGenerator(getContext(), itinCardData)

        val contDesc = sut.listCardContentDescription
        assertEquals("Detroit Nov 18 FLIGHT Itinerary card button.", contDesc)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
    fun testFlightDuration() {
        createSystemUnderTest()
        givenGoodFlightItinDetailView()

        val textView = flightDetailView.findViewById<View>(R.id.flight_duration) as TextView

        assertEquals(View.VISIBLE, textView.visibility)
        assertEquals("Total Duration: 4h 32m", textView.text)
        assertEquals("Total Duration: 4 hour 32 minutes", textView.contentDescription)
    }

    @Test
    fun testAirlinePhoneNumberFeatureOn() {
        createSystemUnderTest()
        givenGoodFlightItinDetailView()

        val expectedLabel = getContext().resources.getString(R.string.flight_itin_airline_support_number_label)
        val expectedPhoneNumber = Db.getAirline(tripFlight.flightTrip.legs[0].firstAirlineCode).mAirlinePhone

        val itinContainer = flightDetailView.findViewById<View>(R.id.itin_shared_info_container) as LinearLayout
        val airlinePhoneView = itinContainer.getChildAt(1)
        val labelTextView = airlinePhoneView.findViewById<View>(R.id.item_label) as TextView
        val phoneNumberTextView = airlinePhoneView.findViewById<View>(R.id.item_text) as TextView

        assertEquals(expectedLabel, labelTextView.text)
        assertEquals(expectedPhoneNumber, phoneNumberTextView.text)
    }

    @Test
    fun testAirlinePhoneNumberWhenPrimaryFlightCodeNull() {
        val parentContainer = FrameLayout(getContext())

        givenTripFlightWithNoPrimaryFlightCode()

        itinCardData = ItinCardDataFlight(tripFlight, 0)
        sut = FlightItinContentGenerator(getContext(), itinCardData)
        val airlinePhoneView = callAddAirlineSupportNumber(parentContainer)

        assertEquals(0, parentContainer.childCount)
        assertFalse(airlinePhoneView)
    }

    @Test
    fun showPendingFlightStatus() {
        createSystemUnderTest()
        givenTripFlightWithInProgressTicketingStatus()

        itinCardData = ItinCardDataFlight(tripFlight, 0)
        sut = FlightItinContentGenerator(getContext(), itinCardData)
        val textView = flightDetailView.findViewById<View>(R.id.item_text) as TextView

        assertEquals("Booking Confirmed. Ticketing in progress.", textView.text)
    }

    private fun givenTripFlightWithNoPrimaryFlightCode() {
        tripFlight = TripFlight()
        val flightTrip = FlightTrip()
        val flightLeg = FlightLeg()
        flightLeg.addSegment(Flight())
        flightTrip.addLeg(flightLeg)
        tripFlight.flightTrip = flightTrip
    }

    private fun givenTripFlightWithInProgressTicketingStatus() {
        tripFlight.ticketingStatus = TicketingStatus.INPROGRESS
        givenGoodFlightItinDetailView()
    }

    private fun createSystemUnderTest() {
        val data = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/flight_trips_summary_with_insurance.json"))).readUtf8()
        val jsonObject = JSONObject(data)
        val jsonArray = jsonObject.getJSONArray("responseData")
        tripFlight = getFlightTrip(jsonArray)!!
    }

    private fun givenGoodFlightItinDetailView() {
        val itinFlight = getItinGeneratorFlight()
        val viewGroupContainer = FrameLayout(getContext())
        val flightDetailView = itinFlight.getDetailsView(null, viewGroupContainer)
        this.flightDetailView = flightDetailView
    }

    private fun getItinGeneratorFlight(): FlightItinContentGenerator {
        return ItinContentGenerator.createGenerator(getContext(), ItinCardDataFlight(tripFlight, 0)) as FlightItinContentGenerator
    }

    private fun getFlightTrip(jsonArray: JSONArray): TripFlight? {
        val tripParser = TripParser()

        var x = 0
        while (x < jsonArray.length()) {
            val tripJsonObj = jsonArray.get(x) as JSONObject
            val tripObj = tripParser.parseTrip(tripJsonObj)
            val tripComponent = tripObj.tripComponents[0]
            if (tripComponent is TripFlight) {
                return tripComponent
            }
            x++
        }
        return null
    }

    private fun getContext(): Context {
        return activity
    }

    private fun callAddAirlineSupportNumber(parentContainer: FrameLayout): Boolean {
        val makeAddAirlineSupportNumber = sut.javaClass.getDeclaredMethod("addAirlineSupportNumber", ViewGroup::class.java)
        makeAddAirlineSupportNumber.isAccessible = true
        return makeAddAirlineSupportNumber.invoke(sut, parentContainer) as Boolean
    }
}
