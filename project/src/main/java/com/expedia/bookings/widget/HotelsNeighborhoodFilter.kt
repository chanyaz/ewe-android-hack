package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelSearchResponse.Neighborhood
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.vm.hotel.BaseHotelFilterViewModel
import rx.Observer
import kotlin.properties.Delegates

class HotelsNeighborhoodFilter(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    val neighborhoodName: TextView by bindView(R.id.neighborhood_name)
    val neighborhoodCheckBox: CheckBox by bindView(R.id.neighborhood_check_box)

    var neighborhood: Neighborhood ?= null
    var hotelFilterViewModel: BaseHotelFilterViewModel by Delegates.notNull()

    val checkObserver : Observer<Unit> = endlessObserver {
        neighborhoodCheckBox.isChecked = !neighborhoodCheckBox.isChecked
        hotelFilterViewModel.selectNeighborhood.onNext(neighborhoodName.text.toString())
        if (neighborhoodCheckBox.isChecked) {
            hotelFilterViewModel.trackHotelFilterNeighborhood()
        }
    }

    fun bind(neighborhood: Neighborhood, vm: BaseHotelFilterViewModel) {
        this.hotelFilterViewModel = vm
        this.neighborhood = neighborhood
        neighborhoodName.text = neighborhood.name
        neighborhoodCheckBox.isChecked = false
    }
}

