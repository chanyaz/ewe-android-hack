package com.expedia.bookings.services

import com.expedia.bookings.data.clientlog.ClientLog
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelApplyCouponParameters
import com.expedia.bookings.data.hotels.HotelCheckoutV2Params
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
import org.joda.time.format.DateTimeFormat
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import retrofit.client.OkClient
import retrofit.converter.GsonConverter
import rx.Observable
import rx.Observer
import rx.Scheduler
import rx.Subscription

class HotelServices(endpoint: String, okHttpClient: OkHttpClient, requestInterceptor: RequestInterceptor, val observeOn: Scheduler, val subscribeOn: Scheduler, logLevel: RestAdapter.LogLevel) {

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

	fun nearbyHotels(params: NearbyHotelParams, observer: Observer<MutableList<Hotel>>): Subscription {
		return hotelApi.nearbyHotelSearch(params.latitude, params.longitude, params.guestCount, params.checkInDate,
			params.checkOutDate, params.sortOrder, params.filterUnavailable)
			.observeOn(observeOn)
			.subscribeOn(subscribeOn)
			.map { response -> response.hotelList.take(25).toMutableList() }
			.subscribe(observer)
	}

	fun search(params: HotelSearchParams, clientLogBuilder: ClientLog.Builder?): Observable<HotelSearchResponse> {
		// null out regionId and lat/lng if they're not set so we don't pass them in the request (Hotels API requirement #7218)
		val lat = if (params.suggestion.coordinates.lat != 0.0) params.suggestion.coordinates.lat else null
		val lng = if (params.suggestion.coordinates.lng != 0.0) params.suggestion.coordinates.lng else null
		val regionId = if (params.suggestion.gaiaId?.isNotBlank() ?: false) params.suggestion.gaiaId else null

		return hotelApi.search(regionId, lat, lng,
				params.checkIn.toString(), params.checkOut.toString(), params.guestString, params.shopWithPoints)
				.observeOn(observeOn)
				.subscribeOn(subscribeOn)
				.doOnNext { response ->
					clientLogBuilder?.responseTime(DateTime.now())
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

					if (!params.suggestion.isCurrentLocationSearch || params.suggestion.isGoogleSuggestionSearch) {
						response.hotelList.forEach { hotel ->
							hotel.proximityDistanceInMiles = 0.0
						}
					}

					response.hotelList = putSponsoredItemsInCorrectPlaces(response.hotelList)
				}
				.doOnNext { it.setHasLoyaltyInformation() }
	}

    fun offers(hotelSearchParams: HotelSearchParams, hotelId: String, observer: Observer<HotelOffersResponse>): Subscription {
        return hotelApi.offers(hotelSearchParams.checkIn.toString(), hotelSearchParams.checkOut.toString(), hotelSearchParams.guestString, hotelId, hotelSearchParams.shopWithPoints)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
				.doOnNext {
					it.hotelRoomResponse
							?.forEach {
								val room = it
								val payLater = room.payLaterOffer
								payLater?.isPayLater = true
								if (payLater != null && room.depositPolicy != null && !room.depositPolicy.isEmpty()) {
									it.rateInfo.chargeableRateInfo.depositAmount = "0";
									it.rateInfo.chargeableRateInfo.depositAmountToShowUsers = "0";
								}
							}
				}
				.doOnNext { hotelOffersResponse ->
					hotelOffersResponse.hotelRoomResponse?.forEach {
						if (it.rateInfo?.chargeableRateInfo?.loyaltyInfo?.isBurnApplied ?: false) {
							hotelOffersResponse.doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo = true
							return@doOnNext
						}
					}
				}
                .subscribe(observer)
    }

    fun info(hotelSearchParams: HotelSearchParams, hotelId: String, observer: Observer<HotelOffersResponse>): Subscription {
        val yyyyMMddDateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd")

        return hotelApi.info(hotelId).doOnNext {
            it.checkInDate = yyyyMMddDateTimeFormat.print(hotelSearchParams.checkIn)
            it.checkOutDate = yyyyMMddDateTimeFormat.print(hotelSearchParams.checkOut)
        }
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
    }

	fun createTrip(body: HotelCreateTripParams, isRewardsEnabledForCurrentPOS: Boolean, observer: Observer<HotelCreateTripResponse>): Subscription {
		return hotelApi.createTrip(body.toQueryMap())
				.observeOn(observeOn)
				.subscribeOn(subscribeOn)
				.doOnNext { it.isRewardsEnabledForCurrentPOS = isRewardsEnabledForCurrentPOS }
				.doOnNext { updatePayLaterRateInfo(it) }
				.subscribe(observer)
	}

	fun applyCoupon(body: HotelApplyCouponParameters, isRewardsEnabledForCurrentPOS: Boolean): Observable<HotelCreateTripResponse> {
		return hotelApi.applyCoupon(body.toQueryMap())
				.observeOn(observeOn)
				.subscribeOn(subscribeOn)
				.doOnNext { it.isRewardsEnabledForCurrentPOS = isRewardsEnabledForCurrentPOS }
				.doOnNext { updatePayLaterRateInfo(it) }
	}

	fun removeCoupon(tripId: String, isRewardsEnabledForCurrentPOS: Boolean): Observable<HotelCreateTripResponse> {
		return hotelApi.removeCoupon(tripId)
				.observeOn(observeOn)
				.subscribeOn(subscribeOn)
				.doOnNext { it.isRewardsEnabledForCurrentPOS = isRewardsEnabledForCurrentPOS }
				.doOnNext { updatePayLaterRateInfo(it) }
	}

	fun checkout(params: HotelCheckoutV2Params, observer: Observer<HotelCheckoutResponse>): Subscription {
		return hotelApi.checkout(params)
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

	companion object {
		fun putSponsoredItemsInCorrectPlaces(hotelList: List<Hotel>): List<Hotel> {
			val (sponsored, nonSponsored) = hotelList.partition { it.isSponsoredListing }
			val firstChunk = sponsored.take(1)
			val secondChunk = nonSponsored.take(49)
			val thirdChunk = sponsored.drop(1)
			val rest = nonSponsored.drop(49)
			return firstChunk + secondChunk + thirdChunk + rest
		}

		private fun updatePayLaterRateInfo(hotelCreateTripResponse: HotelCreateTripResponse) {
			val payLater = hotelCreateTripResponse.newHotelProductResponse?.hotelRoomResponse
			if (payLater != null && payLater.isPayLater && payLater.depositPolicy != null && !payLater.depositPolicy.isEmpty()) {
				payLater.rateInfo.chargeableRateInfo.depositAmount = "0";
				payLater.rateInfo.chargeableRateInfo.depositAmountToShowUsers = "0";
			}
		}
	}
}
