package com.expedia.bookings.hotel.util

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.RetrofitError
import com.expedia.bookings.utils.RetrofitUtils
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

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

    private val offersObserver = object : DisposableObserver<HotelOffersResponse>() {

        override fun onNext(hotelOffersResponse: HotelOffersResponse) {
            if (hotelOffersResponse.hasErrors()
                    && hotelOffersResponse.firstError.getErrorCode() == ApiError.Code.HOTEL_ROOM_UNAVAILABLE) {
                soldOutSubject.onNext(Unit)
            } else if (!hotelOffersResponse.hasErrors()) {
                offerSuccessSubject.onNext(hotelOffersResponse)
            } else {
                apiErrorSubject.onNext(hotelOffersResponse.firstError)
            }
        }

        override fun onComplete() {
        }

        override fun onError(e: Throwable) {
            val retrofitError = RetrofitUtils.getRetrofitError(e)
            offerRetrofitErrorSubject.onNext(retrofitError)
        }
    }

    private val infoObserver = object : DisposableObserver<HotelOffersResponse>() {
        override fun onNext(hotelOffersResponse: HotelOffersResponse) {
            if (!hotelOffersResponse.hasErrors()) {
                infoSuccessSubject.onNext(hotelOffersResponse)
            } else {
                apiErrorSubject.onNext(hotelOffersResponse.firstError)
            }
        }

        override fun onComplete() {
        }

        override fun onError(e: Throwable) {
            val retrofitError = RetrofitUtils.getRetrofitError(e)
            infoRetrofitErrorSubject.onNext(retrofitError)
        }
    }
}
