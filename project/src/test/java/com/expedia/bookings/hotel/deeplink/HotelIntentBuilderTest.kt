package com.expedia.bookings.hotel.deeplink

import android.content.Intent
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.deeplink.HotelDeepLink
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.ui.HotelActivity
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelIntentBuilderTest {
    private val testBuilder = HotelIntentBuilder()

    private val context = RuntimeEnvironment.application

    @Test
    fun testSelectedHotelId() {
        val deepLink = HotelDeepLink()
        deepLink.selectedHotelId = "12345"

        val intent = testBuilder.from(context, deepLink).build(context)

        assertEquals(HotelLandingPage.RESULTS.id, intent.getStringExtra(HotelExtras.LANDING_PAGE),
                "FAILURE: Landing page must == Results when deeplink using selected parameter.")
    }

    @Test
    fun testOldHotelId() {
        val deepLink = HotelDeepLink()
        deepLink.hotelId = "12345"

        val intent = testBuilder.from(context, deepLink).build(context)

        assertNull(intent.getStringExtra(HotelExtras.LANDING_PAGE),
                "FAILURE: When using the legacy hotelId format for deeplinks default to null landing page")
    }

    @Test
    fun testMemberDeal() {
        val deepLink = HotelDeepLink()

        val noMemberDealIntent = testBuilder.from(context, deepLink).build(context)
        assertFalse(noMemberDealIntent.hasExtra(Codes.MEMBER_ONLY_DEALS),
                "FAILURE: If member deals is false from deepLink the extra should not be added to the intent.")

        deepLink.memberOnlyDealSearch = true
        val memberDealIntent = testBuilder.from(context, deepLink).build(context)
        assertTrue(memberDealIntent.getBooleanExtra(Codes.MEMBER_ONLY_DEALS, false))
    }

    @Test
    fun testRoutingActivity() {
        val deepLink = HotelDeepLink()

        val intent = testBuilder.from(context, deepLink).build(context)

        assertEquals(HotelActivity::class.java.name, intent.component.className)
    }

    @Test
    fun testHotelRegionId() {
        val expectedId = "12345"
        val deepLink = HotelDeepLink()
        deepLink.regionId = expectedId

        val intent = testBuilder.from(context, deepLink).build(context)
        val params = getParamsFromIntent(intent)

        assertNotNull(params)
        assertEquals(expectedId, params!!.suggestion.gaiaId)
        assertEquals(com.expedia.bookings.data.HotelSearchParams.SearchType.CITY.name,
                params.suggestion.type)
    }

    @Test
    fun testHotelLocation() {
        val expectedLocation = "12345"
        val deepLink = HotelDeepLink()
        deepLink.location = expectedLocation

        val intent = testBuilder.from(context, deepLink).build(context)
        val params = getParamsFromIntent(intent)

        assertNotNull(params)
        assertEquals(com.expedia.bookings.data.HotelSearchParams.SearchType.CITY.name,
                params!!.suggestion.type)
        assertEquals(expectedLocation, params.suggestion.regionNames.displayName)
    }

    @Test
    fun testNotDatelessSearch() {
        val deepLink = HotelDeepLink()
        deepLink.hotelId = "12345"
        deepLink.checkInDate = LocalDate.now().plusDays(1)
        deepLink.checkOutDate = LocalDate.now().plusDays(2)

        val intent = testBuilder.from(context, deepLink).build(context)
        val params = getParamsFromIntent(intent)

        assertNotNull(params)
        assertFalse(params!!.isDatelessSearch)
        assertEquals(deepLink.checkInDate, params!!.checkIn)
        assertEquals(deepLink.checkOutDate, params!!.checkOut)
    }

    @Test
    fun testDatelessSearchNullCheckIn() {
        val deepLink = HotelDeepLink()
        deepLink.hotelId = "12345"
        deepLink.checkInDate = null
        deepLink.checkOutDate = LocalDate.now().plusDays(2)

        val intent = testBuilder.from(context, deepLink).build(context)
        val params = getParamsFromIntent(intent)

        assertNotNull(params)
        assertTrue(params!!.isDatelessSearch)
        assertEquals(LocalDate.now(), params!!.checkIn)
        assertEquals(deepLink.checkOutDate, params!!.checkOut)
    }

    @Test
    fun testDatelessSearchullCheckOut() {
        val deepLink = HotelDeepLink()
        deepLink.hotelId = "12345"
        deepLink.checkInDate = LocalDate.now().plusDays(1)
        deepLink.checkOutDate = null

        val intent = testBuilder.from(context, deepLink).build(context)
        val params = getParamsFromIntent(intent)

        assertNotNull(params)
        assertTrue(params!!.isDatelessSearch)
        assertEquals(deepLink.checkInDate, params!!.checkIn)
        assertEquals(LocalDate.now().plusDays(2), params!!.checkOut)
    }

    @Test
    fun testDatelessSearchPastCheckIn() {
        val deepLink = HotelDeepLink()
        deepLink.hotelId = "12345"
        deepLink.checkInDate = LocalDate.now().plusDays(-1)
        deepLink.checkOutDate = LocalDate.now().plusDays(2)

        val intent = testBuilder.from(context, deepLink).build(context)
        val params = getParamsFromIntent(intent)

        assertNotNull(params)
        assertTrue(params!!.isDatelessSearch)
        assertEquals(LocalDate.now(), params!!.checkIn)
        assertEquals(LocalDate.now().plusDays(1), params!!.checkOut)
    }

    @Test
    fun testDatelessSearchPastCheckOut() {
        val deepLink = HotelDeepLink()
        deepLink.hotelId = "12345"
        deepLink.checkInDate = LocalDate.now().plusDays(1)
        deepLink.checkOutDate = LocalDate.now().plusDays(-1)

        val intent = testBuilder.from(context, deepLink).build(context)
        val params = getParamsFromIntent(intent)

        assertNotNull(params)
        assertTrue(params!!.isDatelessSearch)
        assertEquals(LocalDate.now(), params!!.checkIn)
        assertEquals(LocalDate.now().plusDays(1), params!!.checkOut)
    }

    private fun getParamsFromIntent(intent: Intent): HotelSearchParams? {
        return HotelsV2DataUtil.getHotelV2SearchParamsFromJSON(intent.getStringExtra(HotelExtras.EXTRA_HOTEL_SEARCH_PARAMS))
    }
}
