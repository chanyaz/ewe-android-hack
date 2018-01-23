package com.expedia.util

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

inline fun <T : Any> notNullAndObservable(crossinline onChange: (newValue: T) -> Unit): ReadWriteProperty<Any?, T> {
    return object : NotNullObservableProperty<T>() {
        override fun afterChange(newValue: T): Unit = onChange(newValue)
    }
}

abstract class NotNullObservableProperty<T : Any> : ReadWriteProperty<Any?, T> {
    private var value: T? = null

    protected open fun afterChange(newValue: T) {
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException("Property ${property.name} should be initialized before get.")
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
        afterChange(value)
    }
}
