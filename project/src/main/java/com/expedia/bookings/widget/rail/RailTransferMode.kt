package com.expedia.bookings.widget.rail
import com.expedia.bookings.R

enum class RailTransferMode(val iconKey: String, val iconValue: Int) {
    BUS("Bus", R.drawable.rails_bus_icon),
    FERRY("Ferry", R.drawable.rails_ferry_icon),
    HOVERCRAFT("Hovercraft", R.drawable.rails_hovercraft_icon),
    SUBWAY("Subway", R.drawable.rails_subway_icon),
    TRAIN("Train", R.drawable.rails_train_icon),
    TRAM("Tram", R.drawable.rails_tram_icon),
    TUBE("Tube", R.drawable.rails_tube_icon),
    WALK("Walk", R.drawable.rails_walk_icon),
    TRANSFER("Transfer", R.drawable.rails_transfer_icon);

    companion object {
        fun findMappedDrawable (travelMode: String): Int {
            when (travelMode) {
                RailTransferMode.BUS.iconKey -> return RailTransferMode.BUS.iconValue
                RailTransferMode.FERRY.iconKey -> return RailTransferMode.FERRY.iconValue
                RailTransferMode.HOVERCRAFT.iconKey -> return RailTransferMode.HOVERCRAFT.iconValue
                RailTransferMode.SUBWAY.iconKey -> return RailTransferMode.SUBWAY.iconValue
                RailTransferMode.TRAIN.iconKey -> return RailTransferMode.TRAIN.iconValue
                RailTransferMode.TRAM.iconKey -> return RailTransferMode.TRAM.iconValue
                RailTransferMode.TUBE.iconKey -> return RailTransferMode.TUBE.iconValue
                RailTransferMode.WALK.iconKey -> return RailTransferMode.WALK.iconValue
                else -> {
                    return return RailTransferMode.TRANSFER.iconValue
                }
            }
        }
    }
}
