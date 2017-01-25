package com.expedia.bookings.widget.itin

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.vm.itin.WeatherForecastViewModel

/**
 * Created by Supreeth on 1/24/17.
 */
class WeatherForecastHolder(val root: ViewGroup, val vm: WeatherForecastViewModel) : RecyclerView.ViewHolder(root) {

    val maxTemp: TextView by root.bindView(R.id.forecast_max_temp)
    val minTemp: TextView by root.bindView(R.id.forecast_min_temp)

    init {
        vm.maxTempObservable.subscribe { temp ->
            maxTemp.setText(temp.fahrenheit)
        }

        vm.minTempObservable.subscribe { temp ->
            minTemp.setText(temp.fahrenheit)
        }
    }

}