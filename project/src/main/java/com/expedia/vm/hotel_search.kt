package com.expedia.vm

import android.app.Activity
import android.content.Context
import android.text.Html
import android.text.style.RelativeSizeSpan
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import com.mobiata.android.time.util.JodaUtils
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

class HotelSearchViewModel(val context: Context) {
    private val paramsBuilder = HotelSearchParams.Builder(context.resources.getInteger(R.integer.calendar_max_days_hotel_stay))

    // Outputs
    val searchParamsObservable = PublishSubject.create<HotelSearchParams>()
    val originObservable = BehaviorSubject.create<Boolean>()
    val externalSearchParamsObservable = BehaviorSubject.create<Boolean>()
    val dateTextObservable = PublishSubject.create<CharSequence>()
    val calendarTooltipTextObservable = PublishSubject.create<Pair<String, String>>()
    val locationTextObservable = PublishSubject.create<String>()
    val searchButtonObservable = PublishSubject.create<Boolean>()
    val errorNoOriginObservable = PublishSubject.create<Unit>()
    val errorNoDatesObservable = PublishSubject.create<Unit>()
    val errorMaxDatesObservable = PublishSubject.create<Unit>()
    val enableDateObservable = PublishSubject.create<Boolean>()
    val enableTravelerObservable = PublishSubject.create<Boolean>()

    val enableDateObserver = endlessObserver<Unit> {
        enableDateObservable.onNext(paramsBuilder.hasOrigin())
    }

    val enableTravelerObserver = endlessObserver<Unit> {
        enableTravelerObservable.onNext(paramsBuilder.hasOrigin())
    }


    // Inputs
    val datesObserver = endlessObserver<Pair<LocalDate?, LocalDate?>> { data ->
        val (start, end) = data

        paramsBuilder.checkIn(start)
        if (start != null && end == null) {
            paramsBuilder.checkOut(start.plusDays(1))
        } else {
            paramsBuilder.checkOut(end)
        }

        dateTextObservable.onNext(computeDateText(start, end))

        calendarTooltipTextObservable.onNext(computeTooltipText(start, end))

        requiredSearchParamsObserver.onNext(Unit)
    }

    var requiredSearchParamsObserver = endlessObserver<Unit> {
        searchButtonObservable.onNext(paramsBuilder.areRequiredParamsFilled())
        originObservable.onNext(paramsBuilder.hasOrigin())
    }

    val travelersObserver = endlessObserver<HotelTravelerParams> { update ->
        paramsBuilder.adults(update.numberOfAdults)
        paramsBuilder.children(update.children)
    }

    val suggestionObserver = endlessObserver<SuggestionV4> { suggestion ->
        paramsBuilder.suggestion(suggestion)
        locationTextObservable.onNext(Html.fromHtml(suggestion.regionNames.displayName).toString())
        requiredSearchParamsObserver.onNext(Unit)
    }

    val suggestionTextChangedObserver = endlessObserver<Unit> {
        paramsBuilder.suggestion(null)
        requiredSearchParamsObserver.onNext(Unit)
    }

    val searchObserver = endlessObserver<Unit> {
        if (paramsBuilder.areRequiredParamsFilled()) {
            if (!paramsBuilder.hasValidDates()) {
                errorMaxDatesObservable.onNext(Unit)
            } else {
                searchParamsObservable.onNext(paramsBuilder.build())
            }
        } else {
            if (!paramsBuilder.hasOrigin()) {
                errorNoOriginObservable.onNext(Unit)
            } else if (!paramsBuilder.hasStartAndEndDates()) {
                errorNoDatesObservable.onNext(Unit)
            }
        }
    }

    // Helpers
    private fun computeDateRangeText(start: LocalDate?, end: LocalDate?): String? {
        if (start == null && end == null) {
            return context.getResources().getString(R.string.select_dates)
        } else if (end == null) {
            return context.getResources().getString(R.string.select_checkout_date_TEMPLATE, DateUtils.localDateToMMMd(start))
        } else {
            return context.getResources().getString(R.string.calendar_instructions_date_range_TEMPLATE, DateUtils.localDateToMMMd(start), DateUtils.localDateToMMMd(end))
        }
    }

    private fun computeDateText(start: LocalDate?, end: LocalDate?): CharSequence {
        val dateRangeText = computeDateRangeText(start, end)
        val sb = SpannableBuilder()
        sb.append(dateRangeText)

        if (start != null && end != null) {
            val nightCount = JodaUtils.daysBetween(start, end)
            val nightsString = context.getResources().getQuantityString(R.plurals.length_of_stay, nightCount, nightCount)
            sb.append(" ");
            sb.append(context.getResources().getString(R.string.nights_count_TEMPLATE, nightsString), RelativeSizeSpan(0.8f))
        }
        return sb.build()
    }

    private fun computeTopTextForToolTip(start: LocalDate?, end: LocalDate?): String {
        if (start == null && end == null) {
            return context.getResources().getString(R.string.select_dates_proper_case)
        } else if (end == null) {
            return DateUtils.localDateToMMMd(start)
        } else {
            return context.getResources().getString(R.string.calendar_instructions_date_range_TEMPLATE, DateUtils.localDateToMMMd(start), DateUtils.localDateToMMMd(end))
        }
    }

    private fun computeTooltipText(start: LocalDate?, end: LocalDate?): Pair<String, String> {
        val resource =
                if (end == null) R.string.hotel_calendar_tooltip_bottom
                else R.string.hotel_calendar_bottom_drag_to_modify
        val instructions = context.getResources().getString(resource)
        return Pair(computeTopTextForToolTip(start, end), instructions)
    }

    init {
        val intent = (context as Activity).getIntent()
        externalSearchParamsObservable.onNext(!intent.hasExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS))
    }
}

public data class HotelTravelerParams(val numberOfAdults: Int, val children: List<Int>)

public class HotelTravelerPickerViewModel(val context: Context) {
    private val MAX_GUESTS = 6
    private val MIN_ADULTS = 1
    private val MIN_CHILDREN = 0
    private val MAX_CHILDREN = 4
    private val DEFAULT_CHILD_AGE = 10

    private var childAges = arrayListOf(DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE)

    // Outputs
    val travelerParamsObservable = BehaviorSubject.create(HotelTravelerParams(1, emptyList()))
    val guestsTextObservable = BehaviorSubject.create<CharSequence>()
    val adultTextObservable = BehaviorSubject.create<String>()
    val childTextObservable = BehaviorSubject.create<String>()
    val adultPlusObservable = BehaviorSubject.create<Boolean>()
    val adultMinusObservable = BehaviorSubject.create<Boolean>()
    val childPlusObservable = BehaviorSubject.create<Boolean>()
    val childMinusObservable = BehaviorSubject.create<Boolean>()

    init {
        travelerParamsObservable.subscribe { travelers ->
            val total = travelers.numberOfAdults + travelers.children.size()
            guestsTextObservable.onNext(
                    StrUtils.formatGuestString(context, total)
            )

            adultTextObservable.onNext(
                    context.getResources().getQuantityString(R.plurals.number_of_adults, travelers.numberOfAdults, travelers.numberOfAdults)
            )

            childTextObservable.onNext(
                    context.getResources().getQuantityString(R.plurals.number_of_children, travelers.children.size(), travelers.children.size())
            )

            adultPlusObservable.onNext(total < MAX_GUESTS)
            childPlusObservable.onNext(total < MAX_GUESTS && travelers.children.size() < MAX_CHILDREN)
            adultMinusObservable.onNext(travelers.numberOfAdults > MIN_ADULTS)
            childMinusObservable.onNext(travelers.children.size() > MIN_CHILDREN)
        }
    }

    // Inputs
    val incrementAdultsObserver: Observer<Unit> = endlessObserver {
        if (adultPlusObservable.getValue()) {
            val hotelTravelerParams = travelerParamsObservable.getValue()
            travelerParamsObservable.onNext(HotelTravelerParams(hotelTravelerParams.numberOfAdults + 1, hotelTravelerParams.children))
            HotelV2Tracking().trackTravelerPickerClick("Add.Adult")
        }
    }

    val decrementAdultsObserver: Observer<Unit> = endlessObserver {
        if (adultMinusObservable.getValue()) {
            val hotelTravelerParams = travelerParamsObservable.getValue()
            travelerParamsObservable.onNext(HotelTravelerParams(hotelTravelerParams.numberOfAdults - 1, hotelTravelerParams.children))
            HotelV2Tracking().trackTravelerPickerClick("Remove.Adult")
        }
    }

    val incrementChildrenObserver: Observer<Unit> = endlessObserver {
        if (childPlusObservable.getValue()) {
            val hotelTravelerParams = travelerParamsObservable.getValue()
            travelerParamsObservable.onNext(HotelTravelerParams(hotelTravelerParams.numberOfAdults, hotelTravelerParams.children.plus(10)))
            HotelV2Tracking().trackTravelerPickerClick("Add.Child")
        }
    }

    val decrementChildrenObserver: Observer<Unit> = endlessObserver {
        if (childMinusObservable.getValue()) {
            val hotelTravelerParams = travelerParamsObservable.getValue()
            travelerParamsObservable.onNext(HotelTravelerParams(hotelTravelerParams.numberOfAdults, hotelTravelerParams.children.subList(0, hotelTravelerParams.children.size() - 1)))
            HotelV2Tracking().trackTravelerPickerClick("Remove.Child")
        }
    }

    val childAgeSelectedObserver: Observer<Pair<Int, Int>> = endlessObserver { p ->
        val (which, age) = p
        childAges[which] = age

        val hotelTravelerParams = travelerParamsObservable.getValue()
        travelerParamsObservable.onNext(HotelTravelerParams(hotelTravelerParams.numberOfAdults, (0..hotelTravelerParams.children.size() - 1).map { childAges[it] }))
    }

}

public class HotelErrorViewModel(private val context: Context) {
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
                    errorMessageObservable.onNext(context.getString(R.string.e3_error_checkout_payment_failed))
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
                    imageObservable.onNext(R.drawable.error_default)
                    errorMessageObservable.onNext(context.getString(R.string.error_room_sold_out))
                    buttonTextObservable.onNext(context.getString(R.string.select_another_room))
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
                    .put("guests", StrUtils.formatGuestString(context, params.guests()))
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
