package com.expedia.bookings.hotel.widget

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.expedia.bookings.R
import com.expedia.vm.WeatherItemViewModel

class WeatherAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var weatherItems: List<WeatherItem> = emptyList()

    fun setWeatherItems(weatherItems: List<WeatherItem>) {
        this.weatherItems = weatherItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return weatherItems.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.weather_item, parent, false)
        val vm = WeatherItemViewModel()
        return WeatherViewHolder(view as ViewGroup, vm)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder) {
            is WeatherViewHolder -> {
                val weatherItem = weatherItems[position]
                holder.bind(weatherItem)
            }
        }
    }

    override fun getFilter(): Filter? {
        return filter
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}

class WeatherItem {
    val temperature = "100"
    val day = "Today or Tomorrow"
}
