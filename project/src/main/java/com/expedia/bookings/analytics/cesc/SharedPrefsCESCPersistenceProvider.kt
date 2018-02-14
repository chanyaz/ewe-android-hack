package com.expedia.bookings.analytics.cesc

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPrefsCESCPersistenceProvider(private val context: Context) : CESCPersistenceProvider {

    private val sharedPrefAndHashMapName = "cesc"

    override fun get(key: String): Pair<String, Long>? {
        val cescData = getStoredData()
        return cescData[key]
    }

    override fun clear() {
        val sharedPref = context.getSharedPreferences(sharedPrefAndHashMapName, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.remove(sharedPrefAndHashMapName)
        editor.apply()
    }

    override fun put(key: String, value: Pair<String, Long>) {
        val sharedPref = context.getSharedPreferences(sharedPrefAndHashMapName, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        val storedData = getStoredData()
        storedData.put(key, value)
        val storedDataJson = Gson().toJson(storedData)
        editor.putString(sharedPrefAndHashMapName, storedDataJson)
        editor.apply()
    }

    private fun getStoredData(): HashMap<String, Pair<String, Long>> {
        val sharedPref = context.getSharedPreferences(sharedPrefAndHashMapName, Context.MODE_PRIVATE)
        val newHashMapJson = Gson().toJson(HashMap<String, Pair<String, Long>>())
        val storedValue = sharedPref.getString(sharedPrefAndHashMapName, newHashMapJson)
        val type = object : TypeToken<HashMap<String, Pair<String, Long>>>() {}.type
        val cescData = Gson().fromJson<HashMap<String, Pair<String, Long>>>(storedValue, type)
        return cescData
    }
}
