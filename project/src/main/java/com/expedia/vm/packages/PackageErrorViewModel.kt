package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.SuggestionStrUtils
import com.expedia.util.endlessObserver
import com.expedia.vm.AbstractErrorViewModel
import com.squareup.phrase.Phrase
import rx.Observer
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

class PackageErrorViewModel(context: Context): AbstractErrorViewModel(context) {

    var error: ApiError by Delegates.notNull()

    // inputs
    val packageSearchApiErrorObserver = PublishSubject.create<PackageApiError.Code>()
    val hotelOffersApiErrorObserver = PublishSubject.create<ApiError.Code>()
    val paramsSubject = PublishSubject.create<PackageSearchParams>()

    init {
        clickBack.subscribe(errorButtonClickedObservable)

        errorButtonClickedObservable.subscribe {
            when (error.errorCode) {
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
                ApiError.Code.PACKAGE_CHECKOUT_UNKNOWN -> {
                    checkoutUnknownErrorObservable.onNext(Unit)
                    PackagesTracking().trackCheckoutErrorRetry()
                }
                ApiError.Code.UNKNOWN_ERROR -> {
                    createTripUnknownErrorObservable.onNext(Unit)
                    PackagesTracking().trackCheckoutErrorRetry()
                }
                ApiError.Code.PACKAGE_DATE_MISMATCH_ERROR -> {
                    createTripUnknownErrorObservable.onNext(Unit)
                }
                else -> {
                    defaultErrorObservable.onNext(Unit)
                    PackagesTracking().trackCheckoutErrorRetry()
                }
            }
        }

        packageSearchApiErrorObserver.withLatestFrom(paramsSubject, { errorCode, searchParams ->
            object {
                val errorCode = errorCode
                val searchParams = searchParams
            }
        }).subscribe {
            error = ApiError(ApiError.Code.PACKAGE_SEARCH_ERROR)
            if (it.errorCode != null) {
                PackagesTracking().trackSearchError(it.errorCode.toString())
            }
            else {
                PackagesTracking().trackSearchError("Error Code is null")
            }
            when (it.errorCode) {
                PackageApiError.Code.pkg_unknown_error,
                PackageApiError.Code.search_response_null,
                PackageApiError.Code.pkg_flight_no_longer_available,
                PackageApiError.Code.pkg_too_many_children_in_lap,
                PackageApiError.Code.pkg_no_flights_available,
                PackageApiError.Code.pkg_hotel_no_longer_available,
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
                else -> {
                    couldNotConnectToServerError()
                }
            }
        }

        hotelOffersApiErrorObserver.subscribe {
            error = ApiError(ApiError.Code.PACKAGE_SEARCH_ERROR)
            PackagesTracking().trackSearchError(it.toString())
            when (it) {
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
            when (error.errorCode) {
                ApiError.Code.PACKAGE_CHECKOUT_CARD_DETAILS -> {
                    imageObservable.onNext(R.drawable.error_payment)
                    if (error.errorInfo?.field == "nameOnCard") {
                        errorMessageObservable.onNext(context.getString(R.string.error_name_on_card_mismatch))
                    } else {
                        errorMessageObservable.onNext(context.getString(R.string.e3_error_checkout_payment_failed))
                    }
                    buttonOneTextObservable.onNext(context.getString(R.string.edit_payment))
                    titleObservable.onNext(context.getString(R.string.payment_failed_label))
                    subTitleObservable.onNext("")
                }
                ApiError.Code.PAYMENT_FAILED -> {
                    imageObservable.onNext(R.drawable.error_payment)
                    errorMessageObservable.onNext(context.getString(R.string.e3_error_checkout_payment_failed))
                    buttonOneTextObservable.onNext(context.getString(R.string.edit_payment))
                    titleObservable.onNext(context.getString(R.string.payment_failed_label))
                    subTitleObservable.onNext("")
                }
                ApiError.Code.PACKAGE_CHECKOUT_TRAVELLER_DETAILS -> {
                    imageObservable.onNext(R.drawable.error_default)
                    val field = error.errorInfo.field
                    if (field == "phone") {
                        errorMessageObservable.onNext(context.getString(R.string.e3_error_checkout_invalid_traveler_info_TEMPLATE, context.getString(R.string.phone_number_field_text)))
                    } else if (field == "mainMobileTraveler.firstName") {
                        errorMessageObservable.onNext(context.getString(R.string.e3_error_checkout_invalid_traveler_info_TEMPLATE, context.getString(R.string.first_name_field_text)))
                    } else if (field == "mainMobileTraveler.lastName") {
                        errorMessageObservable.onNext(context.getString(R.string.e3_error_checkout_invalid_traveler_info_TEMPLATE, context.getString(R.string.last_name_field_text)))
                    } else {
                        errorMessageObservable.onNext(context.getString(R.string.e3_error_checkout_invalid_input))
                    }
                    buttonOneTextObservable.onNext(context.getString(R.string.edit_guest_details))
                    titleObservable.onNext(context.getString(R.string.payment_failed_label))
                    subTitleObservable.onNext("")
                }
                ApiError.Code.PACKAGE_DATE_MISMATCH_ERROR -> {
                    imageObservable.onNext(defaultErrorDrawable())
                    errorMessageObservable.onNext(context.getString(R.string.error_package_date_mismatch_error))
                    buttonOneTextObservable.onNext(context.getString(R.string.retry))
                }
                else -> {
                    couldNotConnectToServerError()
                }
            }
        }
    }

    private fun getToolbarSubtitle(params: PackageSearchParams): String {
        return Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(params.startDate))
                .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(params.endDate!!))
                .put("guests", StrUtils.formatTravelerString(context, params.guests))
                .format()
                .toString()
    }
}
