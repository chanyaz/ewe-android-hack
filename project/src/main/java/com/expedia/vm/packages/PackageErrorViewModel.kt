package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.StrUtils
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

class PackageErrorViewModel(private val context: Context) {
    var error: ApiError by Delegates.notNull()

    // Inputs
    val searchApiErrorObserver = PublishSubject.create<PackageApiError.Code>()
    val hotelOffersApiErrorObserver = PublishSubject.create<ApiError.Code>()
    val checkoutApiErrorObserver = PublishSubject.create<ApiError>()
    val paramsSubject = PublishSubject.create<PackageSearchParams>()

    // Outputs
    val imageObservable = BehaviorSubject.create<Int>()
    val buttonTextObservable = BehaviorSubject.create<String>()
    val errorMessageObservable = BehaviorSubject.create<String>()
    val titleObservable = BehaviorSubject.create<String>()
    val subTitleObservable = BehaviorSubject.create<String>()
    val actionObservable = BehaviorSubject.create<Unit>()

    //handle different errors
    val defaultErrorObservable = BehaviorSubject.create<Unit>()
    val checkoutCardErrorObservable = BehaviorSubject.create<Unit>()
    val checkoutTravelerErrorObservable = BehaviorSubject.create<Unit>()
    val checkoutUnknownErrorObservable = BehaviorSubject.create<Unit>()

    init {
        actionObservable.subscribe {
            when (error.errorCode) {
                ApiError.Code.PACKAGE_CHECKOUT_CARD_DETAILS,
                ApiError.Code.INVALID_CARD_NUMBER,
                ApiError.Code.CID_DID_NOT_MATCHED,
                ApiError.Code.INVALID_CARD_EXPIRATION_DATE,
                ApiError.Code.CARD_LIMIT_EXCEEDED -> {
                    checkoutCardErrorObservable.onNext(Unit)
                    PackagesTracking().trackCheckoutErrorRetry()
                }
                ApiError.Code.PACKAGE_CHECKOUT_TRAVELLER_DETAILS -> {
                    checkoutTravelerErrorObservable.onNext(Unit)
                    PackagesTracking().trackCheckoutErrorRetry()
                }
                ApiError.Code.UNKNOWN_ERROR -> {
                    checkoutUnknownErrorObservable.onNext(Unit)
                    PackagesTracking().trackCheckoutErrorRetry()
                }
                else -> {
                    defaultErrorObservable.onNext(Unit)
                    PackagesTracking().trackCheckoutErrorRetry()
                }
            }
        }

        searchApiErrorObserver.subscribe {
            error = ApiError(ApiError.Code.PACKAGE_SEARCH_ERROR)
            PackagesTracking().trackSearchError(it.toString())
            when (it) {
                PackageApiError.Code.pkg_unknown_error,
                PackageApiError.Code.search_response_null,
                PackageApiError.Code.pkg_destination_resolution_failed,
                PackageApiError.Code.pkg_flight_no_longer_available,
                PackageApiError.Code.pkg_too_many_children_in_lap,
                PackageApiError.Code.pkg_invalid_checkin_checkout_dates -> {
                    imageObservable.onNext(R.drawable.error_default)
                    errorMessageObservable.onNext(context.getString(R.string.error_package_search_message))
                    buttonTextObservable.onNext(context.getString(R.string.edit_search))
                }
                PackageApiError.Code.pkg_piid_expired -> {
                    imageObservable.onNext(R.drawable.error_timeout)
                    errorMessageObservable.onNext(context.getString(R.string.reservation_time_out))
                    buttonTextObservable.onNext(context.getString(R.string.search_again))
                    titleObservable.onNext(context.getString(R.string.session_timeout))
                    subTitleObservable.onNext("")
                    buttonTextObservable.onNext(context.getString(R.string.search_again))
                }
                else -> {
                    makeDefaultError()
                }
            }
        }

        hotelOffersApiErrorObserver.subscribe {
            error = ApiError(ApiError.Code.PACKAGE_SEARCH_ERROR)
            PackagesTracking().trackSearchError(it.toString())
            when (it) {
                ApiError.Code.PACKAGE_SEARCH_ERROR -> {
                    imageObservable.onNext(R.drawable.error_default)
                    errorMessageObservable.onNext(context.getString(R.string.error_package_search_message))
                    buttonTextObservable.onNext(context.getString(R.string.edit_search))
                }
                else -> {
                    makeDefaultError()
                }
            }
        }

        checkoutApiErrorObserver.subscribe() {
            error = it
            PackagesTracking().trackCheckoutError(error.errorCode.toString())
            when (it.errorCode) {
                ApiError.Code.PACKAGE_CHECKOUT_CARD_DETAILS -> {
                    imageObservable.onNext(R.drawable.error_payment)
                    if (error.errorInfo?.field == "nameOnCard") {
                        errorMessageObservable.onNext(context.getString(R.string.error_name_on_card_mismatch))
                    } else {
                        errorMessageObservable.onNext(context.getString(R.string.e3_error_checkout_payment_failed))
                    }
                    buttonTextObservable.onNext(context.getString(R.string.edit_payment))
                    titleObservable.onNext(context.getString(R.string.hotel_payment_failed_text))
                    subTitleObservable.onNext("")
                }
                ApiError.Code.PACKAGE_CHECKOUT_TRAVELLER_DETAILS -> {
                    imageObservable.onNext(R.drawable.error_default)
                    val field = error.errorInfo.field
                    if (field == "phone") {
                        errorMessageObservable.onNext(context.getString(R.string.e3_error_checkout_invalid_traveler_info_TEMPLATE, context.getString(R.string.phone_number_field_text)))
                    } else if ( field == "mainMobileTraveler.firstName") {
                        errorMessageObservable.onNext(context.getString(R.string.e3_error_checkout_invalid_traveler_info_TEMPLATE, context.getString(R.string.first_name_field_text)))
                    } else if (field == "mainMobileTraveler.lastName" ) {
                        errorMessageObservable.onNext(context.getString(R.string.e3_error_checkout_invalid_traveler_info_TEMPLATE, context.getString(R.string.last_name_field_text)))
                    } else {
                        errorMessageObservable.onNext(context.getString(R.string.e3_error_checkout_invalid_input))
                    }
                    buttonTextObservable.onNext(context.getString(R.string.edit_guest_details))
                    titleObservable.onNext(context.getString(R.string.hotel_payment_failed_text))
                    subTitleObservable.onNext("")
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