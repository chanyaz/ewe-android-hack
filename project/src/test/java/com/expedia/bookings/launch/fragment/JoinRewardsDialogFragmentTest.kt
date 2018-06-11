package com.expedia.bookings.launch.fragment

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import kotlinx.android.synthetic.main.fragment_join_rewards_dialog.*
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

    @Before
    fun setUp() {
        joinRewardsDialogFragment.show(activity.supportFragmentManager, null)
        activity.supportFragmentManager.executePendingTransactions()
    }

    @Test
    fun titleTextIsValid() {
        assertEquals(joinRewardsDialogFragment.titleTextView.text, activity.getString(R.string.join_rewards_member_title))
    }

    @Test
    fun testTermsTextIsValid() {
        assertEquals(joinRewardsDialogFragment.termsTextView.text.toString(), "By joining I accept the Terms.")
    }

    @Test
    fun testJoinNowReasonsAreValid() {
        assertEquals(joinRewardsDialogFragment.firstReasonTextView.text, activity.getString(R.string.first_reason_to_join))
        assertEquals(joinRewardsDialogFragment.secondReasonTextView.text, activity.getString(R.string.second_reason_to_join))
        assertEquals(joinRewardsDialogFragment.thirdReasonTextView.text, activity.getString(R.string.third_reason_to_join))
    }

    @Test
    fun testViewAreHiddenForProgressLoading() {
        joinRewardsDialogFragment.hideViewsForProgressLoading()

        assertEquals(joinRewardsDialogFragment.termsTextView.visibility, View.GONE)
        assertEquals(joinRewardsDialogFragment.firstCheckImageView.visibility, View.GONE)
        assertEquals(joinRewardsDialogFragment.firstReasonTextView.visibility, View.GONE)
        assertEquals(joinRewardsDialogFragment.secondCheckImageView.visibility, View.GONE)
        assertEquals(joinRewardsDialogFragment.secondReasonTextView.visibility, View.GONE)
        assertEquals(joinRewardsDialogFragment.thirdCheckImageView.visibility, View.GONE)
        assertEquals(joinRewardsDialogFragment.thirdReasonTextView.visibility, View.GONE)
        assertEquals(joinRewardsDialogFragment.primaryActionCardView.visibility, View.GONE)
        assertEquals(joinRewardsDialogFragment.secondaryActionButton.visibility, View.GONE)
        assertEquals(joinRewardsDialogFragment.confettiView.visibility, View.GONE)
    }

    @Test
    fun testDialogIsDismissed() {
        joinRewardsDialogFragment.onSuccessActions()
        assertEquals(joinRewardsDialogFragment.isVisible, false)
    }

    open class TestAppCompatActivity : AppCompatActivity(), JoinRewardsDialogFragment.UserHasSuccessfullyJoinedRewards {

        override fun onCreate(savedInstanceState: Bundle?) {
            setTheme(R.style.AppTheme_Base)
            super.onCreate(savedInstanceState)
        }

        override fun onJoinRewardsSuccess() {

        }
    }

    class TestDialogFragment : JoinRewardsDialogFragment() {
        override fun joinRewards() {
            // do nothing here
        }
    }
}
