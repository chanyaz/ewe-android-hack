package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import com.expedia.vm.AbstractErrorViewModel
import com.squareup.phrase.Phrase
import rx.Observer
import rx.subjects.PublishSubject

class FlightErrorViewModel(context: Context): AbstractErrorViewModel(context) {

    private val MAX_RETRY_CREATE_TRIP_ATTEMPTS = 2

    val fireRetryCreateTrip = PublishSubject.create<Unit>()
    val paramsSubject = PublishSubject.create<com.expedia.bookings.data.flights.FlightSearchParams>()

    private val retryCreateTripBtnClicked = PublishSubject.create<Unit>()
    private var retryCreateTripBtnCount = 0

    init {
        paramsSubject.subscribe { params ->
            val errorTitle: String = StrUtils.formatCityName(context.resources.getString(R.string.select_flight_to, params.arrivalAirport?.regionNames?.shortName))
            titleObservable.onNext(errorTitle)
            subTitleObservable.onNext(getToolbarSubtitle(params))
        }
        setupRetryCreateTripButton()
    }

    override fun searchErrorHandler(): Observer<ApiError> {
        return endlessObserver {
            when (it.errorCode) {
                ApiError.Code.FLIGHT_SEARCH_NO_RESULTS -> {
                    imageObservable.onNext(R.drawable.error_search)
                    errorMessageObservable.onNext(context.getString(R.string.error_no_result_message))
                    buttonOneTextObservable.onNext(context.getString(R.string.edit_search))
                }
                else -> {
                    makeDefaultError()
                }
            }
        }
    }

    override fun createTripErrorHandler(): Observer<ApiError> {
        val newSearchLabel = context.getString(R.string.flight_new_search)

        fun retryCreateTripErrorHandler() {
            imageObservable.onNext(R.drawable.error_default)
            errorMessageObservable.onNext(context.resources.getString(R.string.flight_error_try_again_warning))
            buttonOneTextObservable.onNext(context.resources.getString(R.string.flight_error_retry))
            titleObservable.onNext(context.resources.getString(R.string.flight_generic_error_title))
            subTitleObservable.onNext("")
            subscribeActionToButtonPress(retryCreateTripBtnClicked)
        }

        return endlessObserver {
            when (it.errorCode) {
                ApiError.Code.UNKNOWN_ERROR -> {
                    retryCreateTripErrorHandler()
                }

                ApiError.Code.SESSION_TIMEOUT -> {
                    imageObservable.onNext(R.drawable.error_timeout)
                    errorMessageObservable.onNext(context.resources.getString(R.string.flight_session_expired_warning))
                    buttonOneTextObservable.onNext(newSearchLabel)
                    titleObservable.onNext(context.resources.getString(R.string.flight_session_expired_toolbar_title))
                    subTitleObservable.onNext("")
                    subscribeActionToButtonPress(defaultErrorObservable)
                }

                ApiError.Code.FLIGHT_PRODUCT_NOT_FOUND -> {
                    imageObservable.onNext(R.drawable.error_default)
                    errorMessageObservable.onNext(context.resources.getString(R.string.flight_unavailable_warning))
                    buttonOneTextObservable.onNext(newSearchLabel)
                    titleObservable.onNext(context.resources.getString(R.string.flight_unavailable_toolbar_title))
                    subTitleObservable.onNext("")
                    subscribeActionToButtonPress(defaultErrorObservable)
                }

                ApiError.Code.FLIGHT_SOLD_OUT -> {
                    imageObservable.onNext(R.drawable.error_default)
                    errorMessageObservable.onNext(context.resources.getString(R.string.flight_sold_out_warning))
                    buttonOneTextObservable.onNext(newSearchLabel)
                    titleObservable.onNext(context.resources.getString(R.string.flight_sold_out_toolbar_title))
                    subTitleObservable.onNext("")
                    subscribeActionToButtonPress(defaultErrorObservable)
                }

                else -> {
                    retryCreateTripErrorHandler()
                }
            }
        }
    }

    override fun checkoutApiErrorHandler(): Observer<ApiError> {
        return createTripErrorHandler()
    }

    private fun getToolbarSubtitle(params: FlightSearchParams): String {
        return Phrase.from(context, R.string.flight_calendar_instructions_date_with_guests_TEMPLATE)
                .put("startdate", DateFormatUtils.formatLocalDateToShortDayAndDate(params.departureDate))
                .put("guests", StrUtils.formatTravelerString(context, params.guests))
                .format()
                .toString()
    }

    private fun setupRetryCreateTripButton() {
        defaultErrorObservable.subscribe {
            retryCreateTripBtnCount = 0
        }

        retryCreateTripBtnClicked.subscribe {
            if (retryCreateTripBtnCount++ < MAX_RETRY_CREATE_TRIP_ATTEMPTS) {
                fireRetryCreateTrip.onNext(Unit)
            }
            else {
                defaultErrorObservable.onNext(Unit)
                retryCreateTripBtnCount = 0
            }
        }
    }
}
