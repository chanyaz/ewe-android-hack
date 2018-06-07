package com.expedia.bookings.itin.utils

import android.content.Context
import com.expedia.bookings.features.Feature
import com.expedia.bookings.features.Features
import com.expedia.bookings.preference.extensions.features
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.FeatureTestUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FeatureProviderTest {
    private val featureProvider = FeatureProvider
    private val context: Context = RuntimeEnvironment.application

    @Test
    fun testIsFeatureEnabledTrue() {
        val feature = Features.all.activityMap
        FeatureTestUtils.enableFeature(context, feature)
        assertTrue(featureProvider.isFeatureEnabled(feature))
    }

    @Test
    fun testIsFeatureEnabledFalse() {
        val feature = Features.all.activityMap
        FeatureTestUtils.disableFeature(context, feature)
        assertFalse(featureProvider.isFeatureEnabled(feature))
    }

    @Test
    fun testFeatureNotFound() {
        val feature = MockFeature()
        assertNull(Features.all.features().find { it.name == feature.name })
        assertFalse(featureProvider.isFeatureEnabled(feature))
    }

    class MockFeature : Feature {
        override val name: String = "MockFeature"
        override fun enabled(): Boolean = true
    }
}
