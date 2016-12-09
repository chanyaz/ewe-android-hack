package com.expedia.bookings.utils

import org.junit.Test
import kotlin.test.assertEquals

class ServicesUtilTest {

    @Test
    fun testDistortCoordinates() {
        assertEquals(37.3, ServicesUtil.distortCoordinates(37.2994921))
        assertEquals(-122.5, ServicesUtil.distortCoordinates(-122.4995990))
        assertEquals(37.2, ServicesUtil.distortCoordinates(37.2134921))
        assertEquals(-122.4, ServicesUtil.distortCoordinates(-122.4445990))
    }

}