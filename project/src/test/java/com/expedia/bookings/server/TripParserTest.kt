package com.expedia.bookings.server

import com.expedia.bookings.data.trips.TripHotel
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
import kotlin.test.assertNotNull
import kotlin.test.fail

@RunWith(RobolectricRunner::class)
class TripParserTest {

    private val roomUpgradeOfferApiUrl = "https://localhost/api/trips/c65fb5fb-489a-4fa8-a007-715b946d3b04/8066893350319/74f89606-241f-4d08-9294-8c17942333dd/1/sGUZBxGESgB2eGM7GeXkhqJuzdi8Ucq1jl7NI9NzcW1mSSoGJ4njkXYWPCT2e__Ilwdc4lgBRnwlanmEgukEJWqNybe4NPSppEUZf9quVqD_kCjh_2HSZY_-K1HvZU-tUQ3h/upgradeOffers"
    private val roomUpgradeWebViewLink = "https://localhost/hotelUpgrades/sGUZBxGESgB2eGM7GeXkhqJuzdi8Ucq1jl7NI9NzcW1mSSoGJ4njkXYWPCT2e__Ilwdc4lgBRnwlanmEgukEJWqNybe4NPSppEUZf9quVqD_kCjh_2HSZY_-K1HvZU-tUQ3h/c65fb5fb-489a-4fa8-a007-715b946d3b04/1/upgradeDeals?mcicid=App.Itinerary.Hotel.Upgrade&mobileWebView=true"

    private lateinit var tripFlight: TripFlight

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
    fun roomCancelled() {
        val tripParser = TripParser()
        val hotelTripJson = getHotelTripJson(withUpgradeOffer = true)
        val hotelJson = hotelTripJson.getJSONArray("hotels").getJSONObject(0)
        val roomsJson = hotelJson.getJSONArray("rooms")

        assertEquals(2, roomsJson.length())

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
        val trip = try {
            tripParser.parseTrip(lxTripJsonObj)
        } catch (e: Exception) {
            fail("Oops, we shouldn't have ended up here")
        }
        val tripActivity = trip.tripComponents[0] as TripActivity
        val activity = tripActivity.activity
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
        val trip = try {
            tripParser.parseTrip(lxTripJsonObj)
        } catch (e: Exception) {
            fail("Oops, we shouldn't have ended up here")
        }
        val tripActivity = trip.tripComponents[0] as TripActivity
        val activity = tripActivity.activity
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
        val trip = try {
            tripParser.parseTrip(lxTripJsonObj)
        } catch (e: Exception) {
            fail("Oops, we shouldn't have ended up here")
        }
        val tripActivity = trip.tripComponents[0] as TripActivity
        val activity = tripActivity.activity
        assertEquals("8AEE006B-E82D-40C1-A77D-5063EF3D47A9_0_224793_224797", activity.id)
        assertEquals("Shared Shuttle: Detroit International Airport (DTW): Hotels to Airport in Detroit City Center", activity.title)
        assertEquals(0, activity.guestCount)
    }

    @Test
    fun flightDurationExpectedFormat() {
        createFlightTripResponse()
        val duration = "PT4H32M"
        tripFlight.flightTrip.legs[0].legDuration = duration
        val parseLegDurationMinutes = tripFlight.flightTrip.legs[0].durationMinutes()
        assertEquals(272, parseLegDurationMinutes)
    }

    @Test
    fun flightDurationNull() {
        createFlightTripResponse()
        val duration = null
        tripFlight.flightTrip.legs[0].legDuration = duration
        val parseLegDurationMinutes = tripFlight.flightTrip.legs[0].durationMinutes()
        assertEquals(0, parseLegDurationMinutes)
    }

    @Test
    fun flightDurationEmpty() {
        createFlightTripResponse()
        val duration = " "
        tripFlight.flightTrip.legs[0].legDuration = duration
        val parseLegDurationMinutes = tripFlight.flightTrip.legs[0].durationMinutes()
        assertEquals(0, parseLegDurationMinutes)
    }

    @Test
    fun flightDurationUnexpectedFormat() {
        createFlightTripResponse()
        val duration = "2016-12-07T15:00:00-08:00"
        tripFlight.flightTrip.legs[0].legDuration = duration
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

    @Test
    fun testParseTraveler() {
        val tripParser = TripParser()
        val array = getPassengersJson()
        val firstTraveler = tripParser.parseTraveler(array.getJSONObject(0))
        val secondTraveler = tripParser.parseTraveler(array.getJSONObject(1))
        assertEquals("123456789", firstTraveler.redressNumber)
        assertEquals("", secondTraveler.redressNumber)
        assertEquals("987654321", firstTraveler.knownTravelerNumber)
        assertEquals("", secondTraveler.knownTravelerNumber)
        assertEquals(1, firstTraveler.frequentFlyerMemberships.size)
        assertEquals(0, secondTraveler.frequentFlyerMemberships.size)
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

    private fun getFlightTripJson(withSeatMap: Boolean = true): JSONObject {
        val data = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/flight_trips_summary_with_insurance.json"))).readUtf8()
        val jsonObject = JSONObject(data)
        val responseData = jsonObject.getJSONArray("responseData").getJSONObject(0)
        val flight = responseData.getJSONArray("flights").getJSONObject(0)
        val legs = flight.getJSONArray("legs").getJSONObject(0)
        val segments = legs.getJSONArray("segments")
        val firstSegment = segments.getJSONObject(0)
        if (!withSeatMap) {
            firstSegment.put("isSeatMapAvailable", false)
            firstSegment.remove("seatList")
        }
        return flight
    }

    private fun getPassengersJson(): JSONArray {
        val data = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/flight_trip_details_multi_segment.json"))).readUtf8()
        val jsonObject = JSONObject(data)
        val responseData = jsonObject.getJSONObject("responseData")
        val flight = responseData.getJSONArray("flights").getJSONObject(0)
        return flight.getJSONArray("passengers")
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

    @Test
    fun testFlightWithSeatsAndMap() {
        val tripParser = TripParser()
        val flightJson = getFlightTripJson()
        val trip = tripParser.parseTripFlight(flightJson).flightTrip
        val flight = trip.legs[0].segments[0]
        assertEquals(flight.isSeatMapAvailable, true)
        assertEquals(flight.assignedSeats, "15A, 10A")
        assertEquals(flight.cabinCode, "Economy / Coach")
        assertNotNull(flight.seats)
        val seats = flight.seats
        assertEquals(seats[0].assigned, "15A")
        assertEquals(seats[0].passenger, "DWAIN HUTCHINSON")
        assertEquals(seats[1].assigned, "10A")
        assertEquals(seats[1].passenger, "DWAIN HUTCHINSON")
    }

    @Test
    fun testFlightWithoutMap() {
        val tripParser = TripParser()
        val flightJson = getFlightTripJson(withSeatMap = false)
        val trip = tripParser.parseTripFlight(flightJson).flightTrip
        val flight = trip.legs[0].segments[0]
        assertEquals(flight.isSeatMapAvailable, false)
        assertEquals(flight.assignedSeats, "")
        assertEquals(flight.cabinCode, "Economy / Coach")
        assertEquals(flight.seats, emptyList())
    }

    @Test
    fun testFlightRules() {
        val tripParser = TripParser()
        val data = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/flight_trip_details.json"))).readUtf8()
        val jsonObject = JSONObject(data)
        val responseData = jsonObject.optJSONObject("responseData")
        val flight = responseData.getJSONArray("flights").getJSONObject(0)
        val trip = tripParser.parseTripFlight(flight).flightTrip
        val cancellationFeeLegalRule = trip.getRule("cancellationFeeLegalText")
        val cancelChangeIntroductionRule = trip.getRule("cancelChangeIntroductionText")
        val feeChangeRefundIntroductionRule = trip.getRule("feeChangeRefundIntroductionText")
        val refundabilityRule = trip.getRule("refundabilityText")
        val completePenaltyRules = trip.getRule("completePenaltyRules")
        val additionalAirlineFeesRule = trip.getRule("additionalAirlineFees")
        val airlineLiabilityLimitationsRule = trip.getRule("airlineLiabilityLimitations")

        val cancellationFeeLegalTextAssert = "A non-refundable administration fee will be applied for changes and cancellations of bookings. Customers will be informed of the administration fee by their Customer Support Centre Agent at the time of their call."
        val cancelChangeIntroductionTextAssert = "We understand that sometimes plans change. We do not charge a cancel or change fee. When the airline charges such fees in accordance with its own policies, the cost will be passed on to you."
        val feeChangeRefundIntroductionTextAssert = "When the airline charges any cancellation or change fees in accordance with its own policies, the cost will be passed on to you."
        val refundabilityTextAssert = "Tickets are nonrefundable, nontransferable and name changes are not allowed."
        val completePenaltyRulesTextAndUrlAssert = "Please read the <a id=\"complete_penalty_rules_for_changes_and_cancellations_link\" href=\"https://www.expedia.com/Fare-Rules?tripid=53a6459c-822c-4425-9e14-3eea43f38a97\" class=\"tooltip\" target=\"rulesAndRestrictions\">complete penalty rules for changes and cancellations <span class=\"visually-hidden\" style=\"display:none;\">(Opens a new window) </span> </a> applicable to this fare."
        val additionalAirlineFeesTextAndUrlAssert = "The airline may charge <a href=\"https://www.expedia.com/Flights-BagFees?originapt=SFO&destinationapt=LAS&cabinclass=3&mktgcarrier=UA&opcarrier=&farebasis=GAA4AKEN&bookingclass=G&travelDate=2017-09-05&flightNumber=681\" class=\"tooltip\" target=\"rulesAndRestrictions\">additional fees <span class=\"visually-hidden\" style=\"display:none;\" >(Opens a new window) </span> </a> for checked baggage or other optional services."
        val airlineLiabilityLimitationsTextAndUrlAssert = "Please read important information regarding <a id=\"airline_liability_limitations_link\" href=\"https://www.expedia.com/p/info-main/warsaw?\" class=\"tooltip\" target=\"rulesAndRestrictions\">airline liability limitations<span class=\"visually-hidden\" style=\"display:none;\" >(Opens a new window) </span></a>."
        val airlineLiabilityLimitationsTextAssert = "Please read important information regarding airline liability limitations."
        val airlineLiabilityLimitationsUrlAssert = "https://www.expedia.com/p/info-main/warsaw?"
        assertEquals(cancellationFeeLegalRule.text, cancellationFeeLegalTextAssert)
        assertEquals(cancelChangeIntroductionRule.text, cancelChangeIntroductionTextAssert)
        assertEquals(feeChangeRefundIntroductionRule.text, feeChangeRefundIntroductionTextAssert)
        assertEquals(refundabilityRule.text, refundabilityTextAssert)
        assertEquals(completePenaltyRules.textAndURL, completePenaltyRulesTextAndUrlAssert)
        assertEquals(additionalAirlineFeesRule.textAndURL, additionalAirlineFeesTextAndUrlAssert)
        assertEquals(airlineLiabilityLimitationsRule.text, airlineLiabilityLimitationsTextAssert)
        assertEquals(airlineLiabilityLimitationsRule.textAndURL, airlineLiabilityLimitationsTextAndUrlAssert)
        assertEquals(airlineLiabilityLimitationsRule.url, airlineLiabilityLimitationsUrlAssert)
    }

    @Test
    fun testParseItinFlightAction() {
        val tripParser = TripParser()
        val data = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/flight_trip_details_multi_segment.json"))).readUtf8()
        val jsonObject = JSONObject(data)
        val responseData = jsonObject.optJSONObject("responseData")
        val flight = responseData.getJSONArray("flights").getJSONObject(0)
        val trip = tripParser.parseTripFlight(flight).flightTrip
        assertEquals(trip.action.isCancellable, false)
        assertEquals(trip.action.isChangeable, false)
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
