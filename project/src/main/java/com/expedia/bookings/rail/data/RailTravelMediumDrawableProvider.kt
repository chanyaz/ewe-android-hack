package com.expedia.bookings.rail.data

import com.expedia.bookings.R
import com.expedia.bookings.data.rail.RailTravelMedium

object RailTravelMediumDrawableProvider {
    fun findMappedDrawable(travelMediumCode: String): Int {
        when (travelMediumCode) {
            RailTravelMedium.BRANCH_LINE_REGIONAL,
            RailTravelMedium.HIGH_SPEED,
            RailTravelMedium.INTER_CITY,
            RailTravelMedium.NAMED_TRAIN,
            RailTravelMedium.NIGHT_TRAIN ->
                return R.drawable.rails_train_icon

            RailTravelMedium.COMMUTER,
            RailTravelMedium.METRO_CITY_TRANSIT ->
                return R.drawable.rails_subway_icon

            RailTravelMedium.FERRY ->
                return R.drawable.rails_ferry_icon

            RailTravelMedium.BUS,
            RailTravelMedium.EXPRESS_BUS ->
                return R.drawable.rails_bus_icon

            RailTravelMedium.HOVERCRAFT ->
                return R.drawable.rails_hovercraft_icon

            RailTravelMedium.PEDESTRIAN ->
                return R.drawable.rails_walk_icon

            RailTravelMedium.TRAM ->
                return R.drawable.rails_tram_icon

            RailTravelMedium.PLATFORM_CHANGE,
            RailTravelMedium.SELF_TRANSFER,
            RailTravelMedium.TRANSFER ->
                return R.drawable.rails_transfer_icon

            else ->
                return R.drawable.rails_transfer_icon
        }
    }
}
