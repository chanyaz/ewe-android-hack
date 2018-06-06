package com.expedia.bookings.hotel.widget

import android.app.Activity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.services.ReviewsServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelReviewSearchResultsViewTest {
    var reviewServicesRule = ServicesRule(ReviewsServices::class.java)
        @Rule get
    private lateinit var view: HotelReviewSearchResultsView

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        view = android.view.LayoutInflater.from(activity).inflate(R.layout.hotel_reviews_search_results_view_test, null) as HotelReviewSearchResultsView
        view.viewModel.reviewsServices = reviewServicesRule.services!!
    }

    @Test
    fun testSearchAndCollapse() {
        view.doSearch("PrivateBank", "123")
        assertNotEquals(view.reviewsPageView.recyclerAdapter.itemCount, 0)
        assertEquals(view.reviewsPageView.visibility, View.VISIBLE)
        assertTrue(view.reviewsPageView.viewModel.hasReviews)
        assertEquals(view.compositeDisposable.size(), 1)
        view.onCollapse()
        assertEquals(view.reviewsPageView.visibility, View.GONE)
        assertEquals(view.reviewsPageView.recyclerAdapter.itemCount, 0)
        assertFalse(view.reviewsPageView.viewModel.hasReviews)
        assertEquals(view.compositeDisposable.size(), 0)
    }

    @Test
    fun testLoadMore() {
        view.doSearch("PrivateBank", "123")
        reviewServicesRule.server.takeRequest()
        view.reviewsPageView.recyclerAdapter.loadMoreObservable.onNext(Unit)
        assertTrue(reviewServicesRule.server.takeRequest().path.contains("start=25"))
    }
}
