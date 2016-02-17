package com.expedia.bookings.services

import com.expedia.bookings.data.PackageFlightDeserializer
import com.expedia.bookings.data.PackageHotelDeserializer
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
import kotlin.collections.filter
import kotlin.collections.find
import kotlin.collections.forEach

public class PackageServices(endpoint: String, okHttpClient: OkHttpClient, requestInterceptor: RequestInterceptor, val observeOn: Scheduler, val subscribeOn: Scheduler, logLevel: RestAdapter.LogLevel) {

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

	public fun packageSearch(params: PackageSearchParams): Observable<PackageSearchResponse> {
		return packageApi.packageSearch(params.toQueryMap()).observeOn(observeOn)
				.subscribeOn(subscribeOn)
				.doOnNext { response ->
					response.packageResult.hotelsPackage.hotels.forEach { hotel ->
						hotel.packageOfferModel = response.packageResult.packageOfferModels.find { offer ->
							offer.hotel == hotel.hotelPid
						}
						val lowRateInfo = HotelRate()
						lowRateInfo.strikethroughPriceToShowUsers = hotel.packageOfferModel.price.sumFlightAndHotel?.amount?.toFloat() ?: 0f
						lowRateInfo.priceToShowUsers = hotel.packageOfferModel.price.pricePerPerson.amount.toFloat()
						lowRateInfo.currencyCode = hotel.packageOfferModel.price.pricePerPerson.currencyCode
						hotel.lowRateInfo = lowRateInfo
					}
					response.packageResult.flightsPackage.flights.forEach { flight ->
						flight.packageOfferModel = response.packageResult.packageOfferModels.filter { it.price != null }.find { offer ->
							offer.flight == flight.flightPid
						}
					}

					val currentFlight = response.packageResult.flightsPackage.flights.find {
						it.flightPid == response.packageResult.currentSelectedOffer?.flight
					}

					currentFlight?.packageOfferModel = response.packageResult.currentSelectedOffer
				}
	}

	public fun hotelOffer(piid: String, checkInDate: String, checkOutDate: String): Observable<PackageOffersResponse> {
		return packageApi.packageHotelOffers(piid, checkInDate, checkOutDate)
				.observeOn(observeOn)
				.subscribeOn(subscribeOn)
	}

	public fun hotelInfo(hotelId: String): Observable<HotelOffersResponse> {
		return packageApi.hotelInfo(hotelId)
				.observeOn(observeOn)
				.subscribeOn(subscribeOn)
	}

	public fun createTrip(body: PackageCreateTripParams): Observable<PackageCreateTripResponse> {
		return packageApi.createTrip(body.toQueryMap())
				.observeOn(observeOn)
				.subscribeOn(subscribeOn)
	}

	public fun checkout(body: Map<String, Any>): Observable<PackageCheckoutResponse> {
		return packageApi.checkout(body)
				.observeOn(observeOn)
				.subscribeOn(subscribeOn)
	}
}
