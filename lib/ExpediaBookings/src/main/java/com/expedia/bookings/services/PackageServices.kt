package com.expedia.bookings.services

import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.multiitem.MultiItemApiSearchResponse
import com.expedia.bookings.data.packages.MultiItemApiCreateTripResponse
import com.expedia.bookings.data.packages.MultiItemCreateTripParams
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import io.reactivex.Scheduler
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class PackageServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    private val PACKAGE_TYPE = "fh"
    private val PRODUCT_TYPE_HOTELS = "hotels"
    private val PRODUCT_TYPE_ROOMS = "rooms"
    private val PRODUCT_TYPE_FLIGHTS = "flights"

    val packageApi: PackageApi by lazy {
        val gson = GsonBuilder()
                .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
                .create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(PackageApi::class.java)
    }

    //Bundle Exposed API
    fun packageSearch(params: PackageSearchParams, type: PackageProductSearchType): Observable<BundleSearchResponse> {
        return when (type) {
            PackageProductSearchType.MultiItemHotels -> multiItemHotelsSearch(params)
            PackageProductSearchType.MultiItemOutboundFlights -> multiItemOutboundFlightsSearch(params)
            PackageProductSearchType.MultiItemInboundFlights -> multiItemInboundFlightsSearch(params)
        }
    }

    //Multi Item API
    private fun multiItemHotelsSearch(params: PackageSearchParams): Observable<BundleSearchResponse> {
        return packageApi.multiItemSearch(
                productType = PRODUCT_TYPE_HOTELS,
                packageType = PACKAGE_TYPE,
                origin = params.origin?.hierarchyInfo?.airport?.airportCode,
                originId = params.originId,
                destination = params.destination?.hierarchyInfo?.airport?.airportCode,
                destinationId = params.destinationId,
                fromDate = params.startDate.toString(),
                toDate = params.endDate.toString(),
                adults = params.adults,
                childAges = params.childAges,
                infantsInSeats = params.infantsInSeats,
                flightPIID = if (params.isChangePackageSearch()) params.latestSelectedOfferInfo.flightPIID else null,
                cabinClass = params.flightCabinClass,
                filterParams = params.filterOptions?.getFiltersQueryMap())
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .doOnNext { it.setup() }
                .map { it }
    }

    fun multiItemRoomSearch(params: PackageSearchParams): Observable<MultiItemApiSearchResponse> {
        return packageApi.multiItemSearch(
                productType = PRODUCT_TYPE_ROOMS,
                packageType = PACKAGE_TYPE,
                origin = params.origin?.hierarchyInfo?.airport?.airportCode,
                originId = params.originId,
                destination = params.destination?.hierarchyInfo?.airport?.airportCode,
                destinationId = params.destinationId,
                fromDate = params.startDate.toString(),
                toDate = params.endDate.toString(),
                adults = params.adults,
                childAges = params.childAges,
                infantsInSeats = params.infantsInSeats,
                hotelId = params.latestSelectedOfferInfo.hotelId,
                flightPIID = params.latestSelectedOfferInfo.flightPIID,
                anchorTotalPrice = params.latestSelectedOfferInfo.productOfferPrice?.packageTotalPrice?.amount,
                currencyCode = params.latestSelectedOfferInfo.productOfferPrice?.packageTotalPrice?.currencyCode,
                cabinClass = params.flightCabinClass,
                filterParams = emptyMap())
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }

    private fun multiItemOutboundFlightsSearch(params: PackageSearchParams): Observable<BundleSearchResponse> {
        return packageApi.multiItemSearch(
                productType = PRODUCT_TYPE_FLIGHTS,
                packageType = PACKAGE_TYPE,
                origin = params.origin?.hierarchyInfo?.airport?.airportCode,
                originId = params.originId,
                destination = params.destination?.hierarchyInfo?.airport?.airportCode,
                destinationId = params.destinationId,
                fromDate = params.startDate.toString(),
                toDate = params.endDate.toString(),
                adults = params.adults,
                childAges = params.childAges,
                infantsInSeats = params.infantsInSeats,
                hotelId = params.latestSelectedOfferInfo.hotelId,
                ratePlanCode = params.latestSelectedOfferInfo.ratePlanCode,
                roomTypeCode = params.latestSelectedOfferInfo.roomTypeCode,
                legIndex = 0,
                anchorTotalPrice = params.latestSelectedOfferInfo.productOfferPrice?.packageTotalPrice?.amount,
                currencyCode = params.latestSelectedOfferInfo.productOfferPrice?.packageTotalPrice?.currencyCode,
                cabinClass = params.flightCabinClass,
                filterParams = emptyMap())
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .doOnNext {
                    it.setup()
                    if (!it.hasErrors()) {
                        params.latestSelectedOfferInfo.productOfferPrice?.let { offerPrice ->
                            it.setCurrentOfferPrice(offerPrice)
                        }
                    }
                }
                .map { it }
    }

    private fun multiItemInboundFlightsSearch(params: PackageSearchParams): Observable<BundleSearchResponse> {
        return packageApi.multiItemSearch(
                productType = PRODUCT_TYPE_FLIGHTS,
                packageType = PACKAGE_TYPE,
                origin = params.origin?.hierarchyInfo?.airport?.airportCode,
                originId = params.originId,
                destination = params.destination?.hierarchyInfo?.airport?.airportCode,
                destinationId = params.destinationId,
                fromDate = params.startDate.toString(),
                toDate = params.endDate.toString(),
                adults = params.adults,
                childAges = params.childAges,
                infantsInSeats = params.infantsInSeats,
                hotelId = params.latestSelectedOfferInfo.hotelId,
                ratePlanCode = params.latestSelectedOfferInfo.ratePlanCode,
                roomTypeCode = params.latestSelectedOfferInfo.roomTypeCode,
                legIndex = 1,
                outboundLegId = params.selectedLegId,
                anchorTotalPrice = params.latestSelectedOfferInfo.productOfferPrice?.packageTotalPrice?.amount,
                currencyCode = params.latestSelectedOfferInfo.productOfferPrice?.packageTotalPrice?.currencyCode,
                cabinClass = params.flightCabinClass,
                filterParams = emptyMap())
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .doOnNext {
                    it.setup()
                    if (!it.hasErrors()) {
                        params.latestSelectedOfferInfo.productOfferPrice?.let { offerPrice ->
                            it.setCurrentOfferPrice(offerPrice)
                        }
                    }
                }
                .map { it }
    }

    //PSS API
    fun hotelInfo(hotelId: String): Observable<HotelOffersResponse> {
        return packageApi.hotelInfo(hotelId)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }

    fun createTrip(body: PackageCreateTripParams): Observable<PackageCreateTripResponse> {
        return packageApi.createTrip(body.productKey, body.destinationId, body.numOfAdults, body.isInfantsInSeat, body.childAges, body.flexEnabled)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }

    fun checkout(body: Map<String, Any>): Observable<PackageCheckoutResponse> {
        return packageApi.checkout(body)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }

    fun multiItemCreateTrip(body: MultiItemCreateTripParams): Observable<MultiItemApiCreateTripResponse> {
        return packageApi.multiItemCreateTrip(body.flightPIID,
                body.hotelID,
                body.inventoryType,
                body.ratePlanCode,
                body.roomTypeCode,
                body.adults,
                body.startDate,
                body.endDate,
                body.totalPrice.packageTotalPrice.amount,
                body.childAges,
                body.infantsInSeats)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }
}
