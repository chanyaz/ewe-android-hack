package com.expedia.bookings.hotel.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase

class PackageHotelResultsViewModel(context: Context) :
        BaseHotelResultsViewModel(context) {

    private var cachedParams: HotelSearchParams? = null

    init {
        paramsSubject.subscribe(endlessObserver { params ->
            doSearch(params)
        })
    }

    private fun doSearch(params: HotelSearchParams) {
        cachedParams = params
        titleSubject.onNext(StrUtils.formatCity(params.suggestion))
        subtitleSubject.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                .put("startdate", DateUtils.localDateToMMMd(params.checkIn))
                .put("enddate", DateUtils.localDateToMMMd(params.checkOut))
                .put("guests", StrUtils.formatGuestString(context, params.guests))
                .format())
    }
}