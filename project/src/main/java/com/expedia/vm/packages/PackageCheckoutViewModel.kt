package com.expedia.vm.packages

import android.content.Context
import android.text.Html
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.User
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.packages.PackageCheckoutParams
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.BookingSuppressionUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.vm.BaseCheckoutViewModel
import com.squareup.phrase.Phrase
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.PublishSubject
import java.math.BigDecimal

class PackageCheckoutViewModel(context: Context, val packageServices: PackageServices) : BaseCheckoutViewModel(context) {
    override val builder = PackageCheckoutParams.Builder()

    val priceChangeObservable = PublishSubject.create<PackageCheckoutResponse>()

    init {
        tripResponseObservable.subscribe { it as PackageCreateTripResponse
            builder.tripId(it.packageDetails.tripId)
            builder.expectedTotalFare(it.packageDetails.pricing.packageTotal.amount.toString())
            builder.expectedFareCurrencyCode(it.packageDetails.pricing.packageTotal.currencyCode)
            builder.suppressFinalBooking(BookingSuppressionUtils.shouldSuppressFinalBooking(context, R.string.preference_suppress_package_bookings))
            builder.bedType(it.packageDetails.hotel.hotelRoomResponse.bedTypes?.firstOrNull()?.id)

            val hotelRate = it.packageDetails.hotel.hotelRoomResponse.rateInfo.chargeableRateInfo
            var depositText = Html.fromHtml("")
            if (hotelRate.showResortFeeMessage) {
                val resortFees = Money(BigDecimal(hotelRate.totalMandatoryFees.toDouble()), hotelRate.currencyCode).formattedMoney
                depositText = Html.fromHtml(context.getString(R.string.resort_fee_disclaimer_TEMPLATE, resortFees, it.packageDetails.pricing.packageTotal));
            }
            depositPolicyText.onNext(depositText)

            legalText.onNext(StrUtils.generateHotelsBookingStatement(context, PointOfSale.getPointOfSale().hotelBookingStatement.toString(), false))
            sliderPurchaseTotalText.onNext(Phrase.from(context, R.string.your_card_will_be_charged_template).put("dueamount", it.getTripTotalExcludingFee().formattedMoneyFromAmountAndCurrencyCode).format())
        }

        checkoutParams.subscribe { params -> params as PackageCheckoutParams
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
                            checkoutErrorObservable.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
                        }
                    }
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
