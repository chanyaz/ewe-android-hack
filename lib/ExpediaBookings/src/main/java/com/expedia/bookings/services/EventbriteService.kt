package com.expedia.bookings.services

import com.expedia.bookings.data.trips.EBRequestParams
import com.expedia.bookings.data.trips.EventbriteResponse
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription


class EventbriteService(val subscribeOn: Scheduler, val observeOn: Scheduler) {

    enum class Endpoint(val value: String) {
        EB("https://www.eventbriteapi.com/v3/")
    }

    companion object {
        fun create(baseUrl: String): EventbriteAPI {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(baseUrl)
                    .build()
            return retrofit.create(EventbriteAPI::class.java)
        }
    }

    val eventriteApi: EventbriteAPI = create(Endpoint.EB.value)

    fun getEvents(params: EBRequestParams, observer: Observer<EventbriteResponse>): Subscription {
        return eventriteApi.eventsNearby(
                params.latitude,
                params.longitude,
                params.within,
                params.start,
                params.end,
                params.expand
        )
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
    }
}