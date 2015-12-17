package com.expedia.bookings.services

import com.expedia.bookings.data.SuggestionResultType
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.data.hotels.SuggestionV4Response
import com.google.gson.GsonBuilder
import com.squareup.okhttp.OkHttpClient
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import retrofit.client.OkClient
import retrofit.converter.GsonConverter
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

        val adapter = RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setLogLevel(logLevel)
                .setClient(OkClient(okHttpClient))
                .setRequestInterceptor(acceptJsonInterceptor)
                .build()

        adapter.create<SuggestApi>(javaClass<SuggestApi>())
    }

    public fun getHotelSuggestionsV4(query: String, observer: Observer<List<SuggestionV4>>): Subscription {
        val type = SuggestionResultType.REGION
        return suggestApi.suggestV4(query, type, "ta_hierarchy")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions }
                .subscribe(observer)
    }

}