package com.expedia.bookings.widget.itin

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.server.TripParser
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.TextView
import okio.Okio
import org.joda.time.DateTime
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.io.File
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinCardTest {
    private lateinit var activity: Activity
    private lateinit var sut: FlightItinCard
    private lateinit var itinCardData: ItinCardDataFlight

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.LaunchTheme)
    }

    @Test
    fun flightCheckInLink() {
        createSystemUnderTest()
        val localTimePlusTwoHours = DateTime.now().plusHours(2)
        itinCardData.tripComponent.startDate = localTimePlusTwoHours
        sut.expand(false)
        assertEquals(View.VISIBLE, getCheckInTextView().visibility)

        val trip = itinCardData.tripComponent as TripFlight
        assertEquals("https://wwwexpediacom.trunk-stubbed.sb.karmalab.net/trips/airline/checkin?airlineCode=WW&firstName=sandi&lastName=ma&confirmation=DL5HBGT&departureAirport=BOS&flightNumber=126&email=sptest%40expedia.com&ticketNumber=&flightDay=27&flightMonth=1", trip.checkInLink)
    }

    @Test
    fun actionButtonVisibleForExpandedCardAfterReload() {
        createSystemUnderTest()
        sut.expand(false)
        sut.rebindExpandedCard(itinCardData) // called on receipt of reload response
        sut.setShowSummary(true)
        assertEquals(View.VISIBLE, getActionButtonLayout().visibility)
    }

    @Test
    fun actionButtonInvisibleForCollapsedCard() {
        createSystemUnderTest()
        sut.collapse(false)
        assertEquals(View.GONE, getActionButtonLayout().visibility)
    }

    @Test
    fun testFlightDurationHourMin() {
        createSystemUnderTest()
        sut.expand(false)
        assertEquals("Total Duration: 4 hour 32 minutes", getFlightDurationTextView().contentDescription)
    }

    @Test
    fun actionButtonVisibileWhenCheckInAvailable() {
        createSystemUnderTest()
        val localTimePlusTwoHours = DateTime.now().plusHours(2)
        itinCardData.tripComponent.startDate = localTimePlusTwoHours
        sut.expand(false)
        assertEquals(View.VISIBLE, getActionButtonLayout().visibility)
    }

    @Test
    fun hotelUpgradeBannerDoesNotShowOnFlights() {
        createSystemUnderTest()
        sut.expand(false)
        assertEquals(View.GONE, getUpgradeTextView().visibility)
    }

    @Test
    fun imageViewContDescFlightDetailsView() {
        createSystemUnderTest()
        sut.expand(false)
        val imageView = sut.findViewById<View>(R.id.header_image_container)

        assertEquals("Image gallery", imageView.contentDescription)
    }

    @Test
    fun testSummaryHiddenWhenOutsideOf24HourCheckInWindow() {
        val startDate = DateTime.now().plusDays(2)
        createSystemUnderTest(startDate)

        val summaryLayout = sut.findViewById<ViewGroup>(R.id.summary_section_layout)
        val checkInLayout = sut.findViewById<ViewGroup>(R.id.checkin_layout)

        assertEquals(View.GONE, summaryLayout.visibility, "summary_section_layout visibility is not GONE.")
        assertEquals(View.GONE, checkInLayout.visibility, "checkin_layout visibility is not GONE.")
    }

    @Test
    fun testSummaryHiddenWhenInsideOf24HourCheckInWindow() {
        val startDate = DateTime.now().plusHours(2)
        createSystemUnderTest(startDate)

        val summaryLayout = sut.findViewById<ViewGroup>(R.id.summary_section_layout)
        val checkInLayout = sut.findViewById<ViewGroup>(R.id.checkin_layout)

        assertEquals(View.GONE, summaryLayout.visibility, "summary_section_layout visibility is not GONE.")
        assertEquals(View.VISIBLE, checkInLayout.visibility, "checkin_layout visibility is not VISIBLE.")
    }

    private fun getActionButtonLayout(): LinearLayout {
        return sut.findViewById<View>(R.id.action_button_layout) as LinearLayout
    }

    private fun getFlightDurationTextView(): TextView {
        return sut.findViewById<View>(R.id.flight_duration) as TextView
    }

    private fun createSystemUnderTest(startDate: DateTime? = null) {
        val data = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/flight_trips_summary_with_insurance.json"))).readUtf8()
        val jsonObject = JSONObject(data)
        val jsonArray = jsonObject.getJSONArray("responseData")
        val tripFlight = getFlightTrip(jsonArray)

        sut = FlightItinCard(activity, null)
        LayoutInflater.from(activity).inflate(R.layout.widget_itin_card, sut)

        itinCardData = TestItinCardDataFlight(tripFlight, 0, startDate)
        sut.bind(itinCardData)
    }

    private fun getFlightTrip(jsonArray: JSONArray): TripFlight {
        val tripParser = TripParser()
        val tripJsonObj = jsonArray.get(0) as JSONObject
        val flightTrip = tripParser.parseTrip(tripJsonObj)
        return flightTrip.tripComponents[0] as TripFlight
    }

    private fun getUpgradeTextView(): TextView {
        return sut.findViewById<View>(R.id.room_upgrade_available_banner) as TextView
    }

    private fun getCheckInTextView(): TextView {
        return sut.findViewById<View>(R.id.checkin_text_view) as TextView
    }

    private class TestItinCardDataFlight(parent: TripFlight?, leg: Int, val overrideStartDate: DateTime?) : ItinCardDataFlight(parent, leg) {
        override fun getStartDate(): DateTime {
            return overrideStartDate ?: super.getStartDate()
        }
    }
}
