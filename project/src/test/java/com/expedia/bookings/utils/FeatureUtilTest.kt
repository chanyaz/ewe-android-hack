package com.expedia.bookings.utils

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
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelMaterialForms)
        assertTrue(isHotelMaterialForms(context))
    }

    @Test
    fun testDisableHotelMaterialForms() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppHotelMaterialForms)
        assertFalse(isHotelMaterialForms(context))
    }

    @Test
    fun testEnableBrandColors() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppBrandColors)
        assertTrue(isBrandColorEnabled(context))
    }

    @Test
    fun testDisableBrandColors() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppBrandColors)
        assertFalse(isBrandColorEnabled(context))
    }
}
