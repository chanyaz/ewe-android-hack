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

    public fun getCarSuggestions(query: String, locale: String, observer: Observer<MutableList<Suggestion>>): Subscription {
        val type = getTypeForCarSuggestions()
        val lob = "CARS";
        return suggestV3(query, type, locale, lob)
                .doOnNext { list -> sortCarSuggestions(list) }
                .subscribe(observer);
    }

    public fun getNearbyCarSuggestions(locale: String, latlng: String, siteId: Int, observer: Observer<MutableList<Suggestion>>): Subscription {
        val type = getTypeForCarSuggestions()
        val sort = "p"
        var lob = "CARS"
        return suggestNearbyV1(locale, latlng, siteId, type, sort, lob)
                .doOnNext { list -> sortCarSuggestions(list) }
                .doOnNext { list -> renameFirstResultIdToCurrentLocation(list) }
                .subscribe(observer)
    }

    private fun renameFirstResultIdToCurrentLocation(suggestions: MutableList<Suggestion>): List<Suggestion> {
        if (suggestions.size() > 0) {
            suggestions.get(0).id = Suggestion.CURRENT_LOCATION_ID
        }
        return suggestions
    }

    private fun getTypeForCarSuggestions(): Int {
        val type = SuggestionResultType.AIRPORT or SuggestionResultType.CITY or SuggestionResultType.MULTI_CITY or
                SuggestionResultType.NEIGHBORHOOD or SuggestionResultType.POINT_OF_INTEREST or SuggestionResultType.AIRPORT_METRO_CODE

        return type;
    }

    private fun sortCarSuggestions(suggestions: MutableList<Suggestion>) {
        Collections.sort(suggestions, object : Comparator<Suggestion> {
            override fun compare(lhs: Suggestion, rhs: Suggestion): Int {
                val leftSuggestionPrecedenceOrder = if (lhs.isMajorAirport()) 1 else 2
                val rightSuggestionPrecedenceOrder = if (rhs.isMajorAirport()) 1 else 2
                return leftSuggestionPrecedenceOrder.compareTo(rightSuggestionPrecedenceOrder)
            }
        })
    }

    public fun getLxSuggestions(query: String, locale: String, observer: Observer<MutableList<Suggestion>>): Subscription {
        val type = SuggestionResultType.CITY or SuggestionResultType.MULTI_CITY or SuggestionResultType.NEIGHBORHOOD
        val lob = "ACTIVITIES"
        return suggestV3(query, type, locale, lob).subscribe(observer)
    }

    private fun suggestV3(query: String, type: Int, locale: String, lob: String?): Observable<MutableList<Suggestion>> {
        return suggestApi.suggestV3(query, type, locale, lob)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions.toArrayList() }
    }

    public fun getNearbyLxSuggestions(locale: String, latlng: String, siteId: Int, observer: Observer<MutableList<Suggestion>>): Subscription {
        return getNearbyLxSuggestions(locale, latlng, siteId)
                .doOnNext { list -> renameFirstResultIdToCurrentLocation(list) }
                .subscribe(observer)
    }

    public fun getNearbyLxSuggestions(locale: String, latlng: String, siteId: Int): Observable<MutableList<Suggestion>> {
        val type = SuggestionResultType.CITY or SuggestionResultType.MULTI_CITY or SuggestionResultType.NEIGHBORHOOD
        val sort = "d"
        val lob = "ACTIVITIES"
        return suggestNearbyV1(locale, latlng, siteId, type, sort, lob)
    }

    private fun suggestNearbyV1(locale: String, latlng: String, siteId: Int, type: Int, sort: String, lob: String): Observable<MutableList<Suggestion>> {
        return suggestApi.suggestNearbyV1(locale, latlng, siteId, type, sort, lob)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions.take(MAX_NEARBY_SUGGESTIONS) }
                .doOnNext { list ->
                    list.forEach {
                        suggestion ->
                        suggestion.iconType = Suggestion.IconType.CURRENT_LOCATION_ICON
                    }
                }
                .map {list -> list.toArrayList() }
    }
}
