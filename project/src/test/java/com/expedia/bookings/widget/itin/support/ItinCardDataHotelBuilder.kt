package com.expedia.bookings.widget.itin.support

import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.data.trips.TripHotel
import com.expedia.bookings.server.TripParser
import okio.Okio
import org.joda.time.DateTime
import org.json.JSONObject
import java.io.File

class ItinCardDataHotelBuilder {

    private var upgradeableRoom = false
    private var vipEnabled = false
    private var isSharedItin = false
    private var checkInDate: DateTime = DateTime.now().plusDays(2)
    private var checkOutDate: DateTime? = checkInDate.plusDays(3)
    private var bookingChangeUrl = ""
    private var roomUpgradeApiUrl = ""
    private var roomUpgradeWebUrl = ""
    private var primaryOccupantFullName = "Kevin Carpenter"
    private var adultCount = 1
    private var childCount = 0
    private var infantCount = 0
    private var guestCount = 1

    fun build(): ItinCardDataHotel {
        val itinCardDataHotel = makeHotel()
        val parentTrip = itinCardDataHotel.tripComponent.parentTrip

        itinCardDataHotel.property.setIsVipAccess(vipEnabled)
        parentTrip.setIsTripUpgradable(upgradeableRoom)
        parentTrip.setIsShared(isSharedItin)
        itinCardDataHotel.property.roomUpgradeOffersApiUrl = roomUpgradeApiUrl
        itinCardDataHotel.property.roomUpgradeWebViewUrl = roomUpgradeWebUrl
        parentTrip.setIsShared(isSharedItin)

        val tripHotel = itinCardDataHotel.tripComponent as TripHotel
        tripHotel.startDate = checkInDate
        tripHotel.endDate = checkOutDate
        tripHotel.guests = adultCount

        if (bookingChangeUrl.isNotEmpty()) {
            itinCardDataHotel.property.bookingChangeWebUrl = bookingChangeUrl
        }

        itinCardDataHotel.lastHotelRoom.primaryOccupant?.fullName = primaryOccupantFullName
        itinCardDataHotel.lastHotelRoom.otherOccupantInfo?.adultCount = adultCount
        itinCardDataHotel.lastHotelRoom.otherOccupantInfo?.childCount = childCount
        itinCardDataHotel.lastHotelRoom.otherOccupantInfo?.infantCount = infantCount

        return itinCardDataHotel
    }

    fun withBookingChangeUrl(url: String): ItinCardDataHotelBuilder {
        bookingChangeUrl = url
        return this
    }

    fun withRoomUpgradeApiUrl(url: String): ItinCardDataHotelBuilder {
        roomUpgradeApiUrl = url
        return this
    }

    fun withRoomUpgradeWebUrl(url: String): ItinCardDataHotelBuilder {
        roomUpgradeWebUrl = url
        return this
    }

    fun withTripUpgradeableFlagTrue(): ItinCardDataHotelBuilder {
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

    fun withAdultChildInfantCount(numOfAdults: Int, numOfChildren: Int, numOfInfants: Int): ItinCardDataHotelBuilder {
        adultCount = numOfAdults
        childCount = numOfChildren
        infantCount = numOfInfants
        return this
    }

    fun withPrimaryOccupantFullName(name: String): ItinCardDataHotelBuilder {
        primaryOccupantFullName = name
        return this
    }

    fun withAdultCount(numOfAdults: Int): ItinCardDataHotelBuilder {
        adultCount = numOfAdults
        return this
    }

    fun withEmptyGuestNameAndOccupants(name: String, numOfOccupants: Int): ItinCardDataHotelBuilder {
        primaryOccupantFullName = name
        adultCount = numOfOccupants
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
        val jsonResponseData = jsonObject.getJSONObject("responseData")
        val tripHotel = getHotelTrip(jsonResponseData)!!
        return tripHotel
    }

    private fun getHotelTrip(jsonObject: JSONObject): TripHotel? {
        val tripParser = TripParser()

        val tripObj = tripParser.parseTrip(jsonObject)
        val tripComponent = tripObj.tripComponents[0]
        if (tripComponent is TripHotel) {
            return tripComponent
        } else {
            return null
        }
    }
}
