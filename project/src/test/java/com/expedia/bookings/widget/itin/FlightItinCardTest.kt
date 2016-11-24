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
import okio.Okio
import org.joda.time.DateTime
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowResourcesEB
import java.io.File
import kotlin.test.assertEquals


@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowResourcesEB::class))
class FlightItinCardTest {

    lateinit private var sut: ItinCard<ItinCardDataFlight>
    lateinit private var itinCardData: ItinCardDataFlight

    @Test
    fun actionButtonVisibleForExpandedCard() {
        createSystemUnderTest()
        sut.expand(false)
        sut.bind(itinCardData)
        assertEquals(View.VISIBLE, getActionButtonLayout().visibility)
    }

    @Test
    fun actionButtonInvisibleForCollapsedCard(){
        createSystemUnderTest()
        sut.collapse(false)
        assertEquals(View.GONE, getActionButtonLayout().visibility)
    }

    @Test
    fun actionButtonVisibileWhenCheckInAvailable(){
        createSystemUnderTest()
        val localTimePlusTwoHours = DateTime.now().plusHours(2)
        itinCardData.tripComponent.startDate = localTimePlusTwoHours
        sut.expand(false)
        sut.bind(itinCardData)
        assertEquals(View.VISIBLE, getActionButtonLayout().visibility)
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
        sut = ItinCard<ItinCardDataFlight>(activity)
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
}
