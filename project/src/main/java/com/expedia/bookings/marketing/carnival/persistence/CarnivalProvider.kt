package com.expedia.bookings.marketing.carnival.persistence

import org.json.JSONObject

/**
 * Created by cplachta on 3/13/18.
 */
interface CarnivalPersistenceProvider {
    fun get(key: String): Any?
    fun put(attributes: JSONObject)
    fun clear()
}
