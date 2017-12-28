package com.expedia.bookings.data.trips

import com.mobiata.android.json.JSONable
import org.json.JSONException
import org.json.JSONObject


class FlightAction : JSONable {
    var isChangeable = false
    var isCancellable = false

    override fun toJson(): JSONObject? {
        val obj = JSONObject()

        try {
            obj.putOpt("isChangeable", isChangeable)
            obj.putOpt("isCancellable", isCancellable)
            return obj
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }

    }

    override fun fromJson(obj: JSONObject): Boolean {
        isChangeable = obj.optBoolean("isChangeable", isChangeable)
        isCancellable = obj.optBoolean("isCancellable", isCancellable)
        return true
    }
}