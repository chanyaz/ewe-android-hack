package com.expedia.bookings.hotel.widget


import io.reactivex.Observable
import retrofit2.http.POST
import retrofit2.http.Query

internal interface TranslationApi {
    @POST("/language/translate/v2")
    fun translateToTargetLanguage(@Query("target") target: String,
                                  @Query("q") textToTranslate: String,
                                  @Query("key") key: String): Observable<TranslationResponse>
}


