package com.expedia.bookings.widget

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.util.subscribeText
import com.expedia.vm.HotelSearchViewModel
import com.expedia.vm.HotelTravelerParams
import com.expedia.vm.HotelTravelerPickerViewModel
import rx.subjects.BehaviorSubject

public class TravelerWidgetV2(context: Context, attrs: AttributeSet?) : SearchInputCardView(context, attrs) {
    var oldTravelerData: HotelTravelerParams? = null;
    val hotelSearchViewModelSubject = BehaviorSubject.create<HotelSearchViewModel>()
    val travelerDialogView: View by lazy {
        val view = LayoutInflater.from(context).inflate(R.layout.widget_hotel_traveler_search, null)
        view
    }

    val traveler: HotelTravelerPickerView by lazy {
        val travelerView = travelerDialogView.findViewById(R.id.traveler_view) as HotelTravelerPickerView
        travelerView.viewmodel = HotelTravelerPickerViewModel(context, false)
        travelerView.viewmodel.travelerParamsObservable.subscribe(hotelSearchViewModelSubject.value.travelersObserver)
        travelerView.viewmodel.guestsTextObservable.subscribeText(this.text)
        travelerView
    }

    val travelerDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        traveler
        builder.setView(travelerDialogView)
        builder.setTitle(R.string.select_traveler_title)
        builder.setPositiveButton(context.getString(R.string.DONE), { dialog, which ->
            oldTravelerData = null
            dialog.dismiss()
        })
        val dialog: AlertDialog = builder.create()
        dialog.setOnShowListener {
            oldTravelerData = traveler.viewmodel.travelerParamsObservable.value
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

    init {
        this.setOnClickListener {
            travelerDialog.show()
        }
    }
}