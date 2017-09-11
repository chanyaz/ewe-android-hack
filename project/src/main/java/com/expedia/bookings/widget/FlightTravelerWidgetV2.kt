package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.expedia.bookings.R
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.utils.Strings
import com.expedia.util.subscribeText
import com.expedia.vm.FlightTravelerPickerViewModel
import rx.subjects.BehaviorSubject

class FlightTravelerWidgetV2(context: Context, attrs: AttributeSet?) : TravelerWidgetV2(context, attrs) {

    var oldInfantPreferenceInLap :Boolean = true

    override val traveler: FlightTravelerPickerView by lazy {
        val travelerView = travelerDialogView.findViewById<FlightTravelerPickerView>(R.id.flight_traveler_view)
        travelerView.viewmodel = FlightTravelerPickerViewModel(context)
        travelerView.viewmodel.travelerParamsObservable.subscribe(travelersSubject)
        travelerView.viewmodel.guestsTextObservable.subscribeText(this)
        travelerView.viewmodel.showInfantErrorMessage.map { tooManyInfants ->
            Strings.isEmpty(tooManyInfants)
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

    override  val travelerDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context, R.style.Theme_AlertDialog)
        traveler
        builder.setView(travelerDialogView)
        builder.setPositiveButton(context.getString(R.string.DONE), { dialog, which ->
            oldTravelerData = null
            dialog.dismiss()
        })
        val dialog: AlertDialog = builder.create()
        dialog.setOnShowListener {
            oldTravelerData = traveler.getViewModel().travelerParamsObservable.value
            oldInfantPreferenceInLap = traveler.getViewModel().isInfantInLapObservable.value
            dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        }
        dialog.setOnDismissListener {
            if (oldTravelerData != null) {
                //if it's not null, the user dismissed the dialog, otherwise we clear it on Done
                traveler.getViewModel().travelerParamsObservable.onNext(oldTravelerData)
                traveler.getViewModel().infantInSeatObservable.onNext(!oldInfantPreferenceInLap)
                oldTravelerData = null
            }
            this.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER)
        }
        dialog
    }

}