package com.expedia.bookings.server

import com.expedia.bookings.data.trips.TripHotel
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.data.trips.TripActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import okio.Okio
import org.joda.time.format.ISODateTimeFormat
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.fail

@RunWith(RobolectricRunner::class)
class TripsParserTest {

    @Test
    fun iso8601Parsing() {
        val checkInDate = "2016-12-07T15:00:00-08:00"
        val checkOutDate = "2016-12-11T11:00:00-08:00"
        val hotelTripJsonObj = getHotelTripJsonWithISO8061dateString(checkInDate, checkOutDate)
        val tripParser = TripParser()

        try {
            val trip = tripParser.parseTrip(hotelTripJsonObj)
            val hotelTripComponent = trip.tripComponents[0] as TripHotel

            val parser = ISODateTimeFormat.dateTimeParser()

            assertEquals(parser.parseDateTime(checkInDate), hotelTripComponent.startDate)
            assertEquals(parser.parseDateTime(checkOutDate), hotelTripComponent.endDate)
        }
        catch (e: Exception) {
            fail("Oops, we shouldn't have ended up here")
        }
    }

    private fun getHotelTripJsonWithISO8061dateString(checkInDate: String, checkOutDate: String): JSONObject {
        val data = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/hotel_trip_details.json"))).readUtf8()
        val jsonObject = JSONObject(data)
        val hotelTripJsonObj = jsonObject.getJSONObject("responseData")

        hotelTripJsonObj.put("checkInDate", checkInDate)
        hotelTripJsonObj.put("checkOutDate", checkOutDate)

        hotelTripJsonObj.remove("checkInDateTime")
        hotelTripJsonObj.remove("checkOutDateTime")

        return hotelTripJsonObj
    }

    @Test
    fun testLXTripParsingMultipleTravelerType() {
        val data = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/lx_trip_details.json"))).readUtf8()
        val jsonObject = JSONObject(data)
        val jsonArray = jsonObject.getJSONArray("responseData")
        val lxTripJsonObj = jsonArray.get(0) as JSONObject

        val tripParser = TripParser()
        var trip: Trip
        try {
            trip = tripParser.parseTrip(lxTripJsonObj)
        }
        catch (e: Exception) {
            fail("Oops, we shouldn't have ended up here")
        }
        val tripActivity = trip.tripComponents.get(0) as TripActivity
        val activity = tripActivity.getActivity()
        assertEquals("200E974C-C7DA-445E-A392-DD12578A96A0_0_358734_358736", activity.id)
        assertEquals("Day Trip to New York by Train with Hop-on Hop-Off Pass: Full-Day Excursion", activity.title)
        assertEquals(5, activity.guestCount)
    }

    @Test
    fun testLXTripParsingSingleTravelerType() {
        val data = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/lx_trip_details.json"))).readUtf8()
        val jsonObject = JSONObject(data)
        val jsonArray = jsonObject.getJSONArray("responseData")
        val lxTripJsonObj = jsonArray.get(1) as JSONObject

        val tripParser = TripParser()
        var trip: Trip
        try {
            trip = tripParser.parseTrip(lxTripJsonObj)
        }
        catch (e: Exception) {
            fail("Oops, we shouldn't have ended up here")
        }
        val tripActivity = trip.tripComponents.get(0) as TripActivity
        val activity = tripActivity.getActivity()
        assertEquals("8AEE006B-E82D-40C1-A77D-5063EF3D47A9_0_224793_224797", activity.id)
        assertEquals("Shared Shuttle: Detroit International Airport (DTW): Hotels to Airport in Detroit City Center", activity.title)
        assertEquals(1, activity.guestCount)
    }

    @Test
    fun testLXTripParsingNoTraveler() {
        val data = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/lx_trip_details.json"))).readUtf8()
        val jsonObject = JSONObject(data)
        val jsonArray = jsonObject.getJSONArray("responseData")
        val lxTripJsonObj = jsonArray.get(2) as JSONObject

        val tripParser = TripParser()
        var trip: Trip
        try {
            trip = tripParser.parseTrip(lxTripJsonObj)
        }
        catch (e: Exception) {
            fail("Oops, we shouldn't have ended up here")
        }
        val tripActivity = trip.tripComponents.get(0) as TripActivity
        val activity = tripActivity.getActivity()
        assertEquals("8AEE006B-E82D-40C1-A77D-5063EF3D47A9_0_224793_224797", activity.id)
        assertEquals("Shared Shuttle: Detroit International Airport (DTW): Hotels to Airport in Detroit City Center", activity.title)
        assertEquals(0, activity.guestCount)
    }
}
