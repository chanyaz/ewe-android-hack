package com.expedia.bookings.packages.vm

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.SpannableStringBuilder
import android.text.TextUtils
import com.expedia.bookings.R
import com.expedia.bookings.data.CardFeeResponse
import com.expedia.bookings.data.packages.PackageCheckoutParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extensions.safeSubscribeOptional
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.vm.AbstractCardFeeEnabledCheckoutViewModel
import com.squareup.phrase.Phrase
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver

class PackageCheckoutViewModel(context: Context, var packageServices: PackageServices) : AbstractCardFeeEnabledCheckoutViewModel(context) {
    override val builder = PackageCheckoutParams.Builder()

    override fun injectComponents() {
        Ui.getApplication(context).packageComponent().inject(this)
    }

    init {
        hasPaymentChargeFeesSubject.onNext(PointOfSale.getPointOfSale().showAirlinePaymentMethodFeeLegalMessage())
        createTripResponseObservable.safeSubscribeOptional {

            var depositText = ""
            depositPolicyText.onNext(HtmlCompat.fromHtml(depositText))
        }
        legalText.onNext(SpannableStringBuilder(getPackagesBookingStatement(ContextCompat.getColor(context, R.color.packages_primary_color))))

        checkoutParams.subscribe { params ->
            params as PackageCheckoutParams
            showCheckoutDialogObservable.onNext(true)
            email = params.travelers.first().email
        }
    }

    override fun getTripId(): String {
        return ""
    }

    override fun getCardFeesCallback(): Observer<CardFeeResponse> {
        return object : DisposableObserver<CardFeeResponse>() {
            override fun onNext(cardFeeResponse: CardFeeResponse) {
                if (!cardFeeResponse.hasErrors()) {
                    cardFeeFlexStatus.onNext(cardFeeResponse.flexStatus)
                }
            }

            override fun onComplete() {
            }

            override fun onError(e: Throwable) {
            }
        }
    }

    override fun resetCardFees() {
    }

    private fun getPackagesBookingStatement(color: Int): CharSequence {
        val pointOfSale = PointOfSale.getPointOfSale()
        val packageBookingStatement = pointOfSale.packagesBookingStatement
        if (TextUtils.isEmpty(packageBookingStatement)) {
            val flightBookingStatement = Phrase.from(context, R.string.flight_booking_statement_TEMPLATE)
                    .put("website_url", pointOfSale.websiteUrl)
                    .format().toString()
            return StrUtils.getSpannableTextByColor(flightBookingStatement, color, false)
        }
        return StrUtils.getSpannableTextByColor(packageBookingStatement, color, false)
    }
}
