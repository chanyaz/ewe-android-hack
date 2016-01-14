package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.ValidPayment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

public class BaseCheckoutViewModel(val context: Context) {

    // Inputs
    val validPayments = PublishSubject.create<List<ValidPayment>>()
    val lineOfBusiness = BehaviorSubject.create<LineOfBusiness>()
    val creditCardRequired = PublishSubject.create<Boolean>()

    // Outputs
    val travelerCompleted = BehaviorSubject.create<Traveler>()
    val paymentCompleted = BehaviorSubject.create<BillingInfo>()
    val infoCompleted = PublishSubject.create<Boolean>()
    val checkout = PublishSubject.create<Unit>()

    init {
        //User has completed traveler and payment info, show the slide to purchase
        Observable.zip(travelerCompleted, paymentCompleted, { traveler, billinginfo ->
            val isValid = (traveler !=null && billinginfo != null)
            infoCompleted.onNext(isValid)
        })
    }
}