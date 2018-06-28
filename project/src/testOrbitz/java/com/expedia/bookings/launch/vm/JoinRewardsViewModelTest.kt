package com.expedia.bookings.launch.vm

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.expedia.account.AccountService
import com.expedia.account.data.JoinRewardsResponse
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers.Companion.withEventsString
import com.expedia.bookings.test.OmnitureMatchers.Companion.withProps
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.ParticleSystem
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = [MultiBrand.ORBITZ])
class JoinRewardsViewModelTest {

    private lateinit var context: Context
    private lateinit var joinRewardsViewModel: JoinRewardsViewModel
    private lateinit var mockAccountService: AccountService
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setUp() {
        context = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        mockAccountService = Mockito.mock(AccountService::class.java)
        joinRewardsViewModel = JoinRewardsViewModel(context, mockAccountService)
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testCONFETTI_SIZE_IN_DP() {
        assertEquals(6, joinRewardsViewModel.CONFETTI_SIZE_IN_DP)
    }

    @Test
    fun test_CONFETTI_BURST_AMOUNT() {
        assertEquals(70, joinRewardsViewModel.CONFETTI_BURST_AMOUNT)
    }

    @Test
    fun test_ShouldAllowUsersToCancelOnTouch() {
        assertEquals(false, joinRewardsViewModel.shouldAllowUsersToCancelOnTouch)
    }

    @Test
    fun test_JoinRewardsIsNotComplete() {
        assertEquals(false, joinRewardsViewModel.joinRewardsIsComplete)
    }

    @Test
    fun test_JoinRewardsIsComplete() {
        setupJoinRewardsServiceCall(true)
        joinRewardsViewModel.joinRewards()
        assertEquals(true, joinRewardsViewModel.joinRewardsIsComplete)
    }

    @Test
    fun test_JoinRewardsWasSuccessful() {
        setupJoinRewardsServiceCall(true)
        joinRewardsViewModel.joinRewards()

        assertEquals(true, joinRewardsViewModel.joinRewardsWasSuccessful)
    }

    @Test
    fun test_JoinRewardsWasNotSuccessful() {
        setupJoinRewardsServiceCall(false)
        joinRewardsViewModel.joinRewards()

        assertEquals(false, joinRewardsViewModel.joinRewardsWasSuccessful)
    }

    @Test
    fun test_SuccessTextIsValid() {
        val successText = joinRewardsViewModel.successText
        assertEquals(context.getString(R.string.your_in_title), successText.modalTitle)
        assertEquals(context.getString(R.string.book_travel), successText.primaryActionText)
        assertEquals(context.getString(R.string.done), successText.secondaryActionText)
    }

    @Test
    fun test_FailureTextIsValid() {
        val failureText = joinRewardsViewModel.failureText
        assertEquals(context.getString(R.string.error_with_request), failureText.modalTitle)
        assertEquals(context.getString(R.string.try_again), failureText.primaryActionText)
        assertEquals(context.getString(R.string.cancel), failureText.secondaryActionText)
    }

    @Test
    fun test_TermsTextIsValid() {
        assertEquals("By joining I accept the Terms.", joinRewardsViewModel.termsText.toString())
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

    @Test
    fun test_verifyConfettiColorListSizeIsValid() {
        assertTrue(joinRewardsViewModel.getConfettiColors().size == 6)
    }

    @Test
    fun test_JoinRewardsSuccessIsTracked() {
        setupJoinRewardsServiceCall(true)
        joinRewardsViewModel.joinRewards()

        OmnitureTestUtils.assertLinkTracked("Rewards Registration", "App.Rewards.Orbitz.Success",
                withEventsString("event61"), mockAnalyticsProvider)
    }

    @Test
    fun test_JoinRewardsErrorIsTracked() {
        setupJoinRewardsServiceCall(false)
        joinRewardsViewModel.joinRewards()

        OmnitureTestUtils.assertLinkTracked("Rewards Registration", "App.Rewards.Orbitz.Error",
                withProps(mapOf(36 to "App:OrbitzRewards: Bad Response")), mockAnalyticsProvider)
    }

    private fun setupJoinRewardsServiceCall(isSuccess: Boolean) {
        val response = JoinRewardsResponse()
        if (isSuccess) {
            response.loyaltyMembershipActive = true
        } else {
            val mobileError = JoinRewardsResponse.MobileError()
            val mobileErrorInfo = JoinRewardsResponse.ErrorInfo()
            mobileErrorInfo.summary = "Bad Response"
            mobileError.errorInfo = mobileErrorInfo
            response.errors = listOf(mobileError)
        }

        Mockito.`when`(mockAccountService.joinRewards())
                .thenReturn(Observable.just(response)
                        .observeOn(Schedulers.trampoline())
                        .subscribeOn(Schedulers.trampoline()))
    }
}
