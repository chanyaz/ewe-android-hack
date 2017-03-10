package com.expedia.vm.hotel

import android.content.Context

class HotelServerFilterViewModel(context: Context) : BaseHotelFilterViewModel(context) {

    init {
        doneButtonEnableObservable.onNext(true)
        doneObservable.subscribe {
            val filterCount = userFilterChoices.filterCount()
            filterCountObservable.onNext(userFilterChoices.filterCount())
            if (filterCount != 0) {
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