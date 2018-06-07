package com.expedia.bookings.itin.utils

import com.expedia.bookings.features.Feature
import com.expedia.bookings.features.Features
import com.expedia.bookings.preference.extensions.features

object FeatureProvider : FeatureSource {
    override fun isFeatureEnabled(feature: Feature): Boolean {
        return Features.all.features().find { it.name == feature.name }?.enabled() == true
    }
}

interface FeatureSource {
    fun isFeatureEnabled(feature: Feature): Boolean
}
