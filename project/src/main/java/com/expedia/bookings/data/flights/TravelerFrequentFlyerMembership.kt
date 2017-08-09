package com.expedia.bookings.data.flights

import com.mobiata.android.Log
import com.mobiata.android.json.JSONable
import org.json.JSONException
import org.json.JSONObject

class TravelerFrequentFlyerMembership : JSONable {
    private var membershipNumber : String = ""
    private var planCode : String = ""
    private var airlineCode : String = ""

    fun getMembershipNumber(): String {
        return membershipNumber
    }

    fun getPlanCode(): String {
        return planCode
    }

    fun getAirlineCode(): String {
        return airlineCode
    }

    fun setMembershipNumber(membershipNumber: String){
        this.membershipNumber = membershipNumber
    }

    fun setPlanCode(planCode: String) {
        this.planCode = planCode
    }

    fun setAirlineCode(airlineCode: String) {
        this.airlineCode = airlineCode
    }

    override fun toJson(): JSONObject? {
        val obj = JSONObject()

        try {
            obj.putOpt("membershipNumber", membershipNumber)
            obj.putOpt("planCode", planCode)
            obj.putOpt("airlineCode", airlineCode)
            return obj
        } catch (e : JSONException) {
            Log.e("Could not convert TravelerFrequentFlyerMembership to JSON", e)
            return null
        }

    }

    override fun fromJson(obj: JSONObject): Boolean {
        membershipNumber = obj.optString("membershipNumber", null)
        planCode = obj.optString("planCode", null)
        airlineCode = obj.optString("airlineCode", null)
        return true
    }

}
