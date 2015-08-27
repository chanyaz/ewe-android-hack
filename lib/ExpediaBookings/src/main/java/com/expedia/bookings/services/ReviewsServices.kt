package com.expedia.bookings.services

import com.expedia.bookings.data.hotels.HotelReviewsParams
import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.squareup.okhttp.OkHttpClient
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import retrofit.client.OkClient
import retrofit.converter.GsonConverter
import rx.Observer
import rx.Scheduler
import rx.Subscription
import java.io.IOException
import kotlin.properties.Delegates

public class ReviewsServices(endPoint: String, private val client: OkHttpClient, private val observeOn: Scheduler, private val subscribeOn: Scheduler, logLevel: RestAdapter.LogLevel) {

    val reviewsApi: ReviewsApi by Delegates.lazy {
        val acceptJsonInterceptor: RequestInterceptor = object : RequestInterceptor {
            override fun intercept(request: RequestInterceptor.RequestFacade) {
                request.addHeader("Accept", "application/json")
            }
        }
        val gson = GsonBuilder()
                .registerTypeAdapter(javaClass<DateTime>(), object : TypeAdapter<DateTime>() {

                    throws(IOException::class)
                    override fun write(out: JsonWriter, value: DateTime) {
                        out.beginObject()
                        out.name(value.toString(ISODateTimeFormat.date()))
                    }

                    throws(IOException::class)
                    override fun read(`in`: JsonReader): DateTime? {
                        return DateTime.parse(`in`.nextString())
                    }
                })
                .create()
        val adapter = RestAdapter.Builder()
                .setEndpoint(endPoint)
                .setLogLevel(logLevel)
                .setRequestInterceptor(acceptJsonInterceptor)
                .setConverter(GsonConverter(gson))
                .setClient(OkClient(this.client))
                .build()
        adapter.create(javaClass<ReviewsApi>())
    }

    public fun reviews(reviewsParams: HotelReviewsParams, observer: Observer<HotelReviewsResponse>): Subscription {
        return reviewsApi.hotelReviews(reviewsParams.hotelId, reviewsParams.sortBy, reviewsParams.pageNumber, reviewsParams.numReviewsPerPage)
                .observeOn(this.observeOn)
                .subscribeOn(this.subscribeOn)
                .subscribe(observer)
    }
}
