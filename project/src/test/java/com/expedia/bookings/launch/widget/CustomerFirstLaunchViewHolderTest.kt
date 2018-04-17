package com.expedia.bookings.launch.widget

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.mia.activity.CustomerFirstActivity
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.vm.launch.CustomerFirstLaunchHolderViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = [MultiBrand.TRAVELOCITY])
class CustomerFirstLaunchViewHolderTest {
    lateinit var sut: CustomerFirstLaunchViewHolder
    lateinit var vm: CustomerFirstLaunchHolderViewModel
    lateinit var mockAnalyticsProvider: AnalyticsProvider
    val activity = Robolectric.buildActivity(Activity::class.java).create().get()

    @Before
    fun before() {
        createSystemUnderTest()
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testPerformClick() {
        sut.itemView.performClick()
        OmnitureTracking.trackTapCustomerFirstGuaranteeLaunchTile()

        val expectedEvar = mapOf(28 to "App.LS.CFG")
        val expectedProp = mapOf(16 to "App.LS.CFG")
        OmnitureTestUtils.assertLinkTracked("App Landing", "App.LS.CFG", OmnitureMatchers.withEvars(expectedEvar), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked("App Landing", "App.LS.CFG", OmnitureMatchers.withProps(expectedProp), mockAnalyticsProvider)

        val expectedCustFirstGuaranteeEvar = mapOf(12 to "App.LS.CFG")
        OmnitureTestUtils.assertLinkTracked(OmnitureMatchers.withEvars(expectedCustFirstGuaranteeEvar), mockAnalyticsProvider)

        val actualIntent = Shadows.shadowOf(activity).nextStartedActivity
        assertEquals(CustomerFirstActivity::class.java.name, actualIntent.component.className)
    }

    private fun createSystemUnderTest() {
        val view = LayoutInflater.from(activity)
                .inflate(R.layout.customer_first_launch_card, null)
        sut = CustomerFirstLaunchViewHolder(view)
        vm = CustomerFirstLaunchHolderViewModel(activity.getString(R.string.customer_first_we_are_here_for_you))
        sut.bind(vm)
    }
}
