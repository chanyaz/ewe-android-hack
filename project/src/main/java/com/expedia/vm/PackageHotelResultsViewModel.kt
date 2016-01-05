package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

public class PackageHotelResultsViewModel(private val context: Context) {

    // Inputs
    val paramsSubject = BehaviorSubject.create<PackageSearchParams>()
    val resultsSubject = BehaviorSubject.create<PackageSearchResponse>()

    // Outputs
    val hotelResultsObservable = BehaviorSubject.create<PackageSearchResponse.HotelPackage>()
    val titleSubject = BehaviorSubject.create<String>()
    val subtitleSubject = PublishSubject.create<CharSequence>()

    init {
        paramsSubject.subscribe(endlessObserver { params ->
            titleSubject.onNext(params.destination.regionNames?.shortName)

            subtitleSubject.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                    .put("startdate", DateUtils.localDateToMMMd(params.checkIn))
                    .put("enddate", DateUtils.localDateToMMMd(params.checkOut))
                    .put("guests", StrUtils.formatGuestString(context, params.guests()))
                    .format())
        })

        resultsSubject.subscribe(endlessObserver { params ->
            hotelResultsObservable.onNext(params.packageResult.hotelsPackage)
        })
    }
}