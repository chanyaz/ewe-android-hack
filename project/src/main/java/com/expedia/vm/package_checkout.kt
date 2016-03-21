package com.expedia.vm

import android.content.Context
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripBucketItemPackages
import com.expedia.bookings.data.BaseCheckoutParams
import com.expedia.bookings.data.User
import com.expedia.bookings.data.packages.PackageCheckoutParams
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.BookingSuppressionUtils
import com.expedia.bookings.utils.StrUtils
import com.squareup.phrase.Phrase
import rx.Observable
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal
import kotlin.collections.firstOrNull
import kotlin.properties.Delegates

class PackageCreateTripViewModel(val packageServices: PackageServices) {

    val tripParams = PublishSubject.create<PackageCreateTripParams>()
    val performCreateTrip = PublishSubject.create<Unit>()
    val tripResponseObservable = BehaviorSubject.create<PackageCreateTripResponse>()
    val createTripBundleTotalObservable = BehaviorSubject.create<PackageCreateTripResponse>()
    val showCreateTripDialogObservable = PublishSubject.create<Unit>()

    init {
        Observable.combineLatest(tripParams, performCreateTrip, { params, createTrip ->
            showCreateTripDialogObservable.onNext(Unit)
            packageServices.createTrip(params).subscribe(makeCreateTripResponseObserver())
        }).subscribe()
    }

    fun makeCreateTripResponseObserver(): Observer<PackageCreateTripResponse> {
        return object : Observer<PackageCreateTripResponse> {
            override fun onNext(response: PackageCreateTripResponse) {
                if (response.hasErrors() && !response.hasPriceChange()) {
                    //TODO handle errors (unhappy path story)
                } else {
                    Db.getTripBucket().clearPackages()
                    Db.getTripBucket().add(TripBucketItemPackages(response))
                    tripResponseObservable.onNext(response)
                    createTripBundleTotalObservable.onNext(response)
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

class PackageCheckoutViewModel(val context: Context, val packageServices: PackageServices) {
    val builder = PackageCheckoutParams.Builder()

    val tripResponseObservable = BehaviorSubject.create<PackageCreateTripResponse>()
    val baseParams = PublishSubject.create<BaseCheckoutParams>()
    val checkoutParams = PublishSubject.create<PackageCheckoutParams>()
    val checkoutResponse = PublishSubject.create<Pair<PackageCheckoutResponse, String>>()

    // Outputs
    val depositPolicyText = PublishSubject.create<Spanned>()
    val legalText = PublishSubject.create<SpannableStringBuilder>()
    val sliderPurchaseTotalText = PublishSubject.create<CharSequence>()
    val emailObservable = BehaviorSubject.create<String>()
    var email: String by Delegates.notNull()

    init {
        tripResponseObservable.subscribe {
            builder.tripId(it.packageDetails.tripId)
            builder.expectedTotalFare(it.packageDetails.pricing.packageTotal.amount.toString())
            builder.expectedFareCurrencyCode(it.packageDetails.pricing.packageTotal.currencyCode)
            builder.bedType(it.packageDetails.hotel.hotelRoomResponse.bedTypes?.firstOrNull()?.id)

            val hotelRate = it.packageDetails.hotel.hotelRoomResponse.rateInfo.chargeableRateInfo
            var depositText = Html.fromHtml("")
            if (hotelRate.showResortFeeMessage) {
                val resortFees = Money(BigDecimal(hotelRate.totalMandatoryFees.toDouble()), hotelRate.currencyCode).formattedMoney
                depositText = Html.fromHtml(context.getString(R.string.resort_fee_disclaimer_TEMPLATE, resortFees, it.packageDetails.pricing.packageTotal));
            }
            depositPolicyText.onNext(depositText)

            legalText.onNext(StrUtils.generateHotelsBookingStatement(context, PointOfSale.getPointOfSale().hotelBookingStatement.toString(), false))
            sliderPurchaseTotalText.onNext(Phrase.from(context, R.string.your_card_will_be_charged_template).put("dueamount", it.getTripTotal().formattedPrice).format())
        }

        baseParams.subscribe { params ->
            val suppressFinalBooking = BookingSuppressionUtils.shouldSuppressFinalBooking(context, R.string.preference_suppress_package_bookings)
            if (User.isLoggedIn(context)) {
                params.billingInfo.email = Db.getUser().primaryTraveler.email
            }
            builder.billingInfo(params.billingInfo)
            builder.travelers(params.travelers)
            builder.cvv(params.cvv)
            builder.suppressFinalBooking(suppressFinalBooking)
            if (builder.hasValidParams()) {
                checkoutParams.onNext(builder.build())
            }
        }

        checkoutParams.subscribe { body ->
            packageServices.checkout(body.toQueryMap()).subscribe(makeCheckoutResponseObserver())
            email = body.billingInfo.email
        }

    }

    fun makeCheckoutResponseObserver(): Observer<PackageCheckoutResponse> {
        return object : Observer<PackageCheckoutResponse> {
            override fun onNext(response: PackageCheckoutResponse) {
                if (response.hasErrors()) {
                    //TODO handle errors (unhappy path story)
                } else {
                    checkoutResponse.onNext(Pair(response, email));
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

