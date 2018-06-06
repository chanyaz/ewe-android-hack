package com.expedia.bookings.services

import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.BaseApiResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.lx.ActivityDetailsResponse
import com.expedia.bookings.data.lx.LXActivity
import com.expedia.bookings.data.lx.LXCheckoutParams
import com.expedia.bookings.data.lx.LXCheckoutResponse
import com.expedia.bookings.data.lx.LXCreateTripParams
import com.expedia.bookings.data.lx.LXCreateTripResponse
import com.expedia.bookings.data.lx.LXCreateTripResponseV2
import com.expedia.bookings.data.lx.LXSearchResponse
import com.expedia.bookings.data.lx.LXSortFilterMetadata
import com.expedia.bookings.data.lx.LXSortType
import com.expedia.bookings.data.lx.LxSearchParams
import com.expedia.bookings.extensions.applySortFilter
import com.expedia.bookings.extensions.subscribeObserver
import com.expedia.bookings.utils.ApiDateUtils
import com.expedia.bookings.utils.Strings
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.joda.time.LocalDate
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
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
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

        adapter.create(LXApi::class.java)
    }

    fun lxSearch(searchParams: LxSearchParams, observer: Observer<LXSearchResponse>): Disposable {
        return lxSearch(searchParams).subscribeObserver(observer)
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

    fun lxDetails(activityId: String?, location: String?, startDate: LocalDate?, endDate: LocalDate?, promoPricingEnabled: Boolean,
                  promoPricingMaxDiscountPercentageEnabled: Boolean, observer: Observer<ActivityDetailsResponse>): Disposable {
        return lxApi.activityDetails(activityId, location, ApiDateUtils.localDateToyyyyMMddSafe(startDate),
                ApiDateUtils.localDateToyyyyMMddSafe(endDate), promoPricingEnabled, promoPricingMaxDiscountPercentageEnabled)
                .observeOn(this.observeOn)
                .subscribeOn(this.subscribeOn)
                .doOnNext(HANDLE_ACTIVITY_DETAILS_ERROR)
                .doOnNext(ACCEPT_ONLY_KNOWN_TICKET_TYPES)
                .doOnNext(DETAILS_TITLE_AND_TICKET_MONEY)
                .subscribeObserver(observer)
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
                    ticket.originalPriceMoney = Money(ticket.originalAmount, response.currencyCode)
                    ticket.prices?.forEach { price ->
                        price.money = Money(price.amount, response.currencyCode)
                        price.originalPriceMoney = Money(price.originalAmount, response.currencyCode)
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

    fun createTrip(createTripParams: LXCreateTripParams, originalPrice: Money, observer: Observer<LXCreateTripResponse>): Disposable {
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
                .subscribeObserver(observer)
    }

    fun createTripV2(createTripParams: LXCreateTripParams, originalPrice: Money, observer: Observer<LXCreateTripResponseV2>): Disposable {
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
                .subscribeObserver(observer)
    }

    fun lxCheckout(checkoutParams: LXCheckoutParams, observer: Observer<LXCheckoutResponse>): Disposable {
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
                .subscribeObserver(observer)
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

    fun lxSearchSortFilter(lxSearchParams: LxSearchParams?, lxSortFilterMetadata: LXSortFilterMetadata?,
                           searchResultFilterObserver: Observer<LXSearchResponse>, lxFilterTextSearchEnabled: Boolean): Disposable {

        val lxSearchResponseObservable = if (lxSearchParams == null)
            Observable.just<LXSearchResponse>(cachedLXSearchResponse) else lxSearch(lxSearchParams)

        return (
                if (lxSortFilterMetadata != null)
                    ObservableOld.combineLatest(lxSearchResponseObservable, Observable.just(lxSortFilterMetadata),
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
                .subscribeObserver(searchResultFilterObserver)
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
        lxSearchResponse.activities.addAll(if (filteredSet.size != 0) filteredSet else unfilteredActivities)

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
}
