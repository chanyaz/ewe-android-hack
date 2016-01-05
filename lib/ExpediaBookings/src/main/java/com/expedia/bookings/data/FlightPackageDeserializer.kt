package com.expedia.bookings.data

import com.expedia.bookings.data.packages.FlightLeg;
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

public class FlightPackageDeserializer : JsonDeserializer<PackageSearchResponse.FlightPackage> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): PackageSearchResponse.FlightPackage {
        val flightPackage = PackageSearchResponse.FlightPackage()
        val packageResult = json.asJsonObject;

        for (entry in packageResult.entrySet()) {
            val jsonObject = packageResult.getAsJsonObject(entry.key)
            val typeToken = object : TypeToken<List<FlightLeg>>() {}.type
            val flightLegs = Gson().fromJson<List<FlightLeg>>(jsonObject.getAsJsonArray("flightLegs"), typeToken)
            flightPackage.flights.addAll(flightLegs)
        }
        return flightPackage
    }
}