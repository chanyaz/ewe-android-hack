package com.expedia.bookings.widget.packages

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView

class FlightAirlineWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    fun update(airlines: List<Airline>, isEarnMessageVisible: Boolean) {
        if ((airlines.size > 2 && isEarnMessageVisible)
                || airlines.size > 3) {
            addAirlineViewWithMultipleCarriersImage()
        } else {
            removeAllViews()
            for (airline in airlines) {
                val airlineView = AirlineView(context, null)
                airlineView.bind(airline)
                addView(airlineView)
            }
        }
    }

    fun addAirlineViewWithMultipleCarriersImage() {
        removeAllViews()
        val airlineView = AirlineView(context, null)
        airlineView.bind(Airline(context.getString(R.string.multiple_carriers_text), null))
        addView(airlineView)
    }

    class AirlineView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
        val airlineName: TextView by bindView(R.id.airline_text_view)
        val airlineLogoImageView: ImageView by bindView(R.id.airline_logo_image_view)

        init {
            View.inflate(context, R.layout.section_airline_logo_name_row, this)
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        fun bind(airline: Airline) {
            airlineName.text = airline.airlineName
            if (airline.airlineName.equals(context.getString(R.string.multiple_carriers_text))) {
                PicassoHelper.Builder(airlineLogoImageView)
                        .build()
                        .load(R.drawable.multi_carrier_icon)
            } else if (airline.airlineLogoUrl != null) {
                PicassoHelper.Builder(airlineLogoImageView)
                        .setPlaceholder(R.drawable.ic_airline_backup)
                        .build()
                        .load(airline.airlineLogoUrl)
            } else {
                PicassoHelper.Builder(airlineLogoImageView)
                        .build()
                        .load(R.drawable.ic_airline_backup)
            }
        }
    }
}
