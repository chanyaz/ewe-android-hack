package com.expedia.bookings.hotel.deeplink

import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class HotelLandingPageTest {
    @Test
    fun testFromNull() {
        val page = HotelLandingPage.fromId(null)

        assertNull(page)
    }

    @Test
    fun testFromGarbage() {
        val page = HotelLandingPage.fromId("dskljgf")

        assertNull(page)
    }

    @Test
    fun testFromSearch() {
        val page = HotelLandingPage.fromId(HotelLandingPage.SEARCH.id)

        assertEquals(HotelLandingPage.SEARCH, page)
    }

    @Test
    fun testFromResults() {
        val page = HotelLandingPage.fromId(HotelLandingPage.RESULTS.id)

        assertEquals(HotelLandingPage.RESULTS, page)
    }

    @Test
    fun testFromInfoSite() {
        val page = HotelLandingPage.fromId(HotelLandingPage.INFO_SITE.id)

        assertEquals(HotelLandingPage.INFO_SITE, page)
    }
}