package com.expedia.bookings.widget.rail

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeInverseVisibility
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.rail.RailSegmentAmenitiesViewModel

class RailSegmentAmenitiesView(context: Context) : FrameLayout(context) {
    val stationInfo: TextView by bindView(R.id.rail_station_info)
    val fareInfo: TextView by bindView(R.id.fare_info)
    val noAmenities: TextView by bindView(R.id.no_amenities)
    val amenitiesText : TextView by bindView(R.id.amenities_text)

    var viewModel: RailSegmentAmenitiesViewModel by notNullAndObservable { vm ->
        vm.stationDescriptionObservable.subscribeText(stationInfo)
        vm.fareInfoObservable.subscribeText(fareInfo)
        vm.noAmenitiesObservable.subscribeVisibility(noAmenities)
        vm.noAmenitiesObservable.subscribeInverseVisibility(amenitiesText)
        vm.formattedAmenitiesObservable.subscribe { formattedAmenitiesString ->
            amenitiesText.text = HtmlCompat.fromHtml(formattedAmenitiesString)
        }
    }
    init {
        View.inflate(context, R.layout.rail_amenity_view, this)
    }
}