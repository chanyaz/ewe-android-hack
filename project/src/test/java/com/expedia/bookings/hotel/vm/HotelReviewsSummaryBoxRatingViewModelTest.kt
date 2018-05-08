package com.expedia.bookings.hotel.vm

import com.expedia.bookings.data.hotels.ReviewSummary
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelReviewsSummaryBoxRatingViewModelTest {

    private val context = RuntimeEnvironment.application

    private lateinit var vm: HotelReviewsSummaryBoxRatingViewModel

    private lateinit var reviewSummary: ReviewSummary

    @Before
    fun before() {
        reviewSummary = createDefaultReviewSummary()
        vm = HotelReviewsSummaryBoxRatingViewModel(context)
    }

    @Test
    fun testGuestRatingString() {
        val testSubscriber = TestObserver.create<String>()
        vm.guestRatingObservable.subscribe(testSubscriber)
        vm.reviewsSummaryObserver.onNext(reviewSummary)
        assertEquals("3.9/5", testSubscriber.values()[0])
    }

    @Test
    fun testShouldShowRatingBar() {
        val testSubscriber = TestObserver.create<Boolean>()
        vm.barRatingViewVisibility.subscribe(testSubscriber)
        vm.reviewsSummaryObserver.onNext(reviewSummary)
        assertTrue(testSubscriber.values()[0])
    }

    @Test
    fun testShouldHideRatingBar() {
        val testSubscriber = TestObserver.create<Boolean>()
        vm.barRatingViewVisibility.subscribe(testSubscriber)
        val reviewSummary = createDefaultReviewSummary()
        reviewSummary.totalReviewCnt = 0
        vm.reviewsSummaryObserver.onNext(reviewSummary)
        assertFalse(testSubscriber.values()[0])
    }

    @Test
    fun testGuestRatingRecommendationStringBase() {
        testGuestRatingRecommendationString(3.4f, " - guest rating")
    }

    @Test
    fun testGuestRatingRecommendationStringGood() {
        testGuestRatingRecommendationString(3.91f, " - Good!")
    }

    @Test
    fun testGuestRatingRecommendationStringVeryGood() {
        testGuestRatingRecommendationString(4.22f, " - Very Good!")
    }

    @Test
    fun testGuestRatingRecommendationStringExcellent() {
        testGuestRatingRecommendationString(4.43f, " - Excellent!")
    }

    @Test
    fun testGuestRatingRecommendationStringWonderful() {
        testGuestRatingRecommendationString(4.64f, " - Wonderful!")
    }

    @Test
    fun testGuestRatingRecommendationStringExceptional() {
        testGuestRatingRecommendationString(4.7f, " - Exceptional!")
    }

    @Test
    fun testGuestRatingRecommendationStringBaseRoundDown() {
        testGuestRatingRecommendationString(3.449f, " - guest rating")
    }

    @Test
    fun testGuestRatingRecommendationStringRoundUpToGood() {
        testGuestRatingRecommendationString(3.45f, " - Good!")
    }

    @Test
    fun testGuestRatingRecommendationStringRatingOverFive() {
        testGuestRatingRecommendationString(420f, " - Exceptional!")
    }

    @Test
    fun testGuestRatingRecommendationStringRatingBelowZero() {
        testGuestRatingRecommendationString(-6f, " - guest rating")
    }

    @Test
    fun testRoomCleanlinessReviewSummary() {
        val testSubscriber = TestObserver.create<HotelReviewsSummaryBoxRatingViewModel.ReviewSummaryDescriptionAndRating>()
        vm.roomCleanlinessObservable.subscribe(testSubscriber)
        vm.reviewsSummaryObserver.onNext(reviewSummary)

        val reviewSummaryDescriptionAndRating = testSubscriber.values()[0]
        assertEquals("Room cleanliness", reviewSummaryDescriptionAndRating.description)
        assertEquals(4.1f, reviewSummaryDescriptionAndRating.rating)
    }

    @Test
    fun testRoomComfortReviewSummary() {
        val testSubscriber = TestObserver.create<HotelReviewsSummaryBoxRatingViewModel.ReviewSummaryDescriptionAndRating>()
        vm.roomComfortObservable.subscribe(testSubscriber)
        vm.reviewsSummaryObserver.onNext(reviewSummary)

        val reviewSummaryDescriptionAndRating = testSubscriber.values()[0]
        assertEquals("Room comfort", reviewSummaryDescriptionAndRating.description)
        assertEquals(3.6f, reviewSummaryDescriptionAndRating.rating)
    }

    @Test
    fun testRoomServiceAndStaffReviewSummary() {
        val testSubscriber = TestObserver.create<HotelReviewsSummaryBoxRatingViewModel.ReviewSummaryDescriptionAndRating>()
        vm.serviceStaffObservable.subscribe(testSubscriber)
        vm.reviewsSummaryObserver.onNext(reviewSummary)

        val reviewSummaryDescriptionAndRating = testSubscriber.values()[0]
        assertEquals("Service & staff", reviewSummaryDescriptionAndRating.description)
        assertEquals(3.6f, reviewSummaryDescriptionAndRating.rating)
    }

    @Test
    fun testHotelConditionReviewSummary() {
        val testSubscriber = TestObserver.create<HotelReviewsSummaryBoxRatingViewModel.ReviewSummaryDescriptionAndRating>()
        vm.hotelConditionObservable.subscribe(testSubscriber)
        vm.reviewsSummaryObserver.onNext(reviewSummary)

        val reviewSummaryDescriptionAndRating = testSubscriber.values()[0]
        assertEquals("Hotel condition", reviewSummaryDescriptionAndRating.description)
        assertEquals(4.1f, reviewSummaryDescriptionAndRating.rating)
    }

    private fun testGuestRatingRecommendationString(rating: Float, string: String) {
        val reviewSummary = createDefaultReviewSummary()
        reviewSummary.avgOverallRating = rating

        val testSubscriber = TestObserver.create<String>()
        vm.guestRatingRecommendationObservable.subscribe(testSubscriber)
        vm.reviewsSummaryObserver.onNext(reviewSummary)

        assertEquals(string, testSubscriber.values()[0])
    }

    private fun createDefaultReviewSummary(): ReviewSummary {
        return ReviewSummary().apply {
            id = "id"
            totalReviewCnt = 10
            hotelId = "hotelId"
            avgOverallRating = 3.85f
            cleanliness = 4.07f
            serviceAndStaff = 3.63f
            hotelCondition = 4.14f
            roomComfort = 3.56f
        }
    }
}
