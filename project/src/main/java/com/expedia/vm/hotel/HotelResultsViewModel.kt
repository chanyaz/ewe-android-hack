package com.expedia.vm.hotel

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotel.Sort
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.tracking.AdImpressionTracking
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.PackageResponseUtils
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import rx.Observer
import rx.Subscription
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class HotelResultsViewModel private constructor(private val context: Context, private val hotelServices: HotelServices?, private val packageServices: PackageServices?, private val lob: LineOfBusiness) {

    constructor(context: Context, hotelServices: HotelServices, lob: LineOfBusiness) : this(context, hotelServices, null, lob)

    constructor(context: Context, packageServices: PackageServices, lob: LineOfBusiness) : this(context, null, packageServices, lob)

    // Inputs
    val paramsSubject = PublishSubject.create<HotelSearchParams>()
    val locationParamsSubject = PublishSubject.create<SuggestionV4>()
    val filterParamsSubject = PublishSubject.create<UserFilterChoices>()

    // Outputs
    val searchingForHotelsDateTime = PublishSubject.create<Unit>()
    val resultsReceivedDateTimeObservable = PublishSubject.create<Unit>()
    val hotelResultsObservable = PublishSubject.create<HotelSearchResponse>()
    val mapResultsObservable = PublishSubject.create<HotelSearchResponse>()
    val filterResultsObservable = PublishSubject.create<HotelSearchResponse>()

    val errorObservable = PublishSubject.create<ApiError>()
    val titleSubject = BehaviorSubject.create<String>()
    val subtitleSubject = PublishSubject.create<CharSequence>()
    val showHotelSearchViewObservable = PublishSubject.create<Unit>()
    val sortByDeepLinkSubject = PublishSubject.create<Sort>()

    private var hotelSearchSubscription: Subscription? = null

    private var cachedParams: HotelSearchParams? = null

    init {
        paramsSubject.subscribe(endlessObserver { params ->
            doSearch(params)
        })

        locationParamsSubject.subscribe(endlessObserver { suggestion ->
            val builder = HotelSearchParams.Builder(context.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                    context.resources.getInteger(R.integer.max_calendar_selectable_date_range_hotels_only))
                    .destination(suggestion)
                    .startDate(cachedParams?.checkIn)
                    .endDate(cachedParams?.checkOut)
                    .adults(cachedParams?.adults!!)
                    .children(cachedParams?.children!!) as HotelSearchParams.Builder
            val params = builder.shopWithPoints(cachedParams?.shopWithPoints ?: false).build()

            doSearch(params)
        })

        filterParamsSubject.subscribe(endlessObserver { filterParams ->
            val builder = HotelSearchParams.Builder(context.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                    context.resources.getInteger(R.integer.max_calendar_selectable_date_range_hotels_only))
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
            trackAdImpression(it.pageViewBeaconPixelUrl)
        }

        mapResultsObservable.subscribe {
            trackAdImpression(it.pageViewBeaconPixelUrl)
        }
    }

    fun unsubscribeSearchResponse() {
        hotelSearchSubscription?.unsubscribe()
    }

    fun getSearchParams() : HotelSearchParams? {
        return cachedParams
    }

    private fun isRemoveBundleOverviewFeatureEnabled(): Boolean {
        return FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_packages_remove_bundle_overview) &&
                Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppPackagesRemoveBundleOverview)
    }

    private fun doSearch(params: HotelSearchParams, isFilteredSearch: Boolean = false) {
        cachedParams = params
        val isPackages = lob == LineOfBusiness.PACKAGES

        titleSubject.onNext(if (isPackages) StrUtils.formatCity(params.suggestion) else params.suggestion.regionNames.shortName)
        subtitleSubject.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                .put("startdate", DateUtils.localDateToMMMd(params.checkIn))
                .put("enddate", DateUtils.localDateToMMMd(params.checkOut))
                .put("guests", StrUtils.formatGuestString(context, params.guests))
                .format())
        searchingForHotelsDateTime.onNext(Unit)

        params.serverSort = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelServerSideFilter) && !isPackages
        if (lob == LineOfBusiness.HOTELS) {
            searchStandAloneHotels(params, isFilteredSearch)
        } else if (isPackages && isRemoveBundleOverviewFeatureEnabled() && !(context as AppCompatActivity).intent.hasExtra(Constants.PACKAGE_LOAD_HOTEL_ROOM)) {
            searchPackageHotels(Db.getPackageParams(), isFilteredSearch)
        }
    }

    private fun searchStandAloneHotels(params: HotelSearchParams, isFilteredSearch: Boolean) {
        val searchResponseObserver = object : Observer<HotelSearchResponse> {
            override fun onNext(hotelSearchResponse: HotelSearchResponse) {
                onSearchResponse(hotelSearchResponse, isFilteredSearch)
            }

            override fun onCompleted() {
                if (params.sortType != null) {
                    val sortType = getSortTypeFromString(params.sortType)
                    sortByDeepLinkSubject.onNext(sortType)
                }
            }

            override fun onError(e: Throwable?) {
                onError(e, isFilteredSearch)
            }
        }

        hotelSearchSubscription = hotelServices?.search(params, resultsReceivedDateTimeObservable,
                hitLPAS = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelLPASEndpoint))
                ?.subscribe(searchResponseObserver)
    }

    private fun searchPackageHotels(params: PackageSearchParams, isFilteredSearch: Boolean) {
        hotelSearchSubscription = packageServices?.packageSearch(params)?.subscribe(object : Observer<PackageSearchResponse> {
            override fun onNext(response: PackageSearchResponse) {
                if (response.hasErrors()) {
                    onResponseError(response.firstError)
                } else if (response.packageResult.hotelsPackage.hotels.isEmpty()) {
                    onResponseError(PackageApiError.Code.search_response_null)
                } else {
                    Db.setPackageResponse(response)
                    val currentFlights = arrayOf(response.packageResult.flightsPackage.flights[0].legId, response.packageResult.flightsPackage.flights[1].legId)
                    Db.getPackageParams().currentFlights = currentFlights
                    Db.getPackageParams().defaultFlights = currentFlights.copyOf()
                    PackageResponseUtils.savePackageResponse(context, response, PackageResponseUtils.RECENT_PACKAGE_HOTELS_FILE)
                    onSearchResponse(HotelSearchResponse.convertPackageToSearchResponse(response), isFilteredSearch)
                }
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                onError(e, isFilteredSearch)
            }
        })
    }

    private fun onError(e: Throwable?, isFilteredSearch: Boolean) {
        if (RetrofitUtils.isNetworkError(e)) {
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
        }
    }

    private fun onResponseError(code: PackageApiError.Code) {
        val intent = Intent()
        intent.putExtra(Constants.PACKAGE_API_ERROR, code.ordinal)
        (context as android.app.Activity).setResult(Constants.PACKAGE_API_ERROR_RESULT_CODE, intent)
        context.finish()
    }

    private fun onSearchResponse(hotelSearchResponse: HotelSearchResponse, isFiltered: Boolean) {
        if (titleSubject.value == null || (titleSubject.value != null && titleSubject.value.isEmpty())) {
            titleSubject.onNext(hotelSearchResponse.searchRegionCity)
        }

        if (hotelSearchResponse.hasErrors()) {
            errorObservable.onNext(hotelSearchResponse.firstError)
        } else if (hotelSearchResponse.hotelList.isEmpty()) {
            var error: ApiError
            if (isFiltered) {
                error = ApiError(ApiError.Code.HOTEL_FILTER_NO_RESULTS)
            } else {
                if (titleSubject.value == context.getString(R.string.visible_map_area)) {
                    error = ApiError(ApiError.Code.HOTEL_MAP_SEARCH_NO_RESULTS)
                } else {
                    error = ApiError(ApiError.Code.HOTEL_SEARCH_NO_RESULTS)
                }
            }
            errorObservable.onNext(error)
        } else if (isFiltered) {
            hotelSearchResponse.isFilteredResponse = true
            filterResultsObservable.onNext(hotelSearchResponse)
        } else if (titleSubject.value == context.getString(R.string.visible_map_area)) {
            mapResultsObservable.onNext(hotelSearchResponse)
        } else {
            hotelResultsObservable.onNext(hotelSearchResponse)
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

    private fun trackAdImpression(url: String) {
        AdImpressionTracking.trackAdClickOrImpressionWithTest(context, url, AbacusUtils.EBAndroidAppHotelServerSideFilter, null)
    }
}
