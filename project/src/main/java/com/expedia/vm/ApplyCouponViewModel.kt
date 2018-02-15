package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.hotels.HotelApplyCouponParameters
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.extensions.subscribeObserver
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.RetrofitUtils
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class ApplyCouponViewModel(val context: Context, hotelServices: HotelServices, val paymentModel: PaymentModel<HotelCreateTripResponse>, val couponErrorMap: Map<String, Int>) {

    val applyActionCouponParam = BehaviorSubject.create<HotelApplyCouponParameters>()
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
                HotelTracking.trackHotelEnteredCouponSuccess(applyActionCouponParam.value.couponCode)
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
        val errorType = trip.firstError.errorInfo.couponErrorType ?: "FallBack"
        val stringId = couponErrorMap[errorType] ?: R.string.coupon_error_fallback
        val text = context.resources.getString(stringId)
        hasDiscountObservable.onNext(false)
        errorMessageObservable.onNext(text)
        if (applyActionCouponParam.value.isFromNotSignedInToSignedIn) {
            errorShowDialogObservable.onNext(trip.firstError)
        }
        HotelTracking.trackHotelEnteredCouponFail(applyActionCouponParam.value.couponCode, errorType)
    }

    fun raiseAlertDialog(e: Throwable) {
        if (RetrofitUtils.isNetworkError(e)) {
            networkErrorAlertDialogObservable.onNext(Unit)
        }
    }

    fun performCouponRemoveFailureTracking(errorMessage: String) {
        HotelTracking.trackHotelCouponRemoveFailure(applyActionCouponParam.value.couponCode, errorMessage)
    }

    fun performCouponRemoveSuccessTracking() {
        HotelTracking.trackHotelCouponRemoveSuccess(applyActionCouponParam.value.couponCode)
    }

    fun couponRemoveErrorTrackingObserver() = object : DisposableObserver<String>() {
        override fun onNext(errorMessage: String) {
            performCouponRemoveFailureTracking(errorMessage)
        }

        override fun onComplete() {
            // ignore
        }

        override fun onError(e: Throwable) {
            //ignore
        }
    }

    fun couponRemoveSuccessTrackingObserver() = object : DisposableObserver<HotelCreateTripResponse>() {
        override fun onNext(tripResponse: HotelCreateTripResponse) {
            performCouponRemoveSuccessTracking()
        }

        override fun onComplete() {
            // ignore
        }

        override fun onError(e: Throwable) {
            //ignore
        }
    }
}
