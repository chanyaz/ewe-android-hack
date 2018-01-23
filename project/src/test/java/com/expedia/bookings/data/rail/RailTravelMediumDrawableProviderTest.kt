package com.expedia.bookings.data.rail

import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailSegment
import com.expedia.bookings.rail.data.RailTravelMediumDrawableProvider
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.testutils.JSONResourceReader
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class RailTravelMediumDrawableProviderTest {
    @Test
    fun testLondonUndergroundDisplaysAsSubway() {
        val resourceReader = JSONResourceReader("src/test/resources/raw/rail/rail_underground_travel_segment.json")
        val railTravelSegment = resourceReader.constructUsingGson(RailSegment::class.java)

        val drawableId = RailTravelMediumDrawableProvider.findMappedDrawable(railTravelSegment.travelMedium.travelMediumCode)

        assertEquals(R.drawable.rails_subway_icon, drawableId)
    }
}
