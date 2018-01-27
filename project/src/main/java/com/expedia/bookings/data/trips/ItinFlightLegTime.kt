package com.expedia.bookings.data.trips

import com.mobiata.android.json.JSONable
import org.json.JSONException
import org.json.JSONObject

class ItinFlightLegTime : JSONable {
    var raw: String = ""
    var localized: String = ""
    var epochSeconds: String = ""
    var timeZoneOffsetSeconds: String = ""
    var localizedShortDate: String = ""
    var localizedMediumDate: String = ""
    var localizedFullDate: String = ""
    var localizedLongDate: String = ""
    var localizedShortTime: String = ""

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
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    override fun fromJson(obj: JSONObject): Boolean {
        raw = obj.optString("raw", raw)
        localized = obj.optString("localized", localized)
        epochSeconds = obj.optString("epochSeconds", epochSeconds)
        timeZoneOffsetSeconds = obj.optString("timeZoneOffsetSeconds", timeZoneOffsetSeconds)
        localizedShortDate = obj.optString("localizedShortDate", localizedShortDate)
        localizedMediumDate = obj.optString("localizedMediumDate", localizedMediumDate)
        localizedFullDate = obj.optString("localizedFullDate", localizedFullDate)
        localizedLongDate = obj.optString("localizedLongDate", localizedLongDate)
        localizedShortTime = obj.optString("localizedShortTime", localizedShortTime)
        return true
    }
}
