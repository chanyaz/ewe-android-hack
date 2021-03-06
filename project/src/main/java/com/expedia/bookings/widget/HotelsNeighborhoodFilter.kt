package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelSearchResponse.Neighborhood
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelFilterViewModel
import rx.Observer
import kotlin.properties.Delegates

public class HotelsNeighborhoodFilter(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    val neighborhoodName: TextView by bindView(R.id.neighborhood_name)
    val neighborhoodCheckBox: CheckBox by bindView(R.id.neighborhood_check_box)

    var neighborhood: Neighborhood ?= null
    var viewModel: HotelFilterViewModel by Delegates.notNull()

    val checkObserver : Observer<Unit> = endlessObserver {
        neighborhoodCheckBox.setChecked(!neighborhoodCheckBox.isChecked())
        viewModel.selectNeighborhood.onNext(neighborhoodName.getText().toString())
        if (neighborhoodCheckBox.isChecked) HotelV2Tracking().trackLinkHotelV2FilterNeighbourhood()
    }

    public fun bind(neighborhood: Neighborhood, vm:HotelFilterViewModel) {
        this.viewModel = vm
        this.neighborhood = neighborhood
        neighborhoodName.setText(neighborhood.name)
        neighborhoodCheckBox.setChecked(false)
    }
}

