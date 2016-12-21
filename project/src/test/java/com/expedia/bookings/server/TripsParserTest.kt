package com.expedia.bookings.server

import com.expedia.bookings.data.trips.TripHotel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import okio.Okio
import org.joda.time.format.DateTimeFormatter
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
        val jsonArray = jsonObject.getJSONArray("responseData")
        val hotelTripJsonObj = jsonArray.get(0) as JSONObject

        hotelTripJsonObj.put("checkInDate", checkInDate)
        hotelTripJsonObj.put("checkOutDate", checkOutDate)

        hotelTripJsonObj.remove("checkInDateTime")
        hotelTripJsonObj.remove("checkOutDateTime")

        return hotelTripJsonObj
    }
}
