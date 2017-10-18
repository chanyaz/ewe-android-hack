package com.expedia.vm.test.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.data.rail.responses.RailResponseStatus
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.services.RailServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.vm.rail.RailOutboundResultsViewModel
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class RailOutboundResultsViewModelTest {
    var railServicesRule = ServicesRule(RailServices::class.java)
        @Rule get

    lateinit var testViewModel: RailOutboundResultsViewModel

    val testOriginString = "Paddington"
    val testDestinationString = "Liverpool"
    lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.application
        testViewModel = RailOutboundResultsViewModel(context, railServicesRule.services!!)
    }

    @Test
    fun testToolbarTitle() {
        val params = defaultBuilder().build()
        val testSubscriber = TestObserver<String>()
        testViewModel.titleSubject.subscribe(testSubscriber)
        testViewModel.paramsSubject.onNext(params)

        assertEquals("${testOriginString} - ${testDestinationString}", testSubscriber.values()[0])
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
    fun testOneWayPriceHeader() {
        val params = defaultBuilder().build()

        val testSubscriber = TestObserver<CharSequence>()
        testViewModel.priceHeaderSubject.subscribe(testSubscriber)
        testViewModel.paramsSubject.onNext(params)

        assertEquals(context.getString(R.string.one_way_from), testSubscriber.values()[0])
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

        assertEquals(context.getString(R.string.select_outbound), testSubscriber.values()[0])
    }

    @Test
    fun testChildrenWarning() {
        val searchResponse = RailSearchResponse()
        val responseStatus = RailResponseStatus()
        val warning = RailResponseStatus.Warning()
        warning.warningCode = "WARN0001"
        responseStatus.warningList = listOf(warning)
        searchResponse.responseStatus = responseStatus

        val testSubscriber = TestObserver<Boolean>()
        testViewModel.showChildrenWarningObservable.subscribe(testSubscriber)
        testViewModel.railResultsObservable.onNext(searchResponse)

        assertTrue(testSubscriber.values()[0])
    }

    @Test
    fun testLegalMessageOneWay() {
        val builder = defaultBuilder()
        val params = builder.endDate(RailSearchRequestMock.returnDate()).build() as RailSearchRequest

        val testSubscriber = TestObserver<String>()
        testViewModel.legalBannerMessageObservable.subscribe(testSubscriber)
        testViewModel.paramsSubject.onNext(params)

        assertEquals(context.getString(R.string.rail_search_legal_banner_one_way), testSubscriber.values()[0])
    }

    @Test
    fun testLegalMessageRoundTrip() {
        val builder = defaultBuilder()
        val params = builder.searchType(true).returnDateTimeMillis(RailSearchRequestMock.departTime()).endDate(RailSearchRequestMock.returnDate()).build() as RailSearchRequest

        val testSubscriber = TestObserver<String>()
        testViewModel.legalBannerMessageObservable.subscribe(testSubscriber)
        testViewModel.paramsSubject.onNext(params)

        assertEquals(context.getString(R.string.rail_search_legal_banner_round_trip), testSubscriber.values()[0])
    }

    private fun defaultBuilder() : RailSearchRequest.Builder {
        return RailSearchRequest.Builder(20, 100)
                .departDateTimeMillis(RailSearchRequestMock.departTime())
                .startDate(RailSearchRequestMock.departDate())
                .origin(RailSearchRequestMock.origin(testOriginString))
                .destination(RailSearchRequestMock.destination(testDestinationString)) as RailSearchRequest.Builder
    }
}