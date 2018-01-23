package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.multiitem.MultiItemApiSearchResponse
import com.expedia.bookings.data.multiitem.MultiItemOffer
import com.expedia.bookings.data.multiitem.HotelOffer
import com.expedia.bookings.data.multiitem.FlightOffer
import com.expedia.bookings.data.multiitem.MultiItemFlightLeg
import com.expedia.bookings.data.multiitem.MultiItemError
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PackageResponseUtilsTest {

    private lateinit var context: Context
    private lateinit var sut: PackageResponseUtils

    @Before
    fun before() {
        context = RuntimeEnvironment.application
        sut = PackageResponseUtils
    }

    @Test
    fun testLoadPackageResponseWhenMidAPIEnabled() {
        var expectedPackageSearchResponse = mockMIDResponse()
        val latch = CountDownLatch(1)
        sut.savePackageResponse(context, expectedPackageSearchResponse, sut.RECENT_PACKAGE_HOTELS_FILE, { _ ->
            latch.countDown()
        })
        latch.await(2, TimeUnit.SECONDS)
        val packageSearchResponse = sut.loadPackageResponse(context, sut.RECENT_PACKAGE_HOTELS_FILE, true)
        assertEquals(expectedPackageSearchResponse, packageSearchResponse)
    }

    @Test
    fun testLoadPackageResponseWhenMidAPIDisabled() {
        var expectedPackageSearchResponse = PackageSearchResponse()
        val latch = CountDownLatch(1)
        sut.savePackageResponse(context, expectedPackageSearchResponse, sut.RECENT_PACKAGE_HOTELS_FILE, { _ ->
            latch.countDown()
        })
        latch.await(2, TimeUnit.SECONDS)
        val packageSearchResponse = sut.loadPackageResponse(context, sut.RECENT_PACKAGE_HOTELS_FILE, false) as PackageSearchResponse
        assertEquals(expectedPackageSearchResponse.packageResult, packageSearchResponse.packageResult)
    }

    @Test
    fun testLoadPackageResponseWhenSavePackageResponseIsNotCalled() {
        val packageSearchResponse = sut.loadPackageResponse(context, sut.RECENT_PACKAGE_HOTELS_FILE, false)
        assertEquals(null, packageSearchResponse)
    }

    @Test
    fun testLoadHotelOfferResponse() {
        var expectedHotelOffersResponse = HotelOffersResponse()
        expectedHotelOffersResponse.checkInDate = "12/12/2099"
        val latch = CountDownLatch(1)
        sut.saveHotelOfferResponse(context, expectedHotelOffersResponse, sut.RECENT_PACKAGE_HOTEL_OFFER_FILE, { _ ->
            latch.countDown()
        })
        latch.await(2, TimeUnit.SECONDS)
        val hotelOffersResponse = sut.loadHotelOfferResponse(context, sut.RECENT_PACKAGE_HOTEL_OFFER_FILE)
        assertEquals(expectedHotelOffersResponse.checkInDate, hotelOffersResponse!!.checkInDate)
    }

    @Test
    fun testLoadHotelOfferResponseWhenSaveHotelOfferResponseIsNotCalled() {
        val hotelOffersResponse = sut.loadHotelOfferResponse(context, sut.RECENT_PACKAGE_HOTEL_OFFER_FILE)
        assertEquals(null, hotelOffersResponse)
    }

    private fun mockMIDResponse(offers: List<MultiItemOffer> = emptyList(),
                                hotels: Map<String, HotelOffer> = emptyMap(),
                                flights: Map<String, FlightOffer> = emptyMap(),
                                flightLegs: Map<String, MultiItemFlightLeg> = emptyMap(),
                                errors: List<MultiItemError>? = null): MultiItemApiSearchResponse {
        return MultiItemApiSearchResponse(offers = offers, hotels = hotels, flights = flights, flightLegs = flightLegs, errors = errors)
    }
}
