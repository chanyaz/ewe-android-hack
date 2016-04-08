package com.expedia.bookings.widget.traveler

import android.content.Context
import android.util.AttributeSet
import android.view.View

import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.vm.traveler.TravelerSelectViewModel
import rx.subjects.PublishSubject
import java.util.ArrayList

class TravelerSelectState(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val travelerIndexSelectedSubject = PublishSubject.create<Int>()
    val viewModelList = ArrayList<TravelerSelectViewModel>()

    init {
        orientation = VERTICAL
    }

    fun refresh(status: TravelerCheckoutStatus, travelerList: List<Traveler>) {
        removeAllViews()
        viewModelList.clear()
        for (i in 1..travelerList.size) {

            val travelerViewModel = TravelerSelectViewModel(context, i - 1)
            travelerViewModel.updateStatus(status)
            viewModelList.add(travelerViewModel)

            val travelerSelectItem = TravelerSelectItem(context, travelerViewModel)
            travelerSelectItem.setOnClickListener {
                travelerIndexSelectedSubject.onNext(i - 1)
                travelerViewModel.status = TravelerCheckoutStatus.DIRTY
            }
            addView(travelerSelectItem)

            if (i != travelerList.size) {
                View.inflate(context, R.layout.traveler_divider_bar, this)
            }
        }
    }

    fun show() {
        visibility = View.VISIBLE
        refreshViewModels()
    }

    private fun refreshViewModels() {
        // Currently no way to tell which traveler changed update all viewModels to be safe.
        for (viewModel in viewModelList) {
            viewModel.updateStatus(viewModel.status)
        }
    }

}