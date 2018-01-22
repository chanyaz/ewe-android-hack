package com.expedia.vm.packages

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.SpannableStringBuilder
import android.text.TextUtils
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.CardFeeResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageCheckoutParams
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.extensions.safeSubscribeOptional
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.BookingSuppressionUtils
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
                        else R.string.package_resort_fees_disclaimer_TEMPLATE

                depositText = Phrase.from(context, messageResId)
                        .put("resort_fee", it.packageDetails.pricing.hotelPricing.mandatoryFees.feeTotal.formattedMoneyFromAmountAndCurrencyCode)
                        .putOptional("trip_total", it.bundleTotal.formattedPrice)
                        .format().toString()
            }
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
        if (createTripResponseObservable.value?.value != null) {
            val flightCreateTripResponse = createTripResponseObservable.value.value as PackageCreateTripResponse
            val tripId = flightCreateTripResponse.packageDetails.tripId!!
            return tripId
        }
        return ""
    }

    fun makeCheckoutResponseObserver(): Observer<PackageCheckoutResponse> {
        return object : DisposableObserver<PackageCheckoutResponse>() {
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
                        ApiError.Code.PACKAGE_CHECKOUT_UNKNOWN,
                        ApiError.Code.PAYMENT_FAILED -> {
                            checkoutErrorObservable.onNext(response.firstError)
                        }
                        ApiError.Code.UNKNOWN_ERROR -> {
                            checkoutErrorObservable.onNext(ApiError(ApiError.Code.PACKAGE_CHECKOUT_UNKNOWN))
                        }
                        ApiError.Code.PRICE_CHANGE -> {
                            checkoutPriceChangeObservable.onNext(response)
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

            override fun onComplete() {
                // ignore
            }
        }
    }

    override fun getCardFeesCallback(): Observer<CardFeeResponse> {
        return object : DisposableObserver<CardFeeResponse>() {
            override fun onNext(cardFeeResponse: CardFeeResponse) {
                if (!cardFeeResponse.hasErrors()) {
                    // add card fee to trip response
                    val cardFee = cardFeeResponse.feePrice
                    val totalPriceInclFees = cardFeeResponse.bundleTotalPrice ?: cardFeeResponse.tripTotalPrice
                    cardFeeFlexStatus.onNext(cardFeeResponse.flexStatus)
                    val response = createTripResponseObservable.value?.value
                    if (response != null) {
                        val newTripResponse = response as PackageCreateTripResponse
                        newTripResponse.selectedCardFees = cardFee
                        newTripResponse.totalPriceIncludingFees = totalPriceInclFees
                        cardFeeTripResponse.onNext(newTripResponse)
                        selectedCardFeeObservable.onNext(cardFee)
                    }
                }
            }

            override fun onComplete() {
            }

            override fun onError(e: Throwable) {
            }
        }
    }

    override fun resetCardFees() {
        val response = createTripResponseObservable.value?.value
        if (response != null) {
            val newTripResponse = response as PackageCreateTripResponse
            newTripResponse.selectedCardFees = null
            newTripResponse.totalPriceIncludingFees = null
            cardFeeTripResponse.onNext(newTripResponse)
        }
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
