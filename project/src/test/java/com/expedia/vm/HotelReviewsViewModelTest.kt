package com.expedia.vm

import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.analytics.OmnitureTestUtils.Companion.assertStateTracked
import com.expedia.bookings.analytics.OmnitureTestUtils.Companion.assertStateTrackedNumTimes
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelReviewsViewModelTest {

    private val context = RuntimeEnvironment.application
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setUp() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testHotelObserver() {
        val vm = HotelReviewsViewModel(context)
        val offer = createHotelOffersResponse()

        val toolbarTitleTestObservable = TestObserver.create<String>()
        val toolbarSubtitleTestObservable = TestObserver.create<String>()
        val hotelIdTestObservable = TestObserver.create<String>()

        vm.toolbarTitleObservable.subscribe(toolbarTitleTestObservable)
        vm.toolbarSubtitleObservable.subscribe(toolbarSubtitleTestObservable)
        vm.hotelIdObservable.subscribe(hotelIdTestObservable)

        vm.hotelOfferObserver.onNext(offer)

        assertEquals("name", toolbarTitleTestObservable.values().last())
        assertEquals("1 reviews", toolbarSubtitleTestObservable.values().last())
        assertEquals("id", hotelIdTestObservable.values().last())

        offer.hotelName = "name2"
        offer.hotelId = "id2"
        offer.totalReviews = 0

        vm.hotelOfferObserver.onNext(offer)

        assertEquals("name2", toolbarTitleTestObservable.values().last())
        assertEquals("0 reviews", toolbarSubtitleTestObservable.values().last())
        assertEquals("id2", hotelIdTestObservable.values().last())
    }

    @Test
    fun testTrackReviewPageLoad() {
        val vm = HotelReviewsViewModel(context)
        vm.trackReviewPageLoad()

        assertReviewTracked(1)
    }

    @Test
    fun testPackageTrackReviewPageLoad() {
        val vm = HotelReviewsViewModel(context, LineOfBusiness.PACKAGES)
        vm.trackReviewPageLoad()

        assertStateTracked("App.Package.Reviews", Matchers.allOf(
                OmnitureMatchers.withProps(mapOf(2 to "package:FH")),
                OmnitureMatchers.withEvars(mapOf(2 to "D=c2", 18 to "D=pageName"))),
                mockAnalyticsProvider)
    }

    @Test
    fun testDontRetrackReviewPageLoad() {
        val vm = HotelReviewsViewModel(context)
        vm.trackReviewPageLoad()

        assertReviewTracked(1)

        vm.trackReviewPageLoad()
        vm.trackReviewPageLoad()

        assertReviewTracked(1)
    }

    @Test
    fun testResetTracking() {
        val vm = HotelReviewsViewModel(context)
        vm.trackReviewPageLoad()

        assertReviewTracked(1)

        vm.resetTracking()
        vm.trackReviewPageLoad()

        assertReviewTracked(2)
    }

    private fun assertReviewTracked(numInvocation: Int) {
        assertStateTrackedNumTimes("App.Hotels.Reviews", Matchers.allOf(
                OmnitureMatchers.withProps(mapOf(2 to "hotels")),
                OmnitureMatchers.withEvars(mapOf(2 to "D=c2", 18 to "App.Hotels.Reviews"))),
                numInvocation, mockAnalyticsProvider)
    }

    private fun createHotelOffersResponse(): HotelOffersResponse {
        val offer = HotelOffersResponse()
        offer.hotelName = "name"
        offer.hotelId = "id"
        offer.totalReviews = 1

        return offer
    }
}
