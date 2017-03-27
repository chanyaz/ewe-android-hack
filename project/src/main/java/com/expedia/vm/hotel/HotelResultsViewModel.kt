package com.expedia.vm.hotel

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.HotelFavoriteHelper
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotel.Sort
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.tracking.AdImpressionTracking
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import rx.Observer
import rx.Subscription
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList

class HotelResultsViewModel(private val context: Context, private val hotelServices: HotelServices?, private val lob: LineOfBusiness) {

    private val INITIAL_RESULTS_TO_BE_LOADED = 25
    private val ALL_RESULTS_TO_BE_LOADED = 200

    // Inputs
    val paramsSubject = BehaviorSubject.create<HotelSearchParams>()
    val locationParamsSubject = PublishSubject.create<SuggestionV4>()
    val filterParamsSubject = PublishSubject.create<UserFilterChoices>()

    // Outputs
    val searchingForHotelsDateTime = PublishSubject.create<Unit>()
    val resultsReceivedDateTimeObservable = PublishSubject.create<Unit>()
    val addHotelResultsObservable = PublishSubject.create<HotelSearchResponse>()
    val hotelResultsObservable = PublishSubject.create<HotelSearchResponse>()
    val mapResultsObservable = PublishSubject.create<HotelSearchResponse>()
    val filterResultsObservable = PublishSubject.create<HotelSearchResponse>()

    val errorObservable = PublishSubject.create<ApiError>()
    val titleSubject = BehaviorSubject.create<String>()
    val subtitleSubject = PublishSubject.create<CharSequence>()
    val showHotelSearchViewObservable = PublishSubject.create<Unit>()
    val sortByDeepLinkSubject = PublishSubject.create<Sort>()

    var isFavoringSupported: Boolean = lob == LineOfBusiness.HOTELS

    private var hotelSearchSubscription: Subscription? = null

    init {
        paramsSubject.subscribe(endlessObserver { params ->
            doSearch(params)
        })

        locationParamsSubject.subscribe(endlessObserver { suggestion ->
            val cachedParams: HotelSearchParams? = paramsSubject.value
            val builder = HotelSearchParams.Builder(context.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                    context.resources.getInteger(R.integer.calendar_max_selectable_date_range))
                    .destination(suggestion)
                    .startDate(cachedParams?.checkIn)
                    .endDate(cachedParams?.checkOut)
                    .adults(cachedParams?.adults!!)
                    .children(cachedParams?.children!!) as HotelSearchParams.Builder
            val params = builder.shopWithPoints(cachedParams?.shopWithPoints ?: false).build()

            doSearch(params)
        })

        filterParamsSubject.subscribe(endlessObserver { filterParams ->
            val cachedParams: HotelSearchParams? = paramsSubject.value
            val builder = HotelSearchParams.Builder(context.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                    context.resources.getInteger(R.integer.calendar_max_selectable_date_range))
                    .destination(cachedParams?.suggestion)
                    .startDate(cachedParams?.checkIn)
                    .endDate(cachedParams?.checkOut)
                    .adults(cachedParams?.adults!!)
                    .children(cachedParams?.children!!) as HotelSearchParams.Builder
            addFilterCriteria(builder, filterParams)
            val params = builder.shopWithPoints(cachedParams?.shopWithPoints ?: false).build()
            doSearch(params, true)
        })

        hotelResultsObservable.subscribe {
            AdImpressionTracking.trackAdClickOrImpression(context, it.pageViewBeaconPixelUrl, null)
        }

        mapResultsObservable.subscribe {
            AdImpressionTracking.trackAdClickOrImpression(context, it.pageViewBeaconPixelUrl, null)
        }
    }

    fun unsubscribeSearchResponse() {
        hotelSearchSubscription?.unsubscribe()
    }

    private fun doSearch(params: HotelSearchParams, isFilteredSearch: Boolean = false) {
        val isPackages = lob == LineOfBusiness.PACKAGES

        titleSubject.onNext(if (isPackages) StrUtils.formatCity(params.suggestion) else params.suggestion.regionNames.shortName)
        subtitleSubject.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                .put("startdate", DateUtils.localDateToMMMd(params.checkIn))
                .put("enddate", DateUtils.localDateToMMMd(params.checkOut))
                .put("guests", StrUtils.formatGuestString(context, params.guests))
                .format())
        searchingForHotelsDateTime.onNext(Unit)

        params.serverSort = FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_hotel_server_side_filters) && !isPackages
        searchHotels(params, isFilteredSearch)
    }

    private fun searchHotels(params: HotelSearchParams, isFilteredSearch: Boolean, isInitial: Boolean = true) {
        val isPerceivedInstant = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelResultsPerceivedInstantTest)
        val makeMultipleCalls = isInitial && isPerceivedInstant && !isFilteredSearch

        hotelSearchSubscription = hotelServices?.search(params, if (makeMultipleCalls) INITIAL_RESULTS_TO_BE_LOADED else ALL_RESULTS_TO_BE_LOADED, resultsReceivedDateTimeObservable)?.subscribe(object : Observer<HotelSearchResponse> {
            override fun onNext(hotelSearchResponse: HotelSearchResponse) {
                onSearchResponse(hotelSearchResponse, isFilteredSearch, isInitial)
                if (makeMultipleCalls) {
                    searchHotels(params, false, isFilteredSearch)
                }
            }

            override fun onCompleted() {
                if (params.sortType != null) {
                    val sortType = getSortTypeFromString(params.sortType)
                    sortByDeepLinkSubject.onNext(sortType)
                }
            }

            override fun onError(e: Throwable?) {
                if (RetrofitUtils.isNetworkError(e)) {
                    val retryFun = fun() {
                        doSearch(paramsSubject.value, isFilteredSearch)
                    }
                    val cancelFun = fun() {
                        showHotelSearchViewObservable.onNext(Unit)
                    }
                    DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
                }
            }
        })
    }

    private fun onSearchResponse(hotelSearchResponse: HotelSearchResponse, isFiltered: Boolean, isInitial: Boolean) {
        if (titleSubject.value == null || (titleSubject.value != null && titleSubject.value.isEmpty())) {
            titleSubject.onNext(hotelSearchResponse.searchRegionCity)
        }

        if (hotelSearchResponse.hasErrors()) {
            errorObservable.onNext(hotelSearchResponse.firstError)
        } else if (hotelSearchResponse.hotelList.isEmpty()) {
            var error: ApiError
            if (titleSubject.value == context.getString(R.string.visible_map_area)) {
                error = ApiError(ApiError.Code.HOTEL_MAP_SEARCH_NO_RESULTS)
            } else {
                error = ApiError(ApiError.Code.HOTEL_SEARCH_NO_RESULTS)
            }
            errorObservable.onNext(error)
        } else if (isFiltered) {
            hotelSearchResponse.isFilteredResponse = true
            filterResultsObservable.onNext(hotelSearchResponse)
        } else if (titleSubject.value == context.getString(R.string.visible_map_area)) {
            mapResultsObservable.onNext(hotelSearchResponse)
        } else {
            if (isInitial) {
                hotelResultsObservable.onNext(hotelSearchResponse)
            } else {
                addHotelResultsObservable.onNext(hotelSearchResponse)
            }
            HotelFavoriteHelper.setLocalFavorites(hotelSearchResponse.hotelList as ArrayList<Hotel>, context)
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

    private fun getSortTypeFromString(sortType: String?): Sort {
        when (sortType?.toLowerCase()) {
            "discounts" -> return Sort.DEALS
            "deals" -> return Sort.DEALS
            "price" -> return Sort.PRICE
            "rating" -> return Sort.RATING
            else -> {
                return Sort.RECOMMENDED
            }
        }
    }
}
