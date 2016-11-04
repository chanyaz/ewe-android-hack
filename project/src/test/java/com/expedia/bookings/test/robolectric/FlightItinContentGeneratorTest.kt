package com.expedia.bookings.test.robolectric

import android.content.Context
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.server.TripParser
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.itin.ItinContentGenerator
import okio.Okio
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.io.File
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinContentGeneratorTest {

    lateinit private var tripFlight: TripFlight

    @Before
    fun setup() {
        val data = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/trips_summary_with_insurance.json"))).readUtf8()
        val jsonObject = JSONObject(data)
        val jsonArray = jsonObject.getJSONArray("responseData")
        tripFlight = getFlightTrip(jsonArray)!!
    }

    @Test
    fun testFlightInsurance() {
        val itinFlight = getItinGeneratorFlight()
        val viewGroupContainer = FrameLayout(getContext())
        val flightDetailView = itinFlight.getDetailsView(null, viewGroupContainer)
        val textView = flightDetailView.findViewById(R.id.insurance_name) as TextView
        assertEquals(View.VISIBLE, textView.visibility)
        assertEquals("Travel Protection - Total Protection Plan", textView.text)
    }

    private fun getItinGeneratorFlight(): ItinContentGenerator<ItinCardDataFlight> {
        return ItinContentGenerator.createGenerator(getContext(), ItinCardDataFlight(tripFlight, 0)) as ItinContentGenerator<ItinCardDataFlight>
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
        return RuntimeEnvironment.application
    }
}
