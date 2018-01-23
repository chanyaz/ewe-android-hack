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
import com.expedia.bookings.services.TestObserver
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
        val testReviewLink = TestObserver<String>()
        val testFeedbackLink = TestObserver<String>()

        vm.reviewLinkSubject.subscribe(testReviewLink)
        vm.feedbackLinkSubject.subscribe(testFeedbackLink)

        vm.reviewSubject.onNext(Unit)
        testReviewLink.assertValue("market://details?id=com.expedia.bookings")

        var shadowActivity = Shadows.shadowOf(context)
        var intent = shadowActivity.nextStartedActivity
        assertEquals("android.intent.action.VIEW", intent.action)
        assertEquals("market://details?id=com.expedia.bookings", intent.dataString)

        vm.feedbackSubject.onNext(Unit)
        testFeedbackLink.assertValue("expda://reviewFeedbackEmail")

        shadowActivity = Shadows.shadowOf(context)
        intent = shadowActivity.nextStartedActivity
        assertEquals("android.intent.action.VIEW", intent.action)
        assertEquals("expda://reviewFeedbackEmail", intent.dataString)
    }

    @Test
    fun testReviewSavedPrefs() {
        val testReviewLink = TestObserver<Unit>()
        vm.reviewSubject.subscribe(testReviewLink)

        assertSavePrefsNotStored()
        vm.reviewSubject.onNext(Unit)
        assertSavePrefsStored()
    }

    @Test
    fun testFeedbackSavedPrefs() {
        val testReviewLink = TestObserver<Unit>()
        vm.feedbackSubject.subscribe(testReviewLink)

        assertSavePrefsNotStored()
        vm.feedbackSubject.onNext(Unit)
        assertSavePrefsStored()
    }

    @Test
    fun testNoThanksSavedPrefs() {
        val testReviewLink = TestObserver<Unit>()
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
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.AIRASIAGO, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS, MultiBrand.CHEAPTICKETS, MultiBrand.WOTIF, MultiBrand.MRJET, MultiBrand.TRAVELOCITY))
    fun testReviewPromptOnlyShowsAgainAfterCleared() {
        SettingUtils.save(context, R.string.preference_user_has_booked_hotel_or_flight, true)

        assertEquals(true, UserReviewDialogViewModel.shouldShowReviewDialog(context))
        SettingUtils.save(context, R.string.preference_user_has_seen_review_prompt, true)
        assertEquals(false, UserReviewDialogViewModel.shouldShowReviewDialog(context))
        SettingUtils.save(context, R.string.preference_user_has_seen_review_prompt, false)
        assertEquals(true, UserReviewDialogViewModel.shouldShowReviewDialog(context))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.AIRASIAGO, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS, MultiBrand.CHEAPTICKETS, MultiBrand.WOTIF, MultiBrand.MRJET, MultiBrand.TRAVELOCITY))
    fun testReviewPromptShowsAfterThreeMonths() {
        SettingUtils.save(context, R.string.preference_user_has_booked_hotel_or_flight, true)

        assertEquals(true, UserReviewDialogViewModel.shouldShowReviewDialog(context))
        SettingUtils.save(context, R.string.preference_user_has_seen_review_prompt, true)
        assertEquals(false, UserReviewDialogViewModel.shouldShowReviewDialog(context))
        SettingUtils.save(context, R.string.preference_date_last_review_prompt_shown, DateTime.now().minusMonths(3).minusDays(1).millis)
        assertEquals(true, UserReviewDialogViewModel.shouldShowReviewDialog(context))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.AIRASIAGO, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS, MultiBrand.CHEAPTICKETS, MultiBrand.WOTIF, MultiBrand.MRJET, MultiBrand.TRAVELOCITY))
    fun testReviewPromptDoesNotShowBeforeThreeMonths() {
        SettingUtils.save(context, R.string.preference_user_has_booked_hotel_or_flight, true)

        assertEquals(true, UserReviewDialogViewModel.shouldShowReviewDialog(context))
        SettingUtils.save(context, R.string.preference_user_has_seen_review_prompt, true)
        assertEquals(false, UserReviewDialogViewModel.shouldShowReviewDialog(context))
        SettingUtils.save(context, R.string.preference_date_last_review_prompt_shown, DateTime.now().minusMonths(2).millis)
        assertEquals(false, UserReviewDialogViewModel.shouldShowReviewDialog(context))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.AIRASIAGO, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS, MultiBrand.CHEAPTICKETS, MultiBrand.WOTIF, MultiBrand.MRJET, MultiBrand.TRAVELOCITY))
    fun testReviewPromptDoesNotShowThreeMonthsInFuture() {
        SettingUtils.save(context, R.string.preference_user_has_booked_hotel_or_flight, true)

        assertEquals(true, UserReviewDialogViewModel.shouldShowReviewDialog(context))
        SettingUtils.save(context, R.string.preference_user_has_seen_review_prompt, true)
        assertEquals(false, UserReviewDialogViewModel.shouldShowReviewDialog(context))
        SettingUtils.save(context, R.string.preference_date_last_review_prompt_shown, DateTime.now().plusMonths(3).plusDays(1).millis)
        assertEquals(false, UserReviewDialogViewModel.shouldShowReviewDialog(context))
    }
}
