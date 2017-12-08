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
    fun testEnableBrandColors() {
        AbacusTestUtils.bucketTestAndEnableFeature(context, AbacusUtils.EBAndroidAppBrandColors, R.string.preference_enable_launch_screen_brand_colors)
        assertTrue(isBrandColorEnabled(context))
    }

    @Test
    fun testDisableBrandColors() {
        AbacusTestUtils.unbucketTestAndDisableFeature(context, AbacusUtils.EBAndroidAppBrandColors, R.string.preference_enable_launch_screen_brand_colors)
        assertFalse(isBrandColorEnabled(context))
    }
}
