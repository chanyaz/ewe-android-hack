package com.expedia.bookings.data

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.expedia.bookings.R

enum class LobInfo(val lineOfBusiness: LineOfBusiness, @StringRes val labelRes: Int, @DrawableRes val iconRes: Int) {
    HOTELS(LineOfBusiness.HOTELS, R.string.nav_hotels, R.drawable.ic_lob_hotels),
    FLIGHTS(LineOfBusiness.FLIGHTS, R.string.flights_title, R.drawable.ic_lob_flights),
    CARS(LineOfBusiness.CARS, R.string.nav_car_rentals, R.drawable.ic_lob_cars),
    ACTIVITIES(LineOfBusiness.LX, R.string.nav_things_to_do, R.drawable.ic_lob_lx),
    PACKAGES(LineOfBusiness.PACKAGES, R.string.nav_packages, R.drawable.ic_lob_packages),
    RAILS(LineOfBusiness.RAILS, R.string.nav_rail, R.drawable.ic_lob_rail);

    companion object {
        @ColorRes
        val disabledColorRes = R.color.launch_lob_disabled_color
    }
}
