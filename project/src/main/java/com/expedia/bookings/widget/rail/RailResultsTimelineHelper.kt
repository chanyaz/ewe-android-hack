package com.expedia.bookings.widget.rail

object RailResultsTimelineHelper {

    @JvmStatic fun findMappedDrawable(travelMode: String): Int {
        when (travelMode) {
            IconMap.BUS.iconKey -> return IconMap.BUS.iconValue
            IconMap.FERRY.iconKey -> return IconMap.FERRY.iconValue
            IconMap.HOVERCRAFT.iconKey -> return IconMap.HOVERCRAFT.iconValue
            IconMap.SUBWAY.iconKey -> return IconMap.SUBWAY.iconValue
            IconMap.TRAIN.iconKey -> return IconMap.TRAIN.iconValue
            IconMap.TRAM.iconKey -> return IconMap.TRAM.iconValue
            IconMap.TUBE.iconKey -> return IconMap.TUBE.iconValue
            IconMap.WALK.iconKey -> return IconMap.WALK.iconValue
            else -> {
                return return IconMap.TRANSFER.iconValue
            }
        }
    }
}