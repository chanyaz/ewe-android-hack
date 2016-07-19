package com.expedia.bookings.widget.traveler

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.utils.bindView
import com.expedia.vm.traveler.TravelerSelectViewModel
import rx.subjects.PublishSubject
import java.util.ArrayList

class TravelerSelectState(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val mainTravelerContainer: LinearLayout by bindView(R.id.main_traveler_container)
    val addTravelersContainer: LinearLayout by bindView(R.id.additional_traveler_container)

    val travelerIndexSelectedSubject = PublishSubject.create<Pair<Int, String>>()
    val viewModelList = ArrayList<TravelerSelectViewModel>()

    init {
        View.inflate(context, R.layout.traveler_select_state, this)
        orientation = VERTICAL
    }

    fun refresh(status: TravelerCheckoutStatus, travelerList: List<Traveler>) {
        mainTravelerContainer.removeAllViews()
        addTravelersContainer.removeAllViews()
        viewModelList.clear()
        travelerList.forEachIndexed { i, traveler ->
            val travelerViewModel = TravelerSelectViewModel(context, i, traveler.searchedAge)
            travelerViewModel.updateStatus(status)
            viewModelList.add(travelerViewModel)

            val travelerSelectItem = TravelerSelectItem(context, travelerViewModel)
            travelerSelectItem.setOnClickListener {
                travelerIndexSelectedSubject.onNext(Pair(i, travelerViewModel.emptyText))
                travelerViewModel.status = TravelerCheckoutStatus.DIRTY
            }
            val parent = if (i==0) mainTravelerContainer else addTravelersContainer
            parent.addView(travelerSelectItem)

            if (i != travelerList.size - 1 && i != 0) {
                View.inflate(context, R.layout.grey_divider_bar, parent)
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