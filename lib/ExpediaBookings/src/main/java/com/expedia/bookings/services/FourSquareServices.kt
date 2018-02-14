package com.expedia.bookings.services

import com.expedia.bookings.data.PackageFlightDeserializer
import com.expedia.bookings.data.PackageHotelDeserializer
import com.expedia.bookings.data.foursquare.FourSquareResponse
import com.expedia.bookings.data.foursquare.Response
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import io.reactivex.Scheduler
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by nbirla on 14/02/18.
 */
class FourSquareServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val fourSquareApi: FourSquareApi by lazy {
        val gson = GsonBuilder()
                .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
                .create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(FourSquareApi::class.java)
    }

    fun searchTrendingPlacesAround(latLng: String, place: String?): Observable<FourSquareResponse> {
        return fourSquareApi.getTrendingPlaces(latLng, place)
    }

    fun getImages(venueId: String): Observable<FourSquareResponse> {
        return fourSquareApi.getImage(venueId)
    }

}
