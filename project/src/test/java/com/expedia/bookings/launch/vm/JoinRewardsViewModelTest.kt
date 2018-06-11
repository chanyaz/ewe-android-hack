package com.expedia.bookings.launch.vm

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.expedia.account.data.JoinRewardsResponse
import com.expedia.bookings.R
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import io.reactivex.subjects.PublishSubject
import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.ParticleSystem
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = [MultiBrand.ORBITZ])
class JoinRewardsViewModelTest {

    private lateinit var context: Context
    private lateinit var joinRewardsViewModel: MockJoinRewardsViewModel

    @Before
    fun setUp() {
        context = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        joinRewardsViewModel = MockJoinRewardsViewModel(context)
    }

    @Test
    fun testCONFETTI_SIZE_IN_DP() {
        assertEquals(joinRewardsViewModel.CONFETTI_SIZE_IN_DP, 6)
    }

    @Test
    fun test_CONFETTI_BURST_AMOUNT() {
        assertEquals(joinRewardsViewModel.CONFETTI_BURST_AMOUNT, 70)

    }

    @Test
    fun test_ShouldAllowUsersToCancelOnTouch() {
        assertEquals(joinRewardsViewModel.shouldAllowUsersToCancelOnTouch, false)
    }

    @Test
    fun test_JoinRewardsIsNotComplete() {
        assertEquals(joinRewardsViewModel.joinRewardsIsComplete, false)
    }

    @Test
    fun test_JoinRewardsIsComplete() {
        joinRewardsViewModel.joinRewards()
        joinRewardsViewModel.getJoinRewardsSubjectForTesting().onComplete()
        assertEquals(joinRewardsViewModel.joinRewardsIsComplete, true)
    }

    @Test
    fun test_JoinRewardsIsCompleteOnNextWithSuccess() {
        joinRewardsViewModel.joinRewards()
        joinRewardsViewModel.getJoinRewardsSubjectForTesting().onNext(getMockJoinRewardsResponse(true))
        assertEquals(joinRewardsViewModel.joinRewardsIsComplete, true)
    }

    @Test
    fun test_JoinRewardsIsCompleteOnNextWithFailure() {
        joinRewardsViewModel.joinRewards()
        joinRewardsViewModel.getJoinRewardsSubjectForTesting().onNext(getMockJoinRewardsResponse(false))
        assertEquals(joinRewardsViewModel.joinRewardsIsComplete, true)
    }

    @Test
    fun test_JoinRewardsWasSuccessful() {
        joinRewardsViewModel.joinRewards()
        joinRewardsViewModel.getJoinRewardsSubjectForTesting().onNext(getMockJoinRewardsResponse(true))
        assertEquals(joinRewardsViewModel.joinRewardsWasSuccessful, true)
    }

    @Test
    fun test_JoinRewardsWasNotSuccessful() {
        joinRewardsViewModel.joinRewards()
        joinRewardsViewModel.getJoinRewardsSubjectForTesting().onNext(getMockJoinRewardsResponse(false))
        assertEquals(joinRewardsViewModel.joinRewardsWasSuccessful, false)
    }

    @Test
    fun test_JoinRewardsOnError() {
        joinRewardsViewModel.joinRewards()
        joinRewardsViewModel.getJoinRewardsSubjectForTesting().onError(NullPointerException("mock exception"))

        assertEquals(joinRewardsViewModel.joinRewardsWasSuccessful, false)
        assertEquals(joinRewardsViewModel.joinRewardsIsComplete, true)
    }

    @Test
    fun test_SuccessTextIsValid() {
        val successText = joinRewardsViewModel.successText
        assertEquals(successText.modalTitle, context.getString(R.string.your_in_title))
        assertEquals(successText.primaryActionText, context.getString(R.string.book_travel))
        assertEquals(successText.secondaryActionText, context.getString(R.string.done))
    }

    @Test
    fun test_FailureTextIsValid() {
        val failureText = joinRewardsViewModel.failureText
        assertEquals(failureText.modalTitle, context.getString(R.string.error_with_request))
        assertEquals(failureText.primaryActionText, context.getString(R.string.try_again))
        assertEquals(failureText.secondaryActionText, context.getString(R.string.cancel))
    }

    @Test
    fun test_TermsTextIsValid() {
        assertEquals(joinRewardsViewModel.termsText.toString(), "By joining I accept the Terms.")
    }

    @Test
    fun test_verifyConfettiViewIsConfigured() {
        val konfettiView = KonfettiView(context)
        val particleSystem = joinRewardsViewModel.configureConfettiView(ParticleSystem(konfettiView))

        particleSystem.addColors(joinRewardsViewModel.getConfettiColors())
            .setDirection(0.0, 359.0)
            .setSpeed(1f, 10f)
            .setFadeOutEnabled(true)
            .setTimeToLive(2000L)
            .addShapes(Shape.RECT)
            .addSizes(Size(joinRewardsViewModel.CONFETTI_SIZE_IN_DP))
    }

    @Test
    fun test_verifyConfettiColors() {
        val colors = joinRewardsViewModel.getConfettiColors()
        assertTrue(colors.contains(ContextCompat.getColor(context, R.color.confetti_color_one)))
        assertTrue(colors.contains(ContextCompat.getColor(context, R.color.confetti_color_two)))
        assertTrue(colors.contains(ContextCompat.getColor(context, R.color.confetti_color_three)))
        assertTrue(colors.contains(ContextCompat.getColor(context, R.color.confetti_color_four)))
        assertTrue(colors.contains(ContextCompat.getColor(context, R.color.confetti_color_five)))
        assertTrue(colors.contains(ContextCompat.getColor(context, R.color.confetti_color_six)))
    }


    private fun getMockJoinRewardsResponse(loyaltyMembershipActive: Boolean): JoinRewardsResponse {
        val joinRewardsResponse = JoinRewardsResponse()
        joinRewardsResponse.loyaltyMembershipActive = loyaltyMembershipActive

        return joinRewardsResponse
    }

    class MockJoinRewardsViewModel(context: Context) : JoinRewardsViewModel(context) {

        override fun joinRewards() {
            setupJoinRewardsSubject()
        }

        fun getJoinRewardsSubjectForTesting(): PublishSubject<JoinRewardsResponse> {
            return joinRewardsSubject
        }
    }
}
