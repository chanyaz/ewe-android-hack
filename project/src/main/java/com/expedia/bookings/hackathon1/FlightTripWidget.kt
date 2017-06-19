package com.expedia.bookings.hackathon1

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.HotelBreakDownViewModel
import com.expedia.vm.HotelBreakDownViewModel.BreakdownItem

class FlightTripWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val logo: ImageView by bindView(R.id.airline_logo_image_view)
    val outboundTextView: TextView by bindView(R.id.outbound_text_view)
//    val outboundTextView: TextView by bindView(R.id.outbound_text_view)
//    val logo: ImageView by bindView(R.id.airline_logo_image_view)


    init {
        //View.inflate(getContext(), R.layout.flight_trip_view, this)
    }
}