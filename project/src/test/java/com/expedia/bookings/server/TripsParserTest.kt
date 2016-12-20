package com.expedia.bookings.server

import com.expedia.bookings.test.robolectric.RobolectricRunner
import okio.Okio
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.fail

@RunWith(RobolectricRunner::class)
class TripsParserTest {

    @Test
    fun dontBlowUpOnParse() {
        val data = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/hotel_trip_details.json"))).readUtf8()
        val jsonObject = JSONObject(data)
        val jsonArray = jsonObject.getJSONArray("responseData")
        val hotelTripJsonObj = jsonArray.get(0) as JSONObject

        val tripParser = TripParser()

        try {
            tripParser.parseTrip(hotelTripJsonObj)
        }
        catch (e: Exception) {
            fail("Oops, we shouldn't have ended up here")
        }
    }
}
