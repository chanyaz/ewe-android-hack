package com.expedia.vm.packages

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.text.Html
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.User
import com.expedia.bookings.data.packages.PackageCheckoutParams
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.BookingSuppressionUtils
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.vm.BaseCheckoutViewModel
import com.squareup.phrase.Phrase
import rx.Observer

class PackageCheckoutViewModel(context: Context, val packageServices: PackageServices) : BaseCheckoutViewModel(context) {
    override val builder = PackageCheckoutParams.Builder()

    init {
        tripResponseObservable.subscribe {
            it as PackageCreateTripResponse
            builder.tripId(it.packageDetails.tripId)
            builder.expectedTotalFare(it.packageDetails.pricing.packageTotal.amount.toString())
            builder.expectedFareCurrencyCode(it.packageDetails.pricing.packageTotal.currencyCode)
            builder.suppressFinalBooking(BookingSuppressionUtils.shouldSuppressFinalBooking(context, R.string.preference_suppress_package_bookings))
            builder.bedType(it.packageDetails.hotel.hotelRoomResponse.bedTypes?.firstOrNull()?.id)

            var depositText = ""
            if (it.packageDetails.pricing.hasResortFee()) {
                val messageResId =
                        if(PointOfSale.getPointOfSale().shouldShowBundleTotalWhenResortFees())
                            R.string.package_resort_fees_and_total_price_disclaimer_TEMPLATE
                        else
                            R.string.package_resort_fees_disclaimer_TEMPLATE

                depositText = Phrase.from(context, messageResId)
                        .put("resort_fee", it.packageDetails.pricing.hotelPricing.mandatoryFees.feeTotal.formattedMoney)
                        .putOptional("trip_total", it.packageDetails.pricing.getBundleTotal().formattedPrice)
                        .format().toString()
            }
            depositPolicyText.onNext(Html.fromHtml(depositText))

            legalText.onNext(StrUtils.generateHotelsBookingStatement(context, PointOfSale.getPointOfSale().hotelBookingStatement.toString(), false))
            val totalPrice = Phrase.from(context, R.string.your_card_will_be_charged_template)
                    .put("dueamount", it.getTripTotalExcludingFee().formattedMoneyFromAmountAndCurrencyCode)
                    .format().toString()
            sliderPurchaseTotalText.onNext(totalPrice)
            val sliderPurchaseContDesc = totalPrice + "," + context.getString(R.string.package_slider_text) + "," + context.getString(R.string.accessibility_cont_desc_role_button)
            sliderPurchaseLayoutContentDescription.onNext(sliderPurchaseContDesc)
        }

        checkoutParams.subscribe { params ->
            params as PackageCheckoutParams
            if (User.isLoggedIn(context)) {
                params.billingInfo.email = Db.getUser().primaryTraveler.email
            }
            packageServices.checkout(params.toQueryMap()).subscribe(makeCheckoutResponseObserver())
            email = params.billingInfo.email
        }
    }

    fun makeCheckoutResponseObserver(): Observer<PackageCheckoutResponse> {
        return object : Observer<PackageCheckoutResponse> {
            override fun onNext(response: PackageCheckoutResponse) {
                if (response.hasErrors()) {
                    when (response.firstError.errorCode) {
                        ApiError.Code.INVALID_INPUT -> {
                            val field = response.firstError.errorInfo.field
                            if (field == "mainMobileTraveler.lastName" ||
                                    field == "mainMobileTraveler.firstName" ||
                                    field == "phone") {
                                val apiError = ApiError(ApiError.Code.PACKAGE_CHECKOUT_TRAVELLER_DETAILS)
                                apiError.errorInfo = ApiError.ErrorInfo()
                                apiError.errorInfo.field = field
                                checkoutErrorObservable.onNext(apiError)
                            } else {
                                val apiError = ApiError(ApiError.Code.PACKAGE_CHECKOUT_CARD_DETAILS)
                                apiError.errorInfo = ApiError.ErrorInfo()
                                apiError.errorInfo.field = field
                                checkoutErrorObservable.onNext(apiError)
                            }
                        }
                        ApiError.Code.INVALID_CARD_NUMBER -> {
                            checkoutErrorObservable.onNext(response.firstError)
                        }
                        ApiError.Code.CID_DID_NOT_MATCHED -> {
                            checkoutErrorObservable.onNext(response.firstError)
                        }
                        ApiError.Code.INVALID_CARD_EXPIRATION_DATE -> {
                            checkoutErrorObservable.onNext(response.firstError)
                        }
                        ApiError.Code.CARD_LIMIT_EXCEEDED -> {
                            checkoutErrorObservable.onNext(response.firstError)
                        }
                        ApiError.Code.PRICE_CHANGE -> {
                            priceChangeObservable.onNext(response)
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
}
