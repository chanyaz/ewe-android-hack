package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class ProWizardBucketCacheTest {
    private val context = RuntimeEnvironment.application

    @Before
    fun setup() {
        context.getSharedPreferences("abacus_prefs", Context.MODE_PRIVATE).edit().clear().apply()
    }

    @Test
    fun testProWizardNoCache() {
        assertFalse(ProWizardBucketCache.isBucketed(context))
        assertEquals(-1, ProWizardBucketCache.getTrackingValue(context),
                "FAILURE: Expected empty cache to result in -1 tracking value")
    }

    @Test
    fun testProWizardControl() {
        ProWizardBucketCache.cacheBucket(context, 0)

        assertFalse(ProWizardBucketCache.isBucketed(context))
        assertEquals(0, ProWizardBucketCache.getTrackingValue(context))
    }

    @Test
    fun testProWizardBucketed() {
        ProWizardBucketCache.cacheBucket(context, 1)

        assertTrue(ProWizardBucketCache.isBucketed(context))
        assertEquals(1, ProWizardBucketCache.getTrackingValue(context))
    }
}
