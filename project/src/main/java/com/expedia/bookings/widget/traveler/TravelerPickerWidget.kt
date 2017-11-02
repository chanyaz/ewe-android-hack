package com.expedia.bookings.widget.traveler

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.subscribeObserver
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.vm.traveler.TravelerPickerWidgetViewModel
import com.expedia.vm.traveler.TravelerSelectItemViewModel
import io.reactivex.disposables.CompositeDisposable

class TravelerPickerWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val mainTravelerMinAgeTextView: TextView by bindView(R.id.bottom_main_traveler_min_age_message)
    private val mainTravelerContainer: LinearLayout by bindView(R.id.main_traveler_container)
    private val addTravelersContainer: LinearLayout by bindView(R.id.additional_traveler_container)
    private var travelerSelectItemRefreshSubscription = CompositeDisposable()

    init {
        View.inflate(context, R.layout.traveler_picker_widget, this)
        orientation = VERTICAL
    }

    val viewModel = TravelerPickerWidgetViewModel()

    fun refresh(travelerList: List<Traveler>) {
        travelerSelectItemRefreshSubscription.dispose()
        mainTravelerContainer.removeAllViews()
        addTravelersContainer.removeAllViews()
        travelerList.forEachIndexed { i, traveler ->
            val travelerCategory = traveler.passengerCategory ?: PassengerCategory.ADULT
            val travelerSelectItemViewModel = TravelerSelectItemViewModel(context, i, traveler.searchedAge, travelerCategory)
            val passportSubscription = viewModel.passportRequired.subscribeObserver(travelerSelectItemViewModel.passportRequired)
            val subscription = viewModel.refreshStatusObservable.subscribeObserver(travelerSelectItemViewModel.refreshStatusObservable)
            travelerSelectItemViewModel.passportRequired.onNext(viewModel.passportRequired.value)
            travelerSelectItemRefreshSubscription.add(subscription)
            travelerSelectItemRefreshSubscription.add(passportSubscription)
            travelerSelectItemViewModel.currentStatusObservable.subscribe(viewModel.currentlySelectedTravelerStatusObservable)
            val travelerSelectItem = TravelerSelectItem(context, travelerSelectItemViewModel)
            travelerSelectItem.setOnClickListener {
                viewModel.selectedTravelerSubject.onNext(travelerSelectItemViewModel)
            }
            val parent = if (i == 0) mainTravelerContainer else addTravelersContainer
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
