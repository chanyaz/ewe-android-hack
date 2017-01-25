package com.expedia.bookings.widget.itin

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.itin.WUndergroundForecastDay
import com.expedia.bookings.widget.suggestions.SuggestionViewHolder
import com.expedia.vm.itin.WeatherForecastViewModel
import com.expedia.vm.packages.SuggestionViewModel
import java.util.*

/**
 * Created by Supreeth on 1/24/17.
 */
class WeatherForecastListAdapter(val viewmodel: WeatherForecastViewModel) : RecyclerView.Adapter<WeatherForecastHolder>() {
    private var forecastDays: ArrayList<WUndergroundForecastDay> = ArrayList()

    fun updateList(forcastList: List<WUndergroundForecastDay>) {
        forecastDays = forcastList as ArrayList<WUndergroundForecastDay>
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return forecastDays.size
    }

    override fun onBindViewHolder(holder: WeatherForecastHolder, position: Int) {
        holder.vm.forecastDayObserver.onNext(forecastDays.get(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherForecastHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.itin_weather_forecast_item, parent, false)
        val vm = WeatherForecastViewModel()
        return makeViewHolder(view as ViewGroup, vm)
    }

    fun makeViewHolder(root: ViewGroup, vm: WeatherForecastViewModel): WeatherForecastHolder {
        return WeatherForecastHolder(root, vm)
    }
}