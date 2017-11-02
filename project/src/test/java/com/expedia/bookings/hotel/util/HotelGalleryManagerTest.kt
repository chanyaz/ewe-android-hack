package com.expedia.bookings.hotel.util

import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.hotel.DEFAULT_HOTEL_GALLERY_CODE
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.testutils.JSONResourceReader
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelGalleryManagerTest {
    private val testManager = HotelGalleryManager()

    @Test
    fun testSaveImagesNoRooms() {
        val response = loadOfferInfo("src/test/resources/raw/hotel/hotel_info_sold_out.json")

        testManager.saveHotelOfferMedia(response)
        val images = testManager.fetchMediaList(DEFAULT_HOTEL_GALLERY_CODE)
        assertEquals(3, images.size)
    }

    @Test
    fun testSaveImagesWithRooms() {
        val roomCode = "200674447"
        val response = loadOfferInfo("src/test/resources/raw/hotel/hotel_happy_offer.json")

        testManager.saveHotelOfferMedia(response)
        val overviewImages = testManager.fetchMediaList(DEFAULT_HOTEL_GALLERY_CODE)
        val roomImages = testManager.fetchMediaList(roomCode)

        assertEquals(2, overviewImages.size)
        assertEquals(1, roomImages.size)
    }

    @Test
    fun testEmptyRemovesOld() {
        val response = loadOfferInfo("src/test/resources/raw/hotel/hotel_info_sold_out.json")
        testManager.saveHotelOfferMedia(response)
        assertEquals(3, testManager.fetchMediaList(DEFAULT_HOTEL_GALLERY_CODE).size)
        response.photos = null
        testManager.saveHotelOfferMedia(response)
        assertEquals(0, testManager.fetchMediaList(DEFAULT_HOTEL_GALLERY_CODE).size)
    }

    private fun loadOfferInfo(resourcePath: String) : HotelOffersResponse {
        val resourceReader = JSONResourceReader(resourcePath)
        return resourceReader.constructUsingGson(HotelOffersResponse::class.java)
    }
}