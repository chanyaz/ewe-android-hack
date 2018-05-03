package com.expedia.vm.hotel

import android.content.Context
import com.expedia.bookings.data.BaseHotelFilterOptions
import com.expedia.bookings.data.hotel.DisplaySort
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.hotels.HotelFilterOptions
import com.expedia.bookings.tracking.hotel.FilterTracker
import com.expedia.bookings.tracking.hotel.HotelFilterTracker
import io.reactivex.subjects.PublishSubject

class HotelFilterViewModel(context: Context) : BaseHotelFilterViewModel(context) {
    //out
    val presetFilterOptionsUpdatedSubject = PublishSubject.create<UserFilterChoices>()

    private var presetFilterOptions = false
    private var previousFilterChoices: UserFilterChoices? = null

    init {
        doneButtonEnableObservable.onNext(true)
        doneObservable.subscribe {
            filterCountObservable.onNext(userFilterChoices.filterCount())
            if (defaultFilterOptions() && !presetFilterOptions) {
                originalResponse?.let {
                    filterObservable.onNext(it)
                }
            } else if (sameFilterOptions()) {
                showPreviousResultsObservable.onNext(Unit)
            } else {
                filterChoicesObservable.onNext(userFilterChoices)
            }
            previousFilterChoices = userFilterChoices.copy()
        }

        clearObservable.subscribe {
            previousFilterChoices = null
        }
    }

    override fun updatePresetOptions(filterOptions: BaseHotelFilterOptions) {
        presetFilterOptions = false
        if (filterOptions.isNotEmpty() && filterOptions is HotelFilterOptions) {
            val filterChoices = UserFilterChoices.fromHotelFilterOptions(filterOptions)
            previousFilterChoices = filterChoices
            presetFilterOptions = true
            presetFilterOptionsUpdatedSubject.onNext(filterChoices)
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

    fun setPreviousFilterChoices(filterChoices: UserFilterChoices) {
        previousFilterChoices = filterChoices
    }

    private fun sameFilterOptions(): Boolean {
        if (previousFilterChoices != null) {
            return userFilterChoices == previousFilterChoices
        }
        return false
    }
}
