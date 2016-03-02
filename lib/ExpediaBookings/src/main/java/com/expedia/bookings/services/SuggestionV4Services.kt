package com.expedia.bookings.services

import com.expedia.bookings.data.SuggestionResultType
import com.expedia.bookings.data.cars.SuggestionResponse
import com.expedia.bookings.data.SuggestionV4
import com.google.gson.GsonBuilder
import com.squareup.okhttp.OkHttpClient
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import retrofit.client.OkClient
import retrofit.converter.GsonConverter
import rx.Observable
import rx.Observer
import rx.Scheduler
import rx.Subscription

class SuggestionV4Services(endpoint: String, okHttpClient: OkHttpClient, interceptor: RequestInterceptor, val observeOn: Scheduler, val subscribeOn: Scheduler, logLevel: RestAdapter.LogLevel) {

    val suggestApi: SuggestApi by lazy {
        val gson = GsonBuilder().registerTypeAdapter(SuggestionResponse::class.java, SuggestionResponse()).create()

        val adapter = RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setLogLevel(logLevel)
                .setConverter(GsonConverter(gson))
                .setClient(OkClient(okHttpClient))
                .setRequestInterceptor(interceptor)
                .build()

        adapter.create<SuggestApi>(SuggestApi::class.java)
    }

    fun getHotelSuggestionsV4(query: String, clientId: String, observer: Observer<List<SuggestionV4>>, locale: String): Subscription {
        val type = SuggestionResultType.HOTEL or SuggestionResultType.AIRPORT or SuggestionResultType.CITY or
                SuggestionResultType.NEIGHBORHOOD or SuggestionResultType.POINT_OF_INTEREST or SuggestionResultType.REGION
        return suggestApi.suggestV4(query, locale, type, false, "ta_hierarchy", clientId, "HOTELS")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions ?: emptyList() }
                .subscribe(observer)
    }

    fun suggestNearbyV4(locale: String, latlng: String, siteId: Int, clientId: String): Observable<MutableList<SuggestionV4>> {
        return suggestApi.suggestNearbyV4(locale, latlng, siteId, SuggestionResultType.MULTI_CITY, "distance", clientId, "HOTELS")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions.take(2).toMutableList() }
    }

    fun suggestNearbyFlightsV4(locale: String, latlng: String, siteId: Int, clientId: String): Observable<MutableList<SuggestionV4>> {
        return suggestApi.suggestNearbyV4(locale, latlng, siteId, SuggestionResultType.AIRPORT, "distance", clientId, "PACKAGES")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions.take(2).toMutableList() }
    }

    fun suggestPackagesV4(query: String, clientId: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>, locale: String): Subscription {
        var suggestType = SuggestionResultType.NEIGHBORHOOD or SuggestionResultType.POINT_OF_INTEREST or SuggestionResultType.MULTI_CITY or SuggestionResultType.CITY
        if (isDest) {
            suggestType = suggestType or SuggestionResultType.AIRPORT or SuggestionResultType.AIRPORT_METRO_CODE
        }
        return suggestApi.suggestV4(query, locale, suggestType, isDest, "ta_hierarchy", clientId, "PACKAGES")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions ?: emptyList() }
                .subscribe(observer)
    }
}
