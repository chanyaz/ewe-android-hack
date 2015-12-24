package com.expedia.bookings.services

import com.expedia.bookings.data.FlightPackageDeserializer
import com.expedia.bookings.data.HotelPackageDeserializer
import com.expedia.bookings.data.hotels.PackageSearchResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.google.gson.GsonBuilder
import com.squareup.okhttp.OkHttpClient
import org.joda.time.DateTime
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import retrofit.client.OkClient
import retrofit.converter.GsonConverter
import rx.Observable
import rx.Scheduler

public class PackageServices(endpoint: String, okHttpClient: OkHttpClient, requestInterceptor: RequestInterceptor, val observeOn: Scheduler, val subscribeOn: Scheduler, logLevel: RestAdapter.LogLevel) {

	val packageApi: PackageApi by lazy {
		val gson = GsonBuilder()
				.registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
				.registerTypeAdapter(PackageSearchResponse.HotelPackage::class.java, HotelPackageDeserializer())
				.registerTypeAdapter(PackageSearchResponse.FlightPackage::class.java, FlightPackageDeserializer())
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
		return packageApi.packageSearch(params.destination.regionNames.shortName, params.arrival.regionNames.shortName, params.destination.gaiaId, params.arrival.gaiaId, params.destination.hierarchyInfo?.airport?.airportCode, params.arrival.hierarchyInfo?.airport?.airportCode,
				params.checkIn.toString(), params.checkOut.toString())
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }
}
