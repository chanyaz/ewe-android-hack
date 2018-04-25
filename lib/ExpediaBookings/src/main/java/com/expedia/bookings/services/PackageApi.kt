package com.expedia.bookings.services

import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.multiitem.MultiItemApiSearchResponse
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.MultiItemApiCreateTripResponse

import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.math.BigDecimal

interface PackageApi {

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
            @Query("originId") originId: String?,
            @Query("destination") destination: String?,
            @Query("destinationId") destinationId: String?,
            @Query("fromDate") fromDate: String,
            @Query("toDate") toDate: String,
            @Query("adults") adults: Int,
            @Query("childAges") childAges: String?,
            @Query("infantsInSeats") infantsInSeats: Boolean?,
            @Query("hotelId") hotelId: String? = null,
            @Query("ratePlanCode") ratePlanCode: String? = null,
            @Query("roomTypeCode") roomTypeCode: String? = null,
            @Query("legIndex") legIndex: Int? = null,
            @Query("legId[0]") outboundLegId: String? = null,
            @Query("legId[1]") inboundLegId: String? = null,
            @Query("flightPIID") flightPIID: String? = null,
            @Query("anchorTotalPrice") anchorTotalPrice: BigDecimal? = null,
            @Query("currencyCode") currencyCode: String? = null,
            @Query("cabinClass") cabinClass: String? = null): Observable<MultiItemApiSearchResponse>

    @GET("/api/multiitem/v1/createTrip")
    fun multiItemCreateTrip(@Query("flightPIID") flightPIID: String,
                            @Query("hotelId") hotelID: String,
                            @Query("inventoryType") inventoryType: String,
                            @Query("ratePlanCode") ratePlanCode: String,
                            @Query("roomTypeCode") roomTypeCode: String,
                            @Query("adults") adults: Int,
                            @Query("checkInDate") checkInDate: String,
                            @Query("checkOutDate") checkOutDate: String,
                            @Query("totalPrice") totalPrice: BigDecimal,
                            @Query("childAges") childAges: String? = null,
                            @Query("infantInSeats") infantsInSeats: Boolean?): Observable<MultiItemApiCreateTripResponse>
}
