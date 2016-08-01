package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.hotels.HotelSearchResponse.Neighborhood
import com.expedia.bookings.tracking.HotelTracking
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelFilterViewModel
import rx.Observer
import kotlin.properties.Delegates

class HotelsNeighborhoodFilter(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    val neighborhoodName: TextView by bindView(R.id.neighborhood_name)
    val neighborhoodCheckBox: CheckBox by bindView(R.id.neighborhood_check_box)

    var neighborhood: Neighborhood ?= null
    var viewModel: HotelFilterViewModel by Delegates.notNull()

    val checkObserver : Observer<Unit> = endlessObserver {
        neighborhoodCheckBox.isChecked = !neighborhoodCheckBox.isChecked
        viewModel.selectNeighborhood.onNext(neighborhoodName.text.toString())
        if (neighborhoodCheckBox.isChecked) {
            if (viewModel.lob == LineOfBusiness.PACKAGES) {
                PackagesTracking().trackHotelFilterNeighbourhood()
            }
            else if (viewModel.lob == LineOfBusiness.HOTELS) {
                HotelTracking().trackLinkHotelFilterNeighbourhood()
            }
        }
    }

    fun bind(neighborhood: Neighborhood, vm:HotelFilterViewModel) {
        this.viewModel = vm
        this.neighborhood = neighborhood
        neighborhoodName.text = neighborhood.name
        neighborhoodCheckBox.isChecked = false
    }
}

