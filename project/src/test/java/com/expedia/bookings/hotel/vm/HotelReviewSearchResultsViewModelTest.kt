package com.expedia.bookings.hotel.vm

import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.services.ReviewsServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import io.reactivex.disposables.CompositeDisposable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class HotelReviewSearchResultsViewModelTest {
    var reviewServicesRule = ServicesRule(ReviewsServices::class.java)
        @Rule get

    private var context = RuntimeEnvironment.application
    private var compositeDisposable = CompositeDisposable()
    private var viewModel = HotelReviewSearchResultsViewModel(context, compositeDisposable)

    @Before
    fun setup() {
        viewModel.reviewsServices = reviewServicesRule.services!!
    }

    @Test
    fun testGetReview() {
        val testObserver = TestObserver<List<HotelReviewsResponse.Review>>()
        viewModel.reviewsObservable.subscribe(testObserver)
        viewModel.doSearch("PrivateBank", "123")
        testObserver.assertValueCount(1)
        assertEquals(compositeDisposable.size(), 1)
    }

    @Test
    fun testCreateParams() {
        val params = viewModel.createSearchParams("test", "123")
        assertEquals(params!!.hotelId, "123")
        assertEquals(params.searchTerm, "test")
        assertEquals(params.languageSort, "")
        assertEquals(params.numReviewsPerPage, 25)
        assertEquals(params.pageNumber, 0)
        assertEquals(params.sortBy, "")

        assertNull(viewModel.createSearchParams(null, "123"))

        assertNull(viewModel.createSearchParams("test", null))
    }
}
