package com.expedia.vm

import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import rx.Observable

public class HotelCheckoutMainViewModel(paymentModel: PaymentModel<HotelCreateTripResponse>) {

    val updateEarnedRewards: Observable<Int> = paymentModel.paymentSplitsAndTripResponseObservable.map { it ->
        it.paymentSplits.payingWithCards.points
    }
}