package com.expedia.bookings.widget.itin.support

import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.server.TripParser
import okio.Okio
import org.json.JSONObject
import java.io.File

class ItinCardDataFlightBuilder {

    fun build(airAttachEnabled:Boolean = false): ItinCardDataFlight {
        val itinCardDataFlight = makeFlight()
        val parentTrip = itinCardDataFlight.tripComponent.parentTrip


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
        val tripFlight = getFlightTrip(jsonResponseData)!!
        return tripFlight
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