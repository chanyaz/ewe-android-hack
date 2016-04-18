package com.expedia.bookings.presenter.rail

import android.content.Context
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.RailCalendarWidget
import com.expedia.bookings.widget.RailSearchLocationWidget
import com.expedia.vm.rail.RailSearchViewModel

class RailSearchWidget : FrameLayout {

    val locationWidget: RailSearchLocationWidget by bindView(R.id.locationCard)
    val calendarWidget: RailCalendarWidget by bindView(R.id.calendar_card)
    val searchViewModel: RailSearchViewModel

    //creating programmatically, don't need the other ctors
    constructor(context: Context?, searchViewModel: RailSearchViewModel) : super(context) {
        LayoutInflater.from(context).inflate(R.layout.widget_rail_search_content, this)
        this.searchViewModel = searchViewModel
        calendarWidget.viewModel = searchViewModel
        locationWidget.viewModel = searchViewModel
        calendarWidget.setOnClickListener {
            calendarWidget.showCalendarDialog()
        }
    }
}

