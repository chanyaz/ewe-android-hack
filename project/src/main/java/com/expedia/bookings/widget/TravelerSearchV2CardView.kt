package com.expedia.bookings.widget

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.util.subscribeText
import com.expedia.vm.HotelSearchViewModel
import com.expedia.vm.HotelTravelerPickerViewModel
import rx.subjects.BehaviorSubject

public class TravelerSearchV2CardView(context: Context, attrs: AttributeSet?) : SearchInputCardView(context, attrs) {

    val hotelSearchViewModelSubject = BehaviorSubject.create<HotelSearchViewModel>()

    val dialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        val li = LayoutInflater.from(context);
        val myView = li.inflate(R.layout.widget_hotel_traveler_search, null)
        builder.setView(myView)

        val traveler = myView.findViewById(R.id.traveler_view) as HotelTravelerPickerView
        traveler.viewmodel = HotelTravelerPickerViewModel(context, false)
        traveler.viewmodel.travelerParamsObservable.subscribe(hotelSearchViewModelSubject.value.travelersObserver)
        traveler.viewmodel.guestsTextObservable.subscribeText(this.text)

        builder.setTitle(R.string.select_traveler_title)
        builder.setPositiveButton(context.getString(R.string.DONE), { dialog, which ->
            dialog.dismiss()

        })
        builder.setNegativeButton(context.getString(R.string.cancel), { dialog, which -> dialog.dismiss() })
        builder.create()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    init {
        this.setOnClickListener {
            dialog.show()
        }
    }


}