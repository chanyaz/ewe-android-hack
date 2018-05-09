package com.expedia.bookings.unit.hotels

import com.expedia.bookings.data.hotels.HotelInfo
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HotelInfoTest {

    private lateinit var hotelInfo: HotelInfo

    @Before
    fun before() {
        hotelInfo = NewHotelSearchResponseTestUtils.createHotelInfo()
    }

    @Test
    fun testConvertToLegacyHotelId() {
        var hotel = hotelInfo.convertToLegacyHotel()
        assertEquals("id0", hotel.hotelId)
        hotelInfo.id = ""
        hotel = hotelInfo.convertToLegacyHotel()
        assertEquals("", hotel.hotelId)
    }

    @Test
    fun testConvertToLegacyHotelLocalizedName() {
        var hotel = hotelInfo.convertToLegacyHotel()
        assertEquals("localizedHotelName0", hotel.localizedName)
        hotelInfo.localizedHotelName = ""
        hotel = hotelInfo.convertToLegacyHotel()
        assertEquals("", hotel.localizedName)
    }

    @Test
    fun testConvertToLegacyHotelIsSponsored() {
        var hotel = hotelInfo.convertToLegacyHotel()
        assertFalse(hotel.isSponsoredListing)
        hotelInfo.isSponsored = true
        hotel = hotelInfo.convertToLegacyHotel()
        assertTrue(hotel.isSponsoredListing)
    }

    @Test
    fun testConvertToLegacyHotelHasFreeCancel() {
        var hotel = hotelInfo.convertToLegacyHotel()
        assertFalse(hotel.hasFreeCancellation)
        hotelInfo.hasFreeCancel = true
        hotel = hotelInfo.convertToLegacyHotel()
        assertTrue(hotel.hasFreeCancellation)
    }

    @Test
    fun testConvertToLegacyHotelHasPayLaterIsPaymentChoiceAvailable() {
        var hotel = hotelInfo.convertToLegacyHotel()
        assertFalse(hotel.isPaymentChoiceAvailable)
        hotelInfo.hasPayLater = true
        hotel = hotelInfo.convertToLegacyHotel()
        assertTrue(hotel.isPaymentChoiceAvailable)
    }

    @Test
    fun testConvertToLegacyHotelHasPayLaterIsShowEtpChoice() {
        var hotel = hotelInfo.convertToLegacyHotel()
        assertFalse(hotel.isShowEtpChoice)
        hotelInfo.hasPayLater = true
        hotel = hotelInfo.convertToLegacyHotel()
        assertTrue(hotel.isShowEtpChoice)
    }

    @Test
    fun testConvertToLegacyHotelRoomsLeftAtThisPrice() {
        var hotel = hotelInfo.convertToLegacyHotel()
        assertEquals(0, hotel.roomsLeftAtThisRate)
        hotelInfo.roomsLeftAtThisPrice = 1
        hotel = hotelInfo.convertToLegacyHotel()
        assertEquals(1, hotel.roomsLeftAtThisRate)
    }

    @Test
    fun testConvertToLegacyHotelStarRating() {
        var hotel = hotelInfo.convertToLegacyHotel()
        assertEquals(4.3f, hotel.hotelStarRating)
        hotelInfo.starRating = 0f
        hotel = hotelInfo.convertToLegacyHotel()
        assertEquals(0f, hotel.hotelStarRating)
    }

    @Test
    fun testConvertToLegacyHotelGuestRating() {
        var hotel = hotelInfo.convertToLegacyHotel()
        assertEquals(3.7f, hotel.hotelGuestRating)
        hotelInfo.guestRating = 0.0954f
        hotel = hotelInfo.convertToLegacyHotel()
        assertEquals(0.1f, hotel.hotelGuestRating)
    }

    @Test
    fun testConvertToLegacyHotelPrice() {
        var hotel = hotelInfo.convertToLegacyHotel()
        assertNotNull(hotel.lowRateInfo)
        hotelInfo.price = null
        hotel = hotelInfo.convertToLegacyHotel()
        assertNull(hotel.lowRateInfo)
    }

    @Test
    fun testConvertToLegacyPriceCurrencyCode() {
        var hotel = hotelInfo.convertToLegacyHotel()
        assertEquals("USD", hotel.rateCurrencyCode)
        hotelInfo.price = NewHotelSearchResponseTestUtils.createHotelPriceInfo(currencyCode = "THB")
        hotel = hotelInfo.convertToLegacyHotel()
        assertEquals("THB", hotel.rateCurrencyCode)
    }

    @Test
    fun testConvertToLegacyHotelVip() {
        var hotel = hotelInfo.convertToLegacyHotel()
        assertFalse(hotel.isVipAccess)
        hotelInfo.vip = true
        hotel = hotelInfo.convertToLegacyHotel()
        assertTrue(hotel.isVipAccess)
    }

    @Test
    fun testConvertToLegacyHotelImageUrl() {
        var hotel = hotelInfo.convertToLegacyHotel()
        assertEquals("imageUrl0", hotel.largeThumbnailUrl)
        hotelInfo.imageUrl = ""
        hotel = hotelInfo.convertToLegacyHotel()
        assertEquals("", hotel.largeThumbnailUrl)
    }

    @Test
    fun testConvertToLegacyHotelLowResImageUrl() {
        var hotel = hotelInfo.convertToLegacyHotel()
        assertEquals("lowResImageUrl0", hotel.thumbnailUrl)
        hotelInfo.lowResImageUrl = ""
        hotel = hotelInfo.convertToLegacyHotel()
        assertEquals("", hotel.thumbnailUrl)
    }

    @Test
    fun testConvertToLegacyHotelLatLng() {
        val hotel = hotelInfo.convertToLegacyHotel()
        assertEquals(41.882199, hotel.latitude)
        assertEquals(-87.640492, hotel.longitude)
    }

    @Test
    fun testConvertToLegacyHotelLatLngEmpty() {
        hotelInfo.latLong = emptyList()
        val hotel = hotelInfo.convertToLegacyHotel()
        assertEquals(0.0, hotel.latitude)
        assertEquals(0.0, hotel.longitude)
    }

    @Test
    fun testConvertToLegacyHotelLatLngOne() {
        hotelInfo.latLong = listOf(1.0)
        val hotel = hotelInfo.convertToLegacyHotel()
        assertEquals(0.0, hotel.latitude)
        assertEquals(0.0, hotel.longitude)
    }

    @Test
    fun testConvertToLegacyHotelLatLngThree() {
        hotelInfo.latLong = listOf(1.0, 2.0, 3.0)
        val hotel = hotelInfo.convertToLegacyHotel()
        assertEquals(0.0, hotel.latitude)
        assertEquals(0.0, hotel.longitude)
    }

    @Test
    fun testConvertToLegacyHotelNeighborhoodId() {
        val hotel = hotelInfo.convertToLegacyHotel()
        assertEquals("neighborhoodId0", hotel.locationId)
    }

    @Test
    fun testConvertToLegacyHotelRegionName() {
        hotelInfo.neighborhoodName = ""
        val hotel = hotelInfo.convertToLegacyHotel()
        assertEquals("regionName0", hotel.neighborhoodName)
    }

    @Test
    fun testConvertToLegacyHotelRegionId() {
        hotelInfo.neighborhoodId = ""
        val hotel = hotelInfo.convertToLegacyHotel()
        assertEquals("regionId0", hotel.locationId)
    }

    @Test
    fun testConvertToLegacyHotelNoNeighborhoodIdNoRegionId() {
        hotelInfo.neighborhoodId = ""
        hotelInfo.regionId = ""
        val hotel = hotelInfo.convertToLegacyHotel()
        assertEquals("", hotel.locationId)
    }

    @Test
    fun testConvertToLegacyHotelDistanceMiles() {
        val hotel = hotelInfo.convertToLegacyHotel()
        assertEquals(1.609344, hotel.proximityDistanceInKiloMeters)
        assertEquals(1.0, hotel.proximityDistanceInMiles)
        assertEquals("Miles", hotel.distanceUnit)
    }

    @Test
    fun testConvertToLegacyHotelDistanceKilometers() {
        hotelInfo.directDistance = HotelInfo.ProximityDistance(1.0, HotelInfo.DistanceUnit.KM)
        val hotel = hotelInfo.convertToLegacyHotel()
        assertEquals(1.0, hotel.proximityDistanceInKiloMeters)
        assertEquals(0.621371192, hotel.proximityDistanceInMiles)
        assertEquals("Kilometers", hotel.distanceUnit)
    }

    @Test
    fun testConvertToLegacyHotelDistanceNull() {
        hotelInfo.directDistance = null
        val hotel = hotelInfo.convertToLegacyHotel()
        assertEquals(0.0, hotel.proximityDistanceInKiloMeters)
        assertEquals(0.0, hotel.proximityDistanceInMiles)
        assertNull(hotel.distanceUnit)
    }

    @Test
    fun testConvertToLegacyHotelNeighborhoodName() {
        var hotel = hotelInfo.convertToLegacyHotel()
        assertEquals("neighborhoodName0", hotel.neighborhoodName)
    }

    @Test
    fun testConvertToLegacyHotelNoNeighborhoodNameNoRegionName() {
        hotelInfo.neighborhoodName = ""
        hotelInfo.regionName = ""
        val hotel = hotelInfo.convertToLegacyHotel()
        assertEquals("", hotel.neighborhoodName)
    }

    @Test
    fun testConvertToLegacyHotelImpressionTrackingUrl() {
        val hotel = hotelInfo.convertToLegacyHotel()
        assertEquals("impressionTrackingUrl0", hotel.impressionTrackingUrl)
    }

    @Test
    fun testConvertToLegacyHotelClickTrackingUrl() {
        val hotel = hotelInfo.convertToLegacyHotel()
        assertEquals("clickTrackingUrl0", hotel.clickTrackingUrl)
    }

    @Test
    fun testConvertToLegacyHotelIsAvailableIsHotelAvailable() {
        var hotel = hotelInfo.convertToLegacyHotel()
        assertTrue(hotel.isHotelAvailable)
        hotelInfo.isAvailable = false
        hotel = hotelInfo.convertToLegacyHotel()
        assertFalse(hotel.isHotelAvailable)
    }

    @Test
    fun testConvertToLegacyHotelIsAvailableIsSoldOut() {
        var hotel = hotelInfo.convertToLegacyHotel()
        assertFalse(hotel.isSoldOut)
        hotelInfo.isAvailable = false
        hotel = hotelInfo.convertToLegacyHotel()
        assertTrue(hotel.isSoldOut)
    }
}
