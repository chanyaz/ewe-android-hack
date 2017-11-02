package com.expedia.bookings.services

import com.expedia.bookings.subscribeObserver
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

class TripsServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) : TripsServicesInterface {

    val tripsApi: TripsApi by lazy {
        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(JsonConverterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(TripsApi::class.java)
    }

    override fun getTripDetails(tripId: String, useCache: Boolean): JSONObject? {
        val call = tripsApi.tripDetails(tripId, if (useCache) "1" else "0")
        val response = call.execute()
        return if (response.isSuccessful) {
            response.body()
        } else {
            val errorResponse = response.errorBody()
            if (errorResponse != null) {
                JSONObject(errorResponse.string())
            } else {
                null
            }
        }
    }

    override fun getSharedTripDetails(sharedTripUrl: String): JSONObject? {
        val call = tripsApi.sharedTripDetails(sharedTripUrl)
        val response = call.execute()
        return if (response.isSuccessful) {
            response.body()
        } else {
            val errorResponse = response.errorBody()
            if (errorResponse != null) {
                JSONObject(errorResponse.string())
            } else {
                null
            }
        }
    }

    override fun getGuestTrip(tripId: String, guestEmail: String, useCache: Boolean): JSONObject? {
        val call = tripsApi.guestTrip(tripId, guestEmail, if (useCache) "1" else "0")
        val response = call.execute()
        return if (response.isSuccessful) {
            response.body()
        } else {
            val errorResponse = response.errorBody()
            if (errorResponse != null) {
                JSONObject(errorResponse.string())
            } else {
                null
            }
        }
    }

    override fun getTripDetailsObservable(tripId: String, useCache: Boolean): Observable<JSONObject> {
        return tripsApi.tripDetailsObservable(tripId, if (useCache) "1" else "0")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }

    override fun getSharedTripDetailsObservable(sharedTripUrl: String): Observable<JSONObject> {
        return tripsApi.sharedTripDetailsObservable(sharedTripUrl)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }

    override fun getGuestTripObservable(tripId: String, guestEmail: String, useCache: Boolean): Observable<JSONObject> {
        return tripsApi.guestTripObservable(tripId, guestEmail, if (useCache) "1" else "0")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }
}
