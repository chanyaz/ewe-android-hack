package com.expedia.bookings.packages.vm

import android.content.Context
import android.util.Log
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.hotel.vm.BaseHotelResultsViewModel
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase

class PackageHotelResultsViewModel(context: Context) :
        BaseHotelResultsViewModel(context) {

    init {
        paramsSubject.subscribe(endlessObserver { params ->
            updateTitleSubtitleFromParams(params)
        })

        filterChoicesSubject.subscribe(endlessObserver { filterChoices ->
            //TODO Search with new filter choices (TBD once the API changes go in)
            //TODO Set New Params in Db
            //TODO push params to paramsSubject
            com.mobiata.android.Log.d("PHRVM", "Current filter:" + filterChoices)
        })
    }

    private fun updateTitleSubtitleFromParams(params: HotelSearchParams) {
        cachedParams = params
        titleSubject.onNext(StrUtils.formatCity(params.suggestion))
        subtitleSubject.onNext(Phrase.from(context, R.string.start_dash_end_date_range_with_guests_TEMPLATE)
                .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(params.checkIn))
                .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(params.checkOut))
                .put("guests", StrUtils.formatGuestString(context, params.guests))
                .format())
        subtitleContDescSubject.onNext(
                Phrase.from(context, R.string.start_to_end_plus_guests_cont_desc_TEMPLATE)
                        .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(params.checkIn))
                        .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(params.checkOut))
                        .put("guests", StrUtils.formatGuestString(context, params.guests))
                        .format().toString())
    }
}
