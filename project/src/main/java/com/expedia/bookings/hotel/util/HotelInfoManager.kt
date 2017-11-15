package com.expedia.bookings.hotel.util

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.RetrofitError
import com.expedia.bookings.utils.RetrofitUtils
import rx.Observer
import rx.subjects.PublishSubject

open class HotelInfoManager(private val hotelServices: HotelServices) {

    val offerSuccessSubject = PublishSubject.create<HotelOffersResponse>()
    val infoSuccessSubject = PublishSubject.create<HotelOffersResponse>()

    val offerRetrofitErrorSubject = PublishSubject.create<RetrofitError>()
    val infoRetrofitErrorSubject = PublishSubject.create<RetrofitError>()

    val apiErrorSubject = PublishSubject.create<ApiError>()
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
                   apiErrorSubject.onNext(response.firstError)
                }
            }
        }

        override fun onCompleted() {
        }

        override fun onError(e: Throwable?) {
            val retrofitError = RetrofitUtils.getRetrofitError(e)
            offerRetrofitErrorSubject.onNext(retrofitError)
        }
    }

    private val infoObserver = object : Observer<HotelOffersResponse> {
        override fun onNext(response: HotelOffersResponse?) {
            response?.let { response ->
                if (!response.hasErrors()) {
                    infoSuccessSubject.onNext(response)
                } else {
                    apiErrorSubject.onNext(response.firstError)
                }
            }
        }

        override fun onCompleted() {
        }

        override fun onError(e: Throwable?) {
            val retrofitError = RetrofitUtils.getRetrofitError(e)
            infoRetrofitErrorSubject.onNext(retrofitError)
        }
    }
}
