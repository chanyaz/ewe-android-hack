package com.expedia.bookings.hotel.util

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.RetrofitUtils
import org.joda.time.LocalDate
import rx.Observer
import rx.subjects.PublishSubject

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

    open fun fetchDatelessInfo(hotelId: String) {
        hotelServices.datelessInfo(hotelId, infoObserver)
    }

    //todo need to standardize params to offers call
    open fun fetchOffers(startDate: String, endDate: String, hotelId: String) {
        hotelServices.offers(startDate, endDate, hotelId, offersObserver)
    }

    open fun fetchOffers(startDate: LocalDate, endDate: LocalDate, hotelId: String) {
        hotelServices.offers(startDate, endDate, hotelId, offersObserver)
    }

    private val offersObserver = object : Observer<HotelOffersResponse> {
        override fun onNext(response: HotelOffersResponse?) {
            response?.let { response ->
                if (response.hasErrors()
                        && response.firstError.errorCode == ApiError.Code.HOTEL_ROOM_UNAVAILABLE) {
                    soldOutSubject.onNext(Unit)
                } else if (!response.hasErrors()) {
                    offerSuccessSubject.onNext(response)
                }
            }
        }

        override fun onCompleted() {
        }

        override fun onError(e: Throwable?) {
            if (RetrofitUtils.isNetworkError(e)) {
                offersNoInternetSubject.onNext(Unit)
            }
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
            if (RetrofitUtils.isNetworkError(e)) {
                infoNoInternetSubject.onNext(Unit)
            }
        }
    }
}