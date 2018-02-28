package com.expedia.bookings.data.abacus

import org.joda.time.DateTime

import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.FlightItinDetailsResponse
import com.expedia.bookings.data.HotelItinDetailsResponse
import com.expedia.bookings.data.ItinDetailsResponse
import com.expedia.bookings.data.MIDItinDetailsResponse
import com.expedia.bookings.data.PackageItinDetailsResponse
import com.expedia.bookings.services.DateTimeTypeAdapter
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class ItinTripDeserializer : JsonDeserializer<AbstractItinDetailsResponse> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): AbstractItinDetailsResponse {
        val jsonObject = json.asJsonObject
        val responseData = jsonObject.getAsJsonObject("responseData")
        val gson = GsonBuilder().registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter()).create()
        if (responseData != null) {
            if (responseData.has("hotels") && responseData.has("flights") && !responseData.has("packages")) {
                return gson.fromJson(jsonObject, MIDItinDetailsResponse::class.java)
            } else if (responseData.has("hotels")) {
                return gson.fromJson(jsonObject, HotelItinDetailsResponse::class.java)
            } else if (responseData.has("flights")) {
                return gson.fromJson(jsonObject, FlightItinDetailsResponse::class.java)
            } else if (responseData.has("packages")) {
                return gson.fromJson(jsonObject, PackageItinDetailsResponse::class.java)
            }
        }
        return gson.fromJson(jsonObject, ItinDetailsResponse::class.java)
    }
}
