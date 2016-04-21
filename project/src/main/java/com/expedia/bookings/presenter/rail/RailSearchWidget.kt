package com.expedia.bookings.presenter.rail

import android.content.Context
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.RailCalendarWidget
import com.expedia.bookings.widget.RailSearchLocationWidget
import com.expedia.bookings.widget.TravelerWidgetV2
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailSearchViewModel

class RailSearchWidget : FrameLayout {

    val locationWidget: RailSearchLocationWidget by bindView(R.id.locationCard)
    val calendarWidget: RailCalendarWidget by bindView(R.id.calendar_card)
    val travelerWidget: TravelerWidgetV2 by bindView(R.id.traveler_card)

    var searchViewModel by notNullAndObservable<RailSearchViewModel>() {
        calendarWidget.viewModel = it
        locationWidget.viewModel = it
    }

    //creating programmatically, don't need the other ctors
    constructor(context: Context?) : super(context) {
        LayoutInflater.from(context).inflate(R.layout.widget_rail_search_content, this)
        calendarWidget.setOnClickListener {
            calendarWidget.showCalendarDialog()
        }
    }
}

