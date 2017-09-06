package com.expedia.bookings.widget.itin.support

import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.server.TripParser
import okio.Okio
import org.joda.time.DateTime
import org.json.JSONObject
import java.io.File

class ItinCardDataFlightBuilder {

    fun build(airAttachEnabled:Boolean = false): ItinCardDataFlight {
        val itinCardDataFlight = makeFlight()

        itinCardDataFlight.setShowAirAttach(airAttachEnabled)

        return itinCardDataFlight
    }

    private fun makeFlight(): ItinCardDataFlight {
        val filename = "flight_trip_details"
        val tripFlight = fetchTripFlight(filename)
        return ItinCardDataFlight(tripFlight, 0)
    }

    private fun fetchTripFlight(jsonFileName: String): TripFlight {
        val data = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/$jsonFileName.json"))).readUtf8()
        val jsonObject = JSONObject(data)
        val jsonResponseData = jsonObject.getJSONObject("responseData")
        fixTimes(jsonResponseData)
        return getFlightTrip(jsonResponseData)!!
    }

    private fun fixTimes(jsonObject: JSONObject) {
        val now = DateTime.now()
        val startTime = now.plusDays(30)
        val endTime = startTime.plusDays(7)

        fixTime(jsonObject, "startTime", startTime)
        fixTime(jsonObject, "endTime", endTime)

        // for now, just handle the one case we need to, which is a one-way flight with one segment
        val segmentJson = jsonObject.getJSONArray("flights").getJSONObject(0)
                .getJSONArray("legs").getJSONObject(0)
                .getJSONArray("segments").getJSONObject(0)
        fixTime(segmentJson, "departureTime", startTime)
        fixTime(segmentJson, "arrivalTime", endTime)
    }

    private fun fixTime(jsonObject: JSONObject, name: String, dateTime: DateTime) {
        if (jsonObject.has(name)) {
            jsonObject.getJSONObject(name).put("epochSeconds", dateTime.millis / 1000)
            jsonObject.getJSONObject(name).put("timeZoneOffsetSeconds", dateTime.zone.getOffset(dateTime) / 1000)
        }
    }

    private fun getFlightTrip(jsonObject: JSONObject): TripFlight? {
        val tripParser = TripParser()

        val tripObj = tripParser.parseTrip(jsonObject)
        val tripComponent = tripObj.tripComponents[0]
        if (tripComponent is TripFlight) {
            return tripComponent
        } else {
            return null
        }
    }
}