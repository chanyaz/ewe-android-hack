package com.expedia.bookings.presenter.flight

import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusTest
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.isShowFlightsCheckoutWebview
import com.mobiata.android.util.SettingUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightsWebCheckoutViewTest {

    private val context = RuntimeEnvironment.application

    @Test
    fun testToggleOnShowFlightsCheckoutWebview() {
        toggleShowFlightsCheckoutWebviewABTestAndFF(true)
        assertTrue(isShowFlightsCheckoutWebview(context))
    }

    @Test
    fun testToggleOffShowFlightsCheckoutWebview() {
        toggleShowFlightsCheckoutWebviewABTestAndFF(false)
        assertFalse(isShowFlightsCheckoutWebview(context))
    }

    private fun toggleShowFlightsCheckoutWebviewABTestAndFF(toggleOn: Boolean) {
        if (toggleOn) {
            AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppShowFlightsCheckoutWebview)
        }
        else {
            AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppShowFlightsCheckoutWebview)
        }
        SettingUtils.save(context, R.string.preference_show_flights_checkout_webview, toggleOn)
    }
}
