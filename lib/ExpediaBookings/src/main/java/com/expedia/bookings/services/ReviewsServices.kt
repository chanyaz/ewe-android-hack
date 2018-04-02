package com.expedia.bookings.services

import com.expedia.bookings.data.hotels.HotelReviewsParams
import com.expedia.bookings.data.hotels.HotelReviewsResponse
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import io.reactivex.Observable
import io.reactivex.Scheduler
import java.io.IOException
import java.util.concurrent.TimeUnit

class ReviewsServices(endPoint: String, client: OkHttpClient, interceptor: Interceptor, private val observeOn: Scheduler, private val subscribeOn: Scheduler) {

    val reviewsApi: ReviewsApi by lazy {
        val gson = GsonBuilder()
                .registerTypeAdapter(DateTime::class.java, object : TypeAdapter<DateTime>() {

                    @Throws(IOException::class)
                    override fun write(out: JsonWriter, value: DateTime) {
                        out.beginObject()
                        out.name(value.toString(ISODateTimeFormat.date()))
                    }

                    @Throws(IOException::class)
                    override fun read(input: JsonReader): DateTime? {
                        return DateTime.parse(input.nextString())
                    }
                }).create()

        val adapter = Retrofit.Builder()
                .baseUrl(endPoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(ReviewsApi::class.java)
    }

    fun reviews(reviewsParams: HotelReviewsParams): Observable<HotelReviewsResponse> {
        return reviewsApi.hotelReviews(reviewsParams.hotelId, reviewsParams.sortBy, reviewsParams.pageNumber * reviewsParams.numReviewsPerPage, reviewsParams.numReviewsPerPage, reviewsParams.languageSort)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }

    fun translate(reviewId: String, targetLanguage: String): Observable<HotelReviewsResponse.Review> {
        return reviewsApi.translate(reviewId, targetLanguage)
                .timeout(2L, TimeUnit.SECONDS)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { translationResponse -> translationResponse.review }
    }
}
