package com.expedia.bookings.services

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.cars.BaseApiResponse
import com.expedia.bookings.data.lx.ActivityDetailsResponse
import com.expedia.bookings.data.lx.LXActivity
import com.expedia.bookings.data.lx.LXCategoryMetadata
import com.expedia.bookings.data.lx.LXCategorySortOrder
import com.expedia.bookings.data.lx.LXCheckoutParams
import com.expedia.bookings.data.lx.LXCheckoutResponse
import com.expedia.bookings.data.lx.LXCreateTripParams
import com.expedia.bookings.data.lx.LXCreateTripResponse
import com.expedia.bookings.data.lx.LXSearchParams
import com.expedia.bookings.data.lx.LXSearchResponse
import com.expedia.bookings.data.lx.LXSortFilterMetadata
import com.expedia.bookings.data.lx.LXSortType
import com.expedia.bookings.data.lx.RecommendedActivitiesResponse
import com.expedia.bookings.utils.CollectionUtils
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.LXUtils
import com.expedia.bookings.utils.Strings
import com.squareup.okhttp.OkHttpClient
import org.joda.time.LocalDate
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import retrofit.client.OkClient
import rx.Observable
import rx.Observer
import rx.Scheduler
import rx.Subscription
import rx.functions.Action1
import rx.functions.Func2
import java.util.Collections
import java.util.Comparator
import java.util.LinkedHashSet

class LXServices(endpoint: String, okHttpClient: OkHttpClient, requestInterceptor: RequestInterceptor,
                        val observeOn: Scheduler, val subscribeOn: Scheduler, logLevel: RestAdapter.LogLevel) {

    private var cachedLXSearchResponse = LXSearchResponse()

    val lxApi: LXApi by lazy {

        val adapter = RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setRequestInterceptor(requestInterceptor)
                .setLogLevel(logLevel)
                .setClient(OkClient(okHttpClient))
                .build()

        adapter.create(LXApi::class.java)
    }

    fun lxCategorySearch(searchParams: LXSearchParams, observer: Observer<LXSearchResponse>): Subscription {
        return lxApi.searchLXActivities(searchParams.location, searchParams.toServerStartDate(), searchParams.toServerEndDate())
                .doOnNext(HANDLE_SEARCH_ERROR)
                .doOnNext(ACTIVITIES_MONEY_TITLE)
                .doOnNext(PUT_POPULARITY_COUNTER_FOR_SORT)
                .doOnNext(CACHE_SEARCH_RESPONSE)
                .doOnNext(PUT_ACTIVITIES_IN_CATEGORY)
                .doOnNext(PUT_CATEGORY_KEY_IN_CATEGORY_METADATA)
                .doOnNext(PUT_CATEGORY_SORT_IN_CATEGORY_METADATA)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
    }

    fun lxSearch(searchParams: LXSearchParams, observer: Observer<LXSearchResponse>): Subscription {
        return lxSearch(searchParams).subscribe(observer)
    }

    fun lxSearch(searchParams: LXSearchParams): Observable<LXSearchResponse> {
        return lxApi.searchLXActivities(searchParams.location, searchParams.toServerStartDate(), searchParams.toServerEndDate())
                .observeOn(this.observeOn)
                .subscribeOn(this.subscribeOn)
                .doOnNext(HANDLE_SEARCH_ERROR)
                .doOnNext(ACTIVITIES_MONEY_TITLE)
                .doOnNext(CACHE_SEARCH_RESPONSE)
    }

    fun lxRecommendedSearch(activityId: String, location: String?, startDate: LocalDate, endDate: LocalDate,
                            observer: Observer<RecommendedActivitiesResponse>): Subscription {
        return lxApi.recommendedActivities(activityId, location, DateUtils.convertToLXDate(startDate), DateUtils.convertToLXDate(endDate))
                .observeOn(this.observeOn)
                .subscribeOn(this.subscribeOn)
                .subscribe(observer)
    }

    private val HANDLE_SEARCH_ERROR = { response: LXSearchResponse ->
        if (response.searchFailure) {
            val apiError = ApiError(ApiError.Code.LX_SEARCH_NO_RESULTS)
            apiError.regionId = response.regionId
            val errorInfo = ApiError.ErrorInfo()
            errorInfo.cause = "No results from api."
            apiError.errorInfo = errorInfo
            throw apiError
        }
    }

    private val PUT_POPULARITY_COUNTER_FOR_SORT = { response: LXSearchResponse ->
        var popularityForClientSort = 0
        for (activity in response.activities) {
            activity.popularityForClientSort = popularityForClientSort++
        }
    }

    private val PUT_CATEGORY_KEY_IN_CATEGORY_METADATA = { response: LXSearchResponse ->
        for (filterCategory in response.filterCategories.entries) {
            val categoryKeyEN = filterCategory.key
            val categoryValue = filterCategory.value
            categoryValue.categoryKeyEN = categoryKeyEN
        }
    }

    private val PUT_CATEGORY_SORT_IN_CATEGORY_METADATA = { response: LXSearchResponse ->
        for (filterCategory in response.filterCategories.entries) {
            val categoryKeyEN = filterCategory.key
            val categoryValue = filterCategory.value
            for (sortOrder in LXCategorySortOrder.values()) {
                if (LXUtils.whitelistAlphanumericFromCategoryKey(categoryKeyEN).equals(sortOrder.toString(), ignoreCase = true)) {
                    categoryValue.sortOrder = sortOrder
                    break
                }
            }
        }
    }

    private val PUT_ACTIVITIES_IN_CATEGORY = { response: LXSearchResponse ->
        for (filterCategory in response.filterCategories.entries) {
            val categoryKeyEN = filterCategory.key
            val categoryValue = filterCategory.value
            for (activity in response.activities) {
                if (CollectionUtils.isNotEmpty(activity.categories) && activity.categories.contains(categoryKeyEN)) {
                    categoryValue.activities.add(activity)
                }
            }
        }
    }

    fun lxDetails(activityId: String?, location: String?, startDate: LocalDate?, endDate: LocalDate?,
                  observer: Observer<ActivityDetailsResponse>): Subscription {
        return lxApi.activityDetails(activityId, location, DateUtils.convertToLXDate(startDate),
                DateUtils.convertToLXDate(endDate))
                .observeOn(this.observeOn)
                .subscribeOn(this.subscribeOn)
                .doOnNext(HANDLE_ACTIVITY_DETAILS_ERROR)
                .doOnNext(ACCEPT_ONLY_KNOWN_TICKET_TYPES)
                .doOnNext(DETAILS_TITLE_AND_TICKET_MONEY)
                .subscribe(observer)
    }

    private val HANDLE_ACTIVITY_DETAILS_ERROR = { response: ActivityDetailsResponse ->
        if (response == null || response.offersDetail == null || response!!.offersDetail.offers == null) {
            throw ApiError(ApiError.Code.LX_DETAILS_FETCH_ERROR)
        }
    }

    // Add money in offer tickets for easier handling and remove &quot; from activity and offer title.
    private val DETAILS_TITLE_AND_TICKET_MONEY = { response: ActivityDetailsResponse ->
        val bags = response.bags
        val passengers = response.passengers
        val isGroundTransport = response.isGroundTransport
        val redemptionType = response.redemptionType

        response.title = Strings.escapeQuotes(response.title)

        for (offer in response.offersDetail.offers) {
            offer.title = Strings.escapeQuotes(offer.title)
            offer.bags = bags
            offer.passengers = passengers
            offer.isGroundTransport = isGroundTransport
            offer.redemptionType = redemptionType
            for (availabilityInfo in offer.availabilityInfo) {
                for (ticket in availabilityInfo.tickets) {
                    ticket.money = Money(ticket.amount, response.currencyCode)
                }
            }
        }
    }

    private val ACCEPT_ONLY_KNOWN_TICKET_TYPES = { response: ActivityDetailsResponse ->
        val offerIterator = response.offersDetail.offers.iterator()
        //Iterate over offers
        while (offerIterator.hasNext()) {
            val offer = offerIterator.next()
            val availabilityInfoIterator = offer.availabilityInfo.iterator()
            //Iterate over Offers's Availability Infos
            while (availabilityInfoIterator.hasNext()) {
                val availabilityInfo = availabilityInfoIterator.next()
                val ticketIterator = availabilityInfo.tickets.iterator()
                //Iterate over Offers's Availability Info's Tickets
                while (ticketIterator.hasNext()) {
                    if (ticketIterator.next().code == null) {
                        //Remove Unknown Ticket Code
                        ticketIterator.remove()
                    }
                }

                //In case no tickets are known, rid off the availability info
                if (availabilityInfo.tickets.size == 0) {
                    availabilityInfoIterator.remove()
                }
            }

            //In case no availability info exists, rid off the offer
            if (offer.availabilityInfo.size == 0) {
                offerIterator.remove()
            }
        }
    }

    fun createTrip(createTripParams: LXCreateTripParams, originalPrice: Money, observer: Observer<LXCreateTripResponse>): Subscription {
        return lxApi.createTrip(createTripParams)
                .doOnNext(HANDLE_ERRORS)
                .doOnNext {
                    response: LXCreateTripResponse ->
                    if (response.hasPriceChange()) {
                        response.originalPrice = originalPrice
                    }
                }
                .observeOn(this.observeOn)
                .subscribeOn(this.subscribeOn)
                .subscribe(observer)
    }

    fun lxCheckout(checkoutParams: LXCheckoutParams, observer: Observer<LXCheckoutResponse>): Subscription {
        val originalPrice = Money(checkoutParams.expectedTotalFare, checkoutParams.expectedFareCurrencyCode)
        return lxApi.checkout(checkoutParams.toQueryMap())
                .doOnNext(HANDLE_ERRORS)
                .doOnNext {
                    response: LXCheckoutResponse ->
                    if (response.hasPriceChange()) {
                        response.originalPrice = originalPrice
                    }
                }
                .observeOn(this.observeOn)
                .subscribeOn(this.subscribeOn)
                .subscribe(observer)
    }

    private val HANDLE_ERRORS = { response: BaseApiResponse ->
        if (response.hasErrors() && !response.hasPriceChange()) {
            throw response.getFirstError()
        }
    }

    // Add money in tickets for easier handling and remove &quot; from activity title.
    private val ACTIVITIES_MONEY_TITLE = { response: LXSearchResponse ->
        val currencyCode = response.currencyCode
        for (activity in response.activities) {
            activity.price = Money(activity.fromPriceValue, currencyCode)
            activity.originalPrice = Money(activity.fromOriginalPriceValue, currencyCode)
            activity.title = Strings.escapeQuotes(activity.title)
        }
    }

    private fun CombineSearchResponseAndSortFilterStreams(lxSearchResponse: LXSearchResponse, lxSortFilterMetadata: LXSortFilterMetadata): LXSearchResponse {

        if (lxSortFilterMetadata.lxCategoryMetadataMap == null) {
            // No filters Applied.
            lxSearchResponse.activities.clear()
            lxSearchResponse.activities.addAll(lxSearchResponse.unFilteredActivities)
            for (filterCategory in lxSearchResponse.filterCategories.entries) {
                val lxCategoryMetadata = filterCategory.value
                lxCategoryMetadata.checked = false
            }
        } else {
            lxSearchResponse.activities = applySortFilter(lxSearchResponse.unFilteredActivities, lxSearchResponse,
                    lxSortFilterMetadata)
        }
        return lxSearchResponse
    }

    private fun SortCategorySearchResponse(lxCategoryMetadata: LXCategoryMetadata, lxSortType: LXSortType): LXCategoryMetadata {

        if (lxSortType == LXSortType.PRICE) {
            Collections.sort<LXActivity>(lxCategoryMetadata.activities, object : Comparator<LXActivity> {
                public override fun compare(lhs: LXActivity, rhs: LXActivity): Int {
                    val leftMoney = lhs.price
                    val rightMoney = rhs.price
                    return leftMoney.compareTo(rightMoney)
                }
            })
        } else if (lxSortType == LXSortType.POPULARITY) {
            Collections.sort<LXActivity>(lxCategoryMetadata.activities, object : Comparator<LXActivity> {
                public override fun compare(lhs: LXActivity, rhs: LXActivity): Int {
                    return if ((lhs.popularityForClientSort < rhs.popularityForClientSort))
                        -1
                    else
                        (if ((lhs.popularityForClientSort == rhs.popularityForClientSort)) 0 else 1)
                }
            })
        }
        return lxCategoryMetadata
    }


    fun lxCategorySort(category: LXCategoryMetadata, lxSortType: LXSortType, categorySortObserver: Observer<LXCategoryMetadata>): Subscription {

        return Observable.combineLatest(Observable.just(category), Observable.just(lxSortType),
                { lxCategoryMetadata, lxSortType ->
                    SortCategorySearchResponse(lxCategoryMetadata, lxSortType)
                })
                .subscribeOn(this.subscribeOn)
                .observeOn(this.observeOn)
                .subscribe(categorySortObserver)
    }

    fun lxSearchSortFilter(lxSearchParams: LXSearchParams?, lxSortFilterMetadata: LXSortFilterMetadata?,
                           searchResultFilterObserver: Observer<LXSearchResponse>): Subscription {

        val lxSearchResponseObservable = if (lxSearchParams == null)
            Observable.just<LXSearchResponse>(cachedLXSearchResponse) else lxSearch(lxSearchParams)

        return (
                if (lxSortFilterMetadata != null)
                    Observable.combineLatest(lxSearchResponseObservable, Observable.just(lxSortFilterMetadata),
                            { lxSearchResponse, lxSortFilterMetadata ->
                                CombineSearchResponseAndSortFilterStreams(lxSearchResponse, lxSortFilterMetadata)
                            })
                else lxSearch(lxSearchParams!!)
                )
                .doOnNext { isFromCachedResponseInjector(lxSearchParams == null, it) }
                .subscribeOn(this.subscribeOn)
                .observeOn(this.observeOn)
                .subscribe(searchResultFilterObserver)
    }

    private fun isFromCachedResponseInjector(isFromCachedResponse: Boolean, lxSearchResponse: LXSearchResponse) = {
        lxSearchResponse.isFromCachedResponse = isFromCachedResponse
    }

    fun applySortFilter(unfilteredActivities: List<LXActivity>, lxSearchResponse: LXSearchResponse, lxSortFilterMetadata: LXSortFilterMetadata): List<LXActivity> {

        val filteredSet = LinkedHashSet<LXActivity>()
        for (i in unfilteredActivities.indices) {
            for (filterCategory in lxSortFilterMetadata.lxCategoryMetadataMap.entries) {
                val lxCategoryMetadata = filterCategory.value
                val lxCategoryMetadataKey = filterCategory.key
                if (lxCategoryMetadata.checked) {
                    if (unfilteredActivities.get(i).categories.contains(lxCategoryMetadataKey)) {
                        filteredSet.add(unfilteredActivities.get(i))
                    }
                }
            }
        }

        lxSearchResponse.activities.clear()

        // Filtering.
        if (filteredSet.size != 0) {
            lxSearchResponse.activities.addAll(filteredSet)
        } else {
            lxSearchResponse.activities.addAll(unfilteredActivities)
        }

        // Sorting.
        if (lxSortFilterMetadata.sort == LXSortType.PRICE) {
            Collections.sort<LXActivity>(lxSearchResponse.activities, object : Comparator<LXActivity> {
                public override fun compare(lhs: LXActivity, rhs: LXActivity): Int {
                    val leftMoney = lhs.price
                    val rightMoney = rhs.price
                    return leftMoney.compareTo(rightMoney)
                }
            })
        }
        return lxSearchResponse.activities
    }


    private val CACHE_SEARCH_RESPONSE = { response: LXSearchResponse ->
        cachedLXSearchResponse = response
        cachedLXSearchResponse.unFilteredActivities.clear()
        cachedLXSearchResponse.unFilteredActivities.addAll(response.activities)
        cachedLXSearchResponse = response
    }
}
