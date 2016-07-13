package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

class FlightErrorViewModel(private val context: Context) {
    var error: ApiError by Delegates.notNull()

    // Inputs
    val searchApiErrorObserver = PublishSubject.create<ApiError.Code>()
    val paramsSubject = PublishSubject.create<com.expedia.bookings.data.flights.FlightSearchParams>()
    val errorDismissedObservable = BehaviorSubject.create<Unit>()


    // Outputs
    val imageObservable = BehaviorSubject.create<Int>()
    val buttonTextObservable = BehaviorSubject.create<String>()
    val errorMessageObservable = BehaviorSubject.create<String>()
    val titleObservable = BehaviorSubject.create<String>()
    val subTitleObservable = BehaviorSubject.create<String>()

    //handle different errors
    val defaultErrorObservable = BehaviorSubject.create<Unit>()

    init {
        errorDismissedObservable.subscribe {
            when (error.errorCode) {
            // TODO checkout error will come here and we open the
                else -> {
                    defaultErrorObservable.onNext(Unit)
                }
            }
        }

        searchApiErrorObserver.subscribe {
            error = ApiError(ApiError.Code.FLIGHT_SEARCH_ERROR)
            when (it) {
                ApiError.Code.FLIGHT_SEARCH_NO_RESULTS -> {
                    imageObservable.onNext(R.drawable.error_search)
                    errorMessageObservable.onNext(context.getString(R.string.error_no_result_message))
                    buttonTextObservable.onNext(context.getString(R.string.edit_search))
                }
                else -> {
                    makeDefaultError()
                }
            }
        }

        paramsSubject.subscribe { params ->
            val errorTitle: String = StrUtils.formatCityName(context.resources.getString(R.string.select_flight_to, params.arrivalAirport?.regionNames?.shortName))
            titleObservable.onNext(errorTitle)
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

    private fun getToolbarSubtitle(params: com.expedia.bookings.data.flights.FlightSearchParams): String {
        return Phrase.from(context, R.string.flight_calendar_instructions_date_with_guests_TEMPLATE)
                .put("startdate", DateFormatUtils.formatLocalDateToShortDayAndDate(params.departureDate))
                .put("guests", StrUtils.formatTravelerString(context, params.guests))
                .format()
                .toString()
    }
}