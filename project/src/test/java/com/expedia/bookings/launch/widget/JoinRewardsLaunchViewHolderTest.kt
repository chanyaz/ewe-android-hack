package com.expedia.bookings.launch.widget

import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.launch.fragment.JoinRewardsDialogFragment
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = [MultiBrand.ORBITZ])
class JoinRewardsLaunchViewHolderTest {
    lateinit var joinRewardsViewHolder: JoinRewardsLaunchViewHolder
    lateinit var mockAnalyticsProvider: AnalyticsProvider
    val activity = Robolectric.buildActivity(TestAppCompatActivity::class.java).create().get()

    @Before
    fun before() {
        createJoinRewardsViewHolder()
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        Mockito.`when`(mockAnalyticsProvider.getUrlWithVisitorData(Mockito.anyString())).thenAnswer { it.arguments[0] }
    }

    @Test
    fun testPerformClickIsTrackedInOmniture() {
        joinRewardsViewHolder.join_button.performClick()
        OmnitureTestUtils.assertLinkTracked("App Landing", "App.LS.JoinRewards",
                OmnitureMatchers.withEvars(mapOf(28 to "App.LS.JoinRewards")), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked("App Landing", "App.LS.JoinRewards",
                OmnitureMatchers.withProps(mapOf(16 to "App.LS.JoinRewards")), mockAnalyticsProvider)
    }

    private fun createJoinRewardsViewHolder() {
        val view = LayoutInflater.from(activity).inflate(R.layout.join_rewards_launch_card, null)
        joinRewardsViewHolder = JoinRewardsLaunchViewHolder(view, activity)
    }

    class TestAppCompatActivity : AppCompatActivity(), JoinRewardsDialogFragment.UserHasSuccessfullyJoinedRewards {
        override fun onJoinRewardsSuccess() {

        }
    }
}
