package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.util.subscribeText
import com.expedia.vm.RailTravelerPickerViewModel

class RailTravelerWidgetV2(context: Context, attrs: AttributeSet?) : TravelerWidgetV2(context, attrs) {

    override val traveler: RailTravelerPickerView by lazy {
        val travelerView = travelerDialogView.findViewById(R.id.rail_traveler_view) as RailTravelerPickerView
        travelerView.viewModel = RailTravelerPickerViewModel(context)
        travelerView.viewModel.travelerParamsObservable.subscribe(travelersSubject)
        travelerView.viewModel.guestsTextObservable.subscribeText(this)
        travelerView
    }

    override val travelerDialogView: View by lazy {
        val view = LayoutInflater.from(context).inflate(R.layout.widget_rail_traveler_search, null)
        view
    }

}