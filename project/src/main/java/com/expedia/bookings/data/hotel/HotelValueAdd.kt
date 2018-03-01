package com.expedia.bookings.data.hotel

import android.content.Context
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.mobiata.android.util.IoUtils
import org.json.JSONObject

class HotelValueAdd(val valueAddsEnum: ValueAddsEnum, val apiDescription: String) : Comparable<HotelValueAdd> {

    val iconId: Int get() = valueAddsEnum.iconId

    override fun compareTo(other: HotelValueAdd): Int {
        val preferenceCompare = valueAddsEnum.compareTo(other.valueAddsEnum)
        if (preferenceCompare == 0) {
            return apiDescription.compareTo(other.apiDescription)
        }
        return preferenceCompare
    }

    companion object {
        private val AMENITY_MAP_PATH = "ExpediaSharedData/ExpediaHotelValueAddsMapping.json"
        private var jsonMapping: Map<Int, ValueAddsEnum>? = null

        fun getHotelValueAdd(context: Context, valueAdd: HotelOffersResponse.ValueAdds): HotelValueAdd? {
            if (jsonMapping == null) {
                jsonMapping = createValueAddsMap(context)
            }
            val valueAddsEnum: ValueAddsEnum? = jsonMapping!!.get(valueAdd.id.toInt())
            return if (valueAddsEnum != null) {
                HotelValueAdd(valueAddsEnum, valueAdd.description)
            } else {
                null
            }
        }

        private fun createValueAddsMap(context: Context): Map<Int, ValueAddsEnum> {
            val jsonData = JSONObject(IoUtils.convertStreamToString(context.assets.open(AMENITY_MAP_PATH)))
            val mapping = HashMap<Int, ValueAddsEnum>()
            ValueAddsEnum.values().forEach { value ->
                createAmenityIdList(value, jsonData, mapping)
            }
            return mapping
        }

        private fun createAmenityIdList(enum: ValueAddsEnum, jsonData: JSONObject, mapping: HashMap<Int, ValueAddsEnum>) {
            val jsonList = jsonData.optJSONArray(enum.jsonKey)
            if (jsonList != null) {
                for (i in 0..jsonList.length()) {
                    mapping.put(jsonList.optInt(i), enum)
                }
            }
        }
    }
}
