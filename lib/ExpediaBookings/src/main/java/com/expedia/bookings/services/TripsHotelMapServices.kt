package com.expedia.bookings.services

import com.expedia.bookings.data.trips.EBRequestParams
import com.expedia.bookings.data.trips.EventbriteResponse
import com.expedia.bookings.data.trips.TcsRequestParams
import com.expedia.bookings.data.trips.TcsResponse
import com.expedia.bookings.data.trips.Trail
import com.expedia.bookings.data.trips.TrailsRequestParams
import com.expedia.bookings.data.trips.YelpAccessToken
import com.expedia.bookings.data.trips.YelpRequestParams
import com.expedia.bookings.data.trips.YelpResponse
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription

open class TripsHotelMapServices(val subscribeOn: Scheduler, val observeOn: Scheduler) {

    enum class Endpoint(val value: String) {
        TCS("https://apim.expedia.com"),
        EB("https://www.eventbriteapi.com/v3/"),
        TRAILS("https://api.transitandtrails.org"),
        YELP("https://api.yelp.com")
    }

    enum class Keys(val value: String) {
        TCS("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJoYWNrYXRob24iLCJpc3MiOiJUcmF2ZWwgQ29udGVudCBTZXJ2aWNlIEF1dGhlbnRpY2F0aW9uIiwiYXV0aCI6IlJPTEVfVVNFUiIsImV4cCI6MTUwOTIwNjU4Mn0.5IUuc_XKbBzhYKBIFj85VlUvCYnnAQHObPIyieUGTlWaCaVcabw55mZ7cOemxjJJmUSWkW6lM_BkfOdZhQQK9Q"),
        TRAILS("1eae79f3f03a42482cc98d8008905a08bb5e04b6931ed2ddbb3f566cb0de6b9b")
    }

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
    val trailsApi: TripsHotelMapAPI = create(Endpoint.TRAILS.value)
    val yelpApi: TripsHotelMapAPI = create(Endpoint.YELP.value)

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

    //Event brite API
    fun getEvents(params: EBRequestParams, observer: Observer<EventbriteResponse>): Subscription {
        return eventriteApi.eventsNearby(
                params.latitude,
                params.longitude,
                params.within,
                params.start,
                params.end,
                params.expand,
                params.categories
        )
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
    }

    //Trails API
    fun getTrails(params: TrailsRequestParams, observer: Observer<Array<Trail>>): Subscription {
        return trailsApi.trailHeadsNearby(
                params.key,
                params.latitude,
                params.longitude,
                params.limit,
                params.distance
        )
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
    }

    //Yelp access token
    fun getYelpAccessToken(observer: Observer<YelpAccessToken>): Subscription {
        return yelpApi.getYelpToken(
                "OAuth2",
                "r0qiL22HHC4vHs6byPC8Aw",
                "cEMJLufyTKrsea84BZAafZzF6oEG6HJC9laGnvhjQW8DT4DQ0q0UgDOMgUehoenF"
        )
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
    }

    //Yelp search API
    fun getYelpSearchResponse(params: YelpRequestParams, observer: Observer<YelpResponse>): Subscription {
        return yelpApi.getYelpBusinesses(
                "Bearer " + params.accessToken,
                params.term,
                params.latitude,
                params.longitude,
                params.limit,
                params.radius,
                params.sortBy
        )
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
    }
}