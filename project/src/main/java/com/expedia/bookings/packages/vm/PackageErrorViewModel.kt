package com.expedia.bookings.packages.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.extensions.withLatestFrom
import com.expedia.bookings.tracking.ApiCallFailing
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.SuggestionStrUtils
import com.expedia.util.endlessObserver
import com.expedia.vm.LobErrorViewModel
import com.squareup.phrase.Phrase
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject
import kotlin.properties.Delegates

class PackageErrorViewModel(context: Context) : LobErrorViewModel(context) {

    var error: ApiError by Delegates.notNull()

    // inputs
    val packageSearchApiErrorObserver = PublishSubject.create<Pair<PackageApiError.Code, ApiCallFailing>>()
    val hotelOffersApiErrorObserver = PublishSubject.create<Pair<ApiError.Code, ApiCallFailing>>()
    val paramsSubject = PublishSubject.create<PackageSearchParams>()

    init {
        clickBack.subscribe(errorButtonClickedObservable)

        errorButtonClickedObservable.subscribe {
            when (error.getErrorCode()) {
                ApiError.Code.PACKAGE_CHECKOUT_CARD_DETAILS,
                ApiError.Code.INVALID_CARD_NUMBER,
                ApiError.Code.CID_DID_NOT_MATCHED,
                ApiError.Code.INVALID_CARD_EXPIRATION_DATE,
                ApiError.Code.PAYMENT_FAILED,
                ApiError.Code.CARD_LIMIT_EXCEEDED -> {
                    checkoutCardErrorObservable.onNext(Unit)
                    PackagesTracking().trackCheckoutErrorRetry()
                }
                ApiError.Code.PACKAGE_CHECKOUT_TRAVELLER_DETAILS -> {
                    checkoutTravelerErrorObservable.onNext(Unit)
                    PackagesTracking().trackCheckoutErrorRetry()
                }
                ApiError.Code.UNKNOWN_ERROR -> {
                    createTripUnknownErrorObservable.onNext(Unit)
                    PackagesTracking().trackCheckoutErrorRetry()
                }
                ApiError.Code.PACKAGE_CHECKOUT_UNKNOWN -> {
                    checkoutUnknownErrorObservable.onNext(Unit)
                    PackagesTracking().trackCheckoutErrorRetry()
                }
                ApiError.Code.PACKAGE_DATE_MISMATCH_ERROR -> {
                    createTripUnknownErrorObservable.onNext(Unit)
                    PackagesTracking().trackCheckoutErrorRetry()
                }
                ApiError.Code.PACKAGE_HOTEL_NO_RESULTS_POST_FILTER -> {
                    filterNoResultsObservable.onNext(Unit)
                }
                else -> {
                    defaultErrorObservable.onNext(Unit)
                    PackagesTracking().trackCheckoutErrorRetry()
                }
            }
        }

        packageSearchApiErrorObserver.withLatestFrom(paramsSubject, { errorDetails, searchParams ->
            object {
                val errorCode = errorDetails.first
                val apiCallFailing = errorDetails.second
                val searchParams = searchParams
            }
        }).subscribe {
            error = ApiError(ApiError.Code.PACKAGE_SEARCH_ERROR)
            PackagesTracking().trackShoppingError(it.apiCallFailing)
            when (it.errorCode) {
                PackageApiError.Code.pkg_unknown_error,
                PackageApiError.Code.search_response_null,
                PackageApiError.Code.pkg_flight_no_longer_available,
                PackageApiError.Code.pkg_too_many_children_in_lap,
                PackageApiError.Code.pkg_no_flights_available,
                PackageApiError.Code.pkg_hotel_no_longer_available,
                PackageApiError.Code.pkg_search_from_date_too_near,
                PackageApiError.Code.mid_could_not_find_results,
                PackageApiError.Code.pkg_invalid_checkin_checkout_dates -> {
                    imageObservable.onNext(R.drawable.error_default)
                    errorMessageObservable.onNext(context.getString(R.string.error_package_search_message))
                    buttonOneTextObservable.onNext(context.getString(R.string.edit_search))
                }
                PackageApiError.Code.pkg_piid_expired,
                PackageApiError.Code.pkg_pss_downstream_service_timeout -> {
                    imageObservable.onNext(R.drawable.error_timeout)
                    errorMessageObservable.onNext(context.getString(R.string.reservation_time_out))
                    titleObservable.onNext(context.getString(R.string.session_timeout))
                    subTitleObservable.onNext("")
                    buttonOneTextObservable.onNext(context.getString(R.string.search_again))
                }
                PackageApiError.Code.pkg_destination_resolution_failed -> {
                    imageObservable.onNext(R.drawable.error_default)
                    errorMessageObservable.onNext(Phrase.from(context, R.string.error_package_destination_resolution_message_TEMPLATE)
                            .put("destination", it.searchParams.destination!!.regionNames!!.shortName)
                            .format().toString())
                    buttonOneTextObservable.onNext(context.getString(R.string.edit_search))
                }
                PackageApiError.Code.pkg_origin_resolution_failed -> {
                    imageObservable.onNext(R.drawable.error_default)
                    errorMessageObservable.onNext(Phrase.from(context, R.string.error_package_origin_resolution_message_TEMPLATE)
                            .put("origin", it.searchParams.origin!!.regionNames!!.shortName)
                            .format().toString())
                    buttonOneTextObservable.onNext(context.getString(R.string.edit_search))
                }
                PackageApiError.Code.mid_fss_hotel_unavailable_for_red_eye_flight -> {
                    imageObservable.onNext(R.drawable.error_default)
                    errorMessageObservable.onNext(context.getString(R.string.error_package_search_red_eye_flight_message))
                    buttonOneTextObservable.onNext(context.getString(R.string.retry))
                }
                PackageApiError.Code.mid_no_offers_post_filtering -> {
                    error = ApiError(ApiError.Code.PACKAGE_HOTEL_NO_RESULTS_POST_FILTER)
                    imageObservable.onNext(R.drawable.error_search)
                    errorMessageObservable.onNext(context.getString(R.string.error_no_filter_result_message))
                    buttonOneTextObservable.onNext(context.getString(R.string.reset_filter))
                }
                else -> {
                    couldNotConnectToServerError()
                }
            }
        }

        hotelOffersApiErrorObserver.subscribe {
            val (errorCode, apiCallFailing) = it
            // TODO Check why is this hardcoded? Ideally it should be errorCode
            error = ApiError(ApiError.Code.PACKAGE_SEARCH_ERROR)
            PackagesTracking().trackShoppingError(apiCallFailing)
            when (errorCode) {
                ApiError.Code.PACKAGE_SEARCH_ERROR -> {
                    imageObservable.onNext(R.drawable.error_search)
                    errorMessageObservable.onNext(context.getString(R.string.error_package_search_message))
                    buttonOneTextObservable.onNext(context.getString(R.string.edit_search))
                }
                else -> {
                    couldNotConnectToServerError()
                }
            }
        }

        paramsSubject.subscribe { params ->
            titleObservable.onNext(String.format(context.getString(R.string.your_trip_to_TEMPLATE), SuggestionStrUtils.formatCityName(params.destination?.regionNames?.fullName)))
            subTitleObservable.onNext(getToolbarSubtitle(params))
        }
    }

    override fun searchErrorHandler(): Observer<ApiError> {
        return endlessObserver { } // do nothing. Package search errors handled internally (in this class)
    }

    override fun createTripErrorHandler(): Observer<ApiError> {
        return checkoutApiErrorHandler()
    }

    override fun checkoutApiErrorHandler(): Observer<ApiError> {
        return endlessObserver {
            error = it
            PackagesTracking().trackCheckoutError(error)
            couldNotConnectToServerError()
        }
    }

    private fun getToolbarSubtitle(params: PackageSearchParams): String {
        return Phrase.from(context, R.string.start_dash_end_date_range_with_guests_TEMPLATE)
                .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(params.startDate))
                .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(params.endDate!!))
                .put("guests", StrUtils.formatTravelerString(context, params.guests))
                .format()
                .toString()
    }
}
