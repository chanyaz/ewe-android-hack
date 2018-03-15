package com.expedia.bookings.data.rail

class RailTravelMedium(
    val travelMediumCode: String,
    val travelMediumName: String
) {

    companion object {
        val AIRPORT = "APT"
        val BRANCH_LINE_REGIONAL = "BLR"
        val BUS = "BUS"
        val COMMUTER = "CMT"
        val FERRY = "FRY"
        val HOVERCRAFT = "HOV"
        val HIGH_SPEED = "HSP"
        val INTER_CITY = "ICY"
        const val METRO_CITY_TRANSIT = "MCT"
        val NAMED_TRAIN = "NAT"
        val NIGHT_TRAIN = "NIT"
        val PEDESTRIAN = "PED"
        val PLATFORM_CHANGE = "PFC"
        val SELF_TRANSFER = "STR"
        val TAXI = "TAX"
        val TRAM = "TRM"
        val TRANSFER = "TRS"
        val EXPRESS_BUS = "XBU"
        val UNKNOWN = "UNK"
    }
}
