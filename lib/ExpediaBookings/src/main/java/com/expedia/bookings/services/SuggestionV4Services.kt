package com.expedia.bookings.services

import com.expedia.bookings.data.GaiaSuggestion
import com.expedia.bookings.data.SuggestionResultType
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.SuggestionV4Response
import com.expedia.bookings.subscribeObserver
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

open class SuggestionV4Services(essEndpoint: String, gaiaEndPoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor,
                                essInterceptor: Interceptor, gaiaInterceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    private val suggestApi: SuggestApi by lazy {
        val gson = GsonBuilder().create()

        val adapter = Retrofit.Builder()
                .baseUrl(essEndpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
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
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(gaiaInterceptor)
                        .addInterceptor(interceptor).build())
                .build()

        adapter.create<GaiaSuggestApi>(GaiaSuggestApi::class.java)
    }

    fun getLxSuggestionsV4(query: String, observer: Observer<List<SuggestionV4>>, disablePOI: Boolean): Disposable {

        var type = SuggestionResultType.CITY or SuggestionResultType.MULTI_CITY or SuggestionResultType.NEIGHBORHOOD or SuggestionResultType.POINT_OF_INTEREST
        if (disablePOI) {
            type = SuggestionResultType.CITY or SuggestionResultType.MULTI_CITY or SuggestionResultType.NEIGHBORHOOD
        }

        return suggestV4(query, type, false, "ta_hierarchy", "ACTIVITIES")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions}
                .subscribeObserver(observer)
    }

    fun getHotelSuggestionsV4(query: String, observer: Observer<List<SuggestionV4>>, sameAsWeb: Boolean, guid: String?): Disposable {
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
                .subscribeObserver(observer)
    }

    fun suggestNearbyGaia(lat: Double, lng: Double, sortType: String, lob: String, locale: String, siteId: Int): Observable<MutableList<GaiaSuggestion>> {
        val limit = 2
        val response = gaiaSuggestApi.gaiaNearBy(lat, lng, limit, lob, sortType, locale, siteId)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
        return response.map { response -> response.toMutableList() }
    }

    fun suggestPackagesV4(query: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>): Disposable {
        var suggestType = SuggestionResultType.NEIGHBORHOOD or SuggestionResultType.POINT_OF_INTEREST or SuggestionResultType.MULTI_CITY or
                SuggestionResultType.CITY or SuggestionResultType.AIRPORT or SuggestionResultType.AIRPORT_METRO_CODE
        if (isDest) {
            suggestType = suggestType or SuggestionResultType.AIRPORT
        }
        return suggestV4(query, suggestType, isDest, "ta_hierarchy", "PACKAGES")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions ?: emptyList() }
                .subscribeObserver(observer)
    }

    fun suggestRailsV4(query: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>): Disposable {
        val suggestType = SuggestionResultType.TRAIN_STATION
        return suggestV4(query, suggestType, isDest, "ta_hierarchy", "RAILS")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions ?: emptyList() }
                .subscribeObserver(observer)
    }
    
    private var airportSuggestionSubscription: Disposable? = null

    fun getAirports(query: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>, guid: String): Disposable {

        airportSuggestionSubscription?.dispose()
        val suggestType = SuggestionResultType.NEIGHBORHOOD or SuggestionResultType.POINT_OF_INTEREST or SuggestionResultType.MULTI_CITY or
                SuggestionResultType.CITY or SuggestionResultType.AIRPORT or SuggestionResultType.AIRPORT_METRO_CODE

        airportSuggestionSubscription = suggestV4(query, suggestType, isDest, "ta_hierarchy|nearby_airport", "FLIGHTS", maxResults = 10, guid = guid)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions ?: emptyList() }
                .subscribeObserver(observer)

        return airportSuggestionSubscription as Disposable
    }

    private fun suggestV4(query: String, suggestType: Int, isDest: Boolean, features: String, lineOfBusiness: String,
                          maxResults: Int? = null, guid: String? = null): Observable<SuggestionV4Response> {
        return suggestApi.suggestV4(query, suggestType, isDest, features, lineOfBusiness, maxResults, guid)
    }
}
