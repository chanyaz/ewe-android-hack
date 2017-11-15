package com.expedia.bookings.utils

import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// TODO: Move all FeatureUtil tests to this class (make an improvement card)
@RunWith(RobolectricRunner::class)
class FeatureUtilTest {

    private val context = RuntimeEnvironment.application

    @Test
    fun testEnableHotelMaterialForms() {
        AbacusTestUtils.bucketTestAndEnableFeature(context, AbacusUtils.EBAndroidAppHotelMaterialForms, R.string.preference_enable_hotel_material_forms)
        assertTrue(isHotelMaterialForms(context))
    }

    @Test
    fun testDisableHotelMaterialForms() {
        AbacusTestUtils.unbucketTestAndDisableFeature(context, AbacusUtils.EBAndroidAppHotelMaterialForms, R.string.preference_enable_hotel_material_forms)
        assertFalse(isHotelMaterialForms(context))
    }

    @Test
    fun testEnableMIDCheckout() {
        AbacusTestUtils.bucketTestAndEnableFeature(context, AbacusUtils.EBAndroidAppMIDCheckout, R.string.preference_enable_mid_checkout)
        assertTrue(isMIDCheckoutEnabled(context))
    }

    @Test
    fun testDisableMIDCheckout() {
        AbacusTestUtils.unbucketTestAndDisableFeature(context, AbacusUtils.EBAndroidAppMIDCheckout, R.string.preference_enable_mid_checkout)
        assertFalse(isMIDCheckoutEnabled(context))
    }
}
