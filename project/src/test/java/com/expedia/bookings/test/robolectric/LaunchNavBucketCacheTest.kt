package com.expedia.bookings.test.robolectric

import android.content.Context
import com.expedia.bookings.utils.LaunchNavBucketCache
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class LaunchNavBucketCacheTest {
    private val context = RuntimeEnvironment.application

    @Before
    fun setup() {
        context.getSharedPreferences("abacus_prefs", Context.MODE_PRIVATE).edit().clear().apply()
    }

    @Test
    fun testLaunchNavNoCache() {
        assertFalse(LaunchNavBucketCache.isBucketed(context))
        assertEquals(-1, LaunchNavBucketCache.getTrackingValue(context),
                "FAILURE: Expected empty cache to result in -1 tracking value")
    }

    @Test
    fun testLaunchNavProWizardControl() {
        LaunchNavBucketCache.cacheBucket(context, 0)

        assertFalse(LaunchNavBucketCache.isBucketed(context))
        assertEquals(0, LaunchNavBucketCache.getTrackingValue(context))
    }

    @Test
    fun testLaunchNavProWizardBucketed() {
        LaunchNavBucketCache.cacheBucket(context, 1)

        assertTrue(LaunchNavBucketCache.isBucketed(context))
        assertEquals(1, LaunchNavBucketCache.getTrackingValue(context))
    }
}
