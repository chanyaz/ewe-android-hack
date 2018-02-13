package com.expedia.bookings.features

import com.expedia.bookings.plugins.Plugins
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

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

interface RemoteFeatureResolver {
    fun isEnabled(key: String): Boolean
}
