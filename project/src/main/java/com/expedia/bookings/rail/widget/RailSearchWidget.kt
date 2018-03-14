package com.expedia.bookings.rail.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.CalendarWidgetWithTimeSlider
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailSearchViewModel

class RailSearchWidget(context: Context, attr: AttributeSet?) : FrameLayout(context, attr) {

    val locationWidget: RailSearchLocationWidget by bindView(R.id.locationCard)
    val calendarWidget: CalendarWidgetWithTimeSlider by bindView(R.id.calendar_card)
    val travelerWidget: RailTravelerWidgetV2 by bindView(R.id.traveler_card)
    val cardPickerWidget: RailCardsPickerWidget by bindView(R.id.cards_picker)

    var searchViewModel by notNullAndObservable<RailSearchViewModel> { vm ->
        calendarWidget.viewModel = vm
        locationWidget.viewModel = vm

        vm.dateAccessibilityObservable.subscribe { text ->
            calendarWidget.contentDescription = text
        }
    }

    init {
        View.inflate(context, R.layout.widget_rail_search_content, this)
        calendarWidget.setOnClickListener {
            calendarWidget.showCalendarDialog()
        }

        cardPickerWidget.railCardPickerViewModel.cardsListForSearchParams.subscribe { railCards ->
            searchViewModel.getParamsBuilder().fareQualifierList(railCards)
        }

        travelerWidget.traveler.getViewModel().travelerParamsObservable
                .map { travelerParams -> travelerParams.getTravelerCount() }
                .subscribe(cardPickerWidget.railCardPickerViewModel.numberOfTravelers)
    }
}
