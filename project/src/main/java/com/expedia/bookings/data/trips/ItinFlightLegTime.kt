package com.expedia.bookings.data.trips

import com.mobiata.android.json.JSONable
import org.json.JSONException
import org.json.JSONObject

class ItinFlightLegTime  : JSONable {
    lateinit var raw : String
    lateinit var localized : String
    lateinit var epochSeconds : String
    lateinit var timeZoneOffsetSeconds : String
    lateinit var localizedShortDate : String
    lateinit var localizedMediumDate : String
    lateinit var localizedFullDate : String
    lateinit var localizedLongDate : String
    lateinit var localizedShortTime : String

    override fun toJson(): JSONObject? {
        val obj = JSONObject()

        try {
            obj.putOpt("raw", raw)
            obj.putOpt("localized", localized)
            obj.putOpt("epochSeconds", epochSeconds)
            obj.putOpt("timeZoneOffsetSeconds", timeZoneOffsetSeconds)
            obj.putOpt("localizedShortDate", localizedShortDate)
            obj.putOpt("localizedMediumDate", localizedMediumDate)
            obj.putOpt("localizedFullDate", localizedFullDate)
            obj.putOpt("localizedLongDate", localizedLongDate)
            obj.putOpt("localizedShortTime", localizedShortTime)
            return obj
        } catch (e : JSONException) {
            throw RuntimeException(e)
        }

    }

    override fun fromJson(obj: JSONObject): Boolean {
        raw = obj.optString("raw", null)
        localized = obj.optString("localized", null)
        epochSeconds = obj.optString("epochSeconds", null)
        timeZoneOffsetSeconds = obj.optString("timeZoneOffsetSeconds", null)
        localizedShortDate = obj.optString("localizedShortDate", null)
        localizedMediumDate = obj.optString("localizedMediumDate", null)
        localizedFullDate = obj.optString("localizedFullDate", null)
        localizedLongDate = obj.optString("localizedLongDate", null)
        localizedShortTime = obj.optString("localizedShortTime", null)
        return true
    }

}
