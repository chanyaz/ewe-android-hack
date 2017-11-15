package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

class HotelErrorViewModel(context: Context): AbstractErrorViewModel(context) {
    // Inputs
    val infositeApiErrorObserver = PublishSubject.create<ApiError>()
    val apiErrorObserver = PublishSubject.create<ApiError>()
    val paramsSubject = BehaviorSubject.create<HotelSearchParams>()
    var error: ApiError by Delegates.notNull()

    // Outputs
    val hotelSoldOutErrorObservable = PublishSubject.create<Boolean>()

    // Handle different errors
    val searchErrorObservable = BehaviorSubject.create<Unit>()
    val filterNoResultsObservable = BehaviorSubject.create<Unit>()

    val pinnedNotFoundToNearByHotelObservable = PublishSubject.create<Unit>()

    init {
        errorButtonClickedObservable.subscribe {
            when (error.errorCode) {
                ApiError.Code.HOTEL_ROOM_UNAVAILABLE -> {
                    soldOutObservable.onNext(Unit)
                }
                ApiError.Code.HOTEL_SEARCH_NO_RESULTS -> {
                    searchErrorObservable.onNext(Unit)
                }
                ApiError.Code.HOTEL_MAP_SEARCH_NO_RESULTS -> {
                    searchErrorObservable.onNext(Unit)
                }
                ApiError.Code.HOTEL_FILTER_NO_RESULTS -> {
                    filterNoResultsObservable.onNext(Unit)
                }
                ApiError.Code.HOTEL_PINNED_NOT_FOUND -> {
                    pinnedNotFoundToNearByHotelObservable.onNext(Unit)
                }
                ApiError.Code.HOTEL_CHECKOUT_CARD_DETAILS -> {
                    checkoutCardErrorObservable.onNext(Unit)
                    HotelTracking.trackHotelsCheckoutErrorRetry()
                }
                ApiError.Code.HOTEL_CHECKOUT_TRAVELLER_DETAILS -> {
                    checkoutTravelerErrorObservable.onNext(Unit)
                    HotelTracking.trackHotelsCheckoutErrorRetry()
                }
                ApiError.Code.HOTEL_PRODUCT_KEY_EXPIRY -> {
                    productKeyExpiryObservable.onNext(Unit)
                    HotelTracking.trackHotelsCheckoutErrorRetry()
                }
                ApiError.Code.TRIP_ALREADY_BOOKED -> {
                    checkoutAlreadyBookedObservable.onNext(Unit)
                    HotelTracking.trackHotelsCheckoutErrorRetry()
                }
                ApiError.Code.PAYMENT_FAILED -> {
                    checkoutPaymentFailedObservable.onNext(Unit)
                    HotelTracking.trackHotelsCheckoutErrorRetry()
                }
                ApiError.Code.SESSION_TIMEOUT -> {
                    sessionTimeOutObservable.onNext(Unit)
                    HotelTracking.trackHotelsCheckoutErrorRetry()
                }
                ApiError.Code.HOTEL_CHECKOUT_UNKNOWN -> {
                    checkoutUnknownErrorObservable.onNext(Unit)
                    HotelTracking.trackHotelsCheckoutErrorRetry()
                }
                else -> {
                    defaultErrorObservable.onNext(Unit)
                }
            }
        }

        apiErrorObserver.subscribe {
            error = it
            hotelSoldOutErrorObservable.onNext(false)
            when (it.errorCode) {
                ApiError.Code.HOTEL_CHECKOUT_CARD_DETAILS -> {
                    imageObservable.onNext(R.drawable.error_payment)
                    if (error.errorInfo?.field == "nameOnCard") {
                        errorMessageObservable.onNext(context.getString(R.string.error_name_on_card_mismatch))
                    } else {
                        errorMessageObservable.onNext(context.getString(R.string.e3_error_checkout_payment_failed))
                    }
                    buttonOneTextObservable.onNext(context.getString(R.string.edit_payment))
                    titleObservable.onNext(context.getString(R.string.payment_failed_label))
                    subTitleObservable.onNext("")
                    HotelTracking.trackHotelsCheckoutError(error)
                }
                ApiError.Code.HOTEL_CHECKOUT_TRAVELLER_DETAILS -> {
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
                    buttonOneTextObservable.onNext(context.getString(R.string.edit_guest_details))
                    titleObservable.onNext(context.getString(R.string.payment_failed_label))
                    subTitleObservable.onNext("")
                    HotelTracking.trackHotelsCheckoutError(error)
                }
                ApiError.Code.HOTEL_PRODUCT_KEY_EXPIRY -> {
                    imageObservable.onNext(R.drawable.error_default)
                    errorMessageObservable.onNext(context.getString(R.string.error_hotel_no_longer_available))
                    buttonOneTextObservable.onNext(context.getString(R.string.search_again))
                    HotelTracking.trackHotelsCheckoutError(error)
                }
                ApiError.Code.HOTEL_ROOM_UNAVAILABLE -> {
                    hotelSoldOutErrorObservable.onNext(true)
                    imageObservable.onNext(R.drawable.error_default)
                    errorMessageObservable.onNext(context.getString(R.string.error_room_sold_out))
                    buttonOneTextObservable.onNext(context.getString(R.string.select_another_room))
                    HotelTracking.trackPageLoadHotelSoldOut()
                }
                ApiError.Code.PAYMENT_FAILED -> {
                    imageObservable.onNext(R.drawable.error_payment)
                    errorMessageObservable.onNext(context.getString(R.string.e3_error_checkout_payment_failed))
                    buttonOneTextObservable.onNext(context.getString(R.string.edit_payment))
                    titleObservable.onNext(context.getString(R.string.payment_failed_label))
                    subTitleObservable.onNext("")
                    HotelTracking.trackHotelsCheckoutError(error)
                }
                ApiError.Code.TRIP_ALREADY_BOOKED -> {
                    imageObservable.onNext(R.drawable.error_trip_booked)
                    errorMessageObservable.onNext(context.getString(R.string.reservation_already_exists_new_hotels))
                    buttonOneTextObservable.onNext(context.getString(R.string.my_trips))
                    titleObservable.onNext(context.getString(R.string.booking_complete))
                    subTitleObservable.onNext("")
                    HotelTracking.trackHotelsCheckoutError(error)
                }
                ApiError.Code.HOTEL_CHECKOUT_UNKNOWN -> {
                    imageObservable.onNext(R.drawable.no_hotel_error)
                    errorMessageObservable.onNext(context.getString(R.string.error_hotel_unhandled))
                    buttonOneTextObservable.onNext(context.getString(R.string.retry))
                    titleObservable.onNext(context.getString(R.string.payment_failed_label))
                    subTitleObservable.onNext("")
                    HotelTracking.trackHotelsCheckoutError(error)
                }
                ApiError.Code.SESSION_TIMEOUT -> {
                    imageObservable.onNext(R.drawable.error_timeout)
                    errorMessageObservable.onNext(context.getString(R.string.error_hotel_no_longer_available))
                    buttonOneTextObservable.onNext(context.getString(R.string.search_again))
                    titleObservable.onNext(context.getString(R.string.session_timeout))
                    subTitleObservable.onNext("")
                    buttonOneTextObservable.onNext(context.getString(R.string.search_again))
                    HotelTracking.trackHotelsCheckoutError(error)
                }
                else -> {
                    makeDefaultError()
                }
            }
        }

        infositeApiErrorObserver.subscribe {
            error = it
            makeDefaultError()
            val errorCodeString = error.errorCode?.name ?: ApiError.Code.UNMAPPED_ERROR.name

            HotelTracking.trackHotelDetailError(errorCodeString)
        }

        paramsSubject.subscribe(endlessObserver { params ->
            titleObservable.onNext(params.suggestion.regionNames.shortName ?: params.suggestion.regionNames.fullName)

            subTitleObservable.onNext(Phrase.from(context, R.string.start_dash_end_date_range_with_guests_TEMPLATE)
                    .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(params.checkIn))
                    .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(params.checkOut))
                    .put("guests", StrUtils.formatGuestString(context, params.guests))
                    .format()
                    .toString())
        })
    }

    override fun searchErrorHandler(): Observer<ApiError> {
        return endlessObserver {
            error = it
            when (error.errorCode) {
                ApiError.Code.INVALID_INPUT -> {
                    imageObservable.onNext(R.drawable.error_search)
                    errorMessageObservable.onNext(context.getString(R.string.error_no_result_message))
                    buttonOneTextObservable.onNext(context.getString(R.string.edit_search))
                    val errorMessage = "${error.errorCode!!.name}:${error.errorInfo?.field ?: ""}"
                    HotelTracking.trackHotelsNoResult(errorMessage)
                }
                ApiError.Code.HOTEL_SEARCH_NO_RESULTS -> {
                    imageObservable.onNext(R.drawable.error_search)
                    errorMessageObservable.onNext(context.getString(R.string.error_no_result_message))
                    buttonOneTextObservable.onNext(context.getString(R.string.edit_search))
                    HotelTracking.trackHotelsNoResult(error.errorCode!!.name)
                }
                ApiError.Code.HOTEL_MAP_SEARCH_NO_RESULTS -> {
                    imageObservable.onNext(R.drawable.error_search)
                    errorMessageObservable.onNext(context.getString(R.string.error_no_result_message))
                    buttonOneTextObservable.onNext(context.getString(R.string.edit_search))
                    titleObservable.onNext(context.getString(R.string.visible_map_area))
                    HotelTracking.trackHotelsNoResult(error.errorCode!!.name)
                }
                ApiError.Code.HOTEL_FILTER_NO_RESULTS -> {
                    imageObservable.onNext(R.drawable.error_search)
                    errorMessageObservable.onNext(context.getString(R.string.error_no_filter_result_message))
                    buttonOneTextObservable.onNext(context.getString(R.string.reset_filter))
                    HotelTracking.trackHotelsNoResult(error.errorCode!!.name)
                }
                ApiError.Code.HOTEL_PINNED_NOT_FOUND -> {
                    imageObservable.onNext(R.drawable.error_search)
                    errorMessageObservable.onNext(context.getString(R.string.error_no_pinned_result_message))
                    buttonOneTextObservable.onNext(context.getString(R.string.nearby_results))
                    HotelTracking.trackHotelsNoPinnedResult("Selected hotel not returned in position 0")
                }
                else -> {
                    makeDefaultError()
                    val errorCodeString = error.errorCode?.name ?: ApiError.Code.UNMAPPED_ERROR.name

                    HotelTracking.trackHotelsNoResult(errorCodeString)
                }
            }
        }
    }

    override fun createTripErrorHandler(): Observer<ApiError> {
        return endlessObserver {  }
    }

    override fun checkoutApiErrorHandler(): Observer<ApiError> {
        return endlessObserver {  }
    }
}
