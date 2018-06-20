package com.expedia.bookings.flights.vm

import android.support.annotation.StringRes
import com.expedia.bookings.R
import io.reactivex.subjects.PublishSubject

enum class AdvanceSearchFilter(var isChecked: Boolean, @StringRes val resId: Int) {
    NonStop(false, R.string.nonstop_flight_filter_label),
    Refundable(false, R.string.refundable_flight_filter_label)
}

class FlightAdvanceSearchViewModel {
    var isAdvanceSearchFilterSelected = false
    var applySelectedFilter = PublishSubject.create<Int>()
    val selectAdvancedSearch = PublishSubject.create<AdvanceSearchFilter>()

    init {
        selectAdvancedSearch.subscribe {
            isAdvanceSearchFilterSelected = AdvanceSearchFilter.NonStop.isChecked || AdvanceSearchFilter.Refundable.isChecked
        }
    }
}
