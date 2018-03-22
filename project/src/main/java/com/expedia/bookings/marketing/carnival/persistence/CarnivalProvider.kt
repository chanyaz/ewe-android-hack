package com.expedia.bookings.marketing.carnival.persistence

import com.carnival.sdk.AttributeMap

interface CarnivalPersistenceProvider {
    fun get(key: String): Any?
    fun put(attributes: AttributeMap)
    fun clear()
}
