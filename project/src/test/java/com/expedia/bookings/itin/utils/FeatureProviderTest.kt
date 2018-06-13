package com.expedia.bookings.itin.utils

import com.expedia.bookings.features.Feature
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FeatureProviderTest {
    private val featureProvider = FeatureProvider

    @Test
    fun testIsFeatureEnabledTrue() {
        val feature = MockFeature(true)
        assertTrue(featureProvider.isFeatureEnabled(feature))
    }

    @Test
    fun testIsFeatureEnabledFalse() {
        val feature = MockFeature(false)
        assertFalse(featureProvider.isFeatureEnabled(feature))
    }

    class MockFeature(val isEnabled: Boolean) : Feature {
        override val name: String = "MockFeature"
        override fun enabled(): Boolean {
            return isEnabled
        }
    }
}
