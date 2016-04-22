package com.expedia.bookings.services

import com.expedia.bookings.data.PackageFlightDeserializer
import com.expedia.bookings.data.PackageHotelDeserializer
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageOffersResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.google.gson.GsonBuilder
import com.squareup.okhttp.OkHttpClient
import org.joda.time.DateTime
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import retrofit.client.OkClient
import retrofit.converter.GsonConverter
import rx.Observable
import rx.Scheduler
import java.text.NumberFormat
import java.util.ArrayList
import java.util.HashMap
import java.util.Currency

class PackageServices(endpoint: String, okHttpClient: OkHttpClient, requestInterceptor: RequestInterceptor, val observeOn: Scheduler, val subscribeOn: Scheduler, logLevel: RestAdapter.LogLevel) {

    val packageApi: PackageApi by lazy {
        val gson = GsonBuilder()
                .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
                .registerTypeAdapter(PackageSearchResponse.HotelPackage::class.java, PackageHotelDeserializer())
                .registerTypeAdapter(PackageSearchResponse.FlightPackage::class.java, PackageFlightDeserializer())
                .create()

        val adapter = RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setRequestInterceptor(requestInterceptor)
                .setLogLevel(logLevel)
                .setConverter(GsonConverter(gson))
                .setClient(OkClient(okHttpClient))
                .build()

        adapter.create(PackageApi::class.java)
    }

    fun packageSearch(params: PackageSearchParams): Observable<PackageSearchResponse> {
        val nf = NumberFormat.getCurrencyInstance()
        return packageApi.packageSearch(params.toQueryMap()).observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .doOnNext { response ->
                    if (response.hasErrors()) return@doOnNext
                    response.packageResult.hotelsPackage.hotels.forEach { hotel ->
                        hotel.packageOfferModel = response.packageResult.packageOfferModels.find { offer ->
                            offer.hotel == hotel.hotelPid
                        }
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

                    response.packageResult.hotelsPackage.hotels = sortedHotels
                }
    }

    fun hotelOffer(piid: String, checkInDate: String, checkOutDate: String, ratePlanCode: String?, roomTypeCode: String?): Observable<PackageOffersResponse> {
        return packageApi.packageHotelOffers(piid, checkInDate, checkOutDate, ratePlanCode, roomTypeCode)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }

    fun hotelInfo(hotelId: String): Observable<HotelOffersResponse> {
        return packageApi.hotelInfo(hotelId)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }

    fun createTrip(body: PackageCreateTripParams): Observable<PackageCreateTripResponse> {
        return packageApi.createTrip(body.productKey, body.destinationId, body.numOfAdults, body.isInfantsInLap, body.childAges)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }

    fun checkout(body: Map<String, Any>): Observable<PackageCheckoutResponse> {
        return packageApi.checkout(body)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }
}
