package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.ValidPayment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

public class CheckoutToolbarViewModel(val context: Context) {

    // inputs
    val toolbarTitle = PublishSubject.create<String>()
    val menuTitle = PublishSubject.create<String>()
    val enableMenu = PublishSubject.create<Boolean>()

    // outputs
    val itemClicked = PublishSubject.create<Unit>()

}