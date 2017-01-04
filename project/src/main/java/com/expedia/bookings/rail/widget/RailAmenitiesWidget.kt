package com.expedia.bookings.rail.widget

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.PassengerSegmentFare
import com.expedia.bookings.data.rail.responses.RailSegment
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailAmenitiesViewModel
import com.expedia.vm.rail.RailSegmentAmenitiesViewModel

class RailAmenitiesWidget(context: Context) : FrameLayout(context) {
    val container: ViewGroup by bindView(R.id.amenities_container)

    var viewModel: RailAmenitiesViewModel by notNullAndObservable { vm ->
        vm.segmentAmenitiesSubject.subscribe { amenities ->
            container.removeAllViews()
            addAmenityViews(amenities)
        }
    }

    init {
        View.inflate(context, R.layout.rail_amenities_widget, this)
    }

    private fun addAmenityViews(segmentAmenities: List<Pair<RailSegment, PassengerSegmentFare>>) {
        segmentAmenities.forEach { segmentAmenity ->
            if (!segmentAmenity.first.isTransfer) {
                val amenityView = RailSegmentAmenitiesView(context)
                amenityView.viewModel = RailSegmentAmenitiesViewModel(context)
                amenityView.viewModel.segmentAmenitiesObservable.onNext(segmentAmenity)
                container.addView(amenityView)
                View.inflate(context, R.layout.grey_divider_bar, container)
            }
        }
    }
}
