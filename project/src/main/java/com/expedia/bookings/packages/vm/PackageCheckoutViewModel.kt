package com.expedia.bookings.packages.vm

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.SpannableStringBuilder
import android.text.TextUtils
import com.expedia.bookings.R
import com.expedia.bookings.data.CardFeeResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageCheckoutParams
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.extensions.safeSubscribeOptional
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.vm.AbstractCardFeeEnabledCheckoutViewModel
import com.squareup.phrase.Phrase
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver

class PackageCheckoutViewModel(context: Context, var packageServices: PackageServices) : AbstractCardFeeEnabledCheckoutViewModel(context) {
    override val builder = PackageCheckoutParams.Builder()
    val e3Endpoint = Ui.getApplication(context).appComponent().endpointProvider().e3EndpointUrl

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
            packageServices.checkout(params.toQueryMap()).subscribe(makeCheckoutResponseObserver())
            email = params.travelers.first().email
        }
    }

    override fun getTripId(): String {
        return ""
    }

    fun makeCheckoutResponseObserver(): Observer<PackageCheckoutResponse> {
        return object : DisposableObserver<PackageCheckoutResponse>() {
            override fun onNext(response: PackageCheckoutResponse) {
                showCheckoutDialogObservable.onNext(false)
            }

            override fun onError(e: Throwable) {
                showCheckoutDialogObservable.onNext(false)
                if (RetrofitUtils.isNetworkError(e)) {
                    val retryFun = fun() {
                        val params = checkoutParams.value
                        packageServices.checkout(params.toQueryMap()).subscribe(makeCheckoutResponseObserver())
                    }
                    val cancelFun = fun() {
                        builder.cvv(null)
                        val activity = context as AppCompatActivity
                        activity.onBackPressed()
                        noNetworkObservable.onNext(Unit)
                    }
                    DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
                }
            }

            override fun onComplete() {
                // ignore
            }
        }
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

    fun updateMayChargeFees(selectedFlight: FlightLeg) {
        if (selectedFlight.airlineMessageModel?.hasAirlineWithCCfee ?: false || selectedFlight.mayChargeObFees) {
            val hasAirlineFeeLink = !selectedFlight.airlineMessageModel?.airlineFeeLink.isNullOrBlank()
            if (hasAirlineFeeLink) {
                obFeeDetailsUrlSubject.onNext(e3Endpoint + selectedFlight.airlineMessageModel.airlineFeeLink)
            }
            val paymentFeeText = context.resources.getString(R.string.payment_and_baggage_fees_may_apply)
            selectedFlightChargesFees.onNext(paymentFeeText)
        } else {
            obFeeDetailsUrlSubject.onNext("")
            selectedFlightChargesFees.onNext("")
        }
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
