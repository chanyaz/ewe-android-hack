package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.hotels.HotelApplyCouponCodeParameters
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.subscribeObserver
import com.expedia.bookings.utils.RetrofitUtils
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class ApplyCouponViewModel(val context: Context, hotelServices: HotelServices, val paymentModel: PaymentModel<HotelCreateTripResponse>, val couponErrorMap: Map<String, Int>) {

    val applyActionCouponParam = BehaviorSubject.create<HotelApplyCouponCodeParameters>()
    val applyCouponProgressObservable = PublishSubject.create<Unit>()
    val applyCouponSuccessObservable = PublishSubject.create<HotelCreateTripResponse>()
    val errorMessageObservable = BehaviorSubject.create<String>()
    val hasDiscountObservable = BehaviorSubject.create<Boolean>()
    val errorShowDialogObservable = PublishSubject.create<ApiError>()
    val onCouponSubmitClicked = PublishSubject.create<Unit>()
    val networkErrorAlertDialogObservable = PublishSubject.create<Unit>()

    init {
        applyActionCouponParam.subscribe { params ->
            applyCouponProgressObservable.onNext(Unit)
            hotelServices.applyCoupon(params, PointOfSale.getPointOfSale().isPwPEnabledForHotels).subscribeObserver(couponResponseObserver)
        }
    }

    val couponResponseObserver = object : DisposableObserver<HotelCreateTripResponse>() {
        override fun onNext(tripResponse: HotelCreateTripResponse) {
            if (tripResponse.hasErrors()) {
                setupErrorObservables(tripResponse)
            } else {
                applyCouponSuccessObservable.onNext(tripResponse)
            }
        }

        override fun onComplete() {
            // ignore
        }

        override fun onError(e: Throwable) {
            raiseAlertDialog(e)
        }
    }

    fun setupErrorObservables(trip: HotelCreateTripResponse) {
        val errorType = trip.firstError.errorInfo.couponErrorType
        val stringId = couponErrorMap[errorType] ?: R.string.coupon_error_fallback
        val text = context.resources.getString(stringId)
        hasDiscountObservable.onNext(false)
        errorMessageObservable.onNext(text)
        if (applyActionCouponParam.value.isFromNotSignedInToSignedIn) {
            errorShowDialogObservable.onNext(trip.firstError)
        }
        //TODO: Handle Coupon Tracking
//            HotelTracking.trackHotelCouponFail(couponParams.getTrackingString(), errorType)
    }

    fun raiseAlertDialog(e: Throwable) {
        if (RetrofitUtils.isNetworkError(e)) {
            networkErrorAlertDialogObservable.onNext(Unit)
        }
    }
}
