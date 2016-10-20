package com.expedia.vm.packages

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.SpannableStringBuilder
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageCheckoutParams
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.BookingSuppressionUtils
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.Ui
import com.expedia.util.safeSubscribe
import com.expedia.vm.BaseCheckoutViewModel
import com.squareup.phrase.Phrase
import rx.Observer

class PackageCheckoutViewModel(context: Context, var packageServices: PackageServices) : BaseCheckoutViewModel(context) {
    override val builder = PackageCheckoutParams.Builder()
    val e3Endpoint = Ui.getApplication(context).appComponent().endpointProvider().e3EndpointUrl

    override fun injectComponents() {
        Ui.getApplication(context).packageComponent().inject(this)
    }

    init {
        tripResponseObservable.safeSubscribe {
            it as PackageCreateTripResponse
            builder.tripId(it.packageDetails.tripId)
            builder.expectedTotalFare(it.tripTotalPayableIncludingFeeIfZeroPayableByPoints().amount.toString())
            builder.expectedFareCurrencyCode(it.tripTotalPayableIncludingFeeIfZeroPayableByPoints().currencyCode)
            builder.suppressFinalBooking(BookingSuppressionUtils.shouldSuppressFinalBooking(context, R.string.preference_suppress_package_bookings))
            builder.bedType(it.packageDetails.hotel.hotelRoomResponse.bedTypes?.firstOrNull()?.id)

            var depositText = ""
            if (it.packageDetails.pricing.hasResortFee()) {
                val messageResId =
                        if (PointOfSale.getPointOfSale().shouldShowBundleTotalWhenResortFees())
                            R.string.package_resort_fees_and_total_price_disclaimer_TEMPLATE
                        else
                            R.string.package_resort_fees_disclaimer_TEMPLATE

                depositText = Phrase.from(context, messageResId)
                        .put("resort_fee", it.packageDetails.pricing.hotelPricing.mandatoryFees.feeTotal.formattedMoneyFromAmountAndCurrencyCode)
                        .putOptional("trip_total", it.bundleTotal.formattedPrice)
                        .format().toString()
            }
            depositPolicyText.onNext(Html.fromHtml(depositText))

            val totalPrice = Phrase.from(context, R.string.your_card_will_be_charged_template)
                    .put("dueamount", it.tripTotalPayableIncludingFeeIfZeroPayableByPoints().formattedMoneyFromAmountAndCurrencyCode)
                    .format().toString()
            sliderPurchaseTotalText.onNext(totalPrice)
            val accessiblePurchaseButtonContDesc = context.getString(R.string.accessibility_purchase_button) + " " + context.getString(R.string.accessibility_cont_desc_role_button)
            accessiblePurchaseButtonContentDescription.onNext(accessiblePurchaseButtonContDesc)
        }
        legalText.onNext(SpannableStringBuilder(PointOfSale.getPointOfSale().getColorizedPackagesBookingStatement(ContextCompat.getColor(context, R.color.packages_primary_color))))

        checkoutParams.subscribe { params -> params as PackageCheckoutParams
            showCheckoutDialogObservable.onNext(true)
            packageServices.checkout(params.toQueryMap()).subscribe(makeCheckoutResponseObserver())
            email = params.travelers.first().email
        }
    }

    override fun useCardFeeService(): Boolean {
        return true
    }

    override fun getTripId(): String {
        if (tripResponseObservable.value != null) {
            val flightCreateTripResponse = tripResponseObservable.value as PackageCreateTripResponse
            val tripId = flightCreateTripResponse.packageDetails.tripId!!
            return tripId
        }
        return ""
    }

    fun makeCheckoutResponseObserver(): Observer<PackageCheckoutResponse> {
        return object : Observer<PackageCheckoutResponse> {
            override fun onNext(response: PackageCheckoutResponse) {
                showCheckoutDialogObservable.onNext(false)
                if (response.hasErrors()) {
                    when (response.firstError.errorCode) {
                        ApiError.Code.INVALID_INPUT -> {
                            val field = response.firstError.errorInfo.field
                            val apiError: ApiError
                            if (field == "mainMobileTraveler.lastName" ||
                                    field == "mainMobileTraveler.firstName" ||
                                    field == "phone") {
                                apiError = ApiError(ApiError.Code.PACKAGE_CHECKOUT_TRAVELLER_DETAILS)

                            } else {
                                apiError = ApiError(ApiError.Code.PACKAGE_CHECKOUT_CARD_DETAILS)
                            }
                            apiError.errorInfo = ApiError.ErrorInfo()
                            apiError.errorInfo.field = field
                            checkoutErrorObservable.onNext(apiError)
                        }
                        ApiError.Code.INVALID_CARD_NUMBER,
                        ApiError.Code.CID_DID_NOT_MATCHED,
                        ApiError.Code.INVALID_CARD_EXPIRATION_DATE,
                        ApiError.Code.CARD_LIMIT_EXCEEDED,
                        ApiError.Code.PAYMENT_FAILED -> {
                            checkoutErrorObservable.onNext(response.firstError)
                        }
                        ApiError.Code.PRICE_CHANGE -> {
                            priceChangeObservable.onNext(response)
                        }
                        ApiError.Code.TRIP_ALREADY_BOOKED -> {
                            bookingSuccessResponse.onNext(Pair(response, email))
                        }
                        else -> {
                            checkoutErrorObservable.onNext(ApiError(ApiError.Code.PACKAGE_CHECKOUT_UNKNOWN))
                        }
                    }
                } else {
                    bookingSuccessResponse.onNext(Pair(response, email))
                }
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

            override fun onCompleted() {
                // ignore
            }
        }
    }

    override fun selectedPaymentHasCardFee(cardFee: Money, totalPriceInclFees: Money?) {
        // add card fee to trip response
        val response =  tripResponseObservable.value
        if (response != null) {
            val newTripResponse = response as PackageCreateTripResponse
            newTripResponse.selectedCardFees = cardFee
            newTripResponse.totalPriceIncludingFees = totalPriceInclFees
            cardFeeTripResponse.onNext(newTripResponse)
            selectedCardFeeObservable.onNext(cardFee)
        }
    }

    override fun resetCardFees() {
        val response =  tripResponseObservable.value
        if (response != null) {
            val newTripResponse = response as PackageCreateTripResponse
            newTripResponse.selectedCardFees = null
            newTripResponse.totalPriceIncludingFees = null
            cardFeeTripResponse.onNext(newTripResponse)
        }
    }

    fun updateMayChargeFees(selectedFlight: FlightLeg) {
        if (selectedFlight.airlineMessageModel?.hasAirlineWithCCfee ?: false || selectedFlight.mayChargeObFees) {
            val hasAirlineFeeLink = selectedFlight.airlineMessageModel?.airlineFeeLink != null
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

}
