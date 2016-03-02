package com.expedia.vm.interfaces

import com.expedia.bookings.data.payment.PaymentModel
import rx.Observable
import rx.subjects.PublishSubject

public interface IPaymentWidgetViewModel {
    //INLETS
    val navigatingOutOfPaymentOptions: PublishSubject<Unit>
    val hasPwpEditBoxFocus: PublishSubject<Boolean>

    //OUTLETS
    val totalDueToday: Observable<String>
    val remainingBalanceDueOnCard: Observable<String>
    val remainingBalanceDueOnCardVisibility: Observable<Boolean>
    val paymentSplitsAndTripResponse: Observable<PaymentModel.PaymentSplitsAndTripResponse>
    val burnAmountApiCallResponsePending: PublishSubject<Boolean>
    val isPwpDirty: Observable<Boolean>
}
