package com.expedia.bookings.featureconfig

import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateUtils
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class SatelliteFeatureConfigManagerTest {
    val context = RuntimeEnvironment.application

    @Test
    fun testEmptyResponse() {
        SatelliteFeatureConfigManager.cacheFeatureConfig(context, emptyList())
        val supportedFeatures = getConfigFromPrefs()
        assertNotNull(supportedFeatures)
        assertEquals(0, supportedFeatures!!.size)
    }

    @Test
    fun testOneElementResponse() {
        val testList = listOf("downloadConfigsOnPOSChange")
        SatelliteFeatureConfigManager.cacheFeatureConfig(context, testList)

        val supportedFeatures = getConfigFromPrefs()
        assertNotNull(supportedFeatures)
        assertEquals(1, supportedFeatures!!.size)
    }

    @Test
    fun testCacheResponse() {
        val testList = listOf("downloadConfigsOnPOSChange", "14731", "14732", "14484")
        SatelliteFeatureConfigManager.cacheFeatureConfig(context, testList)

        val supportedFeatures = getConfigFromPrefs()
        assertNotNull(supportedFeatures)
        assertEquals(4, supportedFeatures!!.size)
    }

    @Test
    fun testDupsAreNotCached() {
        val testList = listOf("downloadConfigsOnPOSChange", "14731", "14732", "14484", "14732")
        SatelliteFeatureConfigManager.cacheFeatureConfig(context, testList)

        val supportedFeatures = getConfigFromPrefs()
        assertNotNull(supportedFeatures)
        assertEquals(4, supportedFeatures!!.size)
    }

    @Test
    fun testShouldNotRefreshConfigIfRecent() {
        updateTimestamp(System.currentTimeMillis())
        assertFalse(SatelliteFeatureConfigManager.shouldUpdateConfig(context))
    }

    @Test
    fun testConfigInvalid() {
        updateTimestamp(DateUtils.DAY_IN_MILLIS + 60)
        assertFalse(SatelliteFeatureConfigManager.configValid(context))
    }

    @Test
    fun testConfigStillValid() {
        updateTimestamp(System.currentTimeMillis())
        assertTrue(SatelliteFeatureConfigManager.configValid(context))
    }

    @Test
    fun testShouldRefreshConfigIfStale() {
        updateTimestamp(DateUtils.DAY_IN_MILLIS + 60)
        assertTrue(SatelliteFeatureConfigManager.shouldUpdateConfig(context))
    }

    @Test
    fun testAbacusTestEnabled() {
        assertFalse(SatelliteFeatureConfigManager.testEnabled(context, 14731))

        val testList = listOf("downloadConfigsOnPOSChange", "14731", "14732", "14484", "14732")
        SatelliteFeatureConfigManager.cacheFeatureConfig(context, testList)

        assertTrue(SatelliteFeatureConfigManager.testEnabled(context, 14731))
        assertFalse(SatelliteFeatureConfigManager.testEnabled(context, 123))
    }

    @Test
    fun testClearWillForceConfigRefresh() {
        updateTimestamp(System.currentTimeMillis())
        assertFalse(SatelliteFeatureConfigManager.shouldUpdateConfig(context))

        clearCache()
        assertTrue(SatelliteFeatureConfigManager.shouldUpdateConfig(context))
        assertFalse(SatelliteFeatureConfigManager.configValid(context))
    }

    private fun updateTimestamp(timestamp: Long) {
        val editor = getSharedPrefs().edit()
        editor.putLong(SatelliteFeatureConfigManager.PREFS_FEATURE_CONFIG_LAST_UPDATED, timestamp)
        editor.apply()
    }

    private fun getConfigFromPrefs(): Set<String>? {
        val prefs = getSharedPrefs()
        val supportedFeatures = prefs.getStringSet(SatelliteFeatureConfigManager.PREFS_SUPPORTED_FEATURE_SET, null)
        return supportedFeatures
    }

    private fun getSharedPrefs(): SharedPreferences {
        val prefs = context.getSharedPreferences("featureConfig", Context.MODE_PRIVATE)
        return prefs
    }

    private fun clearCache() {
        val editor = getSharedPrefs().edit()
        editor.clear().apply()
    }
}