package com.expedia.vm.hotel

import android.content.Context
import com.expedia.bookings.data.hotel.UserFilterChoices

class HotelServerFilterViewModel(context: Context) : BaseHotelFilterViewModel(context) {
    private var previousFilterChoices: UserFilterChoices? = null

    init {
        doneButtonEnableObservable.onNext(true)
        doneObservable.subscribe {
            filterCountObservable.onNext(userFilterChoices.filterCount())
            if (defaultFilterOptions()) {
                filterObservable.onNext(originalResponse)
            } else if (sameFilterOptions()) {
                showPreviousResultsObservable.onNext(Unit)
            } else {
                filterByParamsObservable.onNext(userFilterChoices)
            }
            previousFilterChoices = userFilterChoices.copy()
        }

        clearObservable.subscribe {
            previousFilterChoices = null
        }
    }

    override fun isClientSideFiltering(): Boolean {
        return false
    }

    private fun sameFilterOptions(): Boolean {
        if (previousFilterChoices != null) {
            return userFilterChoices == previousFilterChoices
        }
        return false
    }
}