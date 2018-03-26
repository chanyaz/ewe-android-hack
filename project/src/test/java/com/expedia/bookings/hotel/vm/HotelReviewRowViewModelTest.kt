package com.expedia.bookings.hotel.vm

import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.vm.HotelReviewRowViewModel
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.Locale
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelReviewRowViewModelTest {
    private val context = RuntimeEnvironment.application
    private val viewModel = HotelReviewRowViewModel(context)
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

    private val reviewBadLocale = HotelReviewsResponse.Review().apply {
        reviewId = "1"
        title = "Title"
        reviewText = "Content"
        contentLocale = "e"
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
    }

    @Test
    fun testShowTranslationButton() {
        AbacusTestUtils.bucketTests(AbacusUtils.HoteUGCTranslatations)
        val textObserver = TestObserver<String>()
        viewModel.translateButtonTextObservable.subscribe(textObserver)
        viewModel.reviewObserver.onNext(reviewOtherLanguage)
        textObserver.assertValue(context.getString(R.string.user_review_see_translation))
    }

    @Test
    fun testDoTranslation() {
        AbacusTestUtils.bucketTests(AbacusUtils.HoteUGCTranslatations)
        val buttonTextObserver = TestObserver<String>()
        val titleTextObserver = TestObserver<String>()
        val reviewTextObserver = TestObserver<String>()
        val reviewIdObserver = TestObserver<String>()

        viewModel.translateButtonTextObservable.subscribe(buttonTextObserver)
        viewModel.titleTextObservable.subscribe(titleTextObserver)
        viewModel.reviewBodyObservable.subscribe(reviewTextObserver)
        viewModel.translateReviewIdObservable.subscribe(reviewIdObserver)

        viewModel.reviewObserver.onNext(reviewOtherLanguage)
        viewModel.onTranslateClick.onNext(Unit)
        viewModel.reviewObserver.onNext(reviewNativeLanguage)

        buttonTextObserver.assertValues(context.getString(R.string.user_review_see_translation),
                context.getString(R.string.user_review_translation_loading),
                "")
        titleTextObserver.assertValues("Title in a different language", "Title")
        reviewTextObserver.assertValues("Content in a different language", "Content")
        reviewIdObserver.assertValue("1")
    }

    @Test
    fun testDoNotShowTranslationButton() {
        AbacusTestUtils.bucketTests(AbacusUtils.HoteUGCTranslatations)
        val textObserver = TestObserver<String>()
        viewModel.translateButtonTextObservable.subscribe(textObserver)
        viewModel.reviewObserver.onNext(reviewNativeLanguage)
        textObserver.assertValue("")
    }

    @Test
    fun testBadLocale() {
        AbacusTestUtils.bucketTests(AbacusUtils.HoteUGCTranslatations)
        val textObserver = TestObserver<String>()
        viewModel.translateButtonTextObservable.subscribe(textObserver)
        viewModel.reviewObserver.onNext(reviewBadLocale)
        textObserver.assertValue("")
    }

    @Test
    fun testNotBucketedForTranslations() {
        AbacusTestUtils.unbucketTests(AbacusUtils.HoteUGCTranslatations)
        val textObserver = TestObserver<String>()
        viewModel.translateButtonTextObservable.subscribe(textObserver)
        viewModel.reviewObserver.onNext(reviewNativeLanguage)
        textObserver.assertValue("")
    }

    @Test
    fun testInDifferentLanguage() {
        AbacusTestUtils.bucketTests(AbacusUtils.HoteUGCTranslatations)
        assertFalse(viewModel.reviewInDifferentLanguage())

        viewModel.reviewObserver.onNext(reviewOtherLanguage)
        assertTrue(viewModel.reviewInDifferentLanguage())

        viewModel.reviewObserver.onNext(reviewNativeLanguage)
        assertFalse(viewModel.reviewInDifferentLanguage())

        viewModel.reviewObserver.onNext(reviewBadLocale)
        assertFalse(viewModel.reviewInDifferentLanguage())

        viewModel.reviewObserver.onNext(reviewOtherLanguage)
        viewModel.translatedReviewObserver.onNext(reviewNativeLanguage)
        assertFalse(viewModel.reviewInDifferentLanguage())
    }

    @Test
    fun testHasText() {
        val review = HotelReviewsResponse.Review().apply {
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

        viewModel.reviewObserver.onNext(review)
        assertTrue(viewModel.reviewHasText())

        review.title = ""
        assertTrue(viewModel.reviewHasText())

        review.reviewText = ""
        assertFalse(viewModel.reviewHasText())

        review.title = "Title"
        assertTrue(viewModel.reviewHasText())
    }
}
