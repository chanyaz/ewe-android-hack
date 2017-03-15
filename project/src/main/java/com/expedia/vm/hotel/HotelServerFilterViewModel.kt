package com.expedia.vm.hotel

import android.content.Context

class HotelServerFilterViewModel(context: Context) : BaseHotelFilterViewModel(context) {

    init {
        doneButtonEnableObservable.onNext(true)
        doneObservable.subscribe {
            filterCountObservable.onNext(userFilterChoices.filterCount())
            if (!defaultFilterOptions()) {
                filterByParamsObservable.onNext(userFilterChoices)
            } else {
                filterObservable.onNext(originalResponse)
            }
        }
    }

    override fun isClientSideFiltering(): Boolean {
        return false
    }

    override fun showHotelFavorite(): Boolean {
        return false
    }
}