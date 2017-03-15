package com.expedia.bookings.data.sos

import org.junit.Test
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MemberDealDestinationTest {

    @Test
    fun testHotelHasLeadingPrice() {
        var hotel = MemberDealDestination().Hotel()
        hotel.offerMarkers = arrayListOf("LEADIN_PRICE",
                "HIGHEST_STAR_RATING")
        assertTrue(hotel.hasLeadingPrice())
    }

    @Test
    fun testHotelHasNoLeadingPrice() {
        var hotel = MemberDealDestination().Hotel()
        hotel.offerMarkers = arrayListOf("HIGHEST_DISCOUNT",
                "HIGHEST_STAR_RATING")
        assertFalse(hotel.hasLeadingPrice())
    }

    @Test
    fun testDestinationHasLeadingHotel() {
        var destination = MemberDealDestination()
        destination.hotels  = getHotelListWithLeadingHotel()
        assertNotEquals(null, destination.getLeadingHotel())
        assertTrue(destination.getLeadingHotel()!!.hasLeadingPrice())
    }

    @Test
    fun testDestinationHasNoLeadingHotel() {
        var destination = MemberDealDestination()
        destination.hotels  = getHotelListWithoutLeadingHotel()
        assertEquals(null, destination.getLeadingHotel())
    }

    fun getHotelListWithLeadingHotel(): List<MemberDealDestination.Hotel> {
        var hotels = ArrayList<MemberDealDestination.Hotel>()
        var leadingHotel = MemberDealDestination().Hotel()
        leadingHotel.offerMarkers = arrayListOf("LEADIN_PRICE")
        hotels.add(leadingHotel)
        var notLeadingHotel = MemberDealDestination().Hotel()
        notLeadingHotel.offerMarkers = arrayListOf("HIGHEST_DISCOUNT")
        hotels.add(notLeadingHotel)

        return hotels
    }

    fun getHotelListWithoutLeadingHotel(): List<MemberDealDestination.Hotel> {
        var hotels = ArrayList<MemberDealDestination.Hotel>()
        var leadingHotel = MemberDealDestination().Hotel()
        leadingHotel.offerMarkers = arrayListOf("HIGHEST_STAR_RATING")
        hotels.add(leadingHotel)
        var notLeadingHotel = MemberDealDestination().Hotel()
        notLeadingHotel.offerMarkers = arrayListOf("HIGHEST_DISCOUNT")
        hotels.add(notLeadingHotel)

        return hotels
    }
}
