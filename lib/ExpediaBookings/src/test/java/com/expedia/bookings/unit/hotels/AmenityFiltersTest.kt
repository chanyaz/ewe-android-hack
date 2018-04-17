package com.expedia.bookings.unit.hotels

import com.expedia.bookings.data.hotels.AmenityFilters
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AmenityFiltersTest {

    @Test
    fun testMapToLegacyIdPool() {
        assertEquals("7", AmenityFilters.mapToLegacyId("pool"))
        assertEquals("7", AmenityFilters.mapToLegacyId("childPool"))
    }

    @Test
    fun testMapToLegacyIdFreeParking() {
        assertEquals("14", AmenityFilters.mapToLegacyId("freeParking"))
    }

    @Test
    fun testMapToLegacyIdFreeBreakfast() {
        assertEquals("16", AmenityFilters.mapToLegacyId("freeBreakfast"))
    }

    @Test
    fun testMapToLegacyIdPetsAllowed() {
        assertEquals("17", AmenityFilters.mapToLegacyId("petsAllowed"))
    }

    @Test
    fun testMapToLegacyIdHighSpeedInternet() {
        assertEquals("19", AmenityFilters.mapToLegacyId("highSpeedInternet"))
    }

    @Test
    fun testMapToLegacyIdFreeAirConditioning() {
        assertEquals("27", AmenityFilters.mapToLegacyId("airConditioning"))
    }

    @Test
    fun testMapToLegacyIdFreeAllInclusive() {
        assertEquals("30", AmenityFilters.mapToLegacyId("allInclusive"))
    }

    @Test
    fun testMapToLegacyIdFreeAirportTransport() {
        assertEquals("66", AmenityFilters.mapToLegacyId("freeAirportTransport"))
    }

    @Test
    fun testMapToLegacyId7() {
        assertEquals("7", AmenityFilters.mapToLegacyId("7"))
    }

    @Test
    fun testMapToLegacyId14() {
        assertEquals("14", AmenityFilters.mapToLegacyId("14"))
    }

    @Test
    fun testMapToLegacyId16() {
        assertEquals("16", AmenityFilters.mapToLegacyId("16"))
    }

    @Test
    fun testMapToLegacyId17() {
        assertEquals("17", AmenityFilters.mapToLegacyId("17"))
    }

    @Test
    fun testMapToLegacyId19() {
        assertEquals("19", AmenityFilters.mapToLegacyId("19"))
    }

    @Test
    fun testMapToLegacyId27() {
        assertEquals("27", AmenityFilters.mapToLegacyId("27"))
    }

    @Test
    fun testMapToLegacyId30() {
        assertEquals("30", AmenityFilters.mapToLegacyId("30"))
    }

    @Test
    fun testMapToLegacyId66() {
        assertEquals("66", AmenityFilters.mapToLegacyId("66"))
    }

    @Test
    fun testMapToLegacyIdInvalid() {
        assertNull(AmenityFilters.mapToLegacyId("randomString"))
        assertNull(AmenityFilters.mapToLegacyId("0"))
    }
}
