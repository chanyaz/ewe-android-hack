package com.mobiata.flightlib.data

import org.json.JSONException
import org.json.JSONObject

import com.mobiata.android.json.JSONable

class Seat(var assigned: String? = null, var passenger: String? = null) : JSONable {

    override fun toJson(): JSONObject? {
        try {
            val obj = JSONObject()
            obj.putOpt("assigned", assigned)
            obj.putOpt("passengerName", passenger)
            return obj
        } catch (e: JSONException) {
            return null
        }
    }

    override fun fromJson(obj: JSONObject): Boolean {
        assigned = obj.optString("assigned", null)
        passenger = obj.optString("passengerName", null)
        return true
    }
}
