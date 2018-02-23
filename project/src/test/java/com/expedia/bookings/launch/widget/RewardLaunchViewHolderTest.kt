package com.expedia.bookings.launch.widget

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.Shadows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = [MultiBrand.ORBITZ])
class RewardLaunchViewHolderTest {
    lateinit var sut: RewardLaunchViewHolder
    lateinit var vm: RewardLaunchViewModel
    lateinit var mockAnalyticsProvider: AnalyticsProvider
    val activity = Robolectric.buildActivity(Activity::class.java).create().get()

    @Before
    fun before() {
        createSystemUnderTest()
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        Mockito.`when`(mockAnalyticsProvider.getUrlWithVisitorData(Mockito.anyString())).thenAnswer { it.arguments[0] }
    }

    @Test
    fun testPerformClick_GoToWebView_WithCorrectIntentExtras() {
        val shadowActivity = Shadows.shadowOf(activity)
        sut.itemView.performClick()
        val startedIntent = shadowActivity.nextStartedActivity
        val extras = startedIntent.extras

        assertEquals(WebViewActivity::class.java.name, startedIntent.component.className)
        assertEquals("Orbitz", extras.getString("ARG_TITLE"))
        assertTrue(extras.getBoolean("ARG_HANDLE_BACK"))
        assertTrue(extras.getBoolean("ARG_HANDLE_RETRY_ON_ERROR"))
        assertEquals("https://www.orbitz.com/rewards/orbitzmobileapprewards", extras.getString("ARG_URL"))
    }

    private fun createSystemUnderTest() {
        val view = LayoutInflater.from(activity)
                .inflate(R.layout.reward_launch_card, null)
        sut = RewardLaunchViewHolder(view)
        vm = RewardLaunchViewModel()
        sut.bind(vm)
    }
}
