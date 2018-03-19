package com.expedia.bookings.marketing.carnival.persistence

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject

class MockCarnivalPersistenceProvider : CarnivalPersistenceProvider {

    private var savedAttributes = HashMap<String, Any>()
    private var savedAttributesAsJSONString: String = Gson().toJson(HashMap<String, Any>())

    override fun get(key: String): Any? {
        val storedAttributes = getStoredAttributes()
        return storedAttributes[key]
    }

    override fun clear() {
        savedAttributes.clear()
    }

    override fun put(attributes: JSONObject) {
        val storedDataJson = getStoredAttributesWithNewAttributesAdded(attributes)
        val hashMapJsonString = Gson().toJson(storedDataJson)
        savedAttributesAsJSONString = hashMapJsonString
    }

    private fun getStoredAttributesWithNewAttributesAdded(newAttributes: JSONObject): HashMap<String, Any> {
        val storedDataJson = getStoredAttributes()
        for (key in newAttributes.keys()) {
            storedDataJson[key] = newAttributes.get(key)
        }

        return storedDataJson
    }

    fun getStoredAttributes(): HashMap<String, Any> {
        val type = object : TypeToken<HashMap<String, Any>>() {}.type
        return Gson().fromJson<HashMap<String, Any>>(savedAttributesAsJSONString, type)
    }
}
