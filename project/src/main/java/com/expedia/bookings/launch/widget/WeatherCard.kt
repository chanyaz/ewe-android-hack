package com.expedia.bookings.launch.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.hotel.widget.WeatherAdapter
import com.expedia.bookings.hotel.widget.WeatherItem
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView


class WeatherCard(itemView: View, val context: Context) : RecyclerView.ViewHolder(itemView) {
    val firstLine: TextView by bindView(R.id.first_line)
    val weatherRecyclerView: RecyclerView by bindView(R.id.weather_recycler_view)

    init {
        itemView.setOnClickListener {
            NavUtils.goToItin(context)
        }
        bind()
    }

    fun bind() {
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        weatherRecyclerView.setLayoutManager(linearLayoutManager)

        weatherRecyclerView.adapter = WeatherAdapter()
        (weatherRecyclerView.adapter as WeatherAdapter).setWeatherItems(getDummyWeatherItems())
    }

    private fun getDummyWeatherItems(): List<WeatherItem> {
        val weatherItems = ArrayList<WeatherItem>()
        for (i in 0..10) {
            weatherItems.add(WeatherItem())
        }
        return weatherItems
    }
}
