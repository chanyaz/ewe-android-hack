package com.expedia.bookings.hotel.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.hotel.util.HotelSearchManager
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import rx.subjects.PublishSubject

class HotelResultsViewModel(context: Context, private val hotelSearchManager: HotelSearchManager) :
        BaseHotelResultsViewModel(context) {

    // inputs
    val filterParamsSubject = PublishSubject.create<UserFilterChoices>()
    val locationParamsSubject = PublishSubject.create<SuggestionV4>()

    // outputs
    val searchInProgressSubject = PublishSubject.create<Unit>()

    val filterResultsObservable = PublishSubject.create<HotelSearchResponse>()
    val mapResultsObservable = PublishSubject.create<HotelSearchResponse>()

    val searchingForHotelsDateTime = PublishSubject.create<Unit>()
    val resultsReceivedDateTimeObservable = PublishSubject.create<Unit>()

    private var isFilteredSearch = false
    private var cachedParams: HotelSearchParams? = null

    init {
        mapResultsObservable.subscribe {
            trackAdImpression(it.pageViewBeaconPixelUrl)
        }

        paramsSubject.subscribe(endlessObserver { params -> doSearch(params) })

        locationParamsSubject.subscribe(endlessObserver { suggestion ->
            hotelSearchManager.reset()
            val paramBuilder = newParamBuilder(suggestion, cachedParams)
            doSearch(paramBuilder.build())
        })

        filterParamsSubject.subscribe(endlessObserver { filterParams ->
            val paramBuilder = newParamBuilder(cachedParams?.suggestion, cachedParams)
            addFilterCriteria(paramBuilder, filterParams)
            val newParams = paramBuilder.build()
            newParams?.clearPinnedHotelId()
            doSearch(newParams, true)
        })

        hotelSearchManager.apiCompleteSubject.subscribe(resultsReceivedDateTimeObservable)
        hotelSearchManager.successSubject.subscribe { response ->
            onSearchResponseSuccess(response)
        }

        hotelSearchManager.errorSubject.subscribe(errorObservable)
        hotelSearchManager.noResultsSubject.subscribe {
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
            errorObservable.onNext(error)
        }

        hotelSearchManager.noInternetSubject.subscribe {
            val cancelFun = fun() {
                showHotelSearchViewObservable.onNext(Unit)
            }
            val retryFun = fun() {
                if (cachedParams != null) {
                    doSearch(cachedParams!!, isFilteredSearch)
                } else {
                    cancelFun
                }
            }
            DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
        }
    }

    fun unsubscribeSearchResponse() {
        hotelSearchManager.unsubscribe()
    }

    fun getSearchParams() : HotelSearchParams? {
        return cachedParams
    }

    private fun newParamBuilder(suggestion: SuggestionV4?, params: HotelSearchParams?) : HotelSearchParams.Builder {
        val builder = HotelSearchParams.Builder(context.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                context.resources.getInteger(R.integer.max_calendar_selectable_date_range_hotels_only))
                .destination(suggestion)
                .startDate(params?.checkIn)
                .endDate(params?.checkOut)
                .adults(params?.adults!!)
                .children(params?.children!!) as HotelSearchParams.Builder
        return builder.shopWithPoints(params?.shopWithPoints ?: false)
    }

    private fun doSearch(params: HotelSearchParams, isFilteredSearch: Boolean = false) {
        cachedParams = params
        this.isFilteredSearch = isFilteredSearch
        updateTitles(params)
        searchingForHotelsDateTime.onNext(Unit)
        if (isFilteredSearch && !hotelSearchManager.fetchingResults) {
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

        if (filterParams.hasPriceRange()) {
            searchBuilder.priceRange(HotelSearchParams.PriceRange(filterParams.minPrice, filterParams.maxPrice))
        }

        if (filterParams.isVipOnlyAccess) {
            searchBuilder.vipOnly(filterParams.isVipOnlyAccess)
        }
        if (filterParams.neighborhoods.isNotEmpty()) {
            searchBuilder.neighborhood(filterParams.neighborhoods.elementAt(0).id)
        }
        searchBuilder.userSort(filterParams.userSort.toServerSort())
    }

     private fun onSearchResponseSuccess(hotelSearchResponse: HotelSearchResponse) {
        if (titleSubject.value == null || (titleSubject.value != null && titleSubject.value.isEmpty())) {
            titleSubject.onNext(hotelSearchResponse.searchRegionCity)
        }
        if (isFilteredSearch) {
            hotelSearchResponse.isFilteredResponse = true
            filterResultsObservable.onNext(hotelSearchResponse)
        } else if (titleSubject.value == context.getString(R.string.visible_map_area)) {
            mapResultsObservable.onNext(hotelSearchResponse)
        } else {
            hotelSearchResponse.isPinnedSearch = cachedParams?.isPinnedSearch() ?: false
            hotelResultsObservable.onNext(hotelSearchResponse)
        }
    }

    private fun updateTitles(params: HotelSearchParams) {
        val title = params.suggestion.regionNames?.shortName ?: params.suggestion.regionNames.fullName
        titleSubject.onNext(title)
        subtitleSubject.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(params.checkIn))
                .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(params.checkOut))
                .put("guests", StrUtils.formatGuestString(context, params.guests))
                .format())
    }
}