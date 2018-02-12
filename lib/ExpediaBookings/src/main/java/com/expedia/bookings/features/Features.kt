package com.expedia.bookings.features

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface Feature {
    fun enabled(): Boolean
}

class Features {
    companion object {
        val all = Features()
    }

    val legacyItinCardInActivity: Feature by RemoteFeatureDelegate()
}

class RemoteFeatureDelegate : ReadOnlyProperty<Features, Feature> {
    override fun getValue(thisRef: Features, property: KProperty<*>): Feature {
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
