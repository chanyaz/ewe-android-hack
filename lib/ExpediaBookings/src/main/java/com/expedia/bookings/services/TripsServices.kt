package com.expedia.bookings.services

import io.reactivex.Observable
import io.reactivex.Scheduler
import okhttp3.Interceptor
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.net.CookieManager
import java.net.CookiePolicy

class TripsServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler, private val nonFatalLogger: NonFatalLoggerInterface) : TripsServicesInterface {

    private val LOGTAG = "TRIPS_SERVICES"
    private val tripsApi: TripsApi by lazy {
        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(JsonObjectConverterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(TripsApi::class.java)
    }

    private val tripsApiIgnoreCookies: TripsApi by lazy {
        val cookieHandler = CookieManager(
                null, CookiePolicy.ACCEPT_NONE)
        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(JsonObjectConverterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder().cookieJar(JavaNetCookieJar(cookieHandler)).addInterceptor(interceptor).build())
                .build()

        adapter.create(TripsApi::class.java)
    }

    override fun getTripDetails(tripId: String, useCache: Boolean): JSONObject? {
        val call = tripsApi.tripDetails(tripId, if (useCache) "1" else "0")
        try {
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
        } catch (e: Exception) {
            nonFatalLogger.logException(e)
            print("$LOGTAG Exception occurred when making getTripDetails call: ${e.printStackTrace()}")
            return null
        }
    }

    override fun getSharedTripDetails(sharedTripUrl: String): JSONObject? {
        val call = tripsApiIgnoreCookies.sharedTripDetails(sharedTripUrl)
        try {
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
        } catch (e: Exception) {
            nonFatalLogger.logException(e)
            print("$LOGTAG Exception occurred when making getTripDetails call: ${e.printStackTrace()}")
            return null
        }
    }

    override fun getGuestTrip(tripId: String, guestEmail: String, useCache: Boolean): JSONObject? {
        val call = tripsApiIgnoreCookies.guestTrip(tripId, guestEmail, if (useCache) "1" else "0")
        try {
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
        } catch (e: Exception) {
            nonFatalLogger.logException(e)
            print("$LOGTAG Exception occurred when making getTripDetails call: ${e.printStackTrace()}")
            return null
        }
    }

    override fun getTripDetailsObservable(tripId: String, useCache: Boolean): Observable<JSONObject> {
        return tripsApi.tripDetailsObservable(tripId, if (useCache) "1" else "0")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }

    override fun getSharedTripDetailsObservable(sharedTripUrl: String): Observable<JSONObject> {
        return tripsApiIgnoreCookies.sharedTripDetailsObservable(sharedTripUrl)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }

    override fun getGuestTripObservable(tripId: String, guestEmail: String, useCache: Boolean): Observable<JSONObject> {
        return tripsApiIgnoreCookies.guestTripObservable(tripId, guestEmail, if (useCache) "1" else "0")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }
}
