package com.expedia.bookings.presenter.rail

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.CalendarWidgetWithTimeSlider
import com.expedia.bookings.widget.RailSearchLocationWidget
import com.expedia.bookings.widget.TravelerWidgetV2
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailSearchViewModel

class RailSearchWidget(context: Context, attr: AttributeSet?) : FrameLayout(context, attr) {

    val locationWidget: RailSearchLocationWidget by bindView(R.id.locationCard)
    val calendarWidget: CalendarWidgetWithTimeSlider by bindView(R.id.calendar_card)
    val travelerWidget: TravelerWidgetV2 by bindView(R.id.traveler_card)

    var searchViewModel by notNullAndObservable<RailSearchViewModel>() {
        calendarWidget.viewModel = it
        locationWidget.viewModel = it
    }

    init {
        View.inflate(context, R.layout.widget_rail_search_content, this)
        calendarWidget.setOnClickListener {
            calendarWidget.showCalendarDialog()
        }
    }
}

