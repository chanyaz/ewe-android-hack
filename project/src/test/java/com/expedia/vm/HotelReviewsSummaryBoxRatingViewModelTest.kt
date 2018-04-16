package com.expedia.vm

import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class HotelReviewsSummaryBoxRatingViewModelTest {

    private val context = RuntimeEnvironment.application

    private lateinit var vm: HotelReviewsSummaryBoxRatingViewModel

    @Before
    fun before() {
        val reviewSummary = createDefaultReviewSummary()
        vm = createViewModel(reviewSummary)
    }

    @Test
    fun testGuestRatingString() {
        assertEquals("3.9/5", vm.guestRatingString)
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
    fun testNumberOfReviewsStringZero() {
        vm = createViewModel(createDefaultReviewSummary(), 0)
        assertEquals("0 Reviews", vm.numberOfReviewsString)
    }

    @Test
    fun testNumberOfReviewsStringOne() {
        vm = createViewModel(createDefaultReviewSummary(), 1)
        assertEquals("View 1 Review", vm.numberOfReviewsString)
    }

    @Test
    fun testNumberOfReviewsStringTwo() {
        vm = createViewModel(createDefaultReviewSummary(), 2)
        assertEquals("View 2 Reviews", vm.numberOfReviewsString)
    }

    @Test
    fun testNumberOfReviewsStringMany() {
        vm = createViewModel(createDefaultReviewSummary(), 3)
        assertEquals("View 3 Reviews", vm.numberOfReviewsString)
    }

    @Test
    fun testNoNumberOfReviewsString() {
        assertNull(vm.numberOfReviewsString)
    }

    @Test
    fun testRoomCleanlinessReviewSummary() {
        val reviewSummaryDescriptionAndRating = vm.roomCleanlinessReviewSummary
        assertEquals("Room cleanliness", reviewSummaryDescriptionAndRating.description)
        assertEquals(4.1f, reviewSummaryDescriptionAndRating.rating)
    }

    @Test
    fun testRoomComfortReviewSummary() {
        val reviewSummaryDescriptionAndRating = vm.roomComfortReviewSummary
        assertEquals("Room comfort", reviewSummaryDescriptionAndRating.description)
        assertEquals(3.6f, reviewSummaryDescriptionAndRating.rating)
    }

    @Test
    fun testRoomServiceAndStaffReviewSummary() {
        val reviewSummaryDescriptionAndRating = vm.serviceStaffReviewSummary
        assertEquals("Service & staff", reviewSummaryDescriptionAndRating.description)
        assertEquals(3.6f, reviewSummaryDescriptionAndRating.rating)
    }

    @Test
    fun testHotelConditionReviewSummary() {
        val reviewSummaryDescriptionAndRating = vm.hotelConditionReviewSummary
        assertEquals("Hotel condition", reviewSummaryDescriptionAndRating.description)
        assertEquals(4.1f, reviewSummaryDescriptionAndRating.rating)
    }

    private fun testGuestRatingRecommendationString(rating: Float, string: String) {
        val reviewSummary = createDefaultReviewSummary()
        reviewSummary.avgOverallRating = rating
        vm = createViewModel(reviewSummary)
        assertEquals(string, vm.guestRatingRecommendationString)
    }

    private fun createDefaultReviewSummary(): HotelReviewsResponse.ReviewSummary {
        return HotelReviewsResponse.ReviewSummary().apply {
            id = "id"
            hotelId = "hotelId"
            avgOverallRating = 3.85f
            cleanliness = 4.07f
            serviceAndStaff = 3.63f
            hotelCondition = 4.14f
            roomComfort = 3.56f
        }
    }

    private fun createViewModel(reviewSummary: HotelReviewsResponse.ReviewSummary, numberOfReviews: Int? = null): HotelReviewsSummaryBoxRatingViewModel {
        return HotelReviewsSummaryBoxRatingViewModel(context, reviewSummary, numberOfReviews)
    }
}
