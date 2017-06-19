package com.expedia.bookings.services

import com.expedia.bookings.data.rail.responses.TranslateResponse
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription

class TranslateServices(private val observeOn: Scheduler, private val subscribeOn: Scheduler) {
    val translateApi by lazy {
        val adapter = Retrofit.Builder()
                .baseUrl("https://translate.yandex.net/")
                .addConverterFactory(buildGsonConverter())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(OkHttpClient())
                .build()

        adapter.create(TranslateApi::class.java)
    }

    fun translate(text: String, observer: Observer<TranslateResponse>): Subscription {
        val subscription = translateApi.translate("trnsl.1.1.20170619T150017Z.10e58c69a0144f48.1c098464a0ddd365223e56921afaeaec35835cca", text, "en-th")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
        return subscription
    }

    private fun buildGsonConverter() : GsonConverterFactory {
        val gsonBuilder = GsonBuilder()
        //gsonBuilder.registerTypeAdapter(RailCheckoutResponseWrapper::class.java, RailCheckoutResponseDeserializer());
        val myGson = gsonBuilder.create()
        return GsonConverterFactory.create(myGson)
    }
}
