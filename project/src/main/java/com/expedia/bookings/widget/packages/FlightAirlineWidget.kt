package com.expedia.bookings.widget.packages

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.packages.Airline
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView

public class FlightAirlineWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    fun update(airlines: List<Airline>) {
        removeAllViews()
        for (i in 0..airlines.size - 1) {
            val airlineView = AirlineView(context, null)
            airlineView.bind(airlines[i])
            addView(airlineView)
        }
    }

    class AirlineView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
        val airlineName: TextView by bindView(R.id.airline_text_view)
        val airlineLogoImageView: ImageView by bindView(R.id.airline_logo_image_view)

        init {
            View.inflate(context, R.layout.section_airline_logo_name_row, this)
            orientation = HORIZONTAL
            setGravity(Gravity.CENTER_VERTICAL)
        }

        public fun bind(airline: Airline) {
            airlineName.text = airline.airlineName
            PicassoHelper.Builder(airlineLogoImageView)
                    .build()
                    .load(airline.airlineLogoUrl)
        }
    }
}