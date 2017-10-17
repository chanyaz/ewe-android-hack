package com.expedia.bookings.hotel.util

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.RetrofitUtils
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

open class HotelInfoManager(private val hotelServices: HotelServices) {

    val offerSuccessSubject = PublishSubject.create<HotelOffersResponse>()
    val infoSuccessSubject = PublishSubject.create<HotelOffersResponse>()

    val offersNoInternetSubject = PublishSubject.create<Unit>()
    val infoNoInternetSubject = PublishSubject.create<Unit>()

    val soldOutSubject = PublishSubject.create<Unit>()

    open fun fetchOffers(params: HotelSearchParams, hotelId: String) {
        hotelServices.offers(params, hotelId, offersObserver)
    }

    open fun fetchInfo(params: HotelSearchParams, hotelId: String) {
        hotelServices.info(params, hotelId, infoObserver)
    }

    private val offersObserver = object : DisposableObserver<HotelOffersResponse>() {
        override fun onNext(response: HotelOffersResponse) {
            if (response.hasErrors()
                    && response.firstError.errorCode == ApiError.Code.HOTEL_ROOM_UNAVAILABLE) {
                soldOutSubject.onNext(Unit)
            } else if (!response.hasErrors()) {
                offerSuccessSubject.onNext(response)
            }
        }

        override fun onComplete() {
        }

        override fun onError(e: Throwable) {
            if (RetrofitUtils.isNetworkError(e)) {
                offersNoInternetSubject.onNext(Unit)
            }
        }
    }

    private val infoObserver = object : DisposableObserver<HotelOffersResponse>() {
        override fun onNext(response: HotelOffersResponse) {
            if (!response.hasErrors()) {
                infoSuccessSubject.onNext(response)
            }
        }

        override fun onComplete() {
        }

        override fun onError(e: Throwable) {
            if (RetrofitUtils.isNetworkError(e)) {
                infoNoInternetSubject.onNext(Unit)
            }
        }
    }
}