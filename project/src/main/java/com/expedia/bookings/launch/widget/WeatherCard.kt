package com.expedia.bookings.launch.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.vm.launch.ActiveItinViewModel
import com.expedia.vm.launch.ActiveWeatherViewModel

class WeatherCard(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) {
    val firstLine: TextView by bindView(R.id.first_line)

    init {
        itemView.setOnClickListener {
            NavUtils.goToItin(context)
            OmnitureTracking.trackLaunchActiveItin()

        }
    }

    fun bind(context: Context, vm: ActiveWeatherViewModel) {
        firstLine.text = vm.firstLine
    }
}
