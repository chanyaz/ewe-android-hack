package com.expedia.bookings.data.travelgraph

class TravelGraphHotelSearchInfo {
    var numberOfRooms: Int = 0
    var numberOfTravelers: Int = 0
    var searchRegion: TravelGraphSearchRegion? = null
    var allRegions: List<TravelGraphSearchRegion>? = emptyList()
    var roomList: List<TravelGraphHotelRoom>? = emptyList()

    inner class TravelGraphSearchRegion {
        var id: String? = null
        var type: String? = null
        var name: String? = null
        var shortName: String? = null
        var imageURL: String? = null

        val regionType: TravelGraphRegionType
            get() = TravelGraphRegionType.toEnum(type)
    }

    inner class TravelGraphHotelRoom {
        var roomOccupants: TravelGraphTravelerDetails? = null
    }

    inner class TravelGraphTravelerDetails {
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