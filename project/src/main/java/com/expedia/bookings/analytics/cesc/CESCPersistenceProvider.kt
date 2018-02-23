package com.expedia.bookings.analytics.cesc

interface CESCPersistenceProvider {
    fun get(key: String): Pair<String, Long>?
    fun clear()
    fun put(key: String, value: Pair<String, Long>)
}
