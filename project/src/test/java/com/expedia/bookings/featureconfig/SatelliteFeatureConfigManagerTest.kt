package com.expedia.bookings.featureconfig

import android.content.Context
import android.content.SharedPreferences
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class SatelliteFeatureConfigManagerTest {
    val context = RuntimeEnvironment.application

    @Before
    fun setup() {
        clearCache()
    }

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
        updateTimestamp(TimeUnit.DAYS.toMillis(1) + 60)
        assertFalse(SatelliteFeatureConfigManager.configValid(context))
    }

    @Test
    fun testConfigStillValid() {
        updateTimestamp(System.currentTimeMillis())
        assertTrue(SatelliteFeatureConfigManager.configValid(context))
    }

    @Test
    fun testShouldRefreshConfigIfStale() {
        updateTimestamp(TimeUnit.DAYS.toMillis(1) + 60)
        assertTrue(SatelliteFeatureConfigManager.shouldUpdateConfig(context))
    }

    @Test
    fun testCacheFeatureConfigIntegrationWithSharedPrefs() {
        val mockSharedPreferences = Mockito.mock(SharedPreferences::class.java)
        val satelliteFeatureConfigManager = SatelliteFeatureConfigManager(mockSharedPreferences)
        val mockEditor = Mockito.mock(SharedPreferences.Editor::class.java)
        Mockito.`when`(mockSharedPreferences.edit()).thenReturn(mockEditor)

        val testList = listOf("downloadConfigsOnPOSChange")
        satelliteFeatureConfigManager.cacheFeatureConfig(testList)
        Mockito.verify(mockEditor, Mockito.atLeastOnce()).putStringSet(Mockito.eq("supportedFeatures"), Mockito.eq(testList.toSet()))
        Mockito.verify(mockEditor, Mockito.atLeastOnce()).putLong(Mockito.eq("lastUpdated"), Mockito.anyLong())
        Mockito.verify(mockEditor, Mockito.atLeastOnce()).apply()
    }

    @Test
    fun testFeatureEnabledBasedOnDataInSharedPrefs() {
        val mockSharedPreferences = Mockito.mock(SharedPreferences::class.java)
        val satelliteFeatureConfigManager = SatelliteFeatureConfigManager(mockSharedPreferences)
        val mockEditor = Mockito.mock(SharedPreferences.Editor::class.java)
        Mockito.`when`(mockSharedPreferences.edit()).thenReturn(mockEditor)

        assertFalse(satelliteFeatureConfigManager.isEnabled("downloadConfigsOnPOSChange"))
        val testList = mutableSetOf("downloadConfigsOnPOSChange", "14731")

        Mockito.`when`(mockSharedPreferences.getStringSet(Mockito.anyString(), Mockito.eq(emptySet()))).thenReturn(testList)
        assertTrue(satelliteFeatureConfigManager.isEnabled("downloadConfigsOnPOSChange"))
        assertTrue(satelliteFeatureConfigManager.isEnabled("14731"))
        assertFalse(satelliteFeatureConfigManager.isEnabled("someUnkownFeature"))
        assertFalse(satelliteFeatureConfigManager.isEnabled("123"))
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
