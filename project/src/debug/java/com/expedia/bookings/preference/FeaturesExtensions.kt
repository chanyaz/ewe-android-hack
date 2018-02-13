package com.expedia.bookings.preference

import com.expedia.bookings.features.Feature
import com.expedia.bookings.features.Features
import kotlin.reflect.full.memberProperties

fun Features.namesAndFeatures(): List<Pair<String, Feature>> {
    return this::class.memberProperties.mapNotNull {
        val feature = it.getter.call(this) as? Feature
        if (feature != null) {
            Pair(it.name, feature)
        } else {
            null
        }
    }
}
