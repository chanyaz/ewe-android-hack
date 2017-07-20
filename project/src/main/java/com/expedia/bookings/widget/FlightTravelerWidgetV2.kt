package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.Strings
import com.expedia.util.subscribeText
import com.expedia.vm.FlightTravelerPickerViewModel

class FlightTravelerWidgetV2(context: Context, attrs: AttributeSet?) : TravelerWidgetV2(context, attrs) {

    override val traveler: FlightTravelerPickerView by lazy {
        val travelerView = travelerDialogView.findViewById(R.id.flight_traveler_view) as FlightTravelerPickerView
        travelerView.viewmodel = FlightTravelerPickerViewModel(context)
        travelerView.viewmodel.travelerParamsObservable.subscribe(travelersSubject)
        travelerView.viewmodel.guestsTextObservable.subscribeText(this)
        travelerView.viewmodel.showInfantErrorMessage.map { tooManyInfants ->
            if (Strings.isEmpty(tooManyInfants)) true else false
        }.subscribe { enable ->
            travelerDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = enable
            if (!enable) {
                travelerView.infantError.announceForAccessibility(travelerView.infantError.text)
            }
        }
        travelerView
    }

    override val travelerDialogView: View by lazy {
        val view = LayoutInflater.from(context).inflate(R.layout.widget_flight_traveler_search, null)
        view
    }

}