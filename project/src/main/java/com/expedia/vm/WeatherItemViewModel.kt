package com.expedia.vm

import com.expedia.bookings.R
import com.expedia.bookings.hotel.widget.WeatherItem

class WeatherItemViewModel() {
    private lateinit var weatherItem: WeatherItem
    private var iconDrawableRes: Int = R.drawable.search_type_icon

    fun bind(weatherItem: WeatherItem) {
        this.weatherItem = weatherItem
        iconDrawableRes = R.drawable.search_type_icon
    }

    fun getIcon(): Int {
        return iconDrawableRes
    }

    fun getTitle(): String {
        return weatherItem.day
    }

}
