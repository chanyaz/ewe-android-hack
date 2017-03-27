package com.expedia.vm

import com.expedia.bookings.R
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.android.util.SettingUtils
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class ReviewRatingDialogViewModelTest {

    val context = RuntimeEnvironment.application
    lateinit var vm: UserReviewDialogViewModel

    @Before
    fun before() {
        vm = UserReviewDialogViewModel(context)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testLinksAreCorrect() {
        val testReviewLink = TestSubscriber<String>()
        val testFeedbackLink = TestSubscriber<String>()

        vm.reviewLinkSubject.subscribe(testReviewLink)
        vm.feedbackLinkSubject.subscribe(testFeedbackLink)

        vm.reviewSubject.onNext(Unit)
        testReviewLink.assertValue("market://details?id=com.expedia.bookings")

        var shadowActivity = Shadows.shadowOf(context)
        var intent = shadowActivity.nextStartedActivity
        assertEquals("android.intent.action.VIEW", intent.action)
        assertEquals("market://details?id=com.expedia.bookings", intent.dataString)

        vm.feedbackSubject.onNext(Unit)
        testFeedbackLink.assertValue("expda://reviewSupportEmail")

        shadowActivity = Shadows.shadowOf(context)
        intent = shadowActivity.nextStartedActivity
        assertEquals("android.intent.action.VIEW", intent.action)
        assertEquals("expda://reviewSupportEmail", intent.dataString)

    }

    @Test
    fun testReviewSavedPrefs() {
        val testReviewLink = TestSubscriber<Unit>()
        vm.reviewSubject.subscribe(testReviewLink)

        assertSavePrefsNotStored()
        vm.reviewSubject.onNext(Unit)
        assertSavePrefsStored()
    }

    @Test
    fun testFeedbackSavedPrefs() {
        val testReviewLink = TestSubscriber<Unit>()
        vm.feedbackSubject.subscribe(testReviewLink)

        assertSavePrefsNotStored()
        vm.feedbackSubject.onNext(Unit)
        assertSavePrefsStored()
    }

    @Test
    fun testNoThanksSavedPrefs() {
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

    @Test
    fun testReviewPromptOnlyShowsAgainAfterCleared() {
        SettingUtils.save(context, R.string.preference_user_has_booked_hotel_or_flight, true)

        assertEquals(true, UserReviewDialogViewModel.shouldShowReviewDialog(context))
        SettingUtils.save(context, R.string.preference_user_has_seen_review_prompt, true)
        assertEquals(false, UserReviewDialogViewModel.shouldShowReviewDialog(context))
        SettingUtils.save(context, R.string.preference_user_has_seen_review_prompt, false)
        assertEquals(true, UserReviewDialogViewModel.shouldShowReviewDialog(context))
    }

    @Test
    fun testReviewPromptShowsAfterThreeMonths() {
        SettingUtils.save(context, R.string.preference_user_has_booked_hotel_or_flight, true)

        assertEquals(true, UserReviewDialogViewModel.shouldShowReviewDialog(context))
        SettingUtils.save(context, R.string.preference_user_has_seen_review_prompt, true)
        assertEquals(false, UserReviewDialogViewModel.shouldShowReviewDialog(context))
        SettingUtils.save(context, R.string.preference_date_last_review_prompt_shown, DateTime.now().minusMonths(3).minusDays(1).millis)
        assertEquals(true, UserReviewDialogViewModel.shouldShowReviewDialog(context))
    }

    @Test
    fun testReviewPromptDoesNotShowBeforeThreeMonths() {
        SettingUtils.save(context, R.string.preference_user_has_booked_hotel_or_flight, true)

        assertEquals(true, UserReviewDialogViewModel.shouldShowReviewDialog(context))
        SettingUtils.save(context, R.string.preference_user_has_seen_review_prompt, true)
        assertEquals(false, UserReviewDialogViewModel.shouldShowReviewDialog(context))
        SettingUtils.save(context, R.string.preference_date_last_review_prompt_shown, DateTime.now().minusMonths(2).millis)
        assertEquals(false, UserReviewDialogViewModel.shouldShowReviewDialog(context))
    }

    @Test
    fun testReviewPromptDoesNotShowThreeMonthsInFuture() {
        SettingUtils.save(context, R.string.preference_user_has_booked_hotel_or_flight, true)

        assertEquals(true, UserReviewDialogViewModel.shouldShowReviewDialog(context))
        SettingUtils.save(context, R.string.preference_user_has_seen_review_prompt, true)
        assertEquals(false, UserReviewDialogViewModel.shouldShowReviewDialog(context))
        SettingUtils.save(context, R.string.preference_date_last_review_prompt_shown, DateTime.now().plusMonths(3).plusDays(1).millis)
        assertEquals(false, UserReviewDialogViewModel.shouldShowReviewDialog(context))
    }
}