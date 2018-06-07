package com.expedia.bookings.itin.helpers

import com.expedia.bookings.features.Feature
import com.expedia.bookings.itin.utils.FeatureSource

class MockFeatureProvider : FeatureSource {
    var featureEnabled = true
    override fun isFeatureEnabled(feature: Feature): Boolean {
        return featureEnabled
    }
}
