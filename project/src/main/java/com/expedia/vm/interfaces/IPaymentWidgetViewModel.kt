package com.expedia.vm.interfaces

import com.expedia.bookings.data.payment.PaymentModel
import rx.Observable

public interface IPaymentWidgetViewModel {
    //outlet
    val totalDueToday: Observable<String>
    val remainingBalanceDueOnCard: Observable<String>
    val remainingBalanceDueOnCardVisibility: Observable<Boolean>
    val paymentSplitsAndTripResponse: Observable<PaymentModel.PaymentSplitsAndTripResponse>
}
