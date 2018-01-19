package com.expedia.bookings.hotel.util

import android.util.Log
import com.apollographql.apollo.api.Response
import com.expedia.bookings.apollographql.HotelSearchQuery
import com.expedia.bookings.apollographql.fragment.HotelResult
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.server.apollo.GraphQLServices
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.RetrofitError
import com.expedia.bookings.utils.RetrofitUtils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

open class HotelSearchManager(private val hotelServices: HotelServices?, private val graphQLServices: GraphQLServices?) {
    val successSubject = PublishSubject.create<HotelSearchResponse>()
    val errorSubject = PublishSubject.create<ApiError>()
    val noResultsSubject = PublishSubject.create<Unit>()
    val retrofitErrorSubject = PublishSubject.create<RetrofitError>()

    val apiCompleteSubject = PublishSubject.create<Unit>()

    var fetchingResults: Boolean = false
        private set

    var isCurrentLocationSearch = false
    private var searchResponse: HotelSearchResponse? = null
    private var subscriptions: CompositeDisposable = CompositeDisposable()

    private var prefetchSearch = false

    fun fetchResponse(): HotelSearchResponse? {
        return searchResponse
    }

    open fun doSearch(params: HotelSearchParams, prefetchSearch: Boolean = false) {
//        hotelServices?.let { services ->
//            this.prefetchSearch = prefetchSearch
//            reset()
//            fetchingResults = true
//            isCurrentLocationSearch = params.suggestion.isCurrentLocationSearch
//            subscriptions.add(services.search(params, apiCompleteSubject).subscribeObserver(searchResponseObserver))
//        }
        doGraphQLSearch(params)
    }

    private fun doGraphQLSearch(params: HotelSearchParams) {
        graphQLServices?.let { services ->
            this.prefetchSearch = prefetchSearch
            reset()
            fetchingResults = true
            //isCurrentLocationSearch = params.suggestion.isCurrentLocationSearch
            subscriptions.add(services.doRxSearch(params, graphQLObserver))
        }
    }

    fun reset() {
        searchResponse = null
        subscriptions.clear()
    }

    fun dispose() {
        subscriptions.clear()
    }

    val searchResponseObserver = object : DisposableObserver<HotelSearchResponse>() {
        override fun onNext(hotelSearchResponse: HotelSearchResponse) {
            fetchingResults = false
            if (hotelSearchResponse.hasErrors()) {
                if (!prefetchSearch) {
                    errorSubject.onNext(hotelSearchResponse.firstError)
                }
            } else if (hotelSearchResponse.hotelList.isEmpty()) {
                if (!prefetchSearch) {
                    noResultsSubject.onNext(Unit)
                }
            } else {
                hotelSearchResponse.hotelList.map { hotel ->
                    if (hotel.locationId != null) {
                        hotel.neighborhoodName = hotelSearchResponse.neighborhoodsMap[hotel.locationId]?.name
                    }
                    hotel.isCurrentLocationSearch = isCurrentLocationSearch
                }
                searchResponse = hotelSearchResponse
                successSubject.onNext(hotelSearchResponse)
            }
        }

        override fun onComplete() {
            fetchingResults = false
        }

        override fun onError(e: Throwable) {
            fetchingResults = false
            if (!prefetchSearch) {
                val retrofitError = RetrofitUtils.getRetrofitError(e)
                retrofitErrorSubject.onNext(retrofitError)
            }
        }
    }

    private val graphQLObserver = object : DisposableObserver<Response<HotelSearchQuery.Data>>() {
        override fun onNext(graphSearchResponse: Response<HotelSearchQuery.Data>) {
            fetchingResults = false
            val listings = graphSearchResponse.data()?.hotels()?.listings()
            if (listings == null || listings.isEmpty()) {
                noResultsSubject.onNext(Unit)
            } else {
                val hotelSearchResponse = convertToHotelSearchResponse(listings)
                searchResponse = hotelSearchResponse
                successSubject.onNext(hotelSearchResponse)
            }
        }

        override fun onComplete() {
            fetchingResults = false
        }

        override fun onError(e: Throwable) {
            Log.e("GraphQL", "exception from graphQL", e)
            fetchingResults = false
            if (!prefetchSearch) {
                val retrofitError = RetrofitUtils.getRetrofitError(e)
                retrofitErrorSubject.onNext(retrofitError)
            }
        }
    }

    private fun convertToHotelSearchResponse(listings: List<HotelSearchQuery.Listing>): HotelSearchResponse {
        val response = HotelSearchResponse()
        listings
                .map { listing -> listing.fragments().hotelResult() }
                .forEach {result ->  response.hotelList.add(getHotel(result)) }
        return response
    }

    private fun getHotel(result: HotelResult): Hotel {
        val hotel = Hotel()
        hotel.locationId = result.regionId().toString()
        hotel.hotelId = result.id().toString()
        hotel.localizedName = result.propertyName()
        hotel.hotelStarRating = result.star().toFloat()
        val rate = HotelRate()
        rate.priceToShowUsers = result.price().raw().toFloat()
        rate.currencyCode = "USD"
        hotel.lowRateInfo = rate
        hotel.isVipAccess = result.vip()
        hotel.largeThumbnailUrl = result.image().url()
        hotel.hotelGuestRating  = result.reviewScore().toFloat()
        return hotel
    }
}
