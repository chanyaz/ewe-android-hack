package com.expedia.vm

import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.OmnitureTestUtils.Companion.assertStateTracked
import com.expedia.bookings.OmnitureTestUtils.Companion.assertStateTrackedNumTimes
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
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

        val toolbarTitleTestObservable = TestSubscriber.create<String>()
        val toolbarSubtitleTestObservable = TestSubscriber.create<String>()
        val hotelIdTestObservable = TestSubscriber.create<String>()

        vm.toolbarTitleObservable.subscribe(toolbarTitleTestObservable)
        vm.toolbarSubtitleObservable.subscribe(toolbarSubtitleTestObservable)
        vm.hotelIdObservable.subscribe(hotelIdTestObservable)

        vm.hotelOfferObserver.onNext(offer)

        assertEquals("name", toolbarTitleTestObservable.onNextEvents.last())
        assertEquals("1 reviews", toolbarSubtitleTestObservable.onNextEvents.last())
        assertEquals("id", hotelIdTestObservable.onNextEvents.last())

        offer.hotelName = "name2"
        offer.hotelId = "id2"
        offer.totalReviews = 0

        vm.hotelOfferObserver.onNext(offer)

        assertEquals("name2", toolbarTitleTestObservable.onNextEvents.last())
        assertEquals("0 reviews", toolbarSubtitleTestObservable.onNextEvents.last())
        assertEquals("id2", hotelIdTestObservable.onNextEvents.last())
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
