package com.expedia.bookings.services

import com.expedia.bookings.data.foursquare.FourSquareResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Created by nbirla on 14/02/18.
 */
interface FourSquareApi {

    @GET("/v2/venues/explore?section=trending&limit=50&sortByDistance=0&radius=50000&oauth_token=SBDRDZRZZLYPJLMNTU04AOCR2CWUGJU3KHBLNEW2O4NB5PNM&v=20180209")
    fun getTrendingPlaces(
            @Query("ll") latLong: String?,
            @Query("near") place: String?): Observable<FourSquareResponse>

    @GET("/v2/venues/{venueId}/photos?oauth_token=SBDRDZRZZLYPJLMNTU04AOCR2CWUGJU3KHBLNEW2O4NB5PNM&v=20180209")
    fun getImage(
            @Path("venueId") venueId: String): Observable<FourSquareResponse>
}
