package com.expedia.bookings.data.trips

import com.expedia.bookings.test.robolectric.RobolectricRunner
import okio.Okio
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class TripHotelTest {
    @Test
    fun testFromJSON() {
        val tripHotel = TripHotel()
        tripHotel.fromJson(getTripHotelJSON())

        assertEquals("Crest Hotel", tripHotel.property.name)
        assertEquals("17669432", tripHotel.property.propertyId)
        assertEquals("noon", tripHotel.checkInTime)
        assertEquals("noon", tripHotel.checkOutTime)
        assertEquals(1, tripHotel.guests)
        assertEquals("Nina Ricci", tripHotel.primaryTraveler.fullName)
        assertEquals(listOf(128, 1073742786, 1073742787), tripHotel.rooms[0].amenityIds)
        assertEquals("1 queen bed", tripHotel.rooms[0].occupantSelectedRoomOptions?.bedTypeName)
    }

    @Test
    fun testFromJSONNoRooms() {
        val tripHotel = TripHotel()
        tripHotel.fromJson(getTripHotelJSONNoRooms())

        assertEquals(true, tripHotel.rooms.isEmpty())
    }

    private fun getTripHotelJSON(): JSONObject {
        val data = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/trip_hotel_data.json"))).readUtf8()
        val jsonObject = JSONObject(data)
        return jsonObject
    }

    private fun getTripHotelJSONNoRooms(): JSONObject {
        val jsonObject = getTripHotelJSON()
        jsonObject.remove("rooms")
        return jsonObject
    }
}
