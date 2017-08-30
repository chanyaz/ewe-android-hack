package com.expedia.bookings.services

import com.expedia.bookings.data.SuggestionResultType
import com.expedia.bookings.data.cars.SuggestionResponse
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.GaiaSuggestion
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
import java.util.Comparator
import java.util.Collections

open class SuggestionV4Services(essEndpoint: String, gaiaEndPoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, gaiaInterceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val suggestApi: SuggestApi by lazy {
        val gson = GsonBuilder().registerTypeAdapter(SuggestionResponse::class.java, SuggestionResponse()).create()

        val adapter = Retrofit.Builder()
                .baseUrl(essEndpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create<SuggestApi>(SuggestApi::class.java)
    }

    val gaiaSuggestApi: GaiaSuggestApi by lazy {
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

    fun getLxSuggestionsV4(query: String, client: String, observer: Observer<List<SuggestionV4>>, locale: String, disablePOI: Boolean): Subscription {

        var type = SuggestionResultType.CITY or SuggestionResultType.MULTI_CITY or SuggestionResultType.NEIGHBORHOOD or SuggestionResultType.POINT_OF_INTEREST
        if (disablePOI) {
            type = SuggestionResultType.CITY or SuggestionResultType.MULTI_CITY or SuggestionResultType.NEIGHBORHOOD
        }

        return suggestApi.suggestV4(query, locale, type, false, "ta_hierarchy", client, "ACTIVITIES", null, null, null)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions}
                .subscribe(observer)
    }

    fun getHotelSuggestionsV4(query: String, clientId: String, observer: Observer<List<SuggestionV4>>, locale: String,
                              sameAsWeb: Boolean, guid: String?): Subscription {
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

        return suggestApi.suggestV4(query, locale, regiontype, dest, features, clientId, "HOTELS", null, maxResults, guid)
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

    fun getCarSuggestionsV4(query: String, client: String, observer: Observer<List<SuggestionV4>>, locale: String): Subscription {
        val type = SuggestionResultType.AIRPORT or SuggestionResultType.CITY or SuggestionResultType.MULTI_CITY or
                SuggestionResultType.NEIGHBORHOOD or SuggestionResultType.POINT_OF_INTEREST or SuggestionResultType.AIRPORT_METRO_CODE
        return suggestApi.suggestV4(query, locale, type, false, "cars_rental", client, "CARS", null, null, null)
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

    fun suggestPackagesV4(query: String, clientId: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>, locale: String): Subscription {
        var suggestType = SuggestionResultType.NEIGHBORHOOD or SuggestionResultType.POINT_OF_INTEREST or SuggestionResultType.MULTI_CITY or
                SuggestionResultType.CITY or SuggestionResultType.AIRPORT or SuggestionResultType.AIRPORT_METRO_CODE
        if (isDest) {
            suggestType = suggestType or SuggestionResultType.AIRPORT
        }
        return suggestApi.suggestV4(query, locale, suggestType, isDest, "ta_hierarchy", clientId, "PACKAGES", null, null, null)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions ?: emptyList() }
                .subscribe(observer)
    }

    fun suggestRailsV4(query: String, siteId: Int, clientId: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>, locale: String): Subscription {
        val suggestType = SuggestionResultType.TRAIN_STATION
        return suggestApi.suggestV4(query, locale, suggestType, isDest, "ta_hierarchy", clientId, "RAILS", siteId, null, null)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions ?: emptyList() }
                .subscribe(observer)
    }
    
    var airportSuggestionSubscription: Subscription? = null

    fun getAirports(query: String, clientId: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>, locale: String, guid: String): Subscription {

        airportSuggestionSubscription?.unsubscribe()

        val suggestType = SuggestionResultType.NEIGHBORHOOD or SuggestionResultType.POINT_OF_INTEREST or SuggestionResultType.MULTI_CITY or
                SuggestionResultType.CITY or SuggestionResultType.AIRPORT or SuggestionResultType.AIRPORT_METRO_CODE

        airportSuggestionSubscription = suggestApi.suggestV4(query, locale, suggestType, isDest, "ta_hierarchy|nearby_airport", clientId, "FLIGHTS", null, 10, guid)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions ?: emptyList() }
                .subscribe(observer)

        return airportSuggestionSubscription as Subscription
    }
}
