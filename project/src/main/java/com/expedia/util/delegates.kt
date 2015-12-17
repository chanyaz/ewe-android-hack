package com.expedia.util

import kotlin.properties.ReadWriteProperty

public fun notNullAndObservable<T>(inlineOptions(InlineOption.ONLY_LOCAL_RETURN) onChange: (T) -> Unit): ReadWriteProperty<Any?, T> {
    return object: NotNullObservableProperty<T>() {
        override fun afterChange(newValue: T): Unit = onChange(newValue)
    }
}

private abstract class NotNullObservableProperty<T: Any>() : ReadWriteProperty<Any?, T> {
    private var value: T? = null

    protected open fun afterChange (newValue: T): Unit {}

    public override fun get(thisRef: Any?, desc: PropertyMetadata): T {
        return value ?: throw IllegalStateException("Property ${desc.name} should be initialized before get.")
    }

    public override fun set(thisRef: Any?, desc: PropertyMetadata, value: T) {
        this.value = value
        afterChange(value)
    }
}
