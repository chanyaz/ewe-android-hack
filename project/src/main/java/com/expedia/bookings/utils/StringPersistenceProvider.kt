package com.expedia.bookings.utils

interface StringPersistenceProvider {
    fun getString(key: String, defaultValue: String = ""): String
    fun putString(key: String, value: String)
}
