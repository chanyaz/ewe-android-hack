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
import java.util.Collections
import java.util.Comparator

public class SuggestionServices(endpoint: String, okHttpClient: OkHttpClient, val requestInterceptor: RequestInterceptor, val observeOn: Scheduler, val subscribeOn: Scheduler, logLevel: RestAdapter.LogLevel) {

    val suggestApi: SuggestApi by lazy {
        val gson = GsonBuilder().registerTypeAdapter(SuggestionResponse::class.java, SuggestionResponse()).create()

        val adapter = RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setLogLevel(logLevel)
                .setConverter(GsonConverter(gson))
                .setClient(OkClient(okHttpClient))
                .setRequestInterceptor(requestInterceptor)
                .build()

        adapter.create<SuggestApi>(SuggestApi::class.java)
    }

    private val MAX_NEARBY_SUGGESTIONS = 3

    public fun getCarSuggestions(query: String, locale: String, client: String, observer: Observer<MutableList<SuggestionV4>>): Subscription {
        val type = getTypeForCarSuggestions()
        val lob = "CARS";
        val features = "cars_rental"
        return suggestV4(query, type, locale, features, client, lob)
                .doOnNext { list -> sortCarSuggestions(list) }
                .subscribe(observer);
    }

    public fun getNearbyCarSuggestions(locale: String, latlng: String, siteId: Int, client: String, observer: Observer<MutableList<SuggestionV4>>): Subscription {
        val type = getTypeForCarSuggestions()
        val sort = "p"
        var lob = "CARS"
        return suggestNearbyV4(locale, latlng, siteId, type, sort, client, lob)
                .doOnNext { list -> sortCarSuggestions(list) }
                .doOnNext { list -> renameFirstResultIdToCurrentLocation(list) }
                .subscribe(observer)
    }

    private fun renameFirstResultIdToCurrentLocation(suggestions: MutableList<SuggestionV4>): List<SuggestionV4> {
        if (suggestions.size > 0) {
            suggestions.get(0).gaiaId = SuggestionV4.CURRENT_LOCATION_ID
        }
        return suggestions
    }

    private fun getTypeForCarSuggestions(): Int {
        val type = SuggestionResultType.AIRPORT or SuggestionResultType.CITY or SuggestionResultType.MULTI_CITY or
                SuggestionResultType.NEIGHBORHOOD or SuggestionResultType.POINT_OF_INTEREST or SuggestionResultType.AIRPORT_METRO_CODE

        return type;
    }

    private fun sortCarSuggestions(suggestions: MutableList<SuggestionV4>) {
        Collections.sort(suggestions, object : Comparator<SuggestionV4> {
            override fun compare(lhs: SuggestionV4, rhs: SuggestionV4): Int {
                val leftSuggestionPrecedenceOrder = if (lhs.isMajorAirport()) 1 else 2
                val rightSuggestionPrecedenceOrder = if (rhs.isMajorAirport()) 1 else 2
                return leftSuggestionPrecedenceOrder.compareTo(rightSuggestionPrecedenceOrder)
            }
        })
    }

    public fun getLxSuggestions(query: String, locale: String, client: String, observer: Observer<MutableList<SuggestionV4>>): Subscription {
        val type = SuggestionResultType.CITY or SuggestionResultType.MULTI_CITY or SuggestionResultType.NEIGHBORHOOD
        val lob = "ACTIVITIES"
        val features = "ta_hierarchy"
        return suggestV4(query, type, locale, features, client, lob).subscribe(observer)
    }

    private fun suggestV4(query: String, regiontype: Int, locale: String, features: String, client: String, lob: String?): Observable<MutableList<SuggestionV4>> {
        return suggestApi.suggestV4(query, locale, regiontype, features, client, lob)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions}
    }

    public fun getNearbyLxSuggestions(locale: String, latlng: String, siteId: Int, client: String, observer: Observer<MutableList<SuggestionV4>>): Subscription {
        return getNearbyLxSuggestions(locale, latlng, siteId, client)
                .doOnNext { list -> renameFirstResultIdToCurrentLocation(list) }
                .subscribe(observer)
    }

    public fun getNearbyLxSuggestions(locale: String, latlng: String, siteId: Int, client: String): Observable<MutableList<SuggestionV4>> {
        val type = SuggestionResultType.CITY or SuggestionResultType.MULTI_CITY or SuggestionResultType.NEIGHBORHOOD
        val sort = "d"
        val lob = "ACTIVITIES"
        return suggestNearbyV4(locale, latlng, siteId, type, sort, client, lob)
    }

    private fun suggestNearbyV4(locale: String, latlng: String, siteId: Int, type: Int, sort: String, client: String, lob: String): Observable<MutableList<SuggestionV4>> {
        return suggestApi.suggestNearbyV4(locale, latlng, siteId, type, sort, client, lob)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions.take(MAX_NEARBY_SUGGESTIONS) }
                .doOnNext { list ->
                    list.forEach {
                        suggestion ->
                        suggestion.iconType = SuggestionV4.IconType.CURRENT_LOCATION_ICON
                    }
                }
                .map {list -> list.toMutableList() }
    }
}
