package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.hotels.HotelSavedCouponParameters
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.subscribeObserver
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.withLatestFrom
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class StoredCouponViewModel(val context: Context, hotelServices: HotelServices, val paymentModel: PaymentModel<HotelCreateTripResponse>, val couponErrorMap: Map<String, Int>) {

    val storedCouponActionParam = BehaviorSubject.create<HotelSavedCouponParameters>()
    val storedCouponSuccessObservable = PublishSubject.create<HotelCreateTripResponse>()
    val hasDiscountObservable = BehaviorSubject.create<Boolean>()
    val errorShowDialogObservable = PublishSubject.create<ApiError>()
    val networkErrorAlertDialogObservable = PublishSubject.create<Unit>()
    val applyStoredCouponObservable = PublishSubject.create<HotelCreateTripResponse.SavedCoupon>()
    val storedCouponTrackingObservable = PublishSubject.create<String>()
    val errorMessageObservable = BehaviorSubject.create<String>()
    val performCouponErrorTrackingObservable = PublishSubject.create<String>()
    val performCouponSuccessTrackingObservable = PublishSubject.create<Unit>()

    val couponResponseObserver = object : DisposableObserver<HotelCreateTripResponse>() {
        override fun onNext(tripResponse: HotelCreateTripResponse) {
            if (tripResponse.hasErrors()) {
                setupErrorObservables(tripResponse)
            } else {
                storedCouponSuccessObservable.onNext(tripResponse)
            }
        }

        override fun onComplete() {
            // ignore
        }

        override fun onError(e: Throwable) {
            raiseAlertDialog(e)
        }
    }

    init {
        storedCouponActionParam.subscribe { params ->
            hotelServices.applyCoupon(params, PointOfSale.getPointOfSale().isPwPEnabledForHotels).subscribeObserver(couponResponseObserver)
        }

        storedCouponSuccessObservable.withLatestFrom(storedCouponTrackingObservable, { _, couponTrackingString ->
            HotelTracking.trackHotelSavedCouponSuccess(couponTrackingString)
        }).subscribe()

        errorMessageObservable.withLatestFrom(storedCouponTrackingObservable, { errorText, couponTrackingString ->
            HotelTracking.trackHotelSavedCouponFail(couponTrackingString, errorText)
        }).subscribe()

        performCouponErrorTrackingObservable.withLatestFrom(storedCouponTrackingObservable, { errorMessage, couponTrackingString ->
            HotelTracking.trackHotelCouponRemoveFailure(couponTrackingString, errorMessage)
        }).subscribe()

        performCouponSuccessTrackingObservable.withLatestFrom(storedCouponTrackingObservable, { unit, couponTrackingString ->
            HotelTracking.trackHotelCouponRemoveSuccess(couponTrackingString)
        }).subscribe()
    }

    fun setupErrorObservables(trip: HotelCreateTripResponse) {
        val errorType = trip.firstError.errorInfo.couponErrorType ?: "FallBack"
        val stringId = couponErrorMap[errorType] ?: R.string.coupon_error_fallback
        val text = context.resources.getString(stringId)
        //TODO: Use the following observable to display the error in stored coupon
        errorMessageObservable.onNext(text)
        hasDiscountObservable.onNext(false)
        if (storedCouponActionParam.value.isFromNotSignedInToSignedIn) {
            errorShowDialogObservable.onNext(trip.firstError)
        }
    }

    fun raiseAlertDialog(e: Throwable) {
        if (RetrofitUtils.isNetworkError(e)) {
            networkErrorAlertDialogObservable.onNext(Unit)
        }
    }

    val couponRemoveErrorTrackingObserver = object : DisposableObserver<String>() {
        override fun onNext(errorMessage: String) {
            performCouponErrorTrackingObservable.onNext(errorMessage)
            dispose()
        }

        override fun onComplete() {
            // ignore
        }

        override fun onError(e: Throwable) {
            //ignore
        }
    }

    val couponRemoveSuccessTrackingObserver = object : DisposableObserver<HotelCreateTripResponse>() {
        override fun onNext(tripResponse: HotelCreateTripResponse) {
            performCouponSuccessTrackingObservable.onNext(Unit)
            dispose()
        }

        override fun onComplete() {
            // ignore
        }

        override fun onError(e: Throwable) {
            //ignore
        }
    }
}
