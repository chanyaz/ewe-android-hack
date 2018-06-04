package com.expedia.bookings.itin.helpers

import com.expedia.bookings.features.Feature

class MockFeature : Feature {
    var featureEnabled = true
    override val name: String = "MockFeature"
    override fun enabled(): Boolean {
        return featureEnabled
    }
}
