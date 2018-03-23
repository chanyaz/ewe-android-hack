package com.expedia.bookings.featureconfig

import android.app.Application
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.ABTest
import com.expedia.bookings.data.abacus.AbacusResponse
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class AbacusFeatureConfigManagerTest {
    val context: Application = RuntimeEnvironment.application

    @Before
    fun before() {
        val testList = listOf("12345")
        SatelliteFeatureConfigManager.cacheFeatureConfig(context, testList)
        Db.sharedInstance.abacusResponse = AbacusResponse()
    }

    @Test
    fun testSatelliteManagedEnabledABTest() {
        val abTest = ABTest(12345, true)
        AbacusTestUtils.updateABTest(abTest, AbacusVariant.BUCKETED.value)
        assertTrue(AbacusFeatureConfigManager.isBucketedForTest(context, abTest))
    }

    @Test
    fun testSatelliteManagedDisabledWithOverrideABTest() {
        val abTest = ABTest(99999, true)
        AbacusTestUtils.updateABTest(abTest, AbacusVariant.BUCKETED.value)
        updateTestOverride(abTest.key)
        assertTrue(AbacusFeatureConfigManager.isBucketedForTest(context, abTest))
    }

    @Test
    fun testNonSatelliteABTestIsBucketed() {
        val abTest = ABTest(12345)
        AbacusTestUtils.updateABTest(abTest, AbacusVariant.BUCKETED.value)
        assertTrue(AbacusFeatureConfigManager.isBucketedForTest(context, abTest))
    }

    @Test
    fun testNonSatelliteABTestInControl() {
        val abTest = ABTest(12345)
        AbacusTestUtils.updateABTest(abTest, AbacusVariant.CONTROL.value)
        assertFalse(AbacusFeatureConfigManager.isBucketedForTest(context, abTest))
    }

    @Test
    fun testShouldTrackTestIfNotRemote() {
        val abTest = ABTest(12345)
        assertTrue(AbacusFeatureConfigManager.shouldTrackTest(context, abTest))
    }

    @Test
    fun testShouldNotTrackTestIfRemoteAndDisabled() {
        val abTest = ABTest(99999, true)
        assertFalse(AbacusFeatureConfigManager.shouldTrackTest(context, abTest))
    }

    @Test
    fun testInVariantTrue() {
        val abTest = ABTest(99999)
        AbacusTestUtils.updateABTest(abTest, AbacusVariant.BUCKETED.value)
        assertTrue(AbacusFeatureConfigManager.isBucketedInAnyVariant(context, abTest))
        AbacusTestUtils.updateABTest(abTest, AbacusVariant.ONE.value)
        assertTrue(AbacusFeatureConfigManager.isBucketedInAnyVariant(context, abTest))
        AbacusTestUtils.updateABTest(abTest, AbacusVariant.TWO.value)
        assertTrue(AbacusFeatureConfigManager.isBucketedInAnyVariant(context, abTest))
        AbacusTestUtils.updateABTest(abTest, AbacusVariant.THREE.value)
        assertTrue(AbacusFeatureConfigManager.isBucketedInAnyVariant(context, abTest))
    }

    @Test
    fun testInVariantFalse() {
        val abTest = ABTest(99999)
        AbacusTestUtils.updateABTest(abTest, AbacusVariant.CONTROL.value)
        assertFalse(AbacusFeatureConfigManager.isBucketedInAnyVariant(context, abTest))
        AbacusTestUtils.updateABTest(abTest, AbacusVariant.NO_BUCKET.value)
        assertFalse(AbacusFeatureConfigManager.isBucketedInAnyVariant(context, abTest))
        AbacusTestUtils.updateABTest(abTest, AbacusVariant.DEBUG.value)
        assertFalse(AbacusFeatureConfigManager.isBucketedInAnyVariant(context, abTest))
    }

    @Test
    fun testIsMIDUndeterminedForTestTrue() {
        val abTest = ABTest(12345)
        AbacusTestUtils.updateABTest(abTest, AbacusVariant.NO_BUCKET.value)
        assertTrue(AbacusFeatureConfigManager.isMIDABTestUndetermined(abTest))
    }

    @Test
    fun testIsMIDUndeterminedForTestFalse() {
        val abTest = ABTest(12345)
        AbacusTestUtils.updateABTest(abTest, AbacusVariant.BUCKETED.value)
        assertFalse(AbacusFeatureConfigManager.isMIDABTestUndetermined(abTest))

        AbacusTestUtils.updateABTest(abTest, AbacusVariant.CONTROL.value)
        assertFalse(AbacusFeatureConfigManager.isMIDABTestUndetermined(abTest))
    }

    @Test
    fun testShouldTrackIfRemoteAndEnabled() {
        val abTest = ABTest(12345, true)
        assertTrue(AbacusFeatureConfigManager.shouldTrackTest(context, abTest))
    }

    @Test
    fun testShouldTrackIfRemoteDisabledWithOverride() {
        val abTest = ABTest(99999, true)
        updateTestOverride(abTest.key)
        assertTrue(AbacusFeatureConfigManager.shouldTrackTest(context, abTest))
    }

    private fun updateTestOverride(testKey: Int) {
        SettingUtils.save(context, testKey.toString(), AbacusVariant.BUCKETED.value)
    }
}
