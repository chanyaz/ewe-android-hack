package com.expedia.bookings.featureconfig

import android.app.Application
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.ABTest
import com.expedia.bookings.data.abacus.AbacusResponse
import com.expedia.bookings.data.abacus.AbacusUtils
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
    fun before() = setup()

    @Test
    fun testSatelliteManagedEnabledABTest() {
        val abTest = ABTest(12345, true)
        AbacusTestUtils.updateABTest(abTest, AbacusUtils.DefaultVariant.BUCKETED.ordinal)
        assertTrue(AbacusFeatureConfigManager.isUserBucketedForTest(context, abTest))
    }

    @Test
    fun testSatelliteManagedDisabledABTest() {
        val abTest = ABTest(99999, true)
        AbacusTestUtils.updateABTest(abTest, AbacusUtils.DefaultVariant.BUCKETED.ordinal)
        assertFalse(AbacusFeatureConfigManager.isUserBucketedForTest(context, abTest))
    }

    @Test
    fun testSatelliteManagedDisabledWithOverrideABTest() {
        val abTest = ABTest(99999, true)
        AbacusTestUtils.updateABTest(abTest, AbacusUtils.DefaultVariant.BUCKETED.ordinal)
        updateTestOverride(abTest.key)
        assertTrue(AbacusFeatureConfigManager.isUserBucketedForTest(context, abTest))
    }

    @Test
    fun testNonSatelliteABTestIsBucketed() {
        val abTest = ABTest(12345)
        AbacusTestUtils.updateABTest(abTest, AbacusUtils.DefaultVariant.BUCKETED.ordinal)
        assertTrue(AbacusFeatureConfigManager.isUserBucketedForTest(context, abTest))
    }

    @Test
    fun testNonSatelliteABTestInControl() {
        val abTest = ABTest(12345)
        AbacusTestUtils.updateABTest(abTest, AbacusUtils.DefaultVariant.CONTROL.ordinal)
        assertFalse(AbacusFeatureConfigManager.isUserBucketedForTest(context, abTest))
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

    private fun setup() {
        val testList = listOf("12345")
        SatelliteFeatureConfigManager.cacheFeatureConfig(context, testList)
        val abacusResponse = AbacusResponse()
        Db.sharedInstance.abacusResponse = abacusResponse
    }

    private fun updateTestOverride(testKey: Int) {
        SettingUtils.save(context, testKey.toString(), AbacusUtils.DefaultVariant.BUCKETED.ordinal)
    }
}
