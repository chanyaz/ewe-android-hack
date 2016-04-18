package com.expedia.vm

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

class HotelErrorViewModel(private val context: Context) {
    // Inputs
    val apiErrorObserver = PublishSubject.create<ApiError>()
    val paramsSubject = PublishSubject.create<HotelSearchParams>()
    var error: ApiError by Delegates.notNull()

    // Outputs
    val imageObservable = BehaviorSubject.create<Int>()
    val buttonTextObservable = BehaviorSubject.create<String>()
    val errorMessageObservable = BehaviorSubject.create<String>()
    val titleObservable = BehaviorSubject.create<String>()
    val subTitleObservable = BehaviorSubject.create<String>()
    val actionObservable = BehaviorSubject.create<Unit>()
    val hotelSoldOutErrorObservable = PublishSubject.create<Boolean>()

    // Handle different errors
    val searchErrorObservable = BehaviorSubject.create<Unit>()
    val defaultErrorObservable = BehaviorSubject.create<Unit>()
    val checkoutCardErrorObservable = BehaviorSubject.create<Unit>()
    val checkoutTravellerErrorObservable = BehaviorSubject.create<Unit>()
    val checkoutUnknownErrorObservable = BehaviorSubject.create<Unit>()
    val productKeyExpiryObservable = BehaviorSubject.create<Unit>()
    val checkoutAlreadyBookedObservable = BehaviorSubject.create<Unit>()
    val checkoutPaymentFailedObservable = BehaviorSubject.create<Unit>()
    val sessionTimeOutObservable = BehaviorSubject.create<Unit>()
    val soldOutObservable = BehaviorSubject.create<Unit>()

    init {
        actionObservable.subscribe {
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
                ApiError.Code.HOTEL_CHECKOUT_CARD_DETAILS -> {
                    checkoutCardErrorObservable.onNext(Unit)
                    HotelV2Tracking().trackHotelsV2CheckoutErrorRetry()
                }
                ApiError.Code.HOTEL_CHECKOUT_TRAVELLER_DETAILS -> {
                    checkoutTravellerErrorObservable.onNext(Unit)
                    HotelV2Tracking().trackHotelsV2CheckoutErrorRetry()
                }
                ApiError.Code.HOTEL_PRODUCT_KEY_EXPIRY -> {
                    productKeyExpiryObservable.onNext(Unit)
                    HotelV2Tracking().trackHotelsV2CheckoutErrorRetry()
                }
                ApiError.Code.TRIP_ALREADY_BOOKED -> {
                    checkoutAlreadyBookedObservable.onNext(Unit)
                    HotelV2Tracking().trackHotelsV2CheckoutErrorRetry()
                }
                ApiError.Code.PAYMENT_FAILED -> {
                    checkoutPaymentFailedObservable.onNext(Unit)
                    HotelV2Tracking().trackHotelsV2CheckoutErrorRetry()
                }
                ApiError.Code.SESSION_TIMEOUT -> {
                    sessionTimeOutObservable.onNext(Unit)
                    HotelV2Tracking().trackHotelsV2CheckoutErrorRetry()
                }
                ApiError.Code.HOTEL_CHECKOUT_UNKNOWN -> {
                    checkoutUnknownErrorObservable.onNext(Unit)
                    HotelV2Tracking().trackHotelsV2CheckoutErrorRetry()
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
                ApiError.Code.HOTEL_SEARCH_NO_RESULTS -> {
                    imageObservable.onNext(R.drawable.error_default)
                    errorMessageObservable.onNext(context.getString(R.string.error_car_search_message))
                    buttonTextObservable.onNext(context.getString(R.string.edit_search))
                    HotelV2Tracking().trackHotelsV2NoResult()
                }
                ApiError.Code.HOTEL_MAP_SEARCH_NO_RESULTS -> {
                    imageObservable.onNext(R.drawable.error_default)
                    errorMessageObservable.onNext(context.getString(R.string.error_car_search_message))
                    buttonTextObservable.onNext(context.getString(R.string.edit_search))
                    titleObservable.onNext(context.getString(R.string.visible_map_area))
                    HotelV2Tracking().trackHotelsV2NoResult()
                }
                ApiError.Code.HOTEL_CHECKOUT_CARD_DETAILS -> {
                    imageObservable.onNext(R.drawable.error_payment)
                    if (error.errorInfo?.field == "nameOnCard") {
                        errorMessageObservable.onNext(context.getString(R.string.error_name_on_card_mismatch))
                    } else {
                        errorMessageObservable.onNext(context.getString(R.string.e3_error_checkout_payment_failed))
                    }
                    buttonTextObservable.onNext(context.getString(R.string.edit_payment))
                    titleObservable.onNext(context.getString(R.string.hotel_payment_failed_text))
                    subTitleObservable.onNext("")
                    HotelV2Tracking().trackHotelsV2CardError()
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
                    buttonTextObservable.onNext(context.getString(R.string.edit_guest_details))
                    titleObservable.onNext(context.getString(R.string.hotel_payment_failed_text))
                    subTitleObservable.onNext("")
                    HotelV2Tracking().trackHotelsV2TravelerError()
                }
                ApiError.Code.HOTEL_PRODUCT_KEY_EXPIRY -> {
                    imageObservable.onNext(R.drawable.error_default)
                    errorMessageObservable.onNext(context.getString(R.string.error_hotel_no_longer_available))
                    buttonTextObservable.onNext(context.getString(R.string.search_again))
                    HotelV2Tracking().trackHotelsV2ProductExpiredError()
                }
                ApiError.Code.HOTEL_ROOM_UNAVAILABLE -> {
                    hotelSoldOutErrorObservable.onNext(true)
                    imageObservable.onNext(R.drawable.error_default)
                    errorMessageObservable.onNext(context.getString(R.string.error_room_sold_out))
                    buttonTextObservable.onNext(context.getString(R.string.select_another_room))
                    HotelV2Tracking().trackPageLoadHotelV2SoldOut()
                }
                ApiError.Code.PAYMENT_FAILED -> {
                    imageObservable.onNext(R.drawable.error_payment)
                    errorMessageObservable.onNext(context.getString(R.string.e3_error_checkout_payment_failed))
                    buttonTextObservable.onNext(context.getString(R.string.edit_payment))
                    titleObservable.onNext(context.getString(R.string.hotel_payment_failed_text))
                    subTitleObservable.onNext("")
                    HotelV2Tracking().trackHotelsV2PaymentFailedError()
                }
                ApiError.Code.TRIP_ALREADY_BOOKED -> {
                    imageObservable.onNext(R.drawable.error_trip_booked)
                    errorMessageObservable.onNext(context.getString(R.string.reservation_already_exists_new_hotels))
                    buttonTextObservable.onNext(context.getString(R.string.my_trips))
                    titleObservable.onNext(context.getString(R.string.booking_complete))
                    subTitleObservable.onNext("")
                    HotelV2Tracking().trackHotelsV2TripAlreadyBookedError()
                }
                ApiError.Code.HOTEL_CHECKOUT_UNKNOWN -> {
                    imageObservable.onNext(R.drawable.no_hotel_error)
                    errorMessageObservable.onNext(context.getString(R.string.error_hotel_unhandled))
                    buttonTextObservable.onNext(context.getString(R.string.retry))
                    titleObservable.onNext(context.getString(R.string.hotel_payment_failed_text))
                    subTitleObservable.onNext("")
                    HotelV2Tracking().trackHotelsV2UnknownError()
                }
                ApiError.Code.SESSION_TIMEOUT -> {
                    imageObservable.onNext(R.drawable.error_timeout)
                    errorMessageObservable.onNext(context.getString(R.string.error_hotel_no_longer_available))
                    buttonTextObservable.onNext(context.getString(R.string.search_again))
                    titleObservable.onNext(context.getString(R.string.session_timeout))
                    subTitleObservable.onNext("")
                    buttonTextObservable.onNext(context.getString(R.string.search_again))
                    HotelV2Tracking().trackHotelsV2SessionTimeOutError()
                }
                else -> {
                    makeDefaultError()
                }

            }
        }

        paramsSubject.subscribe(endlessObserver { params ->
            titleObservable.onNext(params.suggestion.regionNames.shortName)

            subTitleObservable.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                    .put("startdate", DateUtils.localDateToMMMd(params.checkIn))
                    .put("enddate", DateUtils.localDateToMMMd(params.checkOut))
                    .put("guests", StrUtils.formatGuestString(context, params.guests))
                    .format()
                    .toString())
        })

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
}