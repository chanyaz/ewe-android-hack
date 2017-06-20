package com.expedia.bookings.server

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class TravelerProfile (val userId : String){

    var client : OkHttpClient = OkHttpClient()

    var numHotel : Int = 0
    var numFlight : Int = 0
    var numCar : Int = 0

    init {
        loadData()
    }

    fun loadData() {
        val request : Request = Request.Builder().url("https://watson-traveler-profile-service.prod.expedia.com/v2/traveler/$userId/attributes").build()
        client.newCall(request).enqueue(callback())
    }

    inner class callback : Callback {
        override fun onFailure(call: Call?, e: IOException?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onResponse(call: Call?, response: Response?) {
            val requestBody = response?.body()?.string()
            val json : JSONObject = JSONObject(requestBody)
            val attributes = json.getJSONObject("attributes")
            numHotel = attributes.optInt("total3yrHotelOrderCnt", 0)
            numFlight = attributes.optInt("total3yrFlightOrderCnt", 0)
            numCar = attributes.optInt("total3yrCarOrderCnt", 0)
        }
    }
}