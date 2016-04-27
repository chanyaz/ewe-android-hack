package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TravelerParams
import com.expedia.util.subscribeText
import com.expedia.vm.TravelerPickerViewModel
import rx.subjects.BehaviorSubject

class TravelerWidgetV2(context: Context, attrs: AttributeSet?) : SearchInputCardView(context, attrs) {
    var oldTravelerData: TravelerParams? = null;
    val travelersSubject = BehaviorSubject.create<TravelerParams>()
    val travelerDialogView: View by lazy {
        val view = LayoutInflater.from(context).inflate(R.layout.widget_hotel_traveler_search, null)
        view
    }

    val traveler: HotelTravelerPickerView by lazy {
        val travelerView = travelerDialogView.findViewById(R.id.traveler_view) as HotelTravelerPickerView
        travelerView.viewmodel = TravelerPickerViewModel(context)
        travelerView.viewmodel.travelerParamsObservable.subscribe(travelersSubject)
        travelerView.viewmodel.guestsTextObservable.subscribeText(this.text)
        travelerView.viewmodel.tooManyInfants.subscribe { tooManyInfants ->
            travelerDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = !tooManyInfants
        }
        travelerView
    }

    val travelerDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context, R.style.Theme_AlertDialog)
        traveler
        builder.setView(travelerDialogView)
        builder.setPositiveButton(context.getString(R.string.DONE), { dialog, which ->
            oldTravelerData = null
            dialog.dismiss()
        })
        val dialog: AlertDialog = builder.create()
        dialog.setOnShowListener {
            oldTravelerData = traveler.viewmodel.travelerParamsObservable.value
            dialog.getWindow()?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        }
        dialog.setOnDismissListener {
            if (oldTravelerData != null) {
                //if it's not null, the user dismissed the dialog, otherwise we clear it on Done
                traveler.viewmodel.travelerParamsObservable.onNext(oldTravelerData)
                oldTravelerData = null
            }
        }
        dialog
    }

}