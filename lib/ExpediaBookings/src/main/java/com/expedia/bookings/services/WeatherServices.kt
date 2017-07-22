package com.expedia.bookings.services

import com.expedia.bookings.data.weather.*
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription

open class WeatherServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val weatherApi: WeatherApi by lazy {
        val gson = GsonBuilder()
                .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
                .create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()
        adapter.create(WeatherApi::class.java)
    }

    fun locationSearch(params: WeatherLocationParams, observer: Observer<List<WeatherLocationResponse>>): Subscription {
        return weatherApi.locationSearch(params.apiKey, params.query)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
    }

    fun getFiveDayForecast(params: WeatherForecastParams, observer: Observer<List<WeatherForecastResponse>>): Subscription {
        return weatherApi.getFiveDayForecast(params.apiKey, params.locationCode)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
    }
}
