package com.expedia.bookings.preference

import com.expedia.bookings.features.Feature
import com.expedia.bookings.features.Features

fun Features.features(): List<Feature> {
    return this::class.java.methods
            .filter { it.returnType == Feature::class.java }
            .sortedBy { it.name }
            .mapNotNull { it.invoke(this) as? Feature }
}
