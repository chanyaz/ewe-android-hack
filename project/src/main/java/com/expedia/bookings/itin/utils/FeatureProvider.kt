package com.expedia.bookings.itin.utils

import com.expedia.bookings.features.Feature

object FeatureProvider : FeatureSource {
    override fun isFeatureEnabled(feature: Feature): Boolean {
        return feature.enabled()
    }
}

interface FeatureSource {
    fun isFeatureEnabled(feature: Feature): Boolean
}
