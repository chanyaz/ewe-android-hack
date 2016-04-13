package com.expedia.vm

import android.app.Activity
import android.content.Context
import android.location.Location
import android.text.Html
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.presenter.hotel.HotelPresenter
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.HotelSearchParamsUtil
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.ui.HotelActivity
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.mobiata.android.Log
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import javax.inject.Inject
import kotlin.properties.Delegates

class HotelSearchViewModel(context: Context) : DatedSearchViewModel(context) {
    override val paramsBuilder = HotelSearchParams.Builder(context.resources.getInteger(R.integer.calendar_max_days_hotel_stay))

    val userBucketedObservable = BehaviorSubject.create<Boolean>()
    val externalSearchParamsObservable = BehaviorSubject.create<Boolean>()
    val searchParamsObservable = PublishSubject.create<HotelSearchParams>()

    // Outputs

    var shopWithPointsViewModel: ShopWithPointsViewModel by notNullAndObservable {
        it.swpEffectiveAvailability.subscribe{
            paramsBuilder.shopWithPoints(it)
        }
    }
        @Inject set

    val maxHotelStay = context.resources.getInteger(R.integer.calendar_max_days_hotel_stay)

    // Inputs
    var requiredSearchParamsObserver = endlessObserver<Unit> {
        searchButtonObservable.onNext(paramsBuilder.areRequiredParamsFilled())
        originObservable.onNext(paramsBuilder.hasDeparture())
    }

    val suggestionObserver = endlessObserver<SuggestionV4> { suggestion ->
        paramsBuilder.departure(suggestion)
        locationTextObservable.onNext(Html.fromHtml(suggestion.regionNames.displayName).toString())
        requiredSearchParamsObserver.onNext(Unit)
    }

    val suggestionTextChangedObserver = endlessObserver<Unit> {
        paramsBuilder.departure(null)
        requiredSearchParamsObserver.onNext(Unit)
    }

    val searchObserver = endlessObserver<Unit> {
        if (paramsBuilder.areRequiredParamsFilled()) {
            if (!paramsBuilder.hasValidDates()) {
                errorMaxDatesObservable.onNext(context.getString(R.string.hotel_search_range_error_TEMPLATE, maxHotelStay))
            } else {
                val hotelSearchParams = paramsBuilder.build()
                HotelSearchParamsUtil.saveSearchHistory(context, hotelSearchParams)

                searchParamsObservable.onNext(hotelSearchParams)
            }
        } else {
            if (!paramsBuilder.hasDeparture()) {
                errorNoOriginObservable.onNext(Unit)
            } else if (!paramsBuilder.hasStartAndEndDates()) {
                errorNoDatesObservable.onNext(Unit)
            }
        }
    }

    override fun onDatesChanged(dates: Pair<LocalDate?, LocalDate?>) {
        val (start, end) = dates

        paramsBuilder.startDate(start)
        if (start != null && end == null) {
            paramsBuilder.endDate(start.plusDays(1))
        } else {
            paramsBuilder.endDate(end)
        }

        dateTextObservable.onNext(computeDateText(start, end))
        dateInstructionObservable.onNext(computeDateInstructionText(start, end))

        calendarTooltipTextObservable.onNext(computeTooltipText(start, end))

        requiredSearchParamsObserver.onNext(Unit)
        datesObservable.onNext(dates)
    }

    init {
        val intent = (context as Activity).intent
        val isUserBucketedForTest = Db.getAbacusResponse().isUserBucketedForTest(
                AbacusUtils.EBAndroidAppHotelRecentSearchTest)
        userBucketedObservable.onNext(isUserBucketedForTest)
        externalSearchParamsObservable.onNext(!intent.hasExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS) && !isUserBucketedForTest)
        Ui.getApplication(context).hotelComponent().inject(this)
    }
}

class HotelTravelerPickerViewModel(val context: Context) {
    var showSeatingPreference = false
    var lob = LineOfBusiness.HOTELS
        set(value) {
            field = value
            val travelers = travelerParamsObservable.value
            makeTravelerText(travelers)
        }

    private val MAX_GUESTS = 6
    private val MIN_ADULTS = 1
    private val MIN_CHILDREN = 0
    private val MAX_CHILDREN = 4
    private val DEFAULT_CHILD_AGE = 10

    private var childAges = arrayListOf(DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE, DEFAULT_CHILD_AGE)

    // Outputs
    val travelerParamsObservable = BehaviorSubject.create(TravelerParams(1, emptyList()))
    val guestsTextObservable = BehaviorSubject.create<CharSequence>()
    val adultTextObservable = BehaviorSubject.create<String>()
    val childTextObservable = BehaviorSubject.create<String>()
    val adultPlusObservable = BehaviorSubject.create<Boolean>()
    val adultMinusObservable = BehaviorSubject.create<Boolean>()
    val childPlusObservable = BehaviorSubject.create<Boolean>()
    val childMinusObservable = BehaviorSubject.create<Boolean>()
    val infantPreferenceSeatingObservable = BehaviorSubject.create<Boolean>(false)
    val isInfantInLapObservable = BehaviorSubject.create<Boolean>(false)
    val tooManyInfants = PublishSubject.create<Boolean>()

    init {
        travelerParamsObservable.subscribe { travelers ->
            val total = travelers.numberOfAdults + travelers.childrenAges.size
            makeTravelerText(travelers)

            adultTextObservable.onNext(
                    context.resources.getQuantityString(R.plurals.number_of_adults, travelers.numberOfAdults, travelers.numberOfAdults)
            )

            childTextObservable.onNext(
                    context.resources.getQuantityString(R.plurals.number_of_children, travelers.childrenAges.size, travelers.childrenAges.size)
            )

            adultPlusObservable.onNext(total < MAX_GUESTS)
            childPlusObservable.onNext(total < MAX_GUESTS && travelers.childrenAges.size < MAX_CHILDREN)
            adultMinusObservable.onNext(travelers.numberOfAdults > MIN_ADULTS)
            childMinusObservable.onNext(travelers.childrenAges.size > MIN_CHILDREN)
            validateInfants()
        }
        isInfantInLapObservable.subscribe { inLap ->
            validateInfants()
        }
    }

    // Inputs
    val incrementAdultsObserver: Observer<Unit> = endlessObserver {
        if (adultPlusObservable.value) {
            val hotelTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(hotelTravelerParams.numberOfAdults + 1, hotelTravelerParams.childrenAges))
            HotelV2Tracking().trackTravelerPickerClick("Add.Adult")
        }
    }

    val decrementAdultsObserver: Observer<Unit> = endlessObserver {
        if (adultMinusObservable.value) {
            val hotelTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(hotelTravelerParams.numberOfAdults - 1, hotelTravelerParams.childrenAges))
            HotelV2Tracking().trackTravelerPickerClick("Remove.Adult")
        }
    }

    val incrementChildrenObserver: Observer<Unit> = endlessObserver {
        if (childPlusObservable.value) {
            val hotelTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(hotelTravelerParams.numberOfAdults, hotelTravelerParams.childrenAges.plus(childAges[hotelTravelerParams.childrenAges.size])))
            HotelV2Tracking().trackTravelerPickerClick("Add.Child")
        }
    }

    val decrementChildrenObserver: Observer<Unit> = endlessObserver {
        if (childMinusObservable.value) {
            val hotelTravelerParams = travelerParamsObservable.value
            travelerParamsObservable.onNext(TravelerParams(hotelTravelerParams.numberOfAdults, hotelTravelerParams.childrenAges.subList(0, hotelTravelerParams.childrenAges.size - 1)))
            HotelV2Tracking().trackTravelerPickerClick("Remove.Child")
        }
    }

    val childAgeSelectedObserver: Observer<Pair<Int, Int>> = endlessObserver { p ->
        val (which, age) = p
        childAges[which] = age
        val hotelTravelerParams = travelerParamsObservable.value
        val children = hotelTravelerParams.childrenAges.toIntArray()
        if (children.size > which) {
            children[which] = childAges[which]
        }
        travelerParamsObservable.onNext(TravelerParams(hotelTravelerParams.numberOfAdults, children.toList()))
    }

    private fun validateInfants() {
        val hotelTravelerParams = travelerParamsObservable.value
        infantPreferenceSeatingObservable.onNext(hotelTravelerParams.childrenAges.contains(0))
        val numberOfInfants = hotelTravelerParams.childrenAges.count { childAge -> childAge < 2 }
        tooManyInfants.onNext(isInfantInLapObservable.value && (numberOfInfants > hotelTravelerParams.numberOfAdults))
    }

    fun makeTravelerText(travelers: TravelerParams) {
        val total = travelers.numberOfAdults + travelers.childrenAges.size
        guestsTextObservable.onNext(
                if (lob == LineOfBusiness.PACKAGES) {
                    StrUtils.formatTravelerString(context, total)
                } else {
                    StrUtils.formatGuestString(context, total)
                }
        )
    }
}

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

class HotelDeepLinkHandler(private val context: Context, private val deepLinkSearchObserver: Observer<HotelSearchParams?>, private val suggestionLookupObserver: Observer<Pair<String, Observer<List<SuggestionV4>>>>, private val currentLocationSearchObserver: Observer<HotelSearchParams?>, private val hotelPresenter: HotelPresenter, private val searchSuggestionObserver: Observer<SuggestionV4>) {
    fun handleNavigationViaDeepLink(hotelSearchParams: HotelSearchParams?) {
        if (hotelSearchParams != null) {
            val lat = hotelSearchParams.suggestion.coordinates?.lat ?: 0.0
            val lon = hotelSearchParams.suggestion.coordinates?.lng ?: 0.0
            // explicit check against MY_LOCATION is required here because current logic in SuggestionV4.isCurrentLocationSearch does not
            // yield correct answer for HotelSearchParams generated by deep links
            val isCurrentLocationSearch = com.expedia.bookings.data.HotelSearchParams.SearchType.MY_LOCATION.name.equals(hotelSearchParams.suggestion.type)
            if (isCurrentLocationSearch) {
                hotelSearchParams.suggestion.regionNames?.displayName = context.getString(R.string.current_location)
                hotelSearchParams.suggestion.regionNames?.shortName = context.getString(R.string.current_location)
                if (lat == 0.0 && lon == 0.0)
                    CurrentLocationObservable.create(context).subscribe(generateLocationServiceCallback(hotelSearchParams))
                else {
                    currentLocationSearchObserver.onNext(hotelSearchParams)
                }
            } else {
                searchSuggestionObserver.onNext(hotelSearchParams.suggestion)
                if (hotelSearchParams.suggestion.hotelId != null) {
                    // go to specific hotel requested
                    deepLinkSearchObserver.onNext(hotelSearchParams)
                    hotelPresenter.setDefaultTransition(HotelActivity.Screen.DETAILS)
                } else if (hotelSearchParams.suggestion.gaiaId != null || lat != 0.0 || lon != 0.0) {
                    // search specified region or lat/lon
                    hotelPresenter.setDefaultTransition(HotelActivity.Screen.RESULTS)
                    deepLinkSearchObserver.onNext(hotelSearchParams)
                } else {
                    val displayName = hotelSearchParams.suggestion.regionNames?.displayName ?: ""
                    if (displayName.length > 0 ) {
                        // get suggestion for searched location
                        suggestionLookupObserver.onNext(Pair(displayName, generateSuggestionServiceCallback(hotelSearchParams)))
                    } else {
                        // this should not happen unless something has gone very wrong, so just send user to search screen
                        hotelPresenter.setDefaultTransition(HotelActivity.Screen.SEARCH)
                    }
                }
            }

        }
    }

    private fun generateLocationServiceCallback(hotelSearchParams: HotelSearchParams?): Observer<Location> {
        return object : Observer<Location> {
            override fun onNext(location: Location) {
                val coordinate = SuggestionV4.LatLng()
                coordinate.lat = location.latitude
                coordinate.lng = location.longitude
                hotelSearchParams?.suggestion?.coordinates = coordinate
                currentLocationSearchObserver.onNext(hotelSearchParams)
            }

            override fun onCompleted() {
                // ignore
            }

            override fun onError(e: Throwable?) {
                hotelPresenter.setDefaultTransition(HotelActivity.Screen.SEARCH)
            }
        }
    }

    private fun generateSuggestionServiceCallback(hotelSearchParams: HotelSearchParams): Observer<List<SuggestionV4>> {
        return object : Observer<List<SuggestionV4>> {
            override fun onNext(essSuggestions: List<SuggestionV4>) {
                hotelPresenter.setDefaultTransition(HotelActivity.Screen.RESULTS)
                hotelSearchParams.suggestion.gaiaId = essSuggestions.first().gaiaId
                deepLinkSearchObserver.onNext(hotelSearchParams)
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                hotelPresenter.setDefaultTransition(HotelActivity.Screen.SEARCH)
                Log.e("Hotel Suggestions Error", e)
            }
        }
    }
}
