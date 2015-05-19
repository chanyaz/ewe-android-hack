package com.expedia.bookings.services

import com.expedia.bookings.data.SuggestionResultType
import com.expedia.bookings.data.cars.Suggestion
import com.expedia.bookings.data.cars.SuggestionResponse
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
import rx.functions.Func1
import kotlin.properties.Delegates

public class SuggestionServices(endpoint: String, okHttpClient: OkHttpClient, val observeOn: Scheduler, val subscribeOn: Scheduler, logLevel: RestAdapter.LogLevel) {
    val acceptJsonInterceptor: RequestInterceptor = object : RequestInterceptor {
        override fun intercept(request: RequestInterceptor.RequestFacade) {
            request.addHeader("Accept", "application/json")
        }
    }

    val suggestApi: SuggestApi by Delegates.lazy {
        val gson = GsonBuilder().registerTypeAdapter(javaClass<SuggestionResponse>(), SuggestionResponse()).create()

        val adapter = RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setLogLevel(logLevel)
                .setConverter(GsonConverter(gson))
                .setClient(OkClient(okHttpClient))
                .setRequestInterceptor(acceptJsonInterceptor)
                .build()

        adapter.create<SuggestApi>(javaClass<SuggestApi>())
    }

    private val MAX_NEARBY_AIRPORTS = 2
    private val MAX_AIRPORTS_RETURNED = 3
    private val MAX_LX_SUGGESTIONS_RETURNED = 3

    public fun getAirportSuggestions(query: String, observer: Observer<MutableList<Suggestion>>): Subscription {
        val type = SuggestionResultType.AIRPORT
        val lob = null;
        return suggestV3(query, type, lob)
                .map { list -> list.take(MAX_AIRPORTS_RETURNED).toArrayList() }
                .subscribe(observer)
    }

    public fun getHotelSuggestions(query: String, observer: Observer<MutableList<Suggestion>>): Subscription {
        val type = SuggestionResultType.REGION
        return suggestApi.suggestV2(query, type)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions.toArrayList() }
                .subscribe(observer)
    }

    public fun getLxSuggestions(query: String, observer: Observer<MutableList<Suggestion>>): Subscription {
        val type = SuggestionResultType.CITY or SuggestionResultType.MULTI_CITY or SuggestionResultType.NEIGHBORHOOD
        val lob = "ACTIVITIES"
        return suggestV3(query, type, lob).subscribe(observer)
    }

    private fun suggestV3(query: String, type: Int, lob: String?): Observable<MutableList<Suggestion>> {
        return suggestApi.suggestV3(query, type, lob)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions.toArrayList() }
    }

    public fun getNearbyAirportSuggestions(locale: String, latlng: String, siteId: Int, observer: Observer<MutableList<Suggestion>>): Subscription {
        val type = SuggestionResultType.AIRPORT
        val sort = "p"
        return suggestNearbyV1(locale, latlng, siteId, type, sort).subscribe(observer)
    }

    public fun getNearbyLxSuggestions(locale: String, latlng: String, siteId: Int, observer: Observer<MutableList<Suggestion>>): Subscription {
        return getNearbyLxSuggestions(locale, latlng, siteId).subscribe(observer)
    }

    public fun getNearbyLxSuggestions(locale: String, latlng: String, siteId: Int): Observable<MutableList<Suggestion>> {
        val type = SuggestionResultType.CITY or SuggestionResultType.MULTI_CITY or SuggestionResultType.NEIGHBORHOOD
        val sort = "d"

        return suggestNearbyV1(locale, latlng, siteId, type, sort)
    }

    private fun suggestNearbyV1(locale: String, latlng: String, siteId: Int, type: Int, sort: String): Observable<MutableList<Suggestion>> {
        return suggestApi.suggestNearbyV1(locale, latlng, siteId, type, sort)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions.take(MAX_NEARBY_AIRPORTS) }
                .doOnNext { list ->
                    list.forEach {
                        suggestion ->
                        suggestion.iconType = Suggestion.IconType.CURRENT_LOCATION_ICON
                    }
                }
                .map {list -> list.toArrayList() }
    }
}
