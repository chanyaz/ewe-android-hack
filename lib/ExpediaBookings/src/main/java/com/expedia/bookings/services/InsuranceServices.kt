package com.expedia.bookings.services

import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.insurance.InsuranceTripParams
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import io.reactivex.Observable
import io.reactivex.Scheduler

open class InsuranceServices(endpoint: String, client: OkHttpClient, interceptor: Interceptor,
                             val observeOn: Scheduler, val subscribeOn: Scheduler) {
    val insuranceApi: InsuranceApi by lazy {
        val gson = GsonBuilder().create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client.newBuilder().addInterceptor(interceptor).build())
                .build()
        adapter.create(InsuranceApi::class.java)
    }

    fun addInsuranceToTrip(params: InsuranceTripParams): Observable<FlightCreateTripResponse> {
        return insuranceApi.addInsuranceToTrip(params.toQueryMap()).observeOn(observeOn).subscribeOn(subscribeOn)
    }

    fun removeInsuranceFromTrip(params: InsuranceTripParams): Observable<FlightCreateTripResponse> {
        return insuranceApi.removeInsuranceFromTrip(params.toQueryMap()).observeOn(observeOn).subscribeOn(subscribeOn)
    }
}
