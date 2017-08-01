package com.expedia.bookings.services

import com.expedia.bookings.data.SuggestionV4
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
import com.expedia.bookings.subscribeObserver
import com.expedia.bookings.utils.Strings
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.HashMap

open class HotelServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val hotelApi: HotelApi by lazy {
        val gson = GsonBuilder()
                .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
                .create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(HotelApi::class.java)
    }

    fun nearbyHotels(params: NearbyHotelParams, observer: Observer<MutableList<Hotel>>): Disposable {
        return hotelApi.nearbyHotelSearch(params.latitude, params.longitude, params.guestCount, params.checkInDate,
                params.checkOutDate, params.sortOrder, params.filterUnavailable)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.hotelList.take(25).toMutableList() }
                .subscribeObserver(observer)
    }

    open fun search(params: HotelSearchParams, resultsResponseReceivedObservable: PublishSubject<Unit>? = null): Observable<HotelSearchResponse> {
        val lat = getLatitude(params.suggestion)
        val long = getLongitude(params.suggestion)
        val regionId = getRegionId(params)
        if (params.suggestion.hotelId != null) {
            params.enableSponsoredListings = false
        }

        return hotelApi.search(regionId, params.suggestion.hotelId, lat, long,
                params.checkIn.toString(), params.checkOut.toString(), params.guestString, params.shopWithPoints,
                params.filterUnavailable.toString(), params.getSortOrder().sortName, params.filterOptions?.getFiltersQueryMap() ?: HashMap(),
                params.mctc, params.enableSponsoredListings)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .doOnNext {
                    resultsResponseReceivedObservable?.onNext(Unit)
                }
                .doOnNext { response ->
                    doPostSearchClientSideWork(params, response)
                }
    }

    fun offers(hotelSearchParams: HotelSearchParams, hotelId: String, observer: Observer<HotelOffersResponse>): Disposable {
            return hotelApi.offers(hotelSearchParams.checkIn.toString(), hotelSearchParams.checkOut.toString(),
                    hotelSearchParams.guestString, hotelId, hotelSearchParams.shopWithPoints, hotelSearchParams.mctc)
                    .observeOn(observeOn)
                    .subscribeOn(subscribeOn)
                    .doOnNext { response ->
                        doPostOffersClientSideWork(response)
                    }
                    .subscribeObserver(observer)
    }

    fun info(hotelSearchParams: HotelSearchParams, hotelId: String, observer: Observer<HotelOffersResponse>): Disposable {
        val yyyyMMddDateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd")

        return hotelApi.info(hotelId).doOnNext {
            it.checkInDate = yyyyMMddDateTimeFormat.print(hotelSearchParams.checkIn)
            it.checkOutDate = yyyyMMddDateTimeFormat.print(hotelSearchParams.checkOut)
        }
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(observer)
    }

    fun createTrip(body: HotelCreateTripParams, isRewardsEnabledForCurrentPOS: Boolean, observer: Observer<HotelCreateTripResponse>): Disposable {
        return hotelApi.createTrip(body.toQueryMap())
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .doOnNext {
                    it.isRewardsEnabledForCurrentPOS = isRewardsEnabledForCurrentPOS
                    updatePayLaterRateInfo(it)
                    removeUnknownRewardsTypes(it)
                }
                .subscribeObserver(observer)
    }

    fun applyCoupon(body: HotelApplyCouponParameters, isRewardsEnabledForCurrentPOS: Boolean): Observable<HotelCreateTripResponse> {
        return hotelApi.applyCoupon(body.toQueryMap())
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .doOnNext {
                    it.isRewardsEnabledForCurrentPOS = isRewardsEnabledForCurrentPOS
                    updatePayLaterRateInfo(it)
                    removeUnknownRewardsTypes(it)
                }
    }

    fun removeCoupon(tripId: String, isRewardsEnabledForCurrentPOS: Boolean): Observable<HotelCreateTripResponse> {
        return hotelApi.removeCoupon(tripId)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .doOnNext {
                    it.isRewardsEnabledForCurrentPOS = isRewardsEnabledForCurrentPOS
                    updatePayLaterRateInfo(it)
                    removeUnknownRewardsTypes(it)
                }
    }

    fun checkout(params: HotelCheckoutV2Params, observer: Observer<HotelCheckoutResponse>): Disposable {
        return hotelApi.checkout(params)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .doOnNext { removeUnknownRewardsTypes(it) }
                .subscribeObserver(observer)
    }

    private fun getRegionId(params: HotelSearchParams) : String? {
        // null out regionId and lat/lng if they're not set so we don't pass them in the request (Hotels API requirement #7218)
        var regionId = if (params.suggestion.gaiaId?.isNotBlank() ?: false) params.suggestion.gaiaId else null
        val filterOptions = params.filterOptions
        if (filterOptions != null && !filterOptions.filterByNeighborhoodId.isNullOrEmpty()) {
            // Override default regionId for neighborhood search
            regionId = params!!.filterOptions!!.filterByNeighborhoodId
        }
        return regionId
    }

    private fun getLatitude(suggestion: SuggestionV4) : Double? {
        // null out regionId and lat/lng if they're not set so we don't pass them in the request (Hotels API requirement #7218)
        return if (suggestion.coordinates.lat != 0.0) suggestion.coordinates.lat else null
    }

    private fun getLongitude(suggestion: SuggestionV4) : Double? {
        // null out regionId and lat/lng if they're not set so we don't pass them in the request (Hotels API requirement #7218)
        return if (suggestion.coordinates.lng != 0.0) suggestion.coordinates.lng else null
    }

    private fun doPostOffersClientSideWork(response: HotelOffersResponse) {
        response.hotelRoomResponse?.forEach { roomResponse ->
            val payLater = roomResponse.payLaterOffer
            payLater?.isPayLater = true
            if (payLater != null && roomResponse.depositPolicy != null && !roomResponse.depositPolicy.isEmpty()) {
                roomResponse.rateInfo.chargeableRateInfo.depositAmount = "0";
                roomResponse.rateInfo.chargeableRateInfo.depositAmountToShowUsers = "0";
            }

            if (roomResponse.rateInfo?.chargeableRateInfo?.loyaltyInfo?.isBurnApplied ?: false) {
                response.doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo = true
                return
            }
        }
    }

    private fun doPostSearchClientSideWork(params: HotelSearchParams, response: HotelSearchResponse) {
        if (response.hasErrors()) return

        response.userPriceType = getUserPriceType(response.hotelList)
        response.allNeighborhoodsInSearchRegion.map { response.neighborhoodsMap.put(it.id, it) }
        response.hotelList.map { hotel ->
            if (hotel.locationId != null && response.neighborhoodsMap.containsKey(hotel.locationId)) {
                response.neighborhoodsMap[hotel.locationId]?.hotels?.add(hotel)
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

        response.hotelList.map { it.isSoldOut = !it.isHotelAvailable }

        response.setHasLoyaltyInformation()
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
        private fun updatePayLaterRateInfo(hotelCreateTripResponse: HotelCreateTripResponse) {
            val payLater = hotelCreateTripResponse.newHotelProductResponse?.hotelRoomResponse
            if (payLater != null && payLater.isPayLater && payLater.depositPolicy != null && !payLater.depositPolicy.isEmpty()) {
                payLater.rateInfo.chargeableRateInfo.depositAmount = "0";
                payLater.rateInfo.chargeableRateInfo.depositAmountToShowUsers = "0";
            }
        }

        private fun removeUnknownRewardsTypes(hotelCreateTripResponse: HotelCreateTripResponse) {
            // any unknown rewards program type will have a null programName; filter those out
            hotelCreateTripResponse.pointsDetails = hotelCreateTripResponse.pointsDetails?.filter { it.programName != null }
        }

        private fun removeUnknownRewardsTypes(hotelCheckoutResponse: HotelCheckoutResponse) {
            // any unknown rewards program type will have a null programName; filter those out
            hotelCheckoutResponse.pointsDetails = hotelCheckoutResponse.pointsDetails?.filter { it.programName != null }
        }
    }
}
