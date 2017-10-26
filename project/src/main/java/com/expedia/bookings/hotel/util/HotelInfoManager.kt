package com.expedia.bookings.hotel.util

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.RetrofitError
import com.expedia.bookings.utils.RetrofitUtils
import rx.Observer
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription

open class HotelInfoManager(private val hotelServices: HotelServices) {

    val offerSuccessSubject = PublishSubject.create<HotelOffersResponse>()
    val infoSuccessSubject = PublishSubject.create<HotelOffersResponse>()

    val offerRetrofitError = PublishSubject.create<RetrofitError>()
    val infoRetrofitError = PublishSubject.create<RetrofitError>()

    val errorSubject = PublishSubject.create<ApiError>()
    val soldOutSubject = PublishSubject.create<Unit>()

    open fun fetchOffers(params: HotelSearchParams, hotelId: String) {
        hotelServices.offers(params, hotelId, offersObserver)
    }

    open fun fetchInfo(params: HotelSearchParams, hotelId: String) {
        hotelServices.info(params, hotelId, infoObserver)
    }

    private val offersObserver = object : Observer<HotelOffersResponse> {
        override fun onNext(response: HotelOffersResponse?) {
            response?.let { response ->
                if (response.hasErrors()
                        && response.firstError.errorCode == ApiError.Code.HOTEL_ROOM_UNAVAILABLE) {
                    soldOutSubject.onNext(Unit)
                } else if (!response.hasErrors()) {
                    offerSuccessSubject.onNext(response)
                } else {
                   errorSubject.onNext(response.firstError)
                }
            }
        }

        override fun onCompleted() {
        }

        override fun onError(e: Throwable?) {
            val error = RetrofitUtils.getRetrofitError(e)
            error?.let { offerRetrofitError.onNext(error) }
        }
    }

    private val infoObserver = object : Observer<HotelOffersResponse> {
        override fun onNext(response: HotelOffersResponse?) {
            response?.let { response ->
                if (!response.hasErrors()) {
                    infoSuccessSubject.onNext(response)
                }
            }
        }

        override fun onCompleted() {
        }

        override fun onError(e: Throwable?) {
            val error = RetrofitUtils.getRetrofitError(e)
            error?.let { infoRetrofitError.onNext(error) }
        }
    }
}