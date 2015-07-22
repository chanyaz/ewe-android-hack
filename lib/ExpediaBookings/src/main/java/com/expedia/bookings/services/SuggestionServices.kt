package com.expedia.bookings.services

import com.expedia.bookings.data.SuggestionResultType
import com.expedia.bookings.data.cars.Suggestion
import com.expedia.bookings.data.cars.SuggestionResponse
import com.expedia.bookings.data.lx.LXActivity
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
import java.util.Locale
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

    private val MAX_NEARBY_SUGGESTIONS = 2

    public fun getCarSuggestions(query: String, locale: String, observer: Observer<MutableList<Suggestion>>): Subscription {
        val type = getTypeForCarSuggestions()
        val lob = null;
        return suggestV3(query, type, locale, lob)
                .doOnNext { list -> sortCarSuggestions(list) }
                .subscribe(observer);
    }

    public fun getNearbyCarSuggestions(locale: String, latlng: String, siteId: Int, observer: Observer<MutableList<Suggestion>>): Subscription {
        val type = getTypeForCarSuggestions()
        val sort = "p"
        return suggestNearbyV1(locale, latlng, siteId, type, sort)
                .doOnNext { list -> sortCarSuggestions(list) }
                .subscribe(observer)
    }

    private fun getTypeForCarSuggestions(): Int {
        val type = SuggestionResultType.AIRPORT or SuggestionResultType.CITY or SuggestionResultType.MULTI_CITY or
                SuggestionResultType.NEIGHBORHOOD or SuggestionResultType.POINT_OF_INTEREST or SuggestionResultType.AIRPORT_METRO_CODE

        return type;
    }

    private fun sortCarSuggestions(suggestions: MutableList<Suggestion>) {
        Collections.sort(suggestions, object : Comparator<Suggestion> {
            override fun compare(lhs: Suggestion, rhs: Suggestion): Int {
                val leftSuggestionPrecedenceOrder = suggestionPrecedenceOrder(lhs.regionType, lhs.isMinorAirport)
                val rightSuggestionPrecedenceOrder = suggestionPrecedenceOrder(rhs.regionType, rhs.isMinorAirport)
                return leftSuggestionPrecedenceOrder.compareTo(rightSuggestionPrecedenceOrder)
            }

            private fun suggestionPrecedenceOrder(type: String, isMinorAirport: Boolean): Int {
                val isMajorAirport = type.toLowerCase(Locale.US).equals("airport") && !isMinorAirport;
                if (isMajorAirport) {
                    return 1;
                }
                else {
                    return 2;
                }
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
