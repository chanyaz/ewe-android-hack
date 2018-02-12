package com.expedia.bookings.features

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface Feature {
    fun enabled(): Boolean
}

class Features {
    companion object {
        val LEGACY_ITIN_CARD_IN_ACTIVITY: Feature by RemoteFeatureDelegate()
    }
}

class RemoteFeatureDelegate : ReadOnlyProperty<Features.Companion, Feature> {
    override fun getValue(thisRef: Features.Companion, property: KProperty<*>): Feature {
        return RemoteFeature(property.name)
    }
}

class RemoteFeature(private val key: String) : Feature {
    override fun enabled(): Boolean {
        return Plugins.remoteFeatureResolver.isEnabled(key)
    }
}

class Plugins {
    companion object {
        lateinit var remoteFeatureResolver: RemoteFeatureResolver
    }
}

interface RemoteFeatureResolver {
    fun isEnabled(key: String): Boolean
}
