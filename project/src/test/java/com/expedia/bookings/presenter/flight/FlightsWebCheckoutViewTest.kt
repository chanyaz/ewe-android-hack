package com.expedia.bookings.presenter.flight

import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.isShowFlightsNativeRateDetailsWebviewCheckoutEnabled
import com.expedia.bookings.utils.isShowFlightsCheckoutWebview
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
        setUpFlightsWebCheckoutViewTest(true)
        assertTrue(isShowFlightsCheckoutWebview(context))
    }

    @Test
    fun testToggleOffShowFlightsCheckoutWebview() {
        setUpFlightsWebCheckoutViewTest(false)
        assertFalse(isShowFlightsCheckoutWebview(context))
    }

    @Test
    fun testToggleOnNativeOverviewWebviewCheckout() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidFlightsNativeRateDetailsWebviewCheckout)
        assertTrue(isShowFlightsNativeRateDetailsWebviewCheckoutEnabled(context))
    }

    @Test
    fun testToggleOffNativeOverviewwWebviewCheckout() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidFlightsNativeRateDetailsWebviewCheckout, AbacusVariant.CONTROL.value)
        assertFalse(isShowFlightsNativeRateDetailsWebviewCheckoutEnabled(context))
    }

    private fun setUpFlightsWebCheckoutViewTest(bucketed: Boolean) {
        if (bucketed) {
            AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppShowFlightsCheckoutWebview)
        } else {
            AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppShowFlightsCheckoutWebview, AbacusVariant.CONTROL.value)
        }
    }
}
