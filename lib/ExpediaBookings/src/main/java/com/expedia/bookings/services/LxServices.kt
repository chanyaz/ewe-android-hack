package com.expedia.bookings.services

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.BaseApiResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.lx.ActivityDetailsResponse
import com.expedia.bookings.data.lx.LXActivity
import com.expedia.bookings.data.lx.LXCategoryType
import com.expedia.bookings.data.lx.LXCheckoutParams
import com.expedia.bookings.data.lx.LXCheckoutResponse
import com.expedia.bookings.data.lx.LXCreateTripParams
import com.expedia.bookings.data.lx.LXCreateTripResponse
import com.expedia.bookings.data.lx.LXCreateTripResponseV2
import com.expedia.bookings.data.lx.LXSearchResponse
import com.expedia.bookings.data.lx.LXSortFilterMetadata
import com.expedia.bookings.data.lx.LXSortType
import com.expedia.bookings.data.lx.LXTheme
import com.expedia.bookings.data.lx.LXThemeType
import com.expedia.bookings.data.lx.LxSearchParams
import com.expedia.bookings.utils.CollectionUtils
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.LXUtils
import com.expedia.bookings.utils.Strings
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.joda.time.LocalDate
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observable
import rx.Observer
import rx.Scheduler
import rx.Subscription
import java.util.Collections
import java.util.Comparator
import java.util.LinkedHashSet

class LxServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    private var cachedLXSearchResponse = LXSearchResponse()

    val lxApi: LXApi by lazy {

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()

        adapter.create(LXApi::class.java)
    }

    fun lxCategorySearch(searchParams: LxSearchParams, observer: Observer<LXSearchResponse>): Subscription {
        return lxApi.searchLXActivities(searchParams.location, searchParams.toServerStartDate(), searchParams.toServerEndDate(), searchParams.modQualified)
                .doOnNext(HANDLE_SEARCH_ERROR)
                .doOnNext(ACTIVITIES_MONEY_TITLE)
                .doOnNext(PUT_POPULARITY_COUNTER_FOR_SORT)
                .doOnNext(CACHE_SEARCH_RESPONSE)
                .doOnNext(PUT_CATEGORY_KEY_IN_CATEGORY_METADATA)
                .doOnNext(PUT_CATEGORY_TYPE_IN_CATEGORY_METADATA)
                .doOnNext(PUT_CATEGORIES_AND_ACTIVITES_IN_THEME)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
    }

    fun lxSearch(searchParams: LxSearchParams, observer: Observer<LXSearchResponse>): Subscription {
        return lxSearch(searchParams).subscribe(observer)
    }

    fun lxSearch(searchParams: LxSearchParams): Observable<LXSearchResponse> {
        return lxApi.searchLXActivities(searchParams.location, searchParams.toServerStartDate(), searchParams.toServerEndDate(), searchParams.modQualified)
                .observeOn(this.observeOn)
                .subscribeOn(this.subscribeOn)
                .doOnNext(HANDLE_SEARCH_ERROR)
                .doOnNext(ACTIVITIES_MONEY_TITLE)
                .doOnNext(CACHE_SEARCH_RESPONSE)
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

    private val PUT_CATEGORY_TYPE_IN_CATEGORY_METADATA = { response: LXSearchResponse ->
        for (filterCategory in response.filterCategories.entries) {
            val categoryKeyEN = filterCategory.key
            val categoryValue = filterCategory.value
            for (sortOrder in LXCategoryType.values()) {
                if (LXUtils.whitelistAlphanumericFromCategoryKey(categoryKeyEN).equals(sortOrder.toString(), ignoreCase = true)) {
                    categoryValue.categoryType = sortOrder
                    break
                }
            }
        }
    }

    private val PUT_CATEGORIES_AND_ACTIVITES_IN_THEME = { response: LXSearchResponse ->
        for (themeType in LXThemeType.values()) {
            val theme = LXTheme(themeType)

            when (themeType) {
                LXThemeType.AllThingsToDo -> {
                    theme.filterCategories.putAll(response.filterCategories)
                    theme.activities.addAll(response.activities)
                    theme.unfilteredActivities.addAll(response.activities)
                }
                LXThemeType.TopRatedActivities -> {
                    theme.activities.addAll(response.activities.take(10))
                    theme.unfilteredActivities.addAll(response.activities.take(10))
                }
                else -> {
                    // Traverse the category, match the category in Themes -> Add category
                    for (filterCategory in response.filterCategories.entries) {
                        val categoryKeyEN = filterCategory.key
                        val categoryValue = filterCategory.value
                        for (themeCategory in theme.themeType.categories) {
                            if (LXUtils.whitelistAlphanumericFromCategoryKey(categoryKeyEN).equals(themeCategory.toString(), ignoreCase = true)) {
                                theme.filterCategories.put(categoryKeyEN, categoryValue)
                                categoryValue.categoryType = themeCategory
                                break
                            }
                        }
                    }

                    // Traverse the category in themes, match the activity in response -> Add Activity
                    val filteredSet = LinkedHashSet<LXActivity>()
                    for (filterCategory in theme.filterCategories.entries) {
                        val categoryKeyEN = filterCategory.key
                        for (activity in response.activities) {
                            if (CollectionUtils.isNotEmpty(activity.categories) && activity.categories.contains(categoryKeyEN)) {
                                filteredSet.add(activity)
                            }
                        }
                    }
                    theme.activities.addAll(filteredSet)
                    theme.unfilteredActivities.addAll(filteredSet)
                }
            }
            if (theme.activities.size > 0 ) {
                response.lxThemes.add(theme)
            }

        }
    }

    fun lxDetails(activityId: String?, location: String?, startDate: LocalDate?, endDate: LocalDate?, promoPricingEnabled: Boolean,
                  observer: Observer<ActivityDetailsResponse>): Subscription {
        return lxApi.activityDetails(activityId, location, DateUtils.convertToLXDate(startDate),
                DateUtils.convertToLXDate(endDate), promoPricingEnabled)
                .observeOn(this.observeOn)
                .subscribeOn(this.subscribeOn)
                .doOnNext(HANDLE_ACTIVITY_DETAILS_ERROR)
                .doOnNext(ACCEPT_ONLY_KNOWN_TICKET_TYPES)
                .doOnNext(DETAILS_TITLE_AND_TICKET_MONEY)
                .subscribe(observer)
    }

    private val HANDLE_ACTIVITY_DETAILS_ERROR = { response: ActivityDetailsResponse ->
        if (response.offersDetail == null || response.offersDetail.offers == null) {
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
                    ticket.prices?.forEach { price ->
                        price.money = Money(price.amount, response.currencyCode)
                    }
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

    fun createTripV2(createTripParams: LXCreateTripParams, originalPrice: Money, observer: Observer<LXCreateTripResponseV2>): Subscription {
        return lxApi.createTripV2(createTripParams)
                .doOnNext(HANDLE_ERRORS)
                .doOnNext {
                    response: LXCreateTripResponseV2 ->
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
            throw response.firstError
        }
    }

    // Add money in tickets for easier handling and remove &quot; from activity title.
    private val ACTIVITIES_MONEY_TITLE = { response: LXSearchResponse ->
        val currencyCode = response.currencyCode
        for (activity in response.activities) {
            activity.price = Money(activity.fromPriceValue, currencyCode)
            activity.originalPrice = Money(activity.fromOriginalPriceValue, currencyCode)
            activity.title = Strings.escapeQuotes(activity.title)
            activity.mipOriginalPrice = Money(activity.mipFromOriginalPriceValue, currencyCode)
            activity.mipPrice = Money(activity.mipFromPriceValue, currencyCode)
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

    private fun sortFilterThemeSearchResponse(theme: LXTheme, sortFilterMetadata: LXSortFilterMetadata): LXTheme {

        // Filtering
        val filteredSet = LinkedHashSet<LXActivity>()
        val unfilteredActivities = theme.unfilteredActivities
        for (i in unfilteredActivities.indices) {
            for (filterCategory in sortFilterMetadata.lxCategoryMetadataMap.entries) {
                val lxCategoryMetadata = filterCategory.value
                val lxCategoryMetadataKey = filterCategory.key
                if (lxCategoryMetadata.checked) {
                    if (unfilteredActivities[i].categories.contains(lxCategoryMetadataKey)) {
                        filteredSet.add(unfilteredActivities[i])
                    }
                }
            }
        }

        theme.activities.clear()

        // Filtering.
        if (filteredSet.size != 0) {
            theme.activities.addAll(filteredSet)
        } else {
            theme.activities.addAll(unfilteredActivities)
        }

        // Sorting
        if (sortFilterMetadata.sort == LXSortType.PRICE) {
            Collections.sort<LXActivity>(theme.activities, object : Comparator<LXActivity> {
                override fun compare(lhs: LXActivity, rhs: LXActivity): Int {
                    val leftMoney = lhs.price
                    val rightMoney = rhs.price
                    return leftMoney.compareTo(rightMoney)
                }
            })
        } else if (sortFilterMetadata.sort == LXSortType.POPULARITY) {
            Collections.sort<LXActivity>(theme.activities, object : Comparator<LXActivity> {
                override fun compare(lhs: LXActivity, rhs: LXActivity): Int {
                    return if ((lhs.popularityForClientSort < rhs.popularityForClientSort))
                        -1
                    else
                        (if ((lhs.popularityForClientSort == rhs.popularityForClientSort)) 0 else 1)
                }
            })
        }

        return theme
    }

    fun lxThemeSortAndFilter(currentTheme: LXTheme, lxSortType: LXSortFilterMetadata, categorySortObserver: Observer<LXTheme>, lxFilterTextSearchEnabled: Boolean): Subscription {
        return Observable.combineLatest(Observable.just(currentTheme), Observable.just(lxSortType),
                { theme, sortType ->
                    if (lxFilterTextSearchEnabled) {
                        theme.activities = theme.unfilteredActivities.applySortFilter(sortType)
                        theme
                    } else {
                        sortFilterThemeSearchResponse(theme, sortType)
                    }
                })
                .subscribeOn(this.subscribeOn)
                .observeOn(this.observeOn)
                .subscribe(categorySortObserver)
    }

    fun lxSearchSortFilter(lxSearchParams: LxSearchParams?, lxSortFilterMetadata: LXSortFilterMetadata?,
                           searchResultFilterObserver: Observer<LXSearchResponse>, lxFilterTextSearchEnabled: Boolean): Subscription {

        val lxSearchResponseObservable = if (lxSearchParams == null)
            Observable.just<LXSearchResponse>(cachedLXSearchResponse) else lxSearch(lxSearchParams)

        return (
                if (lxSortFilterMetadata != null)
                    Observable.combineLatest(lxSearchResponseObservable, Observable.just(lxSortFilterMetadata),
                            { searchResponse, sortFilterMetadata ->
                                if (lxFilterTextSearchEnabled) {
                                    searchResponse.activities = searchResponse.unFilteredActivities.applySortFilter(sortFilterMetadata)
                                    searchResponse
                                } else {
                                    CombineSearchResponseAndSortFilterStreams(searchResponse, sortFilterMetadata)
                                }
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

    private val CACHE_SEARCH_RESPONSE = { response: LXSearchResponse ->
        cachedLXSearchResponse = response
        cachedLXSearchResponse.unFilteredActivities.clear()
        cachedLXSearchResponse.unFilteredActivities.addAll(response.activities)
        cachedLXSearchResponse = response
    }

    fun applySortFilter(unfilteredActivities: List<LXActivity>, lxSearchResponse: LXSearchResponse, lxSortFilterMetadata: LXSortFilterMetadata): List<LXActivity> {

        val filteredSet = LinkedHashSet<LXActivity>()
        for (i in unfilteredActivities.indices) {
            for (filterCategory in lxSortFilterMetadata.lxCategoryMetadataMap.entries) {
                val lxCategoryMetadata = filterCategory.value
                val lxCategoryMetadataKey = filterCategory.key
                if (lxCategoryMetadata.checked) {
                    if (unfilteredActivities[i].categories.contains(lxCategoryMetadataKey)) {
                        filteredSet.add(unfilteredActivities[i])
                    }
                }
            }
        }

        lxSearchResponse.activities.clear()

        // Filtering.
        lxSearchResponse.activities.addAll(if(filteredSet.size != 0) filteredSet else unfilteredActivities)

        // Sorting.
        if (lxSortFilterMetadata.sort == LXSortType.PRICE) {
            Collections.sort<LXActivity>(lxSearchResponse.activities, object : Comparator<LXActivity> {
                override fun compare(lhs: LXActivity, rhs: LXActivity): Int {
                    val leftMoney = lhs.price
                    val rightMoney = rhs.price
                    return leftMoney.compareTo(rightMoney)
                }
            })
        }
        return lxSearchResponse.activities
    }

    companion object {

        @JvmStatic fun List<LXActivity>.applySortFilter(lxCategoryMetadata: LXSortFilterMetadata): List<LXActivity> {

            // Activity name filter
            var activities = this.filter { it.title.contains(lxCategoryMetadata.filter, true) }
            // Sorting
            activities = when (lxCategoryMetadata.sort) {
                LXSortType.POPULARITY -> activities.sortedBy { it.popularityForClientSort }
                LXSortType.PRICE -> activities.sortedBy { it.price.amount.toInt() }
                null -> activities
            }

            val filteredSet = LinkedHashSet<LXActivity>()
            for (i in activities.indices) {
                for (filterCategory in lxCategoryMetadata.lxCategoryMetadataMap.entries) {
                    val innerLxCategoryMetadata = filterCategory.value
                    val lxCategoryMetadataKey = filterCategory.key
                    if (innerLxCategoryMetadata.checked) {
                        if (activities[i].categories.contains(lxCategoryMetadataKey)) {
                            filteredSet.add(activities[i])
                }
                    }
                }
            }
            return if (filteredSet.size > 0 || lxCategoryMetadata.lxCategoryMetadataMap.size > 0) {
                filteredSet.toList()
            }
            else {
                activities
            }
        }
    }
}
