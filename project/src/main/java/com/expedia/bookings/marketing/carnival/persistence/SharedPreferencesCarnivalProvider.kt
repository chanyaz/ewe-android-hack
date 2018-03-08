package com.expedia.bookings.marketing.carnival.persistence

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject

class SharedPreferencesCarnivalProvider(private val context: Context) : CarnivalPersistenceProvider {

    private val carnivalSharedPreferencesName = "carnivalSharedPreferencesInstance"

    override fun get(key: String): Any? {
        val storedAttributes = getStoredAttributes()
        return storedAttributes[key]
    }

    override fun put(attributes: JSONObject) {
        val sharedPref = context.getSharedPreferences(carnivalSharedPreferencesName, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        val storedDataJson = getStoredAttributesWithNewAttributesAdded(attributes)
        val hashMap = Gson().toJson(storedDataJson)

        editor.putString(carnivalSharedPreferencesName, hashMap)
        editor.apply()
    }

    private fun getStoredAttributesWithNewAttributesAdded(newAttributes: JSONObject): HashMap<String, Any> {
        val storedDataJson = getStoredAttributes()
        for (key in newAttributes.keys()) {
            storedDataJson[key] = newAttributes.get(key)
        }

        return storedDataJson
    }

    private fun getStoredAttributes(): HashMap<String, Any> {
        val sharedPref = context.getSharedPreferences(carnivalSharedPreferencesName, Context.MODE_PRIVATE)
        val defaultObject = Gson().toJson(HashMap<String, Any>())
        val storedValue = sharedPref.getString(carnivalSharedPreferencesName, defaultObject)
        val type = object : TypeToken<HashMap<String, Any>>() {}.type
        return Gson().fromJson<HashMap<String, Any>>(storedValue, type)
    }

    override fun clear() {
        val sharedPref = context.getSharedPreferences(carnivalSharedPreferencesName, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.remove(carnivalSharedPreferencesName)
        editor.apply()
    }
}
