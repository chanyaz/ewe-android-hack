package com.expedia.bookings.data.travelgraph

import com.expedia.bookings.data.SuggestionV4

class TravelGraphHotelSearchInfo {
    var numberOfRooms: Int = 0
    var numberOfTravelers: Int = 0
    var searchRegion: TravelGraphSearchRegion? = null
    var allRegions: List<TravelGraphSearchRegion> = emptyList()
    var roomList: List<TravelGraphHotelRoom> = emptyList()

    class TravelGraphSearchRegion {
        var id: String? = null
        var type: String? = null
        var name: String = ""
        var shortName: String = ""
        var imageURL: String? = null

        val regionType: TravelGraphRegionType
            get() = TravelGraphRegionType.toEnum(type)

        fun toSuggestionV4(): SuggestionV4? {
            if (id != null) {
                val suggestion = SuggestionV4()
                suggestion.gaiaId = id
                suggestion.iconType = SuggestionV4.IconType.RECENT_SEARCH_ICON
                val regionName = SuggestionV4.RegionNames()
                regionName.displayName = shortName
                regionName.shortName = shortName
                regionName.fullName = name
                suggestion.regionNames = regionName
                suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
                suggestion.hierarchyInfo?.isChild = false
                return suggestion
            }
            return null
        }
    }

    class TravelGraphHotelRoom {
        var roomOccupants: TravelGraphTravelerDetails? = null
    }

    class TravelGraphTravelerDetails {
        var numberOfAdults: Int = 0
        var numberOfOccupants: Int = 0
        var agesOfChildren: List<Int> = emptyList()
    }

    enum class TravelGraphRegionType {
        NEIGHBORHOOD,
        CITY,
        MULTI_CITY,
        AIRPORT_METRO_CODE,
        NONE;

        companion object {
            fun toEnum(value: String?): TravelGraphRegionType {
                return if ("neighborhood" == value) {
                    TravelGraphRegionType.NEIGHBORHOOD
                } else if ("city" == value) {
                    TravelGraphRegionType.CITY
                } else if ("multi_city_vicinity" == value) {
                    TravelGraphRegionType.MULTI_CITY
                } else if ("airport_metro_code" == value) {
                    TravelGraphRegionType.AIRPORT_METRO_CODE
                } else {
                    TravelGraphRegionType.NONE
                }
            }
        }
    }
}
