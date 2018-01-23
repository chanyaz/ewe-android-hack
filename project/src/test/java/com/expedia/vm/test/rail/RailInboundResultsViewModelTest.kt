package com.expedia.vm.test.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.vm.rail.RailInboundResultsViewModel
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class RailInboundResultsViewModelTest {
    lateinit var testViewModel: RailInboundResultsViewModel

    val testOriginString = "Paddington"
    val testDestinationString = "Liverpool"
    lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.application
        testViewModel = RailInboundResultsViewModel(context)
    }

    @Test
    fun testToolbarTitle() {
        val params = defaultBuilder().build()
        val testSubscriber = TestObserver<String>()
        testViewModel.titleSubject.subscribe(testSubscriber)
        testViewModel.paramsSubject.onNext(params)

        assertEquals("$testOriginString - $testDestinationString", testSubscriber.values()[0])
    }

    @Test
    fun testSubtitleOneTraveler() {
        val builder = defaultBuilder()
        val params = builder.adults(1).build() as RailSearchRequest

        val expectedDateString = DateFormatUtils.formatRailDateRange(RuntimeEnvironment.application, params.startDate, params.endDate)
        val expectedTravelerString = "1 traveler"
        val expectedSubtitle = Phrase.from(context, R.string.rail_results_toolbar_subtitle_TEMPLATE)
                .put("searchdates", expectedDateString)
                .put("travelerspart", expectedTravelerString).format().toString()

        val testSubscriber = TestObserver<CharSequence>()
        testViewModel.subtitleSubject.subscribe(testSubscriber)
        testViewModel.paramsSubject.onNext(params)

        assertEquals(expectedSubtitle, testSubscriber.values()[0])
    }

    @Test
    fun testSubtitleMultipleTravelers() {
        val builder = defaultBuilder()
        val params = builder.adults(2).build() as RailSearchRequest

        val expectedDateString = DateFormatUtils.formatRailDateRange(context, params.startDate, params.endDate)
        val expectedTravelerString = "2 travelers"
        val expectedSubtitle = Phrase.from(RuntimeEnvironment.application, R.string.rail_results_toolbar_subtitle_TEMPLATE)
                .put("searchdates", expectedDateString)
                .put("travelerspart", expectedTravelerString).format().toString()

        val testSubscriber = TestObserver<CharSequence>()
        testViewModel.subtitleSubject.subscribe(testSubscriber)
        testViewModel.paramsSubject.onNext(params)

        assertEquals(expectedSubtitle, testSubscriber.values()[0])
    }

    @Test
    fun testRoundTripPriceHeader() {
        val builder = defaultBuilder()
        val params = builder.endDate(RailSearchRequestMock.returnDate()).build() as RailSearchRequest

        val testSubscriber = TestObserver<CharSequence>()
        testViewModel.priceHeaderSubject.subscribe(testSubscriber)
        testViewModel.paramsSubject.onNext(params)

        assertEquals(context.getString(R.string.total_from), testSubscriber.values()[0])
    }

    @Test
    fun testDirectionHeader() {
        val testSubscriber = TestObserver<CharSequence>()
        testViewModel.directionHeaderSubject.subscribe(testSubscriber)

        assertEquals(context.getString(R.string.select_return), testSubscriber.values()[0])
    }

    private fun defaultBuilder(): RailSearchRequest.Builder {
        return RailSearchRequest.Builder(20, 100)
                .departDateTimeMillis(RailSearchRequestMock.departTime())
                .startDate(RailSearchRequestMock.departDate())
                .origin(RailSearchRequestMock.origin(testOriginString))
                .destination(RailSearchRequestMock.destination(testDestinationString)) as RailSearchRequest.Builder
    }
}
