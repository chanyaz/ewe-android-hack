package com.expedia.bookings.itin.triplist.upcoming

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R

class TripListTabView(context: Context, attributeSet: AttributeSet?) : LinearLayout(context, attributeSet) {

    var viewModel: ITripListTabViewModel

    init {
        View.inflate(context, R.layout.trip_folders_trip_list_tab, this)
        viewModel = TripListTabViewModel()
    }
}
