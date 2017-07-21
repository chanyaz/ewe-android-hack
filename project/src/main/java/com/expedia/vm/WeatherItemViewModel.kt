package com.expedia.vm

import com.expedia.bookings.R
import com.expedia.bookings.hotel.widget.WeatherItem

class WeatherItemViewModel() {
    private lateinit var weatherItem: WeatherItem
    private var iconDrawableRes: Int = R.drawable.blue_pwp_icon

    fun bind(weatherItem: WeatherItem) {
        this.weatherItem = weatherItem
        iconDrawableRes = R.drawable.blue_pwp_icon
    }

    fun getIcon(): Int {
        return iconDrawableRes
    }

    fun getTitle(): String {
        return weatherItem.day
    }

    fun getTemperature(): String {
        return weatherItem.temperature
    }

}
