package com.expedia.bookings.data

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.expedia.bookings.R

enum class LobInfo(val lineOfBusiness: LineOfBusiness, @StringRes val labelRes: Int, @DrawableRes val iconRes: Int, @ColorRes val colorRes: Int) {
    HOTELS(LineOfBusiness.HOTELS, R.string.nav_hotels, R.drawable.ic_lob_hotels, R.color.new_launch_hotels_lob_color),
    FLIGHTS(LineOfBusiness.FLIGHTS, R.string.flights_title, R.drawable.ic_lob_flights, R.color.new_launch_flights_lob_color),
    CARS(LineOfBusiness.CARS, R.string.nav_car_rentals, R.drawable.ic_lob_cars, R.color.new_launch_cars_lob_color),
    ACTIVITIES(LineOfBusiness.LX, R.string.nav_things_to_do, R.drawable.ic_lob_lx, R.color.new_launch_lx_lob_color),
    TRANSPORT(LineOfBusiness.TRANSPORT, R.string.nav_transport, R.drawable.ic_lob_gt, R.color.new_launch_gt_lob_color),
    PACKAGES(LineOfBusiness.PACKAGES, R.string.nav_packages, R.drawable.ic_lob_packages, R.color.new_launch_packages_lob_color),
    RAILS(LineOfBusiness.RAILS, R.string.nav_rail, R.drawable.ic_lob_rail, R.color.new_launch_rail_lob_color);

    companion object {
        @ColorRes
        val disabledColorRes = R.color.new_launch_lob_disabled_color
    }
}
