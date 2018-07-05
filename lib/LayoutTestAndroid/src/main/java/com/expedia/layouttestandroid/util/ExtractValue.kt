package com.expedia.layouttestandroid.util

import kotlin.reflect.KProperty

class ExtractValue<T : Any>(private val dataSpec: Map<String, Any?>) {
    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return dataSpec[property.name] as T
    }
}

class ExtractOptionalValue<T : Any?>(private val dataSpec: Map<String, Any?>) {
    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return dataSpec[property.name] as? T?
    }
}
