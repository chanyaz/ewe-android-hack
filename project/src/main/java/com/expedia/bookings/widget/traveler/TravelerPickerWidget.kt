package com.expedia.bookings.widget.traveler

import android.content.Context
import android.util.AttributeSet
import android.util.Pair
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.vm.traveler.TravelerPickerTravelerViewModel
import rx.subjects.BehaviorSubject
import java.util.ArrayList

class TravelerPickerWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val mainTravelerMinAgeTextView: TextView by bindView(R.id.bottom_main_traveler_min_age_message)
    private val mainTravelerContainer: LinearLayout by bindView(R.id.main_traveler_container)
    private val addTravelersContainer: LinearLayout by bindView(R.id.additional_traveler_container)

    val travelerIndexSelectedSubject = BehaviorSubject.create<Pair<Int, String>>()
    val travelerViewModels = ArrayList<TravelerPickerTravelerViewModel>()
    var passportRequired = BehaviorSubject.create<Boolean>(false)

    init {
        View.inflate(context, R.layout.traveler_picker_widget, this)
        orientation = VERTICAL
    }

    fun refresh(travelerList: List<Traveler>) {
        mainTravelerContainer.removeAllViews()
        addTravelersContainer.removeAllViews()
        travelerViewModels.clear()
        travelerList.forEachIndexed { i, traveler ->
            val travelerViewModel = TravelerPickerTravelerViewModel(context, i, traveler.searchedAge)
            travelerViewModel.isPassportRequired = passportRequired.value
            travelerViewModel.updateStatus(TravelerCheckoutStatus.CLEAN)
            travelerViewModels.add(travelerViewModel)

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
    }

}
