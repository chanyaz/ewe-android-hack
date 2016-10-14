package com.expedia.bookings.test.robolectric

import com.expedia.bookings.R
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.RailUtils
import com.expedia.vm.test.rail.RailSearchRequestMock
import com.squareup.phrase.Phrase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class RailUtilsTest {

    val context = RuntimeEnvironment.application
    val testOriginString = "Paddington"
    val testDestinationString = "Liverpool"

    @Test
    fun testToolbarTitle() {
        val params = defaultBuilder().build()
        val toolbarTitle = RailUtils.getToolbarTitleFromSearchRequest(params)

        assertEquals("${testOriginString} - ${testDestinationString}", toolbarTitle)
    }

    @Test
    fun testSubtitleOneTraveler() {
        val builder = defaultBuilder()
        val params = builder.adults(1).build() as RailSearchRequest

        val expectedDateString = DateFormatUtils.formatRailDateRange(context, params.startDate, params.endDate)
        val expectedTravelerString = "1 Traveler"
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
        val expectedTravelerString = "2 Travelers"
        val expectedSubtitle = Phrase.from(context, R.string.rail_results_toolbar_subtitle_TEMPLATE)
                .put("searchdates", expectedDateString)
                .put("travelerspart", expectedTravelerString).format().toString()

        val toolbarSubtitle = RailUtils.getToolbarSubtitleFromSearchRequest(context, params)
        assertEquals(expectedSubtitle, toolbarSubtitle)
    }

        private fun defaultBuilder() : RailSearchRequest.Builder {
        return RailSearchRequest.Builder(20, 100)
                .departDateTimeMillis(RailSearchRequestMock.departTime())
                .startDate(RailSearchRequestMock.departDate())
                .origin(RailSearchRequestMock.origin(testOriginString))
                .destination(RailSearchRequestMock.destination(testDestinationString)) as RailSearchRequest.Builder
    }
}