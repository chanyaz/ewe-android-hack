package com.expedia.bookings.tracking.hotel

import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.hotel.data.TranslatedReview
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.HotelReviewRowViewModel
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.Locale

@RunWith(RobolectricRunner::class)
class HotelReviewTranslationTrackingTest {
    private val context = RuntimeEnvironment.application
    private val viewModel = HotelReviewRowViewModel(context)
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    private var reviewOtherLanguage = HotelReviewsResponse.Review().apply {
        reviewId = "1"
        title = "Title in a different language"
        reviewText = "Content in a different language"
        contentLocale = "zz_zz"
        hotelId = "1"
        ratingOverall = 5
        userDisplayName = "Bob"
        recommended = true
        isRecommended = HotelReviewsResponse.Review.IsRecommended.YES
        userLocation = "Chicago"
        reviewSubmissionTime = DateTime.now()
    }

    private val reviewNativeLanguage = HotelReviewsResponse.Review().apply {
        reviewId = "1"
        title = "Title"
        reviewText = "Content"
        contentLocale = "en_us"
        hotelId = "1"
        ratingOverall = 5
        userDisplayName = "Bob"
        recommended = true
        isRecommended = HotelReviewsResponse.Review.IsRecommended.YES
        userLocation = "Chicago"
        reviewSubmissionTime = DateTime.now()
    }

    @Before
    fun setup() {
        Locale.setDefault(Locale.US)
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testSeeTranslationAnalytics() {
        viewModel.reviewObserver.onNext(reviewOtherLanguage)
        viewModel.onTranslateClick.onNext(Unit)
        OmnitureTestUtils.assertLinkTracked("Translate User Review", "App.Hotels.Reviews.SeeTranslation", mockAnalyticsProvider)
    }

    @Test
    fun testSeeOriginalAnalytics() {
        viewModel.reviewObserver.onNext(reviewOtherLanguage)
        viewModel.translatedReviewObserver.onNext(TranslatedReview(reviewNativeLanguage, true))
        viewModel.onTranslateClick.onNext(Unit)
        OmnitureTestUtils.assertLinkTracked("Translate User Review", "App.Hotels.Reviews.SeeOriginal", mockAnalyticsProvider)
    }
}
