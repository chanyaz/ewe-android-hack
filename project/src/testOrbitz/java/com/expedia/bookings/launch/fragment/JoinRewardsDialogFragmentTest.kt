package com.expedia.bookings.launch.fragment

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.launch.interfaces.UserHasSuccessfullyJoinedRewards
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import kotlinx.android.synthetic.orbitz.fragment_join_rewards_dialog.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = [MultiBrand.ORBITZ])
class JoinRewardsDialogFragmentTest {
    private val activity = Robolectric.buildActivity(TestAppCompatActivity::class.java).create().start().resume().get()
    private val joinRewardsDialogFragment = TestDialogFragment()
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setUp() {
        joinRewardsDialogFragment.show(activity.supportFragmentManager, null)
        activity.supportFragmentManager.executePendingTransactions()
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun titleTextIsValid() {
        assertEquals(activity.getString(R.string.join_rewards_member_title), joinRewardsDialogFragment.joinRewardsTitleTextView.text)
    }

    @Test
    fun testTermsTextIsValid() {
        assertEquals("By joining I accept the Terms.", joinRewardsDialogFragment.termsTextView.text.toString())
    }

    @Test
    fun testJoinNowReasonsAreValid() {
        assertEquals(activity.getString(R.string.first_reason_to_join), joinRewardsDialogFragment.firstReasonTextView.text)
        assertEquals(activity.getString(R.string.second_reason_to_join), joinRewardsDialogFragment.secondReasonTextView.text)
        assertEquals(activity.getString(R.string.third_reason_to_join), joinRewardsDialogFragment.thirdReasonTextView.text)
    }

    @Test
    fun testViewAreHiddenForProgressLoading() {
        joinRewardsDialogFragment.hideViewsForProgressLoading()

        assertEquals(View.GONE, joinRewardsDialogFragment.termsTextView.visibility)
        assertEquals(View.GONE, joinRewardsDialogFragment.firstCheckImageView.visibility)
        assertEquals(View.GONE, joinRewardsDialogFragment.firstReasonTextView.visibility)
        assertEquals(View.GONE, joinRewardsDialogFragment.secondCheckImageView.visibility)
        assertEquals(View.GONE, joinRewardsDialogFragment.secondReasonTextView.visibility)
        assertEquals(View.GONE, joinRewardsDialogFragment.thirdCheckImageView.visibility)
        assertEquals(View.GONE, joinRewardsDialogFragment.thirdReasonTextView.visibility)
        assertEquals(View.GONE, joinRewardsDialogFragment.primaryActionCardView.visibility)
        assertEquals(View.GONE, joinRewardsDialogFragment.secondaryActionButton.visibility)
        assertEquals(View.GONE, joinRewardsDialogFragment.confettiView.visibility)
    }

    @Test
    fun testDialogIsDismissed() {
        joinRewardsDialogFragment.onSuccessActions()
        assertEquals(false, joinRewardsDialogFragment.isVisible)
    }

    @Test
    fun testClickJoinIsTracked() {
        joinRewardsDialogFragment.primaryActionCardView.performClick()

        OmnitureTestUtils.assertLinkTracked("Rewards Registration", "App.Rewards.Orbitz.JoinNow", mockAnalyticsProvider)
    }

    @Test
    fun testClickMaybeLaterIsTracked() {
        joinRewardsDialogFragment.secondaryActionButton.performClick()

        OmnitureTestUtils.assertLinkTracked("Rewards Registration", "App.Rewards.Orbitz.Later", mockAnalyticsProvider)
    }

    @Test
    fun testClickBookTravelIsTracked() {
        joinRewardsDialogFragment.onSuccessActions()

        OmnitureTestUtils.assertLinkTracked("Rewards Registration", "App.Rewards.Orbitz.BookTravel", mockAnalyticsProvider)
    }

    open class TestAppCompatActivity : AppCompatActivity(), UserHasSuccessfullyJoinedRewards {

        override fun onCreate(savedInstanceState: Bundle?) {
            setTheme(R.style.AppTheme_Base)
            super.onCreate(savedInstanceState)
        }

        override fun onJoinRewardsSuccess() {
        }
    }

    class TestDialogFragment : JoinRewardsDialogFragment() {
        override fun joinRewards() {
        }
    }
}
