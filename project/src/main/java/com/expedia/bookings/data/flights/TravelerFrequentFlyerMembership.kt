package com.expedia.bookings.data.flights

import com.mobiata.android.Log
import com.mobiata.android.json.JSONable
import org.json.JSONException
import org.json.JSONObject

class TravelerFrequentFlyerMembership : JSONable {
    lateinit var membershipNumber: String
    lateinit var planCode: String
    lateinit var airlineCode: String
    var programName = ""
    var frequentFlyerPlanID = ""

    override fun toJson(): JSONObject? {
        val obj = JSONObject()

        try {
            obj.putOpt("membershipNumber", membershipNumber)
            obj.putOpt("planCode", planCode)
            obj.putOpt("airlineCode", airlineCode)
            obj.putOpt("programName", programName)
            return obj
        } catch (e: JSONException) {
            Log.e("Could not convert TravelerFrequentFlyerMembership to JSON", e)
            return null
        }
    }

    override fun fromJson(obj: JSONObject): Boolean {
        membershipNumber = obj.optString("membershipNumber", null)
        planCode = obj.optString("planCode", null)
        airlineCode = obj.optString("airlineCode", null)
        programName = obj.optString("programName", null)
        return true
    }
}
