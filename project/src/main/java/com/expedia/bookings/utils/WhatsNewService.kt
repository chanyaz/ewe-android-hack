package com.expedia.bookings.utils

import com.expedia.model.SuperPojo
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call

/**
 * Created by nbirla on 20/11/17.
 */
interface WhatsNewService {

    @GET("features")
    fun getFeatures(@Query("pos") pos: String,
                    @Query("platform") platform: String,
                    @Query("brand") brand: String,
                    @Query("approvalState") approvalState: String):
            Call<SuperPojo>

    companion object {
        fun create(): WhatsNewService {

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(
                            RxJavaCallAdapterFactory.create())
                    .addConverterFactory(
                            GsonConverterFactory.create())
                    .baseUrl("http://whatsnumobile.us-west-2.test.expedia.com/")
                    .build()

            return retrofit.create(WhatsNewService::class.java)
        }
    }

}