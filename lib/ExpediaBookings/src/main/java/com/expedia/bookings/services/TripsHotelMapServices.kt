package com.expedia.bookings.services

import com.expedia.bookings.data.trips.EBRequestParams
import com.expedia.bookings.data.trips.EventbriteResponse
import com.expedia.bookings.data.trips.TcsRequestParams
import com.expedia.bookings.data.trips.TcsResponse
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription

open class TripsHotelMapServices(val subscribeOn: Scheduler, val observeOn: Scheduler) {

    enum class Endpoint(val value: String) {
        TCS("https://apim.expedia.com"),
        EB("https://www.eventbriteapi.com/v3/")
    }

    val tcsApkKey = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJoYWNrYXRob24iLCJpc3MiOiJUcmF2ZWwgQ29udGVudCBTZXJ2aWNlIEF1dGhlbnRpY2F0aW9uIiwiYXV0aCI6IlJPTEVfVVNFUiIsImV4cCI6MTUwOTIwNjU4Mn0.5IUuc_XKbBzhYKBIFj85VlUvCYnnAQHObPIyieUGTlWaCaVcabw55mZ7cOemxjJJmUSWkW6lM_BkfOdZhQQK9Q"

    companion object {
        fun create(baseUrl: String): TripsHotelMapAPI {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(baseUrl)
                    .build()

            return retrofit.create(TripsHotelMapAPI::class.java)
        }
    }

    val tcsApi: TripsHotelMapAPI = create(Endpoint.TCS.value)
    val eventriteApi: TripsHotelMapAPI = create(Endpoint.EB.value)

    //Points of interest from the TCS API
    fun getPoiNearby(params: TcsRequestParams, observer: Observer<TcsResponse>): Subscription {
        return tcsApi.poiNearby(
                params.latitude,
                params.longitude,
                params.tcsApkKey,
                params.langId,
                params.sections,
                params.version,
                params.useCache
        )
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
    }

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