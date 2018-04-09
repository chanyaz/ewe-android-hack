package com.expedia.bookings.test

import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.data.hotels.HotelReviewsResponse.ReviewSummary
import com.expedia.bookings.data.hotels.ReviewSort
import com.expedia.bookings.hotel.data.TranslatedReview
import com.expedia.bookings.services.ReviewsServices
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.vm.HotelReviewsAdapterViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import io.reactivex.Observable
import com.expedia.bookings.services.TestObserver
import org.hamcrest.CoreMatchers
import org.junit.Assert.assertThat
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HotelReviewsTest {

    var reviewServicesRule = ServicesRule(ReviewsServices::class.java)
        @Rule get

    private val HOTEL_ID = "26650"

    var vm: HotelReviewsAdapterViewModel by Delegates.notNull()

    @Before
    fun before() {
        vm = HotelReviewsAdapterViewModel(HOTEL_ID, reviewServicesRule.services!!, "en_US")
    }

    @Test
    fun moreReviewsFetchedOnSubsequentCalls() {
        Observable.concat(
                Observable.just(ReviewSort.LOWEST_RATING_FIRST, ReviewSort.LOWEST_RATING_FIRST, ReviewSort.LOWEST_RATING_FIRST),
                Observable.never())
                .subscribe(vm.reviewsObserver)

        val recordedRequest1 = reviewServicesRule.server.takeRequest()
        assertEquals(getExpectedReviewRequestStr(0), recordedRequest1.path)
        val recordedRequest2 = reviewServicesRule.server.takeRequest()
        assertEquals(getExpectedReviewRequestStr(25), recordedRequest2.path)
        val recordedRequest3 = reviewServicesRule.server.takeRequest()
        assertEquals(getExpectedReviewRequestStr(50), recordedRequest3.path)
    }

    @Test
    fun testGetReviewSort() {
        Observable.concat(
                Observable.just(ReviewSort.LOWEST_RATING_FIRST, ReviewSort.HIGHEST_RATING_FIRST, ReviewSort.NEWEST_REVIEW_FIRST),
                Observable.never())
                .subscribe(vm.reviewsObserver)

        var recordedRequest = reviewServicesRule.server.takeRequest()
        assertThat(recordedRequest.path, CoreMatchers.containsString("RATINGASC"))

        recordedRequest = reviewServicesRule.server.takeRequest()
        assertThat(recordedRequest.path, CoreMatchers.containsString("RATINGDESC"))

        recordedRequest = reviewServicesRule.server.takeRequest()
        assertThat(recordedRequest.path, CoreMatchers.containsString("DATEDESCWITHLANGBUCKETS"))
    }

    @Test
    fun testStartDownloads() {
        val testSubscriber = TestObserver<ReviewSort>()
        vm.reviewsObservable.map { it.first }.subscribe(testSubscriber)
        testSubscriber.assertValueSet(listOf(ReviewSort.LOWEST_RATING_FIRST, ReviewSort.HIGHEST_RATING_FIRST, ReviewSort.NEWEST_REVIEW_FIRST))
    }

    @Test
    fun reviewsSummary() {
        val testSubscriber = TestObserver<ReviewSummary>()
        vm.reviewsSummaryObservable.take(1).subscribe(testSubscriber)

        Observable.concat(
                Observable.just(ReviewSort.NEWEST_REVIEW_FIRST),
                Observable.never())
                .subscribe(vm.reviewsObserver)

        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testSubscriber.assertNoErrors()
        testSubscriber.assertComplete()
    }

    @Test
    fun testReviewTranslation() {
        val testSubscriber = TestObserver<String>()
        vm.translationUpdatedObservable.subscribe(testSubscriber)
        vm.toggleReviewTranslationObserver.onNext("5a2cc5ffa6ffd10dd50e1844")
        testSubscriber.assertValueCount(1)
        assertEquals(vm.translationMap.size, 1)
    }

    @Test
    fun testReviewTranslationFail() {
        val testSubscriber = TestObserver<String>()
        vm.translationUpdatedObservable.subscribe(testSubscriber)
        vm.toggleReviewTranslationObserver.onNext("")
        testSubscriber.assertValueCount(1)
        assertEquals(vm.translationMap.size, 0)
    }

    @Test
    fun testReviewAlreadyTranslated() {
        val testSubscriber = TestObserver<String>()
        vm.translationUpdatedObservable.subscribe(testSubscriber)
        vm.translationMap["a1"] = TranslatedReview(HotelReviewsResponse.Review(), false)
        vm.toggleReviewTranslationObserver.onNext("a1")
        testSubscriber.assertValue("a1")
        assertTrue(vm.translationMap["a1"]!!.showToUser)
    }

    private fun getExpectedReviewRequestStr(startParam: Int): String {
        return "/api/hotelreviews/hotel/%s?sortBy=RATINGASC&start=%d&items=25&locale=en_US".format(HOTEL_ID, startParam) + "&clientid=expedia.app.android.phone%3A6.9.0"
    }
}
