package com.expedia.bookings.hotel.vm

import com.expedia.bookings.data.hotels.ReviewSummary
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.HotelReviewsSummaryWidgetViewModel
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelReviewsSummaryWidgetViewModelTest {

    private val context = RuntimeEnvironment.application
    private lateinit var vm: HotelReviewsSummaryWidgetViewModel
    private lateinit var reviewSummary: ReviewSummary

    @Before
    fun before() {
        reviewSummary = createDefaultReviewSummary()
        vm = HotelReviewsSummaryWidgetViewModel(context)
    }

    @Test
    fun testDefaultReview() {
        val overallObservable = TestObserver.create<String>()
        val cleanlinessObservable = TestObserver.create<CharSequence>()
        val comfortObservable = TestObserver.create<CharSequence>()
        val serviceObservable = TestObserver.create<CharSequence>()
        val conditionObservable = TestObserver.create<CharSequence>()

        vm.reviewsSummaryObserver.onNext(reviewSummary)

        vm.overallRatingObservable.subscribe(overallObservable)
        vm.roomCleanlinessObservable.subscribe(cleanlinessObservable)
        vm.roomComfortObservable.subscribe(comfortObservable)
        vm.serviceStaffObservable.subscribe(serviceObservable)
        vm.hotelConditionObservable.subscribe(conditionObservable)

        overallObservable.assertValue("3.8")
        assertEquals(cleanlinessObservable.values()[0].toString(), "4.1 - Room cleanliness")
        assertEquals(comfortObservable.values()[0].toString(), "3.6 - Room comfort")
        assertEquals(serviceObservable.values()[0].toString(), ".0 - Service & staff")
        assertEquals(conditionObservable.values()[0].toString(), "4.1 - Hotel condition")
    }

    private fun createDefaultReviewSummary(): ReviewSummary {
        return ReviewSummary().apply {
            id = "id"
            totalReviewCnt = 10
            hotelId = "hotelId"
            avgOverallRating = 3.85f
            cleanliness = 4.07f
            serviceAndStaff = 0f
            hotelCondition = 4.14f
            roomComfort = 3.56f
        }
    }
}
