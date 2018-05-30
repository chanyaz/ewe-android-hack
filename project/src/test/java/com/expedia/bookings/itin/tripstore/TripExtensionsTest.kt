package com.expedia.bookings.itin.tripstore

import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.tripstore.data.ItinLx
import com.expedia.bookings.itin.tripstore.extensions.TripProducts
import com.expedia.bookings.itin.tripstore.extensions.eligibleForRewards
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.itin.tripstore.extensions.firstLx
import com.expedia.bookings.itin.tripstore.extensions.isMultiItemCheckout
import com.expedia.bookings.itin.tripstore.extensions.isPackage
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
        val hotel = makeItinHotel("api/trips/hotel_trip_details_for_mocker.json")
        assertNotNull(hotel)
        assertEquals("B43C38B0-6F14-4D9D-BBEC-F56AEA3DF3B9_0", hotel?.uniqueID)
    }

    @Test
    fun testFirstHotelPackageHotel() {
        val hotel = makeItinHotel("api/trips/package_trip_details.json")
        assertNotNull(hotel)
        assertEquals("CDF71043-5237-4788-9784-51EDE36DEE04", hotel?.uniqueID)
    }

    @Test
    fun testFirstHotelArrayEmpty() {
        val hotel = makeItinHotel("api/trips/hotel_trip_details_empty_hotels.json")
        assertNull(hotel)
    }

    @Test
    fun testFirstHotelNoHotels() {
        val hotel = makeItinHotel("api/trips/hotel_trip_details_no_hotels.json")
        assertNull(hotel)
    }

    @Test
    fun testFirstHotelPackageArrayEmpty() {
        val hotel = makeItinHotel("api/trips/package_trip_details_no_package.json")
        assertNull(hotel)
    }

    @Test
    fun validRewardList() {
        val trip = mockObject(ItinDetailsResponse::class.java, "api/trips/trip_valid_rewards_list.json")
        assertTrue(trip?.itin?.eligibleForRewards()!!)
    }

    @Test
    fun nullRewardList() {
        val trip = mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details_for_mocker.json")
        assertTrue(trip?.itin?.eligibleForRewards()!!)
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

    @Test
    fun testEmptyPackagePrice() {
        val itin = makeItin("api/trips/package_trip_details_no_package.json")!!
        assertNull(itin.packagePrice())
    }

    @Test
    fun testFirstLxValid() {
        val lx = makeItinLx("api/trips/activity_trip_details.json")
        assertNotNull(lx)
        assertEquals("200E974C-C7DA-445E-A392-DD12578A96A0_0_358734_358736", lx?.uniqueID)
    }

    @Test
    fun testFirstLxEmptyPackage() {
        val lxItin = makeItinLx("api/trips/package_trip_details_no_package.json")
        assertNull(lxItin)
    }

    @Test
    fun testIsItinMultiItemCheckout() {
        val hotelStandaloneItin = ItinMocker.hotelDetailsHappy
        assertFalse(hotelStandaloneItin.isMultiItemCheckout())

        val packageItin = ItinMocker.hotelPackageHappy
        assertFalse(packageItin.isMultiItemCheckout())

        val multiItemCheckoutItin = ItinMocker.mickoHotelHappy
        assertTrue(multiItemCheckoutItin.isMultiItemCheckout())

        val multiHotelItin = ItinMocker.mickoMultiHotel
        assertTrue(multiHotelItin.isMultiItemCheckout())
    }

    @Test
    fun testIsItinPackage() {
        val hotelStandaloneItin = ItinMocker.hotelDetailsHappy
        assertFalse(hotelStandaloneItin.isPackage())

        val multiItemCheckoutItin = ItinMocker.mickoHotelHappy
        assertFalse(multiItemCheckoutItin.isPackage())

        val packageItin = ItinMocker.hotelPackageHappy
        assertTrue(packageItin.isPackage())

        val packageEmptyItin = ItinMocker.packageEmpty
        assertTrue(packageEmptyItin.isPackage())
    }

    @Test
    fun testMakeListOfTripProducts() {
        val packageItin = ItinMocker.hotelPackageHappy
        val packageProducts = packageItin.packages?.first()!!.listOfTripProducts()
        assertTrue(packageProducts.isNotEmpty())
        assertTrue(packageProducts.containsAll(listOf(TripProducts.FLIGHT, TripProducts.HOTEL)))

        val hotelStandaloneItin = ItinMocker.hotelDetailsHappy
        val hotelProducts = hotelStandaloneItin.listOfTripProducts()
        assertTrue(hotelProducts.isNotEmpty())
        assertTrue(hotelProducts.containsAll(listOf(TripProducts.HOTEL)))

        val multiItemCheckoutItin = ItinMocker.mickoHotelHappy
        val mickoProducts = multiItemCheckoutItin.listOfTripProducts()
        assertTrue(mickoProducts.isNotEmpty())
        assertTrue(mickoProducts.containsAll(listOf(TripProducts.HOTEL, TripProducts.FLIGHT)))

        val multiHotelItin = ItinMocker.mickoMultiHotel
        val multiHotelProducts = multiHotelItin.listOfTripProducts()
        assertTrue(multiHotelProducts.isNotEmpty())
        assertTrue(multiHotelProducts.containsAll(listOf(TripProducts.HOTEL)))
    }

    private fun makeItinHotel(mockName: String): ItinHotel? {
        return makeItin(mockName)?.firstHotel()
    }

    private fun makeItin(mockName: String): Itin? {
        val json = mockObject(ItinDetailsResponse::class.java, mockName)
        return json?.itin
    }

    private fun makeItinLx(mockName: String): ItinLx? {
        return makeItin(mockName)?.firstLx()
    }
}
