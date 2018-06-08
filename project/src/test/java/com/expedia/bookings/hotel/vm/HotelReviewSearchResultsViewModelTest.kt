package com.expedia.bookings.hotel.vm

import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.services.ReviewsServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import io.reactivex.disposables.CompositeDisposable
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelReviewSearchResultsViewModelTest {
    var reviewServicesRule = ServicesRule(ReviewsServices::class.java)
        @Rule get

    private var compositeDisposable = CompositeDisposable()
    private var viewModel = HotelReviewSearchResultsViewModel(compositeDisposable)
    lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setup() {
        viewModel.reviewsServices = reviewServicesRule.services!!
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testGetReview() {
        val testObserver = TestObserver<List<HotelReviewsResponse.Review>>()
        viewModel.reviewsObservable.subscribe(testObserver)
        viewModel.doSearch("PrivateBank", "123")
        testObserver.assertValueCount(1)
        val request = reviewServicesRule.server.takeRequest()
        assertEquals("0", request.requestUrl.queryParameter("start"))
        assertTrue(request.path.contains("123"))
        assertEquals(compositeDisposable.size(), 1)
    }

    @Test
    fun testGetNextPage() {
        val testObserver = TestObserver<List<HotelReviewsResponse.Review>>()
        viewModel.reviewsObservable.subscribe(testObserver)
        viewModel.doSearch("PrivateBank", "123")
        assertEquals(viewModel.pageNumber, 0)
        viewModel.getNextPage()
        assertEquals(viewModel.pageNumber, 1)
        viewModel.doSearch("PrivateBank", "a_different_hotel_id")

        val firstRequest = reviewServicesRule.server.takeRequest()
        val secondRequest = reviewServicesRule.server.takeRequest()
        val thirdRequest = reviewServicesRule.server.takeRequest()

        assertEquals("0", firstRequest.requestUrl.queryParameter("start"))
        assertTrue(firstRequest.path.contains("123"))
        assertEquals("25", secondRequest.requestUrl.queryParameter("start"))
        assertEquals("0", thirdRequest.requestUrl.queryParameter("start"))
        assertTrue(thirdRequest.path.contains("a_different_hotel_id"))

        testObserver.assertValueCount(3)
        assertEquals(compositeDisposable.size(), 3)

        OmnitureTestUtils.assertStateTrackedNumTimes("App.Hotels.Reviews.Search.Results",
                Matchers.allOf(
                        OmnitureMatchers.withProps(mapOf(2 to "hotels", 48 to "PrivateBank")),
                        OmnitureMatchers.withEvars(mapOf(2 to "D=c2"))
                ), 2,
                mockAnalyticsProvider)
    }

    @Test
    fun testSearchError() {
        viewModel.doSearch("query_with_no_results", "123")
        OmnitureTestUtils.assertStateTracked("App.Hotels.Reviews.Search.Results",
                Matchers.allOf(
                        OmnitureMatchers.withProps(mapOf(2 to "hotels", 48 to "query_with_no_results")),
                        OmnitureMatchers.withEvars(mapOf(2 to "D=c2")),
                        OmnitureMatchers.withEventsString("event332")
                ),
                mockAnalyticsProvider)
    }

    @Test
    fun testBadParam() {
        val testObserver = TestObserver<List<HotelReviewsResponse.Review>>()
        viewModel.reviewsObservable.subscribe(testObserver)
        viewModel.doSearch("PrivateBank", null)
        testObserver.assertValueCount(0)
    }

    @Test
    fun testCreateParams() {
        viewModel.currentHotelId = "123"
        viewModel.currentQuery = "test"
        val params = viewModel.createSearchParams()
        assertEquals(params!!.hotelId, "123")
        assertEquals(params.searchTerm, "test")
        assertEquals(params.numReviewsPerPage, 25)
        assertEquals(params.pageNumber, 0)
        assertEquals(params.sortBy, "")

        viewModel.currentQuery = null
        assertNull(viewModel.createSearchParams())

        viewModel.currentQuery = "test"
        viewModel.currentHotelId = null
        assertNull(viewModel.createSearchParams())

        viewModel.currentQuery = null
        viewModel.currentHotelId = null
        assertNull(viewModel.createSearchParams())

        viewModel.currentQuery = null
        viewModel.currentHotelId = " "
        assertNull(viewModel.createSearchParams())

        viewModel.currentQuery = " "
        viewModel.currentHotelId = null
        assertNull(viewModel.createSearchParams())
    }
}
