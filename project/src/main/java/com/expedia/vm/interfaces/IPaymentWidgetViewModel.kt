package com.expedia.vm.interfaces

import com.expedia.bookings.data.payment.PaymentModel
import rx.Observable

public interface IPaymentWidgetViewModel {
    //outlet
    val remainingBalanceDueOnCard: Observable<String>
    val paymentSplitsAndTripResponse: Observable<PaymentModel.PaymentSplitsAndTripResponse>
}
