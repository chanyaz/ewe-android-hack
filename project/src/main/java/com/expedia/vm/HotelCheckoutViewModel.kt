package com.expedia.vm

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.hotels.HotelCheckoutV2Params
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.services.HotelServices
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.Date

open class HotelCheckoutViewModel(val hotelServices: HotelServices, val paymentModel: PaymentModel<HotelCreateTripResponse>) {

    // inputs
    val checkoutParams = PublishSubject.create<HotelCheckoutV2Params>()

    // outputs
    val errorObservable = PublishSubject.create<ApiError>()
    val noResponseObservable = PublishSubject.create<Throwable>()
    val checkoutResponseObservable = BehaviorSubject.create<HotelCheckoutResponse>()
    val priceChangeResponseObservable = BehaviorSubject.create<HotelCreateTripResponse>()
    val checkoutRequestStartTimeObservable = BehaviorSubject.create<Long>()

    init {
        checkoutParams.subscribe { params ->
            checkoutRequestStartTimeObservable.onNext(Date().time)
            hotelServices.checkout(params, getCheckoutResponseObserver())
        }
    }

    open fun getCheckoutResponseObserver(): Observer<HotelCheckoutResponse> {
        return object : DisposableObserver<HotelCheckoutResponse>() {
            override fun onNext(checkout: HotelCheckoutResponse) {
                if (checkout.hasErrors()) {
                    when (checkout.firstError.errorCode) {
                        ApiError.Code.PRICE_CHANGE -> {
                            val hotelCreateTripResponse = Db.getTripBucket().hotelV2.updateAfterCheckoutPriceChange(checkout)
                            priceChangeResponseObservable.onNext(hotelCreateTripResponse)
                            paymentModel.priceChangeDuringCheckoutSubject.onNext(hotelCreateTripResponse)
                        }
                        ApiError.Code.INVALID_INPUT -> {
                            val field = checkout.firstError.errorInfo.field
                            if (field == "mainMobileTraveler.lastName" ||
                                    field == "mainMobileTraveler.firstName" ||
                                    field == "phone") {
                                val apiError = ApiError(ApiError.Code.HOTEL_CHECKOUT_TRAVELLER_DETAILS)
                                apiError.errorInfo = ApiError.ErrorInfo()
                                apiError.errorInfo.field = field
                                errorObservable.onNext(apiError)
                            } else {
                                val apiError = ApiError(ApiError.Code.HOTEL_CHECKOUT_CARD_DETAILS)
                                apiError.errorInfo = ApiError.ErrorInfo()
                                apiError.errorInfo.field = field
                                errorObservable.onNext(apiError)
                            }
                        }
                        ApiError.Code.TRIP_ALREADY_BOOKED -> {
                            errorObservable.onNext(checkout.firstError)
                        }
                        ApiError.Code.SESSION_TIMEOUT -> {
                            errorObservable.onNext(checkout.firstError)
                        }
                        ApiError.Code.PAYMENT_FAILED -> {
                            errorObservable.onNext(checkout.firstError)
                        }
                        ApiError.Code.HOTEL_ROOM_UNAVAILABLE -> {
                            errorObservable.onNext(checkout.firstError)
                        }
                        else -> {
                            errorObservable.onNext(ApiError(ApiError.Code.HOTEL_CHECKOUT_UNKNOWN))
                        }
                    }
                } else {
                    checkoutResponseObservable.onNext(checkout)
                }
            }

            override fun onError(e: Throwable) {
                noResponseObservable.onNext(e)
            }

            override fun onComplete() {
                // ignore
            }
        }
    }
}

