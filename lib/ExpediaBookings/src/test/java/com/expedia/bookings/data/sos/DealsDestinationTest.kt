package com.expedia.bookings.data.sos

import org.junit.Test
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DealsDestinationTest {

    @Test
    fun testHotelHasLeadingPrice() {
        val hotel = DealsDestination().Hotel()
        hotel.offerMarkers = arrayListOf("LEADIN_PRICE",
                "HIGHEST_STAR_RATING")
        assertTrue(hotel.hasLeadingPrice())
    }

    @Test
    fun testHotelHasNoLeadingPrice() {
        val hotel = DealsDestination().Hotel()
        hotel.offerMarkers = arrayListOf("HIGHEST_DISCOUNT",
                "HIGHEST_STAR_RATING")
        assertFalse(hotel.hasLeadingPrice())
    }

    @Test
    fun testDestinationHasLeadingHotel() {
        val destination = DealsDestination()
        destination.hotels = getHotelListWithLeadingHotel()
        assertNotEquals(null, destination.getLeadingHotel())
        assertTrue(destination.getLeadingHotel()!!.hasLeadingPrice())
    }

    @Test
    fun testDestinationHasNoLeadingHotel() {
        val destination = DealsDestination()
        destination.hotels = getHotelListWithoutLeadingHotel()
        assertEquals(null, destination.getLeadingHotel())
    }

    fun getHotelListWithLeadingHotel(): List<DealsDestination.Hotel> {
        val hotels = ArrayList<DealsDestination.Hotel>()
        val leadingHotel = DealsDestination().Hotel()
        leadingHotel.offerMarkers = arrayListOf("LEADIN_PRICE")
        hotels.add(leadingHotel)
        val notLeadingHotel = DealsDestination().Hotel()
        notLeadingHotel.offerMarkers = arrayListOf("HIGHEST_DISCOUNT")
        hotels.add(notLeadingHotel)

        return hotels
    }

    fun getHotelListWithoutLeadingHotel(): List<DealsDestination.Hotel> {
        val hotels = ArrayList<DealsDestination.Hotel>()
        val leadingHotel = DealsDestination().Hotel()
        leadingHotel.offerMarkers = arrayListOf("HIGHEST_STAR_RATING")
        hotels.add(leadingHotel)
        val notLeadingHotel = DealsDestination().Hotel()
        notLeadingHotel.offerMarkers = arrayListOf("HIGHEST_DISCOUNT")
        hotels.add(notLeadingHotel)

        return hotels
    }
}
