package com.expedia.bookings.utils

import com.expedia.bookings.R
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.rail.util.RailUtils
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.vm.test.rail.RailSearchRequestMock
import com.squareup.phrase.Phrase
import com.expedia.bookings.test.robolectric.shadows.ShadowDateFormat
import org.joda.time.DateTime
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowDateFormat::class))
class RailUtilsTest {
    val context = RuntimeEnvironment.application
    val testOriginString = "Paddington"
    val testDestinationString = "Liverpool"

    @Test
    fun testRailChangeTextNoChange() {
        val noChangeText = RailUtils.formatRailChangesText(context, 0)
        assertEquals(context.getString(R.string.rail_direct), noChangeText)
    }

    @Test
    fun testRailChangeTextSingular() {
        val oneChangeText = RailUtils.formatRailChangesText(context, 1)
        assertEquals("1 Change", oneChangeText)
    }

    @Test
    fun testRailChangeTextPlural() {
        val pluralChangeText = RailUtils.formatRailChangesText(context, 3)
        assertEquals("3 Changes", pluralChangeText)
    }

    @Test
    fun testToolbarTitle() {
        val params = defaultBuilder().build()
        val toolbarTitle = RailUtils.getToolbarTitleFromSearchRequest(params)

        assertEquals("$testOriginString - $testDestinationString", toolbarTitle)
    }

    @Test
    fun testSubtitleOneTraveler() {
        val builder = defaultBuilder()
        val params = builder.adults(1).build() as RailSearchRequest

        val expectedDateString = DateFormatUtils.formatRailDateRange(context, params.startDate, params.endDate)
        val expectedTravelerString = "1 traveler"
        val expectedSubtitle = Phrase.from(context, R.string.rail_results_toolbar_subtitle_TEMPLATE)
                .put("searchdates", expectedDateString)
                .put("travelerspart", expectedTravelerString).format().toString()

        val toolbarSubtitle = RailUtils.getToolbarSubtitleFromSearchRequest(context, params)
        assertEquals(expectedSubtitle, toolbarSubtitle)
    }

    @Test
    fun testSubtitleMultipleTravelers() {
        val builder = defaultBuilder()
        val params = builder.adults(2).build() as RailSearchRequest

        val expectedDateString = DateFormatUtils.formatRailDateRange(context, params.startDate, params.endDate)
        val expectedTravelerString = "2 travelers"
        val expectedSubtitle = Phrase.from(context, R.string.rail_results_toolbar_subtitle_TEMPLATE)
                .put("searchdates", expectedDateString)
                .put("travelerspart", expectedTravelerString).format().toString()

        val toolbarSubtitle = RailUtils.getToolbarSubtitleFromSearchRequest(context, params)
        assertEquals(expectedSubtitle, toolbarSubtitle)
    }

    private fun defaultBuilder(): RailSearchRequest.Builder {
        return RailSearchRequest.Builder(20, 100)
                .departDateTimeMillis(RailSearchRequestMock.departTime())
                .startDate(RailSearchRequestMock.departDate())
                .origin(RailSearchRequestMock.origin(testOriginString))
                .destination(RailSearchRequestMock.destination(testDestinationString)) as RailSearchRequest.Builder
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO,
            MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS))
    fun testFormatTimeInterval() {
        var start = DateTime().withTime(8, 30, 0, 0)
        var end = DateTime().withTime(11, 10, 0, 0)

        assertEquals("8:30 AM – 11:10 AM", RailUtils.formatTimeIntervalToDeviceFormat(context, start, end))

        start = DateTime().withTime(8, 30, 0, 0)
        end = DateTime().plusDays(1).withTime(8, 30, 0, 0)

        assertEquals("8:30 AM - 8:30 AM +1d", RailUtils.formatTimeIntervalToDeviceFormat(context, start, end))
    }
}
