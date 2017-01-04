package com.expedia.bookings.widget.itin.support

import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.data.trips.TripHotel
import com.expedia.bookings.server.TripParser
import okio.Okio
import org.joda.time.DateTime
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class ItinCardDataHotelBuilder {

    private var upgradeableRoom = false
    private var vipEnabled = false
    private var isSharedItin = false
    private var checkInDate: DateTime = DateTime.now().plusDays(2)
    private var checkOutDate: DateTime? = checkInDate.plusDays(3)

    fun build(): ItinCardDataHotel {
        val itinCardDataHotel = makeHotel()
        val parentTrip = itinCardDataHotel.tripComponent.parentTrip

        itinCardDataHotel.property.setIsVipAccess(vipEnabled)
        parentTrip.setIsTripUpgradable(upgradeableRoom)
        parentTrip.setIsShared(isSharedItin)

        val tripHotel = itinCardDataHotel.tripComponent
        tripHotel.startDate = checkInDate
        tripHotel.endDate = checkOutDate

        return itinCardDataHotel
    }

    fun withUpgradeableRoom(): ItinCardDataHotelBuilder {
        upgradeableRoom = true
        return this
    }

    fun withVipEnabled(enabled: Boolean): ItinCardDataHotelBuilder {
        vipEnabled = enabled
        return this
    }

    fun isSharedItin(isShared: Boolean): ItinCardDataHotelBuilder {
        isSharedItin = isShared
        return this
    }

    fun withCheckInDate(checkInDate: DateTime): ItinCardDataHotelBuilder {
        this.checkInDate = checkInDate
        return this
    }

    fun withCheckOutDate(checkOutDate: DateTime?): ItinCardDataHotelBuilder {
        this.checkOutDate = checkOutDate
        return this
    }

    private fun makeHotel(): ItinCardDataHotel {
        val fileName = "hotel_trip_details"
        val tripHotel = fetchTripHotel(fileName)

        return ItinCardDataHotel(tripHotel)
    }

    private fun fetchTripHotel(jsonFileName: String): TripHotel {
        val data = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/$jsonFileName.json"))).readUtf8()
        val jsonObject = JSONObject(data)
        val jsonArray = jsonObject.getJSONArray("responseData")
        val tripHotel = getHotelTrip(jsonArray)!!
        return tripHotel
    }

    private fun getHotelTrip(jsonArray: JSONArray): TripHotel? {
        val tripParser = TripParser()

        var index = 0
        while (index < jsonArray.length()) {
            val tripJsonObj = jsonArray.get(index) as JSONObject
            val tripObj = tripParser.parseTrip(tripJsonObj)
            val tripComponent = tripObj.tripComponents[0]
            if (tripComponent is TripHotel) {
                return tripComponent
            }
            index++
        }
        return null
    }
}
