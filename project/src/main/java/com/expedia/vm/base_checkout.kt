package com.expedia.vm

import android.content.Context
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import com.expedia.bookings.R
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.packages.BaseCheckoutParams
import com.expedia.bookings.data.packages.PackageCheckoutParams
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.StrUtils
import com.squareup.phrase.Phrase
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal

public class BaseCheckoutViewModel(val context: Context, val packageServices: PackageServices?) {
    val builder = BaseCheckoutParams.Builder()

    // Inputs
    val lineOfBusiness = BehaviorSubject.create<LineOfBusiness>()
    val creditCardRequired = PublishSubject.create<Boolean>()
    val travelerCompleted = BehaviorSubject.create<Traveler?>()
    val paymentCompleted = BehaviorSubject.create<BillingInfo?>()
    val cvvCompleted = BehaviorSubject.create<String>()
    val checkoutInfoCompleted = PublishSubject.create<BaseCheckoutParams>()

    // Outputs
    val infoCompleted = BehaviorSubject.create<Boolean>()

    init {
        travelerCompleted.subscribe {
            builder.travelers(it)
            infoCompleted.onNext(builder.hasValidTravelerAndBillingInfo())
        }

        paymentCompleted.subscribe { billingInfo ->
            builder.billingInfo(billingInfo)
            builder.cvv(billingInfo?.securityCode)
            infoCompleted.onNext(builder.hasValidTravelerAndBillingInfo())
        }

        cvvCompleted.subscribe {
            builder.cvv(it)
            if (builder.hasValidParams()) {
                checkoutInfoCompleted.onNext(builder.build())
            }
        }
    }
}