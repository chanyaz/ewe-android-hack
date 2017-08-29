package com.expedia.bookings.server

import com.expedia.bookings.data.trips.TripHotel
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.data.trips.TripActivity
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.android.json.JSONUtils
import okio.Okio
import org.joda.time.format.ISODateTimeFormat
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.fail

@RunWith(RobolectricRunner::class)
class TripParserTest {

    val roomUpgradeOfferApiUrl = "https://localhost/api/trips/c65fb5fb-489a-4fa8-a007-715b946d3b04/8066893350319/74f89606-241f-4d08-9294-8c17942333dd/1/sGUZBxGESgB2eGM7GeXkhqJuzdi8Ucq1jl7NI9NzcW1mSSoGJ4njkXYWPCT2e__Ilwdc4lgBRnwlanmEgukEJWqNybe4NPSppEUZf9quVqD_kCjh_2HSZY_-K1HvZU-tUQ3h/upgradeOffers"
    val roomUpgradeWebViewLink = "https://localhost/hotelUpgrades/sGUZBxGESgB2eGM7GeXkhqJuzdi8Ucq1jl7NI9NzcW1mSSoGJ4njkXYWPCT2e__Ilwdc4lgBRnwlanmEgukEJWqNybe4NPSppEUZf9quVqD_kCjh_2HSZY_-K1HvZU-tUQ3h/c65fb5fb-489a-4fa8-a007-715b946d3b04/1/upgradeDeals?mcicid=App.Itinerary.Hotel.Upgrade&mobileWebView=true"

    lateinit private var tripFlight: TripFlight

    @Test
    fun roomUpgradePropertiesParsed() {
        val tripParser = TripParser()
        val hotelTripJson = getHotelTripJson(withUpgradeOffer = true)
        val parsedHotelTrip = tripParser.parseTrip(hotelTripJson).tripComponents[0] as TripHotel

        assertEquals(roomUpgradeOfferApiUrl, parsedHotelTrip.property.roomUpgradeOffersApiUrl)
        assertEquals(roomUpgradeWebViewLink, parsedHotelTrip.property.roomUpgradeWebViewUrl)
    }

    @Test
    fun roomsParsed() {
        val tripParser = TripParser()
        val hotelTripJson = getHotelTripJson(withUpgradeOffer = true)
        val parsedHotelTrip = tripParser.parseTrip(hotelTripJson).tripComponents[0] as TripHotel
        val rooms = parsedHotelTrip.rooms
        val room = rooms[0]

        assertEquals(1, rooms.size)
        assertEquals("Deluxe Room, 1 King Bed", room.roomType)
        assertEquals("BOOKED", room.bookingStatus)
        assertEquals("Kevin", room.primaryOccupant?.firstName)
        assertEquals(1, room.otherOccupantInfo?.adultCount)
        assertEquals("1 king bed", room.occupantSelectedRoomOptions?.bedTypeName)
        assertEquals("Free Wireless Internet", room.amenities[0])
    }

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
        } catch (e: Exception) {
            fail("Oops, we shouldn't have ended up here")
        }
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
        } catch (e: Exception) {
            fail("Oops, we shouldn't have ended up here")
        }
        val tripActivity = trip.tripComponents[0] as TripActivity
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
        } catch (e: Exception) {
            fail("Oops, we shouldn't have ended up here")
        }
        val tripActivity = trip.tripComponents[0] as TripActivity
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
        } catch (e: Exception) {
            fail("Oops, we shouldn't have ended up here")
        }
        val tripActivity = trip.tripComponents[0] as TripActivity
        val activity = tripActivity.getActivity()
        assertEquals("8AEE006B-E82D-40C1-A77D-5063EF3D47A9_0_224793_224797", activity.id)
        assertEquals("Shared Shuttle: Detroit International Airport (DTW): Hotels to Airport in Detroit City Center", activity.title)
        assertEquals(0, activity.guestCount)
    }

    @Test
    fun flightDurationExpectedFormat() {
        createFlightTripResponse()
        val duration = "PT4H32M"
        tripFlight.flightTrip.legs[0].setLegDuration(duration)
        val parseLegDurationMinutes = tripFlight.flightTrip.legs[0].durationMinutes()
        assertEquals(272, parseLegDurationMinutes)
    }

    @Test
    fun flightDurationNull() {
        createFlightTripResponse()
        val duration = null
        tripFlight.flightTrip.legs[0].setLegDuration(duration)
        val parseLegDurationMinutes = tripFlight.flightTrip.legs[0].durationMinutes()
        assertEquals(0, parseLegDurationMinutes)
    }

    @Test
    fun flightDurationEmpty() {
        createFlightTripResponse()
        val duration = " "
        tripFlight.flightTrip.legs[0].setLegDuration(duration)
        val parseLegDurationMinutes = tripFlight.flightTrip.legs[0].durationMinutes()
        assertEquals(0, parseLegDurationMinutes)
    }

    @Test
    fun flightDurationUnexpectedFormat() {
        createFlightTripResponse()
        val duration = "2016-12-07T15:00:00-08:00"
        tripFlight.flightTrip.legs[0].setLegDuration(duration)
        val parseLegDurationMinutes = tripFlight.flightTrip.legs[0].durationMinutes()
        assertEquals(0, parseLegDurationMinutes)
    }

    @Test
    fun testTripHotelJSONClone() {
        val tripParser = TripParser()
        val hotelTripJson = getHotelTripJson(false)
        val parsedTripHotel = tripParser.parseTrip(hotelTripJson).tripComponents[0] as TripHotel
        val clonedTripHotel = JSONUtils.clone(parsedTripHotel, TripHotel::class.java)
        assertEquals(parsedTripHotel.property.propertyId, clonedTripHotel.property.propertyId)
        assertEquals(parsedTripHotel.checkInTime, clonedTripHotel.checkInTime)
        assertEquals(parsedTripHotel.checkOutTime, clonedTripHotel.checkOutTime)
        assertEquals(parsedTripHotel.rooms.size, clonedTripHotel.rooms.size)
        assertEquals(parsedTripHotel.rooms[0].roomType, clonedTripHotel.rooms[0].roomType)
    }

    private fun getHotelTripJsonWithISO8061dateString(checkInDate: String, checkOutDate: String): JSONObject {
        val hotelTripJsonObj = getHotelTripJson()

        hotelTripJsonObj.put("checkInDate", checkInDate)
        hotelTripJsonObj.put("checkOutDate", checkOutDate)

        hotelTripJsonObj.remove("checkInDateTime")
        hotelTripJsonObj.remove("checkOutDateTime")

        return hotelTripJsonObj
    }

    private fun getHotelTripJson(withUpgradeOffer: Boolean = false): JSONObject {
        val data = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/hotel_trip_details.json"))).readUtf8()
        val jsonObject = JSONObject(data)
        val responseData = jsonObject.getJSONObject("responseData")
        val hotel = responseData.getJSONArray("hotels").getJSONObject(0)
        val rooms = hotel.getJSONArray("rooms")
        val firstRoom = rooms.getJSONObject(0)
        if (withUpgradeOffer) {
            firstRoom.put("roomUpgradeOfferApiUrl", roomUpgradeOfferApiUrl)
            firstRoom.put("roomUpgradeLink", roomUpgradeWebViewLink)
        }

        return responseData
    }

    private fun createFlightTripResponse() {
        val data = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/flight_trips_summary_with_insurance.json"))).readUtf8()
        val jsonObject = JSONObject(data)
        val jsonArray = jsonObject.getJSONArray("responseData")
        tripFlight = getFlightTrip(jsonArray)!!
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

    @Test
    fun testTripHotelNoRooms() {
        val tripParser = TripParser()
        val parsedHotelTrip = tripParser.parseTrip(getHotelNoRoomsJSON()).tripComponents[0] as TripHotel

        assertEquals(true, parsedHotelTrip.rooms.isEmpty())
    }

    @Test
    fun testTripHotelNoPrimaryOccupant() {
        val tripParser = TripParser()
        val parsedHotelTrip = tripParser.parseTrip(getHotelNoRoomPreferenceJSON("primaryOccupant")).tripComponents[0] as TripHotel
        val rooms = parsedHotelTrip.rooms
        assertEquals(true, rooms.isNotEmpty())

        val firstRoom = rooms[0]
        assertEquals(null, firstRoom.primaryOccupant)
    }

    @Test
    fun testTripHotelNoOtherOccupantInfo() {
        val tripParser = TripParser()
        val parsedHotelTrip = tripParser.parseTrip(getHotelNoRoomPreferenceJSON("otherOccupantInfo")).tripComponents[0] as TripHotel
        val rooms = parsedHotelTrip.rooms
        assertEquals(true, rooms.isNotEmpty())

        val firstRoom = rooms[0]
        assertEquals(null, firstRoom.otherOccupantInfo)
    }

    @Test
    fun testTripHotelNoOccupantSelectedRoomOptions() {
        val tripParser = TripParser()
        val parsedHotelTrip = tripParser.parseTrip(getHotelNoRoomPreferenceJSON("occupantSelectedRoomOptions")).tripComponents[0] as TripHotel
        val rooms = parsedHotelTrip.rooms
        assertEquals(true, rooms.isNotEmpty())

        val firstRoom = rooms[0]
        assertEquals(null, firstRoom.occupantSelectedRoomOptions)
    }

    @Test
    fun testTripHotelNoAmenityIDs() {
        val tripParser = TripParser()
        val parsedHotelTrip = tripParser.parseTrip(getHotelNoRoomAmenities()).tripComponents[0] as TripHotel
        val rooms = parsedHotelTrip.rooms
        assertEquals(true, rooms.isNotEmpty())

        val firstRoom = rooms[0]
        assertEquals(true, firstRoom.amenityIds.isEmpty())
    }

    @Test
    fun testTripHotelNoCheckInPolicies() {
        val tripParser = TripParser()
        val parsedHotelTrip = tripParser.parseTrip(getHotelNoCheckInPolicies()).tripComponents[0] as TripHotel
        val checkInPolicies = parsedHotelTrip.property.checkInPolicies
        assertEquals(true, checkInPolicies.isEmpty())
    }

    @Test
    fun testTripHotelCheckInPolicies() {
        val tripParser = TripParser()
        val parsedHotelTrip = tripParser.parseTrip(getHotelTripJson()).tripComponents[0] as TripHotel
        val checkInPolicies = parsedHotelTrip.property.checkInPolicies
        val testPolicies = listOf("Minimum check-in age is 18", "Check-in time starts at 3 PM")
        assertEquals(testPolicies, checkInPolicies)
    }

    @Test
    fun testTripHotelEmptyCheckInPolicies() {
        val tripParser = TripParser()
        val parsedHotelTrip = tripParser.parseTrip(getHotelNoCheckInPolicies(true)).tripComponents[0] as TripHotel
        val checkInPolicies = parsedHotelTrip.property.checkInPolicies
        assertEquals(true, checkInPolicies.isEmpty())
    }

    @Test
    fun testTripHotelNoChangeCancelRules() {
        val tripParser = TripParser()
        val parsedHotelTrip = tripParser.parseTrip(getHotelNoChangeCancelRules()).tripComponents[0] as TripHotel
        val changeAndCancelRules = parsedHotelTrip.changeAndCancelRules
        assertEquals(true, changeAndCancelRules.isEmpty())
    }

    @Test
    fun testTripHotelChangeCancelRules() {
        val tripParser = TripParser()
        val parsedHotelTrip = tripParser.parseTrip(getHotelTripJson()).tripComponents[0] as TripHotel
        val changeAndCancelRules = parsedHotelTrip.changeAndCancelRules
        val testRules = listOf("We understand that sometimes plans fall through. We do not charge a cancel or change fee. When the property charges such fees in accordance with its own policies, the cost will be passed on to you. Adante Hotel, a C-Two Hotel charges the following cancellation and change fees.",
                "Cancellations or changes made after 3:00PM (Pacific Daylight Time (US & Canada); Tijuana) on Oct 17, 2017 or no-shows are subject to a property fee equal to 100% of the total amount paid for the reservation.",
                "Prices and hotel availability are not guaranteed until full payment is received.If you would like to book multiple rooms, you must use a different name for each room. Otherwise, the duplicate reservation will be canceled by the hotel.")
        assertEquals(testRules, changeAndCancelRules)
    }

    @Test
    fun testTripHotelEmptyChangeCancelRules() {
        val tripParser = TripParser()
        val parsedHotelTrip = tripParser.parseTrip(getHotelNoChangeCancelRules(true)).tripComponents[0] as TripHotel
        val rules = parsedHotelTrip.changeAndCancelRules
        assertEquals(true, rules.isEmpty())
    }

    private fun getHotelNoRoomsJSON(): JSONObject {
        val hotelTripJson = getHotelTripJson()
        val hotel = hotelTripJson.getJSONArray("hotels").getJSONObject(0)
        hotel.remove("rooms")
        return hotelTripJson
    }

    private fun getHotelNoRoomPreferenceJSON(prefToRemove: String): JSONObject {
        val hotelTripJson = getHotelTripJson()
        val roomPrefs = hotelTripJson.getJSONArray("hotels").getJSONObject(0).getJSONArray("rooms").getJSONObject(0).getJSONObject("roomPreferences")
        roomPrefs.remove(prefToRemove)
        return hotelTripJson
    }

    private fun getHotelNoRoomAmenities(): JSONObject {
        val hotelTripJson = getHotelTripJson()
        val room = hotelTripJson.getJSONArray("hotels").getJSONObject(0).getJSONArray("rooms").getJSONObject(0)
        room.remove("amenityIds")
        return hotelTripJson
    }

    private fun getHotelNoCheckInPolicies(isEmpty: Boolean = false): JSONObject {
        val hotelTripJson = getHotelTripJson()
        val hotelPropertyInfo = hotelTripJson.getJSONArray("hotels").getJSONObject(0).getJSONObject("hotelPropertyInfo")
        hotelPropertyInfo.remove("checkInPolicies")
        if (isEmpty) hotelPropertyInfo.put("checkInPolicies", JSONArray())
        return hotelTripJson
    }

    private fun getHotelNoChangeCancelRules(isEmpty: Boolean = false): JSONObject {
        val hotelTripJson = getHotelTripJson()
        val rules = hotelTripJson.getJSONArray("hotels").getJSONObject(0).getJSONObject("rules")
        rules.remove("cancelChangeRulesIntroduction")
        rules.remove("cancelChangeRules")
        if (isEmpty) {
            rules.put("cancelChangeRulesIntroduction", "")
            rules.put("cancelChangeRules", JSONArray())
        }
        return hotelTripJson
    }
}
