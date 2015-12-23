package com.expedia.bookings.data

import com.expedia.bookings.data.packages.Hotel
import com.expedia.bookings.data.hotels.PackageSearchResponse
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

public class HotelPackageDeserializer : JsonDeserializer<PackageSearchResponse.HotelPackage> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): PackageSearchResponse.HotelPackage {
        val hotelPackage = PackageSearchResponse.HotelPackage()
        val packageResult = json.asJsonObject;

        for (entry in packageResult.entrySet()) {
            val hotel = Gson().fromJson<Hotel>(packageResult.getAsJsonObject(entry.key), Hotel::class.java)
            hotelPackage.hotels.add(hotel)
        }
        return hotelPackage
    }
}