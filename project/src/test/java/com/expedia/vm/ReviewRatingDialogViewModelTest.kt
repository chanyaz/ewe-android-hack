package com.expedia.vm

import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.android.util.SettingUtils
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class ReviewRatingDialogViewModelTest {

    val context = RuntimeEnvironment.application
    lateinit var vm: UserReviewDialogViewModel

    @Test
    fun testLinksAreCorrect() {
        vm = UserReviewDialogViewModel(context)

        val testReviewLink = TestSubscriber<String>()
        val testFeedbackLink = TestSubscriber<String>()

        vm.reviewLinkSubject.subscribe(testReviewLink)
        vm.feedbackLinkSubject.subscribe(testFeedbackLink)

        vm.reviewSubject.onNext(Unit)
        vm.feedbackSubject.onNext(Unit)

        testReviewLink.assertValue("market://details?id=com.expedia.bookings")
        testFeedbackLink.assertValue("expda://supportEmail")
    }

    @Test
    fun testReviewSavedPrefs() {
        vm = UserReviewDialogViewModel(context)

        val testReviewLink = TestSubscriber<Unit>()
        vm.reviewSubject.subscribe(testReviewLink)

        assertSavePrefsNotStored()
        vm.reviewSubject.onNext(Unit)
        assertSavePrefsStored()
    }

    @Test
    fun testFeedbackSavedPrefs() {
        vm = UserReviewDialogViewModel(context)

        val testReviewLink = TestSubscriber<Unit>()
        vm.feedbackSubject.subscribe(testReviewLink)

        assertSavePrefsNotStored()
        vm.feedbackSubject.onNext(Unit)
        assertSavePrefsStored()
    }

    @Test
    fun testNoThanksSavedPrefs() {
        vm = UserReviewDialogViewModel(context)

        val testReviewLink = TestSubscriber<Unit>()
        vm.noSubject.subscribe(testReviewLink)

        assertSavePrefsNotStored()
        vm.noSubject.onNext(Unit)
        assertSavePrefsStored()
    }

    private fun assertSavePrefsNotStored() {
        val savedDate = SettingUtils.get(context, R.string.preference_date_last_review_prompt_shown, 0)
        assertEquals(0, savedDate)
        val hasShownUserReview = SettingUtils.get(context, R.string.preference_user_has_seen_review_prompt, false)
        assertFalse(hasShownUserReview)
    }

    private fun assertSavePrefsStored() {
        val savedDate = DateTime(SettingUtils.get(context, R.string.preference_date_last_review_prompt_shown, DateTime.now().millis))
        assertTrue(savedDate.toLocalDate().equals(LocalDate()))
        val hasShownUserReview = SettingUtils.get(context, R.string.preference_user_has_seen_review_prompt, false)
        assertTrue(hasShownUserReview)
    }
}