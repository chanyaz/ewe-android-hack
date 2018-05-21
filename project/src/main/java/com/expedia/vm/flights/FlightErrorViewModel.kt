package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.SuggestionStrUtils
import com.expedia.util.endlessObserver
import com.expedia.vm.LobErrorViewModel
import com.squareup.phrase.Phrase
import io.reactivex.Observer
import com.expedia.bookings.extensions.withLatestFrom
import com.expedia.bookings.tracking.ApiCallFailing
import com.expedia.bookings.utils.Constants
import io.reactivex.subjects.PublishSubject
import kotlin.properties.Delegates

class FlightErrorViewModel(context: Context) : LobErrorViewModel(context) {

    private val MAX_RETRY_CREATE_TRIP_ATTEMPTS = 2
    private val MAX_RETRY_ATTEMPTS = 3

    val fireRetryCreateTrip = PublishSubject.create<Unit>()
    val retryCheckout = PublishSubject.create<Unit>()
    val showPaymentForm = PublishSubject.create<Unit>()
    val showConfirmation = PublishSubject.create<Unit>()
    val showSearch = PublishSubject.create<Unit>()
    val retrySearch = PublishSubject.create<Unit>()
    val paramsSubject = PublishSubject.create<com.expedia.bookings.data.flights.FlightSearchParams>()
    val showTravelerForm = PublishSubject.create<Unit>()
    val isSearchError = PublishSubject.create<Boolean>()
    private val retryCreateTripBtnClicked = PublishSubject.create<Unit>()
    val retryBtnClicked = PublishSubject.create<Unit>()

    private var retryCreateTripBtnCount = 0
    private var retryBtnCount = 0

    var error: ApiError by Delegates.notNull()

    init {
        clickBack.withLatestFrom(isSearchError, { _, isFromSearch ->
            if (isFromSearch) {
                defaultErrorObservable.onNext(Unit)
            } else {
                when (error.getErrorCode()) {
                    ApiError.Code.PAYMENT_FAILED, ApiError.Code.INVALID_INPUT -> errorButtonClickedObservable.onNext(Unit)
                    else -> defaultErrorObservable.onNext(Unit)
                }
            }
        }).subscribe()

        paramsSubject.subscribe { params ->
            val errorTitle: String = SuggestionStrUtils.formatCityName(context.resources.getString(R.string.select_flight_to,
                    HtmlCompat.stripHtml(params.arrivalAirport.regionNames.displayName)))
            titleObservable.onNext(errorTitle)
            subTitleObservable.onNext(getToolbarSubtitle(params))
        }
        setupRetryCreateTripButton()
        setupRetryButton()
    }

    override fun searchErrorHandler(): Observer<ApiError> {
        return endlessObserver {
            isSearchError.onNext(true)
            error = it
            when (it.getErrorCode()) {
                ApiError.Code.FLIGHT_SEARCH_NO_RESULTS -> {
                    imageObservable.onNext(R.drawable.error_search)
                    errorMessageObservable.onNext(context.getString(R.string.error_no_result_message))
                    buttonOneTextObservable.onNext(context.getString(R.string.edit_search))
                    subscribeActionToButtonPress(showSearch)
                    FlightsV2Tracking.trackFlightShoppingError(ApiCallFailing.FlightSearch(it.errorCode.toString()))
                }
                else -> {
                    makeDefaultError()
                    subscribeActionToButtonPress(retryBtnClicked)
                    FlightsV2Tracking.trackFlightShoppingError(ApiCallFailing.FlightSearch(it.errorCode?.toString() ?: Constants.UNKNOWN_ERROR_CODE))
                }
            }
        }
    }

    override fun makeDefaultError() {
        imageObservable.onNext(R.drawable.error_default)
        errorMessageObservable.onNext(context.getString(R.string.error_try_again_warning))
        buttonOneTextObservable.onNext(context.getString(R.string.retry))
    }

    override fun createTripErrorHandler(): Observer<ApiError> {
        val newSearchLabel = context.getString(R.string.flight_new_search)

        fun retryCreateTripErrorHandler() {
            imageObservable.onNext(R.drawable.error_default)
            errorMessageObservable.onNext(context.resources.getString(R.string.error_try_again_warning))
            buttonOneTextObservable.onNext(context.resources.getString(R.string.flight_error_retry))
            titleObservable.onNext(context.resources.getString(R.string.flight_generic_error_title))
            subTitleObservable.onNext("")
            subscribeActionToButtonPress(retryCreateTripBtnClicked)
        }

        return endlessObserver {
            error = it
            isSearchError.onNext(false)
            when (it.getErrorCode()) {
                ApiError.Code.UNKNOWN_ERROR -> {
                    retryCreateTripErrorHandler()
                    FlightsV2Tracking.trackFlightShoppingError(ApiCallFailing.FlightCreateTrip(it.errorCode.toString()))
                }

                ApiError.Code.SESSION_TIMEOUT -> {
                    handleSessionTimeout()
                    FlightsV2Tracking.trackFlightShoppingError(ApiCallFailing.FlightCreateTrip(it.errorCode.toString()))
                }

                ApiError.Code.FLIGHT_PRODUCT_NOT_FOUND -> {
                    imageObservable.onNext(R.drawable.error_default)
                    errorMessageObservable.onNext(context.resources.getString(R.string.flight_unavailable_warning))
                    buttonOneTextObservable.onNext(newSearchLabel)
                    titleObservable.onNext(context.resources.getString(R.string.flight_unavailable_toolbar_title))
                    subTitleObservable.onNext("")
                    subscribeActionToButtonPress(defaultErrorObservable)
                    FlightsV2Tracking.trackFlightShoppingError(ApiCallFailing.FlightCreateTrip(it.errorCode.toString()))
                }

                ApiError.Code.FLIGHT_SOLD_OUT -> {
                    imageObservable.onNext(R.drawable.error_default)
                    errorMessageObservable.onNext(context.resources.getString(R.string.flight_sold_out_warning))
                    buttonOneTextObservable.onNext(newSearchLabel)
                    titleObservable.onNext(context.resources.getString(R.string.flight_sold_out_toolbar_title))
                    subTitleObservable.onNext("")
                    subscribeActionToButtonPress(defaultErrorObservable)
                    FlightsV2Tracking.trackFlightShoppingError(ApiCallFailing.FlightCreateTrip(it.errorCode.toString()))
                }

                else -> {
                    retryCreateTripErrorHandler()
                }
            }
        }
    }

    override fun checkoutApiErrorHandler(): Observer<ApiError> {
        return endlessObserver {
            error = it
            isSearchError.onNext(false)
            FlightsV2Tracking.trackFlightCheckoutError(error)
            when (it.getErrorCode()) {
                ApiError.Code.UNKNOWN_ERROR -> {
                    handleCheckoutUnknownError()
                }

                ApiError.Code.PAYMENT_FAILED -> {
                    checkoutUnknownErrorObservable.onNext(Unit)
                    imageObservable.onNext(R.drawable.error_default)
                    errorMessageObservable.onNext(context.resources.getString(R.string.e3_error_checkout_payment_failed))
                    buttonOneTextObservable.onNext(context.resources.getString(R.string.edit_payment))
                    titleObservable.onNext(context.resources.getString(R.string.payment_failed_label))
                    subTitleObservable.onNext("")
                    subscribeActionToButtonPress(showPaymentForm)
                }

                ApiError.Code.SESSION_TIMEOUT -> {
                    handleSessionTimeout()
                }

                ApiError.Code.TRIP_ALREADY_BOOKED -> {
                    showConfirmation.onNext(Unit)
                }

                ApiError.Code.INVALID_INPUT -> {
                    val isTravelerFormInputError = it.errorInfo.field.contains("Passenger")

                    if (isTravelerFormInputError) {
                        subscribeActionToButtonPress(showTravelerForm)
                        buttonOneTextObservable.onNext(context.getString(R.string.edit_traveler_details))
                    } else {
                        subscribeActionToButtonPress(showPaymentForm)
                        buttonOneTextObservable.onNext(context.resources.getString(R.string.edit_payment))
                    }
                    errorMessageObservable.onNext(context.getString(R.string.e3_error_checkout_invalid_input))
                    checkoutUnknownErrorObservable.onNext(Unit)
                    imageObservable.onNext(R.drawable.error_default)
                    titleObservable.onNext(context.resources.getString(R.string.e3_error_checkout_invalid_info))
                    subTitleObservable.onNext("")
                }

                else -> {
                    handleCheckoutUnknownError()
                }
            }
        }
    }

    private fun handleCheckoutUnknownError() {
        checkoutUnknownErrorObservable.onNext(Unit)
        imageObservable.onNext(R.drawable.error_default)
        errorMessageObservable.onNext(context.resources.getString(R.string.error_try_again_warning))
        buttonOneTextObservable.onNext(context.resources.getString(R.string.flight_error_retry))
        titleObservable.onNext(context.resources.getString(R.string.flight_generic_error_title))
        subTitleObservable.onNext("")
        subscribeActionToButtonPress(retryCheckout)
    }

    private fun handleSessionTimeout() {
        imageObservable.onNext(R.drawable.error_timeout)
        errorMessageObservable.onNext(context.resources.getString(R.string.flight_session_expired_warning))
        buttonOneTextObservable.onNext(context.getString(R.string.flight_new_search))
        titleObservable.onNext(context.resources.getString(R.string.flight_session_expired_toolbar_title))
        subTitleObservable.onNext("")
        subscribeActionToButtonPress(defaultErrorObservable)
    }

    private fun getToolbarSubtitle(params: FlightSearchParams): String {
        return Phrase.from(context, R.string.flight_calendar_instructions_date_with_guests_TEMPLATE)
                .put("startdate", LocaleBasedDateFormatUtils.dateTimeToEEEMMMddyyyy(params.departureDate))
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
            } else {
                defaultErrorObservable.onNext(Unit)
                retryCreateTripBtnCount = 0
            }
        }
    }

    private fun setupRetryButton() {
        defaultErrorObservable.subscribe {
            retryBtnCount = 0
        }

        retryBtnClicked.subscribe {
            if (retryBtnCount++ < MAX_RETRY_ATTEMPTS) {
                retrySearch.onNext(Unit)
            } else {
                defaultErrorObservable.onNext(Unit)
                retryBtnCount = 0
            }
        }
    }
}
