package com.expedia.bookings.hotel.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.extensions.subscribeObserver
import com.expedia.bookings.extensions.trackingString
import com.expedia.bookings.hotel.util.HotelCalendarDirections
import com.expedia.bookings.hotel.util.HotelCalendarRules
import com.expedia.bookings.hotel.util.HotelSearchManager
import com.expedia.bookings.model.HotelStayDates
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

class HotelResultsViewModel(context: Context, private val hotelSearchManager: HotelSearchManager) :
        BaseHotelResultsViewModel(context) {

    // inputs
    val locationParamsSubject = PublishSubject.create<SuggestionV4>()
    val dateChangedParamsSubject = PublishSubject.create<HotelStayDates>()

    // outputs
    val searchInProgressSubject = PublishSubject.create<Unit>()

    val searchingForHotelsDateTime = PublishSubject.create<Unit>()
    val resultsReceivedDateTimeObservable = PublishSubject.create<Unit>()

    val searchApiErrorObservable = PublishSubject.create<ApiError>()

    val paramChangedSubject = PublishSubject.create<HotelSearchParams>()

    val changeDateStringSubject = PublishSubject.create<String>()
    val guestStringSubject = PublishSubject.create<String>()

    var cachedResponse: HotelSearchResponse? = null
        private set

    private var isFilteredSearch = false
    private var apiSubscriptions = CompositeDisposable()

    private val hotelCalendarRules by lazy { HotelCalendarRules(context) }
    private val hotelCalendarDirections by lazy { HotelCalendarDirections(context) }

    init {
        paramsSubject.subscribe(endlessObserver { params ->
            doSearch(params)
        })

        locationParamsSubject.subscribe(endlessObserver { suggestion ->
            hotelSearchManager.reset()
            val builder = newParamBuilderFromParams(cachedParams, true).destination(suggestion)
            val newParams = builder.build()
            doSearch(newParams)
            paramChangedSubject.onNext(newParams)
        })

        filterChoicesSubject.subscribe(endlessObserver { filterChoices ->
            val builder = newParamBuilderFromParams(cachedParams, false)
            addFilterCriteria(builder, filterChoices)
            val newParams = builder.build()
            newParams.clearPinnedHotelId()
            doSearch(newParams)
            paramChangedSubject.onNext(newParams)
        })

        dateChangedParamsSubject.subscribe(endlessObserver { stayDates ->
            val builder = newParamBuilderFromParams(cachedParams, true)
                    .startDate(stayDates.getStartDate()).endDate(stayDates.getEndDate()) as HotelSearchParams.Builder
            val newParams = builder.build()
            doSearch(newParams, isChangeDateSearch = true)
            paramChangedSubject.onNext(newParams)
        })

        setupSearchSubscriptions()
    }

    fun clearSubscriptions() {
        apiSubscriptions.clear()
        hotelSearchManager.dispose()
    }

    fun unsubscribeSearchResponse() {
        hotelSearchManager.dispose()
    }

    private fun setupSearchSubscriptions() {
        apiSubscriptions.add(hotelSearchManager.apiCompleteSubject.subscribeObserver(resultsReceivedDateTimeObservable))
        apiSubscriptions.add(hotelSearchManager.successSubject.subscribe { response ->
            if (response.isPinnedSearch && !response.hasPinnedHotel()) {
                val error = ApiError(ApiError.Code.HOTEL_PINNED_NOT_FOUND)
                searchApiErrorObservable.onNext(error)
                cachedResponse = response
            } else {
                onSearchResponseSuccess(response)
            }
        })

        apiSubscriptions.add(hotelSearchManager.errorSubject.subscribeObserver(searchApiErrorObservable))

        apiSubscriptions.add(hotelSearchManager.noResultsSubject.subscribe {
            var error: ApiError
            if (isFilteredSearch) {
                error = ApiError(ApiError.Code.HOTEL_FILTER_NO_RESULTS)
            } else {
                if (titleSubject.value == context.getString(R.string.visible_map_area)) {
                    error = ApiError(ApiError.Code.HOTEL_MAP_SEARCH_NO_RESULTS)
                } else {
                    error = ApiError(ApiError.Code.HOTEL_SEARCH_NO_RESULTS)
                }
            }
            searchApiErrorObservable.onNext(error)
        })

        apiSubscriptions.add(hotelSearchManager.retrofitErrorSubject.subscribe { retrofitError ->
            HotelTracking.trackHotelsNoResult(retrofitError.trackingString())

            val cancelFun = fun() {
                showHotelSearchViewObservable.onNext(Unit)
            }
            val retryFun = fun() {
                if (cachedParams != null) {
                    doSearch(cachedParams!!, isFilteredSearch)
                } else {
                    cancelFun()
                }
            }
            DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
        })
    }

    private fun newParamBuilder(): HotelSearchParams.Builder {
        val maxStay = hotelCalendarRules.getMaxSearchDurationDays()
        val maxRange = hotelCalendarRules.getMaxDateRange()
        return HotelSearchParams.Builder(maxStay, maxRange)
    }

    private fun newParamBuilderFromParams(params: HotelSearchParams?, keepSortFilter: Boolean): HotelSearchParams.Builder {
        val builder = newParamBuilder()
        if (params != null) {
            builder.from(params, keepSortFilter)
        }
        return builder
    }

    private fun doSearch(params: HotelSearchParams, isChangeDateSearch: Boolean = false) {
        cachedParams = params
        isFilteredSearch = params.filterOptions?.isNotEmpty() == true
        updateTitles(params)
        updateChangeDateString(params)
        searchingForHotelsDateTime.onNext(Unit)
        if ((isChangeDateSearch || isFilteredSearch) && !hotelSearchManager.fetchingResults) {
            searchInProgressSubject.onNext(Unit)
            hotelSearchManager.doSearch(params)
        } else {
            val response = hotelSearchManager.fetchResponse()
            if (response != null) {
                onSearchResponseSuccess(response)
            } else {
                searchInProgressSubject.onNext(Unit)
                if (!hotelSearchManager.fetchingResults) {
                    hotelSearchManager.doSearch(params)
                }
            }
        }
    }

    private fun addFilterCriteria(searchBuilder: HotelSearchParams.Builder, filterParams: UserFilterChoices) {
        if (filterParams.name.isNotEmpty()) {
            searchBuilder.hotelName(filterParams.name)
        }

        if (filterParams.hotelStarRating.getStarRatingParamsAsList().isNotEmpty()) {
            searchBuilder.starRatings(filterParams.hotelStarRating.getStarRatingParamsAsList())
        }

        if (filterParams.hotelGuestRating.getGuestRatingParamAsList().isNotEmpty()) {
            searchBuilder.guestRatings(filterParams.hotelGuestRating.getGuestRatingParamAsList())
        }

        if (filterParams.hasPriceRange()) {
            searchBuilder.priceRange(HotelSearchParams.PriceRange(filterParams.minPrice, filterParams.maxPrice))
        }

        if (filterParams.isVipOnlyAccess) {
            searchBuilder.vipOnly(filterParams.isVipOnlyAccess)
        }
        if (filterParams.neighborhoods.isNotEmpty()) {
            searchBuilder.neighborhood(filterParams.neighborhoods.elementAt(0))
        }

        searchBuilder.amenities(filterParams.amenities)

        searchBuilder.userSort(filterParams.userSort.toServerSort())
    }

    private fun onSearchResponseSuccess(hotelSearchResponse: HotelSearchResponse) {
        if (titleSubject.value == null || (titleSubject.value != null && titleSubject.value.isEmpty())) {
            titleSubject.onNext(hotelSearchResponse.searchRegionCity)
        }
        if (isFilteredSearch) {
            hotelSearchResponse.isFilteredResponse = true
            filterResultsObservable.onNext(hotelSearchResponse)
        } else {
            hotelResultsObservable.onNext(hotelSearchResponse)
        }
    }

    private fun updateTitles(params: HotelSearchParams) {
        val title = params.suggestion.regionNames?.shortName ?: params.suggestion.regionNames.fullName ?: ""
        titleSubject.onNext(title)
        subtitleSubject.onNext(Phrase.from(context, R.string.start_dash_end_date_range_with_guests_TEMPLATE)
                .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(params.checkIn))
                .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(params.checkOut))
                .put("guests", StrUtils.formatGuestString(context, params.guests))
                .format())
        subtitleContDescSubject.onNext(
                Phrase.from(context, R.string.start_to_end_plus_guests_cont_desc_TEMPLATE)
                        .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(params.checkIn))
                        .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(params.checkOut))
                        .put("guests", StrUtils.formatGuestString(context, params.guests))
                        .format().toString())
    }

    private fun updateChangeDateString(params: HotelSearchParams) {
        val endDate = params.endDate ?: params.startDate.plusDays(1)
        changeDateStringSubject.onNext(hotelCalendarDirections.getCompleteDateText(params.startDate, endDate, false))
        guestStringSubject.onNext(StrUtils.formatGuestString(context, params.guests))
    }
}
