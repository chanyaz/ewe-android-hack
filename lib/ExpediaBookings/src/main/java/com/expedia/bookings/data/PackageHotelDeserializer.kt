package com.expedia.bookings.data

import com.expedia.bookings.data.packages.PackageHotel
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class PackageHotelDeserializer : JsonDeserializer<PackageSearchResponse.HotelPackage> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): PackageSearchResponse.HotelPackage {
        val hotelPackage = PackageSearchResponse.HotelPackage()
        val packageResult = json.asJsonObject

        for (entry in packageResult.entrySet()) {
            val pHotel = Gson().fromJson<PackageHotel>(packageResult.getAsJsonObject(entry.key), PackageHotel::class.java)
            pHotel.hotelPid = entry.key
            val hotel = com.expedia.bookings.data.hotels.Hotel.convertPackageHotel(pHotel)
            hotelPackage.hotels.add(hotel)
        }
        return hotelPackage
    }
}
