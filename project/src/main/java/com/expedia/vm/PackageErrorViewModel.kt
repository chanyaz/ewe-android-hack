package com.expedia.vm

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.StrUtils
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

class PackageErrorViewModel(private val context: Context) {
    // Inputs
    val apiErrorObserver = PublishSubject.create<PackageApiError.Code>()
    val paramsSubject = PublishSubject.create<PackageSearchParams>()
    var error: PackageApiError.Code by Delegates.notNull()

    // Outputs
    val imageObservable = BehaviorSubject.create<Int>()
    val buttonTextObservable = BehaviorSubject.create<String>()
    val errorMessageObservable = BehaviorSubject.create<String>()
    val titleObservable = BehaviorSubject.create<String>()
    val subTitleObservable = BehaviorSubject.create<String>()
    val actionObservable = BehaviorSubject.create<Unit>()

    //handle different errors
    val defaultErrorObservable = BehaviorSubject.create<Unit>()

    init {
        actionObservable.subscribe {
            defaultErrorObservable.onNext(Unit)
        }

        apiErrorObserver.subscribe {
            when (it) {
                PackageApiError.Code.pkg_unknown_error,
                PackageApiError.Code.search_response_null,
                PackageApiError.Code.pkg_destination_resolution_failed,
                PackageApiError.Code.pkg_flight_no_longer_available,
                PackageApiError.Code.pkg_invalid_checkin_checkout_dates -> {
                    imageObservable.onNext(R.drawable.error_default)
                    errorMessageObservable.onNext(context.getString(R.string.error_package_search_message))
                    buttonTextObservable.onNext(context.getString(R.string.edit_search))
                }
                else -> {
                    makeDefaultError()
                }
            }
        }
        paramsSubject.subscribe { params ->
            titleObservable.onNext(String.format(context.getString(R.string.your_trip_to_TEMPLATE), StrUtils.formatCityName(params.destination.regionNames.fullName)))
            subTitleObservable.onNext(getToolbarSubtitle(params))
        }
    }

    private fun makeDefaultError() {
        imageObservable.onNext(R.drawable.error_default)
        val message = Phrase.from(context, R.string.error_server_TEMPLATE)
                .put("brand", BuildConfig.brand)
                .format()
                .toString()
        errorMessageObservable.onNext(message)
        buttonTextObservable.onNext(context.getString(R.string.retry))
    }

    private fun getToolbarSubtitle(params: PackageSearchParams): String {
        return Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                .put("startdate", DateUtils.localDateToMMMd(params.checkIn))
                .put("enddate", DateUtils.localDateToMMMd(params.checkOut))
                .put("guests", StrUtils.formatTravelerString(context, params.guests))
                .format()
                .toString()
    }
}