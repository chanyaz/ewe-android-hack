package com.expedia.vm.interfaces

import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.utils.ArrowXDrawableUtil
import rx.Observable
import rx.subjects.PublishSubject

interface IPaymentWidgetViewModel {
    //INLETS
    val navigatingOutOfPaymentOptions: PublishSubject<Unit>
    val hasPwpEditBoxFocus: PublishSubject<Boolean>

    //OUTLETS
    val totalDueToday: Observable<String>
    val remainingBalanceDueOnCard: Observable<String>
    val remainingBalanceDueOnCardVisibility: Observable<Boolean>
    val paymentSplitsAndTripResponse: Observable<PaymentModel.PaymentSplitsAndTripResponse<TripResponse>>
    val burnAmountApiCallResponsePending: PublishSubject<Boolean>
    val isPwpDirty: Observable<Boolean>
    val toolbarNavIcon: PublishSubject<ArrowXDrawableUtil.ArrowDrawableType>
}
