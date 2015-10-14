package com.expedia.bookings.services

import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelApplyCouponParams
import com.expedia.bookings.data.hotels.HotelCheckoutParams
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.hotels.NearbyHotelParams
import com.expedia.bookings.utils.Strings
import com.google.gson.GsonBuilder
import com.squareup.okhttp.OkHttpClient
import org.joda.time.DateTime
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import retrofit.client.OkClient
import retrofit.converter.GsonConverter
import rx.Observable
import rx.Observer
import rx.Scheduler
import rx.Subscription
import kotlin.properties.Delegates

public class HotelServices(endpoint: String, okHttpClient: OkHttpClient, requestInterceptor: RequestInterceptor, val observeOn: Scheduler, val subscribeOn: Scheduler, logLevel: RestAdapter.LogLevel) {

	val hotelApi: HotelApi by lazy {
		val gson = GsonBuilder()
			.registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
			.create()

		val adapter = RestAdapter.Builder()
			.setEndpoint(endpoint)
			.setRequestInterceptor(requestInterceptor)
			.setLogLevel(logLevel)
			.setConverter(GsonConverter(gson))
			.setClient(OkClient(okHttpClient))
			.build()

		adapter.create(HotelApi::class.java)
	}

	public fun nearbyHotels(params: NearbyHotelParams, observer: Observer<MutableList<Hotel>>): Subscription {
		return hotelApi.nearbyHotelSearch(params.latitude, params.longitude, params.guestCount, params.checkInDate,
			params.checkOutDate, params.sortOrder, params.filterUnavailable)
			.observeOn(observeOn)
			.subscribeOn(subscribeOn)
			.map { response -> response.hotelList.take(25).toArrayList() }
			.subscribe(observer)
	}

    public fun regionSearch(params: HotelSearchParams): Observable<HotelSearchResponse> {
        return hotelApi.search(params.toQueryMap())
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .doOnNext { response ->
                    if (response.hasErrors()) return@doOnNext

                    response.userPriceType = getUserPriceType(response.hotelList)
                    response.allNeighborhoodsInSearchRegion.map { response.neighborhoodsMap.put(it.id, it) }
                    response.hotelList.map { hotel ->
                        if (hotel.locationId != null && response.neighborhoodsMap.containsKey(hotel.locationId)) {
                            response.neighborhoodsMap.get(hotel.locationId)?.hotels?.add(hotel)
                        }
                    }

                    response.allNeighborhoodsInSearchRegion.map {
                        it.score = it.hotels.map { 1 }.sum()
                    }

		     if (!params.suggestion.isCurrentLocationSearch) {
			     response.hotelList.forEach { hotel ->
				     hotel.proximityDistanceInMiles = 0.0
			    }
		     }

                    val (sponsored, nonSponsored) = response.hotelList.partition { it.isSponsoredListing }
                    val firstChunk = sponsored.take(1)
                    val secondChunk = nonSponsored.take(49)
                    val thirdChunk = sponsored.drop(1)
                    val rest = nonSponsored.drop(49)

                    response.hotelList = firstChunk + secondChunk + thirdChunk + rest
                }
    }

    public fun details(hotelSearchParams: HotelSearchParams, hotelId: String, observer: Observer<HotelOffersResponse>): Subscription {
        return hotelApi.offers(hotelSearchParams.checkIn.toString(), hotelSearchParams.checkOut.toString(),
                hotelSearchParams.getGuestString(), hotelId)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
    }

	public fun createTrip(body: HotelCreateTripParams, observer: Observer<HotelCreateTripResponse>): Subscription {
		return hotelApi.createTrip(body.toQueryMap())
				.observeOn(observeOn)
				.subscribeOn(subscribeOn)
				.subscribe(observer)
	}

	public fun applyCoupon(body: HotelApplyCouponParams): Observable<HotelCreateTripResponse> {
		return hotelApi.applyCoupon(body.toQueryMap())
				.observeOn(observeOn)
				.subscribeOn(subscribeOn)
	}

	public fun checkout(params: HotelCheckoutParams, observer: Observer<HotelCheckoutResponse>): Subscription {
		return hotelApi.checkout(params.toQueryMap())
				.observeOn(observeOn)
				.subscribeOn(subscribeOn)
				.subscribe(observer)
	}

	private fun getUserPriceType(hotels: List<Hotel>?): HotelRate.UserPriceType {
		if (hotels != null) {
			for (hotel in hotels) {
				val rate = hotel.lowRateInfo
				if (rate != null && Strings.isNotEmpty(rate.userPriceType)) {
					return HotelRate.UserPriceType.toEnum(rate.userPriceType)
				}
			}
		}
		return HotelRate.UserPriceType.UNKNOWN
	}
}
