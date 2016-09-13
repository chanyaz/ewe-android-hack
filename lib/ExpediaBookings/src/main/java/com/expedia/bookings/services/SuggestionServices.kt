package com.expedia.bookings.services

import com.expedia.bookings.data.SuggestionResultType
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.cars.SuggestionResponse
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

class SuggestionServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val suggestApi: SuggestApi by lazy {
        val gson = GsonBuilder().registerTypeAdapter(SuggestionResponse::class.java, SuggestionResponse()).create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()


        adapter.create<SuggestApi>(SuggestApi::class.java)
    }

    private val MAX_NEARBY_SUGGESTIONS = 3

    fun getCarSuggestions(query: String, locale: String, client: String, observer: Observer<MutableList<SuggestionV4>>): Subscription {
        val type = getTypeForCarSuggestions()
        val lob = "CARS";
        val features = "cars_rental"
        return suggestV4(query, type, locale, features, client, lob, null)
                .doOnNext { list -> sortCarSuggestions(list) }
                .subscribe(observer);
    }

    fun getNearbyCarSuggestions(locale: String, latlng: String, siteId: Int, client: String, observer: Observer<MutableList<SuggestionV4>>): Subscription {
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
            suggestions[0].gaiaId = SuggestionV4.CURRENT_LOCATION_ID
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
                val leftSuggestionPrecedenceOrder = if (lhs.isMajorAirport) 1 else 2
                val rightSuggestionPrecedenceOrder = if (rhs.isMajorAirport) 1 else 2
                return leftSuggestionPrecedenceOrder.compareTo(rightSuggestionPrecedenceOrder)
            }
        })
    }

    fun getLxSuggestions(query: String, locale: String, client: String, observer: Observer<MutableList<SuggestionV4>>): Subscription {
        val type = SuggestionResultType.CITY or SuggestionResultType.MULTI_CITY or SuggestionResultType.NEIGHBORHOOD or
                SuggestionResultType.POINT_OF_INTEREST
        val lob = "ACTIVITIES"
        val features = "ta_hierarchy"
        return suggestV4(query, type, locale, features, client, lob, null).subscribe(observer)
    }

    private fun suggestV4(query: String, regiontype: Int, locale: String, features: String, client: String, lob: String?, siteId: Int?): Observable<MutableList<SuggestionV4>> {
        return suggestApi.suggestV4(query, locale, regiontype, false, features, client, lob, siteId)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions}
    }

    fun getNearbyLxSuggestions(locale: String, latlng: String, siteId: Int, client: String, observer: Observer<MutableList<SuggestionV4>>): Subscription {
        return getNearbyLxSuggestions(locale, latlng, siteId, client)
                .doOnNext { list -> renameFirstResultIdToCurrentLocation(list) }
                .subscribe(observer)
    }

    fun getNearbyLxSuggestions(locale: String, latlng: String, siteId: Int, client: String): Observable<MutableList<SuggestionV4>> {
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
