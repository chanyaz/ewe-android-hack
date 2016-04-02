package com.expedia.bookings.widget.traveler

import android.content.Context
import android.util.AttributeSet
import android.view.View

import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.vm.traveler.CheckoutTravelerViewModel
import com.expedia.vm.traveler.TravelerViewModel
import rx.subjects.PublishSubject

class TravelerSelectState(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val travelerSelectedSubject = PublishSubject.create<TravelerViewModel>()

    lateinit var viewModel: CheckoutTravelerViewModel

    init {
        orientation = VERTICAL
    }

    fun refresh() {
        removeAllViews()
        val travelerList = viewModel.getTravelers();
        for (i in 1..travelerList.size) {
            val travelerViewModel = TravelerViewModel(context, i)
            travelerViewModel.travelerObservable.onNext(viewModel.getTraveler(i-1))
            val travelerSelectItem = TravelerSelectItem(context, travelerViewModel)
            travelerViewModel.emptyTravelerObservable.onNext(true)
            travelerSelectItem.setOnClickListener {
                travelerSelectedSubject.onNext(travelerViewModel)
            }
            addView(travelerSelectItem)
            if (i != travelerList.size) {
                View.inflate(context, R.layout.traveler_divider_bar, this)
            }
        }
    }
}