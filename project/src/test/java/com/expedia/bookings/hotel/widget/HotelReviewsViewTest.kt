package com.expedia.bookings.hotel.widget

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.presenter.hotel.HotelReviewsView
import com.expedia.bookings.services.ReviewsServices
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelReviewsViewTest {
    var reviewServicesRule = ServicesRule(ReviewsServices::class.java)
        @Rule get
    lateinit var mockAnalyticsProvider: AnalyticsProvider
    private lateinit var view: HotelReviewsView
    private var context = RuntimeEnvironment.application

    @Before
    fun setup() {
        AbacusTestUtils.bucketTests(AbacusUtils.HotelUGCSearch)
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        view = android.view.LayoutInflater.from(activity).inflate(R.layout.hotel_reviews_view_test, null) as HotelReviewsView
        view.reviewServices = reviewServicesRule.services!!
    }

    @Test
    fun testExpandSearchView() {
        view.toolbar.menu.getItem(0).expandActionView()

        OmnitureTestUtils.assertStateTracked("App.Hotels.Reviews.Search",
                Matchers.allOf(
                        OmnitureMatchers.withProps(mapOf(2 to "hotels")),
                        OmnitureMatchers.withEvars(mapOf(2 to "D=c2"))
                ),
                mockAnalyticsProvider)
        assertEquals((view.toolbar.background as ColorDrawable).color, ContextCompat.getColor(context, R.color.white))
        assertEquals(view.searchResultsView.visibility, View.VISIBLE)
    }

    @Test
    fun testCollapseSearchView() {
        view.toolbar.menu.getItem(0).collapseActionView()

        assertEquals((view.toolbar.background as ColorDrawable).color, ContextCompat.getColor(context, R.color.app_primary))
        assertEquals(view.searchResultsView.visibility, View.GONE)
    }
}
