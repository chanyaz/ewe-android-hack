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
import com.expedia.bookings.data.ValidPayment
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal
import java.util.ArrayList
import kotlin.collections.isNotEmpty

public class BaseCheckoutViewModel(val context: Context) {
    val builder = BaseCheckoutParams.Builder()

    // Inputs
    val lineOfBusiness = BehaviorSubject.create<LineOfBusiness>()
    val creditCardRequired = PublishSubject.create<Boolean>()
    val travelerCompleted = BehaviorSubject.create<Traveler>()
    val paymentCompleted = BehaviorSubject.create<BillingInfo>()
    val cvvCompleted = BehaviorSubject.create<String>()
    val packageTripResponse = PublishSubject.create<PackageCreateTripResponse>()

    // Outputs
    val depositPolicyText = PublishSubject.create<Spanned>()
    val legalText = PublishSubject.create<SpannableStringBuilder>()
    val infoCompleted = PublishSubject.create<Boolean>()
    val checkout = PublishSubject.create<BaseCheckoutParams>()

    init {
        packageTripResponse.subscribe {
            val hotelRate = it.packageDetails.hotel.hotelRoomResponse.rateInfo.chargeableRateInfo
            var depositText = Html.fromHtml("")
            if (hotelRate.showResortFeeMessage) {
                val resortFees = Money(BigDecimal(hotelRate.totalMandatoryFees.toDouble()), hotelRate.currencyCode).formattedMoney
                depositText = Html.fromHtml(context.getString(R.string.resort_fee_disclaimer_TEMPLATE, resortFees, it.packageDetails.pricing.packageTotal));
            }
            depositPolicyText.onNext(depositText)
        }

        travelerCompleted.subscribe {
            builder.travelers(it)
            infoCompleted.onNext(builder.hasValidTravelerAndBillingInfo())

        }

        paymentCompleted.subscribe {
            builder.billingInfo(it)
            infoCompleted.onNext(builder.hasValidTravelerAndBillingInfo())
        }

        cvvCompleted.subscribe {
            builder.cvv(it)
            if (builder.hasValidParams()) {
              checkout.onNext(builder.build())
            }
        }
    }

}

public data class BaseCheckoutParams(val travelers: ArrayList<Traveler>, val billingInfo: BillingInfo, val cvv: String) {

    class Builder() {
        private var travelers: ArrayList<Traveler> = ArrayList()
        private var billingInfo: BillingInfo? = null
        private var cvv: String? = null

        fun travelers(traveler: Traveler): BaseCheckoutParams.Builder {
            this.travelers.add(traveler)
            return this
        }

        fun billingInfo(billingInfo: BillingInfo?): BaseCheckoutParams.Builder {
            this.billingInfo = billingInfo
            return this
        }

        fun cvv(cvv: String?): BaseCheckoutParams.Builder {
            this.cvv = cvv
            return this
        }

        fun build(): BaseCheckoutParams {
            if (travelers.isEmpty()) {
                throw IllegalArgumentException()
            }
            val billingInfo = billingInfo ?: throw IllegalArgumentException()
            val cvv = cvv ?: throw IllegalArgumentException()

            return BaseCheckoutParams(travelers, billingInfo, cvv)
        }

        public fun hasValidTravelerAndBillingInfo(): Boolean {
            return travelers.isNotEmpty() && billingInfo != null
        }

        public fun hasValidParams(): Boolean {
            return travelers.isNotEmpty() && billingInfo != null && cvv != null
        }

    }
}