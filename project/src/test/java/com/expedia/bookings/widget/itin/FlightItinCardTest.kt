package com.expedia.bookings.widget.itin


import android.app.Activity
import android.view.LayoutInflater
import android.view.View
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
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.io.File
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinCardTest {

    lateinit private var sut: FlightItinCard
    lateinit private var itinCardData: ItinCardDataFlight

    @Test
    fun flightCheckInLink(){
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

    private fun getActionButtonLayout(): LinearLayout {
        val actionButtonLayout = sut.findViewById(R.id.action_button_layout) as LinearLayout
        return actionButtonLayout
    }

    private fun createSystemUnderTest() {
        val data = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/flight_trips_summary_with_insurance.json"))).readUtf8()
        val jsonObject = JSONObject(data)
        val jsonArray = jsonObject.getJSONArray("responseData")
        val tripFlight = getFlightTrip(jsonArray)

        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.NewLaunchTheme)
        sut = FlightItinCard(activity, null)
        LayoutInflater.from(activity).inflate(R.layout.widget_itin_card, sut)

        itinCardData = ItinCardDataFlight(tripFlight, 0)
        sut.bind(itinCardData)
    }

    private fun getFlightTrip(jsonArray: JSONArray): TripFlight {
        val tripParser = TripParser()
        val tripJsonObj = jsonArray.get(0) as JSONObject
        val flightTrip = tripParser.parseTrip(tripJsonObj)
        return flightTrip.tripComponents[0] as TripFlight
    }

    private fun getUpgradeTextView(): TextView {
        val upgradeText = sut.findViewById(R.id.room_upgrade_available_banner) as TextView
        return upgradeText
    }

    private fun getCheckInTextView(): TextView {
        val checkInTextView = sut.findViewById(R.id.checkin_text_view) as TextView
        return checkInTextView
    }

}
