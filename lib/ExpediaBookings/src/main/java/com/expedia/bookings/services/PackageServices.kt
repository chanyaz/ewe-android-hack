package com.expedia.bookings.services

import com.expedia.bookings.data.PackageFlightDeserializer
import com.expedia.bookings.data.PackageHotelDeserializer
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.multiitem.MultiItemApiSearchResponse
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageOffersResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observable
import rx.Scheduler
import java.text.NumberFormat
import java.util.ArrayList
import java.util.Currency
import java.util.HashMap

class PackageServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    private val PACKAGE_TYPE = "fh"
    private val PRODUCT_TYPE_HOTELS = "hotels"
    private val PRODUCT_TYPE_ROOMS = "rooms"
    private val PRODUCT_TYPE_FLIGHTS = "flights"

    val packageApi: PackageApi by lazy {
        val gson = GsonBuilder()
                .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
                .registerTypeAdapter(PackageSearchResponse.HotelPackage::class.java, PackageHotelDeserializer())
                .registerTypeAdapter(PackageSearchResponse.FlightPackage::class.java, PackageFlightDeserializer())
                .create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(PackageApi::class.java)
    }

    //Bundle Exposed API
    fun packageSearch(params: PackageSearchParams, type: ProductSearchType): Observable<BundleSearchResponse> {
        return when(type) {
            ProductSearchType.OldPackageSearch -> oldPackageSearch(params)
            ProductSearchType.MultiItemHotels -> multiItemHotelsSearch(params)
            ProductSearchType.MultiItemHotelRooms -> throw RuntimeException("Use `multiItemRoomSearch`for MID rooms search")
            ProductSearchType.MultiItemOutboundFlights -> multiItemOutboundFlightsSearch(params)
            ProductSearchType.MultiItemInboundFlights -> multiItemInboundFlightsSearch(params)
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
                adults = params.adults)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { it.setup() }
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
                hotelId = params.hotelId)
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
                hotelId = params.hotelId,
                ratePlanCode = params.ratePlanCode,
                roomTypeCode = params.roomTypeCode,
                legIndex = 0)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map {
                    it.setup()
                    it.setCurrentOfferModel(it.getHotels().first().packageOfferModel)//TODO PUK
                    it
                }
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
                hotelId = params.hotelId,
                ratePlanCode = params.ratePlanCode,
                roomTypeCode = params.roomTypeCode,
                legIndex = 1,
                outboundLegId = params.selectedLegId)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map {
                    it.setup()
                    it.setCurrentOfferModel(it.getHotels().first().packageOfferModel)//TODO PUK
                    it
                }
    }

    //PSS API
    private fun oldPackageSearch(params: PackageSearchParams): Observable<BundleSearchResponse> {
        val nf = NumberFormat.getCurrencyInstance()
        return packageApi.packageSearch(params.toQueryMap()).observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .doOnNext { response ->
                    if (response.hasErrors()) return@doOnNext
                    response.packageResult.hotelsPackage.hotels.forEach { hotel ->
                        hotel.packageOfferModel = response.packageResult.packageOfferModels.find { offer ->
                            offer.hotel == hotel.hotelPid
                        }
                    }

                    //filter out the hotels and packageoffer model list with null piid, price or price per person values
                    response.packageResult.hotelsPackage.hotels = response.packageResult.hotelsPackage.hotels.filter { it.packageOfferModel?.price?.pricePerPerson != null }
                    response.packageResult.packageOfferModels = response.packageResult.packageOfferModels.filter { it.price?.pricePerPerson != null }

                    //return if the hotels list is empty after filtering
                    if (response.packageResult.hotelsPackage.hotels.isEmpty()) {
                        return@doOnNext
                    }

                    response.packageResult.hotelsPackage.hotels.forEach { hotel ->
                        val lowRateInfo = HotelRate()
                        val currencyCode = hotel.packageOfferModel.price.pricePerPerson.currencyCode
                        val currency = Currency.getInstance(currencyCode)
                        if (currency != null) {
                            nf.currency = currency
                        }

                        val formattedPrice = hotel.packageOfferModel.price.flightPlusHotelPricePerPersonFormatted
                        var strikeThroughPrice = 0f
                        if (formattedPrice != null) {
                            try {
                                strikeThroughPrice = nf.parse(formattedPrice).toFloat()
                            } catch (ex: Exception) {
                            }
                        }
                        lowRateInfo.strikethroughPriceToShowUsers = strikeThroughPrice
                        lowRateInfo.priceToShowUsers = hotel.packageOfferModel.price.pricePerPerson.amount.toFloat()
                        lowRateInfo.currencyCode = currencyCode
                        hotel.lowRateInfo = lowRateInfo
                    }

                    response.packageResult.flightsPackage.flights.forEach { flight ->
                        flight.packageOfferModel = response.packageResult.packageOfferModels.filter { it.price != null }.find { offer ->
                            offer.flight == flight.flightPid
                        }
                    }

                    //get anchor flights (outbound and inbound) from currentSelectedOffer,
                    //which will show as "Best Flight" in flight list
                    val bestFlights = response.packageResult.flightsPackage.flights.filter {
                        it.flightPid == response.packageResult.currentSelectedOffer?.flight
                    }
                    bestFlights.forEach {
                        it.isBestFlight = true
                        it.packageOfferModel = response.packageResult.currentSelectedOffer
                    }

                    //match the order of packageOfferModel list for hotel list order
                    val hotelsMap = HashMap<String, Hotel>()
                    response.packageResult.hotelsPackage.hotels.forEach { hotel ->
                        hotelsMap.put(hotel.hotelPid, hotel)
                    }

                    val sortedHotels = ArrayList<Hotel?>()
                    response.packageResult.packageOfferModels.forEach { offer ->
                        sortedHotels.add(hotelsMap[offer.hotel])
                    }

                    //add default sort index to hotel list items
                    for (i in sortedHotels.indices) {
                        sortedHotels[i]?.sortIndex = i
                    }

                    response.packageResult.hotelsPackage.hotels = sortedHotels
                }
                .map { it }
    }

    fun hotelOffer(piid: String, checkInDate: String, checkOutDate: String, ratePlanCode: String?, roomTypeCode: String?, numberOfAdultTravelers: Int, childTravelerAge: Int?): Observable<PackageOffersResponse> {
        return packageApi.packageHotelOffers(piid, checkInDate, checkOutDate, ratePlanCode, roomTypeCode, numberOfAdultTravelers, childTravelerAge)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }

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
}
