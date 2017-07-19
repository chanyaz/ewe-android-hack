package com.expedia.bookings.services

import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.multiitem.MultiItemApiSearchResponse
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageOffersResponse
import com.expedia.bookings.data.packages.PackageSearchResponse

import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import rx.Observable

interface PackageApi {

    @FormUrlEncoded
    @POST("/getpackages/v1?forceNoRedir=1&packageType=fh")
    fun packageSearch(
            @FieldMap queryParams: Map<String, @JvmSuppressWildcards Any?>): Observable<PackageSearchResponse>

    @GET("/api/packages/hotelOffers")
    fun packageHotelOffers(
            @Query("productKey") productKey: String,
            @Query("checkInDate") checkInDate: String,
            @Query("checkOutDate") checkOutDate: String,
            @Query("ratePlanCode") ratePlanCode: String?,
            @Query("roomTypeCode") roomTypeCode: String?,
            @Query("numberOfAdultTravelers") numberOfAdultTravelers: Int?,
            @Query("childTravelerAge") childTravelerAge: Int?): Observable<PackageOffersResponse>

    @GET("/m/api/hotel/info")
    fun hotelInfo(
            @Query("hotelId") hotelId: String): Observable<HotelOffersResponse>

    @FormUrlEncoded
    @POST("/api/packages/createTrip")
    fun createTrip(
            @Field("productKey") productKey: String,
            @Query("destinationId") destId: String,
            @Query("roomOccupants[0].numberOfAdultGuests") numberOfAdults: Int,
            @Query("roomOccupants[0].infantsInSeat") infantInSeat: Boolean,
            @Query("roomOccupants[0].childGuestAge") childAges: List<Int>,
            @Query("mobileFlexEnabled") flexEnabled: Boolean): Observable<PackageCreateTripResponse>

    @FormUrlEncoded
    @POST("/api/packages/checkout")
    fun checkout(
            @FieldMap queryParams: Map<String, @JvmSuppressWildcards Any>): Observable<PackageCheckoutResponse>

    //MID API

    @GET("/api/multiitem/v1/{productType}")
    fun multiItemSearch(
            @Path("productType") productType: String,
            @Query("packageType") packageType: String?,
            @Query("origin") origin: String?,
            @Query("destination") destination: String?,
            @Query("fromDate") fromDate: String,
            @Query("toDate") toDate: String,
            @Query("adults") adults: Int,
            @Query("hotelId") hotelId: String? = null,
            @Query("ratePlanCode") ratePlanCode: String? = null,
            @Query("roomTypeCode") roomTypeCode: String? = null,
            @Query("legIndex") legIndex: Int? = null,
            @Query("legId[0]") outboundLegId: String? = null,
            @Query("legId[1]") inboundLegId: String? = null): Observable<MultiItemApiSearchResponse>
}
