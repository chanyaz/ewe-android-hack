package com.expedia.bookings.unit.multiitem

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.multiitem.FlightOffer
import com.expedia.bookings.data.multiitem.HotelOffer
import com.expedia.bookings.data.multiitem.MultiItemApiSearchResponse
import com.expedia.bookings.data.multiitem.MultiItemError
import com.expedia.bookings.data.multiitem.MultiItemFlightLeg
import com.expedia.bookings.data.multiitem.MultiItemOffer
import com.expedia.bookings.data.multiitem.ProductType
import com.expedia.bookings.data.packages.PackageApiError
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MultiItemSearchResponseTests {

    @Test
    fun testCouldNotFindResultsError() {
        val midResponse = mockMIDResponse(errors = arrayListOf(MultiItemError("description", "MIS_FLIGHT_PRODUCT_NOT_FOUND", ProductType.Bundle)))
        assertNotNull(midResponse.firstError)
        assertEquals(PackageApiError.Code.mid_could_not_find_results, midResponse.firstError.errorCode)
    }

    @Test
    fun testOriginResolutionError() {
        val midResponse = mockMIDResponse(errors = arrayListOf(MultiItemError("description", "MIS_ORIGIN_RESOLUTION_ERROR", ProductType.Bundle)))
        assertNotNull(midResponse.firstError)
        assertEquals(PackageApiError.Code.pkg_origin_resolution_failed, midResponse.firstError.errorCode)
    }

    @Test
    fun testAmbiguousOriginError() {
        val midResponse = mockMIDResponse(errors = arrayListOf(MultiItemError("description", "MIS_AMBIGUOUS_ORIGIN_ERROR", ProductType.Bundle)))
        assertNotNull(midResponse.firstError)
        assertEquals(PackageApiError.Code.pkg_origin_resolution_failed, midResponse.firstError.errorCode)
    }

    @Test
    fun testDestinationResolutionError() {
        val midResponse = mockMIDResponse(errors = arrayListOf(MultiItemError("description", "MIS_DESTINATION_RESOLUTION_ERROR", ProductType.Bundle)))
        assertNotNull(midResponse.firstError)
        assertEquals(PackageApiError.Code.pkg_destination_resolution_failed, midResponse.firstError.errorCode)
    }

    @Test
    fun testProhibitedDestinationError() {
        val midResponse = mockMIDResponse(errors = arrayListOf(MultiItemError("description", "MIS_PROHIBITED_DESTINATION_ERROR", ProductType.Bundle)))
        assertNotNull(midResponse.firstError)
        assertEquals(PackageApiError.Code.pkg_destination_resolution_failed, midResponse.firstError.errorCode)
    }

    @Test
    fun testAmbiguousDestinationError() {
        val midResponse = mockMIDResponse(errors = arrayListOf(MultiItemError("description", "MIS_AMBIGUOUS_DESTINATION_ERROR", ProductType.Bundle)))
        assertNotNull(midResponse.firstError)
        assertEquals(PackageApiError.Code.pkg_destination_resolution_failed, midResponse.firstError.errorCode)
    }

    @Test
    fun testRedEyeError() {
        val midResponse = mockMIDResponse(errors = arrayListOf(MultiItemError("description", "FSS_HOTEL_UNAVAILABLE_FOR_RED_EYE_FLIGHT", ProductType.Bundle)))
        assertNotNull(midResponse.firstError)
        assertEquals(PackageApiError.Code.mid_fss_hotel_unavailable_for_red_eye_flight, midResponse.firstError.errorCode)
    }

    @Test
    fun testFallbackError() {
        val midResponse = mockMIDResponse(errors = arrayListOf(MultiItemError("description", "FALLBACK", ProductType.Bundle)))
        assertNotNull(midResponse.firstError)
        assertEquals(PackageApiError.Code.mid_internal_server_error, midResponse.firstError.errorCode)
    }

    @Test
    fun testRoomResponseError() {
        val midResponse = mockMIDResponse(errors = arrayListOf(MultiItemError("description", "MIS_FLIGHT_PRODUCT_NOT_FOUND", ProductType.Bundle)))
        assertNotNull(midResponse.firstError)
        assertEquals(ApiError.Code.PACKAGE_SEARCH_ERROR, midResponse.roomResponseFirstErrorCode.errorCode)
    }

    @Test
    fun testRoomResponseFallbackError() {
        val midResponse = mockMIDResponse(errors = arrayListOf(MultiItemError("description", "FALLBACK", ProductType.Bundle)))
        assertNotNull(midResponse.firstError)
        assertEquals(ApiError.Code.UNKNOWN_ERROR, midResponse.roomResponseFirstErrorCode.errorCode)
    }

    private fun mockMIDResponse(offers: List<MultiItemOffer> = emptyList(),
                                hotels: Map<String, HotelOffer> = emptyMap(),
                                flights: Map<String, FlightOffer> = emptyMap(),
                                flightLegs: Map<String, MultiItemFlightLeg> = emptyMap(),
                                errors: List<MultiItemError>? = null): MultiItemApiSearchResponse {
        return MultiItemApiSearchResponse(offers = offers, hotels = hotels, flights = flights, flightLegs = flightLegs, errors = errors)
    }
}
