package com.expedia.bookings.services

import com.expedia.bookings.data.SuggestionResultType
import com.expedia.bookings.data.cars.Suggestion
import com.expedia.bookings.data.cars.SuggestionResponse
import com.expedia.bookings.data.hotels.SuggestionV4
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
import kotlin.properties.Delegates

public class SuggestionV4Services(endpoint: String, okHttpClient: OkHttpClient, val observeOn: Scheduler, val subscribeOn: Scheduler, logLevel: RestAdapter.LogLevel) {
    val acceptJsonInterceptor: RequestInterceptor = object : RequestInterceptor {
        override fun intercept(request: RequestInterceptor.RequestFacade) {
            request.addHeader("Accept", "application/json")
        }
    }

    val suggestApi: SuggestApi by Delegates.lazy {
        val gson = GsonBuilder().registerTypeAdapter(SuggestionResponse::class.java, SuggestionResponse()).create()

        val adapter = RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setLogLevel(logLevel)
                .setConverter(GsonConverter(gson))
                .setClient(OkClient(okHttpClient))
                .setRequestInterceptor(acceptJsonInterceptor)
                .build()

        adapter.create<SuggestApi>(SuggestApi::class.java)
    }

    public fun getHotelSuggestionsV4(query: String, observer: Observer<List<SuggestionV4>>): Subscription {
        val type = SuggestionResultType.REGION
        return suggestApi.suggestV4(query, type, "ta_hierarchy")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions }
                .subscribe(observer)
    }

    public fun suggestNearbyV1(locale: String, latlng: String, siteId: Int): Observable<MutableList<SuggestionV4>> {
        return suggestApi.suggestNearbyV1(locale, latlng, siteId, SuggestionResultType.CITY, "d", "HOTELS")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions.take(3) }
                .map { list ->
                    list.map {
                        SuggestionV4.convertV1toV4(it)
                    }.toArrayList()
                }
    }
}
