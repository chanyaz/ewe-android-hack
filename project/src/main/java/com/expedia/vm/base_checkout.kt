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
import com.expedia.bookings.data.packages.PackageCheckoutParams
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.StrUtils
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal
import kotlin.collections.firstOrNull

public class BaseCheckoutViewModel(val context: Context, val packageServices: PackageServices) {
    val builder = PackageCheckoutParams.Builder()

    // Inputs
    val lineOfBusiness = BehaviorSubject.create<LineOfBusiness>()
    val creditCardRequired = PublishSubject.create<Boolean>()
    val travelerCompleted = BehaviorSubject.create<Traveler>()
    val paymentCompleted = BehaviorSubject.create<BillingInfo>()
    val cvvCompleted = BehaviorSubject.create<String>()
    val packageTripResponse = PublishSubject.create<PackageCreateTripResponse>()
    val checkoutInfoCompleted = PublishSubject.create<PackageCheckoutParams>()

    // Outputs
    val depositPolicyText = PublishSubject.create<Spanned>()
    val legalText = PublishSubject.create<SpannableStringBuilder>()
    val infoCompleted = PublishSubject.create<Boolean>()
    val checkoutResponse = PublishSubject.create<PackageCheckoutResponse>()

    init {
        packageTripResponse.subscribe {
            builder.tripId(it.packageDetails.tripId)
            builder.expectedTotalFare(it.packageDetails.pricing.packageTotal.amount.toString())
            builder.expectedFareCurrencyCode(it.packageDetails.pricing.packageTotal.currencyCode)
            builder.bedType(it.packageDetails.hotel.hotelRoomResponse.bedTypes?.firstOrNull()?.id)
            infoCompleted.onNext(builder.hasValidParams())

            val hotelRate = it.packageDetails.hotel.hotelRoomResponse.rateInfo.chargeableRateInfo
            var depositText = Html.fromHtml("")
            if (hotelRate.showResortFeeMessage) {
                val resortFees = Money(BigDecimal(hotelRate.totalMandatoryFees.toDouble()), hotelRate.currencyCode).formattedMoney
                depositText = Html.fromHtml(context.getString(R.string.resort_fee_disclaimer_TEMPLATE, resortFees, it.packageDetails.pricing.packageTotal));
            }
            depositPolicyText.onNext(depositText)

            legalText.onNext(StrUtils.generateHotelsBookingStatement(context, PointOfSale.getPointOfSale().hotelBookingStatement.toString(), false))
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
                checkoutInfoCompleted.onNext(builder.build())
            }
        }

        checkoutInfoCompleted.subscribe { body ->
            packageServices.checkout(body.toQueryMap()).subscribe(makeCheckoutResponseObserver())
        }
    }

    fun makeCheckoutResponseObserver(): Observer<PackageCheckoutResponse> {
        return object : Observer<PackageCheckoutResponse> {
            override fun onNext(response: PackageCheckoutResponse) {
                if (response.hasErrors()) {
                    //TODO handle errors (unhappy path story)
                } else {
                    checkoutResponse.onNext(response);
                }
            }

            override fun onError(e: Throwable) {
                throw OnErrorNotImplementedException(e)
            }

            override fun onCompleted() {
                // ignore
            }
        }
    }
}