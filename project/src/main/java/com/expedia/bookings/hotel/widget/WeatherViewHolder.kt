package com.expedia.bookings.hotel.widget

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.vm.WeatherItemViewModel

class WeatherViewHolder(val root: ViewGroup, private val vm: WeatherItemViewModel) : RecyclerView.ViewHolder(root) {
    val title: TextView by root.bindView(R.id.weather_day_name)
    val temp: TextView by root.bindView(R.id.weather_temp)
    val icon: ImageView by root.bindView(R.id.weather_imageview)

    fun bind(weatherItem: WeatherItem) {
        vm.bind(weatherItem)
        title.text = vm.getTitle()
        icon.setImageResource(vm.getIcon())
        temp.text = vm.getTemp()
    }
}