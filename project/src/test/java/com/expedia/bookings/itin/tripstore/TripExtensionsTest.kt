package com.expedia.bookings.itin.tripstore

import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.tripstore.extensions.eligibleForRewards
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.itin.tripstore.extensions.packagePrice
import com.mobiata.mocke3.mockObject
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TripExtensionsTest {

    @Test
    fun testFirstHotelValidHotel() {
        val hotel = makeItinHotel("api/trips/hotel_trip_details.json")
        assertNotNull(hotel)
        assertEquals("5D5D96B6-0BB4-4A08-B82F-C9FBADF85532_0", hotel?.uniqueID)
    }

    @Test
    fun testFirstHotelPackageHotel() {
        val hotel = makeItinHotel("api/trips/package_trip_details.json")
        assertNotNull(hotel)
        assertEquals("CDF71043-5237-4788-9784-51EDE36DEE04", hotel?.uniqueID)
    }

    @Test
    fun testFirstHotelArrayEmpty() {
        val hotel = makeItinHotel("api/trips/hotel_trip_details_no_hotels.json")
        assertNull(hotel)
    }

    @Test
    fun testFirstHotelNoHotels() {
        val hotel = makeItinHotel("api/trips/hotel_trip_details_empty_hotels.json")
        assertNull(hotel)
    }

    @Test
    fun validRewardList() {
        val trip = mockObject(ItinDetailsResponse::class.java, "api/trips/trip_valid_rewards_list.json")
        assertTrue(trip?.itin?.eligibleForRewards()!!)
    }

    @Test
    fun nullRewardList() {
        val trip = mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details.json")
        assertFalse(trip?.itin?.eligibleForRewards()!!)
    }

    @Test
    fun emptyRewardList() {
        val trip = mockObject(ItinDetailsResponse::class.java, "api/trips/trip_empty_rewards_list.json")
        assertFalse(trip?.itin?.eligibleForRewards()!!)
    }

    @Test
    fun packPriceTest() {
        val itin = makeItin("api/trips/itin_package_mock.json")
        assertEquals("$122.55", itin?.packagePrice())
    }

    private fun makeItinHotel(mockName: String): ItinHotel? {
        return makeItin(mockName)?.firstHotel()
    }

    private fun makeItin(mockName: String): Itin? {
        val json = mockObject(ItinDetailsResponse::class.java, mockName)
        return json?.itin
    }
}
