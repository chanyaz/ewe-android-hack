package com.expedia.bookings.services

import com.expedia.bookings.data.HolidayCalendarResponse
import com.expedia.bookings.extensions.subscribeObserver
import com.google.gson.GsonBuilder
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class HolidayCalendarService(val endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    private val holidayCalendarApi: HolidayCalendarApi by lazy {
        val gson = GsonBuilder()
                .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
                .create()

        val okHttpClientBuilder = okHttpClient.newBuilder()

        okHttpClientBuilder.addInterceptor(interceptor)

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClientBuilder.build())
                .build()

        adapter.create(HolidayCalendarApi::class.java)
    }

    private var holidayCalendarSubscription: Disposable? = null

    fun getHoliday(siteName: String, langId: String, observer: Observer<HolidayCalendarResponse>): Disposable {
        holidayCalendarSubscription?.dispose()
        val holidayCalendarSubscription = holidayCalendarApi.holidayInfo(siteName, langId)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(observer)
        this.holidayCalendarSubscription = holidayCalendarSubscription
        return holidayCalendarSubscription
    }
}
