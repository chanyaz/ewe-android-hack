package com.expedia.bookings.presenter.flight

import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
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

    private fun setUpFlightsWebCheckoutViewTest(bucketed: Boolean) {
        if (bucketed) {
            AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppShowFlightsCheckoutWebview)
        } else {
            AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppShowFlightsCheckoutWebview)
        }
    }
}
