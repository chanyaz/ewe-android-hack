package com.expedia.vm.hotel

import android.content.Context

class HotelServerFilterViewModel(context: Context) : BaseHotelFilterViewModel(context) {

    init {
        doneButtonEnableObservable.onNext(true)
        doneObservable.subscribe {
            filterCountObservable.onNext(userFilterChoices.filterCount())
            //TODO Handle the case when filter criteria are reset - show original response to prevent searching again
            filterByParamsObservable.onNext(userFilterChoices)
        }
    }

    override fun isClientSideFiltering(): Boolean {
        return false
    }

    override fun showHotelFavorite(): Boolean {
        return false
    }
}