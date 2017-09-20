package com.expedia.bookings.services

import com.expedia.bookings.data.GaiaSuggestion
import com.expedia.bookings.data.SuggestionResultType
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.SuggestionV4Response
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observable
import rx.Observer
import rx.Scheduler
import rx.Subscription
import java.util.Collections
import java.util.Comparator

open class SuggestionV4Services(essEndpoint: String, gaiaEndPoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor,
                                essInterceptor: Interceptor, gaiaInterceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    private val suggestApi: SuggestApi by lazy {
        val gson = GsonBuilder().create()

        val adapter = Retrofit.Builder()
                .baseUrl(essEndpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor)
                        .addInterceptor(essInterceptor).build())
                .build()

        adapter.create<SuggestApi>(SuggestApi::class.java)
    }

    private val gaiaSuggestApi: GaiaSuggestApi by lazy {
        val gson = GsonBuilder().create()
        val adapter = Retrofit.Builder()
                .baseUrl(gaiaEndPoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(gaiaInterceptor)
                        .addInterceptor(interceptor).build())
                .build()

        adapter.create<GaiaSuggestApi>(GaiaSuggestApi::class.java)
    }

    fun getLxSuggestionsV4(query: String, observer: Observer<List<SuggestionV4>>, disablePOI: Boolean): Subscription {

        var type = SuggestionResultType.CITY or SuggestionResultType.MULTI_CITY or SuggestionResultType.NEIGHBORHOOD or SuggestionResultType.POINT_OF_INTEREST
        if (disablePOI) {
            type = SuggestionResultType.CITY or SuggestionResultType.MULTI_CITY or SuggestionResultType.NEIGHBORHOOD
        }

        return suggestV4(query, type, false, "ta_hierarchy", "ACTIVITIES")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions}
                .subscribe(observer)
    }

    fun getHotelSuggestionsV4(query: String, observer: Observer<List<SuggestionV4>>, sameAsWeb: Boolean, guid: String?): Subscription {
        val regiontype: Int
        val dest: Boolean
        val features: String
        val maxResults: Int?

        if (sameAsWeb) {
            regiontype = SuggestionResultType.ALL_REGION
            dest = true
            features = "ta_hierarchy|postal_code|contextual_ta"
            maxResults = 10
        } else {
            regiontype = SuggestionResultType.REGION
            dest = false
            features = "ta_hierarchy"
            maxResults = null
        }

        return suggestV4(query, regiontype, dest, features, "HOTELS", maxResults = maxResults, guid = guid)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions ?: emptyList() }
                .subscribe(observer)
    }

    fun suggestNearbyGaia(lat: Double, lng: Double, sortType: String, lob: String, locale: String, siteId: Int): Observable<MutableList<GaiaSuggestion>> {
        val limit = 2
        val response = gaiaSuggestApi.gaiaNearBy(lat, lng, limit, lob, sortType, locale, siteId)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
        return response.map { response -> response.toMutableList() }
    }

    fun getCarSuggestionsV4(query: String, observer: Observer<List<SuggestionV4>>): Subscription {
        val type = SuggestionResultType.AIRPORT or SuggestionResultType.CITY or SuggestionResultType.MULTI_CITY or
                SuggestionResultType.NEIGHBORHOOD or SuggestionResultType.POINT_OF_INTEREST or SuggestionResultType.AIRPORT_METRO_CODE
        return suggestV4(query, type, false, "cars_rental", "CARS")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions }
                .doOnNext { list -> sortCarSuggestions(list) }
                .subscribe(observer)
    }

    private fun sortCarSuggestions(suggestions: MutableList<SuggestionV4>) {
        Collections.sort(suggestions, object : Comparator<SuggestionV4> {
            override fun compare(lhs: SuggestionV4, rhs: SuggestionV4): Int {
                val leftSuggestionPrecedenceOrder = if (lhs.isMajorAirport) 1 else 2
                val rightSuggestionPrecedenceOrder = if (rhs.isMajorAirport) 1 else 2
                return leftSuggestionPrecedenceOrder.compareTo(rightSuggestionPrecedenceOrder)
            }
        })
    }

    fun suggestPackagesV4(query: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>): Subscription {
        var suggestType = SuggestionResultType.NEIGHBORHOOD or SuggestionResultType.POINT_OF_INTEREST or SuggestionResultType.MULTI_CITY or
                SuggestionResultType.CITY or SuggestionResultType.AIRPORT or SuggestionResultType.AIRPORT_METRO_CODE
        if (isDest) {
            suggestType = suggestType or SuggestionResultType.AIRPORT
        }
        return suggestV4(query, suggestType, isDest, "ta_hierarchy", "PACKAGES")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions ?: emptyList() }
                .subscribe(observer)
    }

    fun suggestRailsV4(query: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>): Subscription {
        val suggestType = SuggestionResultType.TRAIN_STATION
        return suggestV4(query, suggestType, isDest, "ta_hierarchy", "RAILS")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions ?: emptyList() }
                .subscribe(observer)
    }
    
    private var airportSuggestionSubscription: Subscription? = null

    fun getAirports(query: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>, guid: String): Subscription {

        airportSuggestionSubscription?.unsubscribe()

        val suggestType = SuggestionResultType.NEIGHBORHOOD or SuggestionResultType.POINT_OF_INTEREST or SuggestionResultType.MULTI_CITY or
                SuggestionResultType.CITY or SuggestionResultType.AIRPORT or SuggestionResultType.AIRPORT_METRO_CODE

        airportSuggestionSubscription = suggestV4(query, suggestType, isDest, "ta_hierarchy|nearby_airport", "FLIGHTS", maxResults = 10, guid = guid)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions ?: emptyList() }
                .subscribe(observer)

        return airportSuggestionSubscription as Subscription
    }

    private fun suggestV4(query: String, suggestType: Int, isDest: Boolean, features: String, lineOfBusiness: String,
                          maxResults: Int? = null, guid: String? = null): Observable<SuggestionV4Response> {
        return suggestApi.suggestV4(query, suggestType, isDest, features, lineOfBusiness, maxResults, guid)
    }
}
