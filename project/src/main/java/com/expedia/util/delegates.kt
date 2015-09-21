package com.expedia.util

import kotlin.properties.ReadWriteProperty

public inline fun notNullAndObservable<T: Any>(crossinline onChange: (newValue: T) -> Unit): ReadWriteProperty<Any?, T> {
    return object: NotNullObservableProperty<T>() {
        override fun afterChange(newValue: T): Unit = onChange(newValue)
    }
}

public abstract class NotNullObservableProperty<T: Any>() : ReadWriteProperty<Any?, T> {
    private var value: T? = null

    protected open fun afterChange (newValue: T): Unit {}

    public override fun get(thisRef: Any?, property: PropertyMetadata): T {
        return value ?: throw IllegalStateException("Property ${property.name} should be initialized before get.")
    }

    public override fun set(thisRef: Any?, property: PropertyMetadata, value: T) {
        this.value = value
        afterChange(value)
    }
}
