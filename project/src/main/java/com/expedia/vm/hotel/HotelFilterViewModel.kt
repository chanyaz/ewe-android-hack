package com.expedia.vm.hotel

import android.content.Context
import com.expedia.bookings.data.hotel.DisplaySort
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.tracking.hotel.FilterTracker
import com.expedia.bookings.tracking.hotel.HotelFilterTracker
import io.reactivex.subjects.PublishSubject

class HotelFilterViewModel(context: Context) : BaseHotelFilterViewModel(context) {
    //in
    val searchOptionsUpdatedObservable = PublishSubject.create<UserFilterChoices>()

    private var withSearchOptions = false
    private var previousFilterChoices: UserFilterChoices? = null

    init {
        doneButtonEnableObservable.onNext(true)
        doneObservable.subscribe {
            filterCountObservable.onNext(userFilterChoices.filterCount())
            if (defaultFilterOptions() && !withSearchOptions) {
                originalResponse?.let {
                    filterObservable.onNext(it)
                }
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

        newSearchOptionsObservable.subscribe { searchOptions ->
            withSearchOptions = false
            if (searchOptions.isNotEmpty()) {
                val filterChoices = UserFilterChoices.fromHotelFilterOptions(searchOptions)
                previousFilterChoices = filterChoices
                withSearchOptions = true
                searchOptionsUpdatedObservable.onNext(filterChoices)
            }
        }
    }

    override fun getDefaultSort(): DisplaySort {
        if (isCurrentLocationSearch.value) {
            return DisplaySort.DISTANCE
        }
        return super.getDefaultSort()
    }

    override fun sortItemToRemove(): DisplaySort {
        return DisplaySort.PACKAGE_DISCOUNT
    }

    override fun createFilterTracker(): FilterTracker {
        return HotelFilterTracker()
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
