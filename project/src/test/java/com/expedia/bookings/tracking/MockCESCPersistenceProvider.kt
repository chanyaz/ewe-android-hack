package com.expedia.bookings.tracking

import com.expedia.bookings.analytics.cesc.CESCPersistenceProvider

class MockCESCPersistenceProvider : CESCPersistenceProvider {

    private val hashMap = HashMap<String, Pair<String, Long>>()

    override fun get(key: String): Pair<String, Long>? {
        return hashMap[key]
    }

    override fun clear() {
        hashMap.clear()
    }

    override fun put(key: String, value: Pair<String, Long>) {
        hashMap[key] = value
    }
}
