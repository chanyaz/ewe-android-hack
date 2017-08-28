package com.expedia.bookings.featureconfig

import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateUtils
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FeatureConfigManagerTest {
    var featureConfigMgr: FeatureConfigManager by Delegates.notNull()
    val context = RuntimeEnvironment.application

    @Before
    fun before() {
        featureConfigMgr = FeatureConfigManager(context)
    }

    @Test
    fun testEmptyResponse() {
        featureConfigMgr.cacheFeatureConfig(emptyList())

        val supportedFeatures = getConfigFromPrefs()
        assertNotNull(supportedFeatures)
        assertEquals(0, supportedFeatures!!.size)
    }

    @Test
    fun testOneElementResponse() {
        val testList = listOf("downloadConfigsOnPOSChange")
        featureConfigMgr.cacheFeatureConfig(testList)

        val supportedFeatures = getConfigFromPrefs()
        assertNotNull(supportedFeatures)
        assertEquals(1, supportedFeatures!!.size)
    }

    @Test
    fun testCacheResponse() {
        val testList = listOf("downloadConfigsOnPOSChange", "14731", "14732", "14484")
        featureConfigMgr.cacheFeatureConfig(testList)

        val supportedFeatures = getConfigFromPrefs()
        assertNotNull(supportedFeatures)
        assertEquals(4, supportedFeatures!!.size)
    }

    @Test
    fun testDupsAreNotCached() {
        val testList = listOf("downloadConfigsOnPOSChange", "14731", "14732", "14484", "14732")
        featureConfigMgr.cacheFeatureConfig(testList)

        val supportedFeatures = getConfigFromPrefs()
        assertNotNull(supportedFeatures)
        assertEquals(4, supportedFeatures!!.size)
    }

    @Test
    fun testShouldNotRefreshConfigIfRecent() {
        updateTimestamp(System.currentTimeMillis())
        assertFalse(featureConfigMgr.shouldUpdateConfig())
    }

    @Test
    fun testShouldRefreshConfigIfStale() {
        updateTimestamp(DateUtils.DAY_IN_MILLIS + 60)
        assertTrue(featureConfigMgr.shouldUpdateConfig())
    }

    private fun updateTimestamp(timestamp: Long) {
        val editor = getSharedPrefs().edit()
        editor.putLong("lastUpdated", timestamp)
        editor.apply()
    }

    private fun getConfigFromPrefs(): Set<String>? {
        val prefs = getSharedPrefs()
        val supportedFeatures = prefs.getStringSet("supportedFeatures", null)
        return supportedFeatures
    }

    private fun getSharedPrefs(): SharedPreferences {
        val prefs = context.getSharedPreferences("featureConfig", Context.MODE_PRIVATE)
        return prefs
    }
}