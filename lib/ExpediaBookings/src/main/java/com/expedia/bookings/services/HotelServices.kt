package com.expedia.bookings.services

import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.NearbyHotelParams
import com.google.gson.GsonBuilder
import com.squareup.okhttp.OkHttpClient
import org.joda.time.DateTime
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import retrofit.client.OkClient
import retrofit.converter.GsonConverter
import rx.Observer
import rx.Scheduler
import rx.Subscription
import kotlin.properties.Delegates

public class HotelServices(endpoint: String, okHttpClient: OkHttpClient, requestInterceptor: RequestInterceptor, val observeOn: Scheduler, val subscribeOn: Scheduler, logLevel: RestAdapter.LogLevel) {

	val hotelApi: HotelApi by Delegates.lazy {
		val gson = GsonBuilder()
			.registerTypeAdapter(javaClass<DateTime>(), DateTimeTypeAdapter())
			.create()

		val adapter = RestAdapter.Builder()
			.setEndpoint(endpoint)
			.setRequestInterceptor(requestInterceptor)
			.setLogLevel(logLevel)
			.setConverter(GsonConverter(gson))
			.setClient(OkClient(okHttpClient))
			.build()

		adapter.create(javaClass<HotelApi>())
	}

	public fun nearbyHotels(params: NearbyHotelParams, observer: Observer<MutableList<Hotel>>): Subscription {
		return hotelApi.nearbyHotelSearch(params.latitude, params.longitude, params.guestCount, params.checkInDate,
			params.checkOutDate, params.sortOrder, params.filterUnavailable)
			.observeOn(observeOn)
			.subscribeOn(subscribeOn)
			.map { response -> response.hotelList.take(25).toArrayList() }
			.subscribe(observer)
	}

	public fun suggestHotels(params: HotelSearchParams, observer: Observer<List<Hotel>>): Subscription {
		return hotelApi.suggestionHotelSearch(params.city.regionNames.shortName, params.checkIn.toString(), params.checkOut.toString(),
				params.getGuestString())
				.observeOn(observeOn)
				.subscribeOn(subscribeOn)
				.map { response -> response.hotelList }
				.subscribe(observer)
	}

    public fun getHotelDetails(hotelSearchParams: HotelSearchParams, hotelId: String, observer: Observer<HotelOffersResponse>): Subscription {
        return hotelApi.getHotelDetails(hotelSearchParams.checkIn.toString(), hotelSearchParams.checkOut.toString(),
                hotelSearchParams.getGuestString(), hotelId)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
    }
}
