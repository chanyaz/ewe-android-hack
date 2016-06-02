package com.expedia.vm

import android.content.Context
import android.text.Html
import android.text.SpannableStringBuilder
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.User
import com.expedia.bookings.data.flights.FlightCheckoutParams
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.utils.BookingSuppressionUtils
import com.squareup.phrase.Phrase
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException

class FlightCheckoutViewModel(context: Context, val flightServices: FlightServices) : BaseCheckoutViewModel(context) {

    override val builder = FlightCheckoutParams.Builder()

    init {
        tripResponseObservable.subscribe { it as FlightCreateTripResponse
            builder.tripId(it.newTrip.tripId)
            builder.expectedTotalFare(it.totalPrice.amount.toString())
            builder.expectedFareCurrencyCode(it.details.offer.currency)
            builder.tealeafTransactionId(it.tealeafTransactionId)

            var depositText = Html.fromHtml("")
            depositPolicyText.onNext(depositText)

            legalText.onNext(SpannableStringBuilder(PointOfSale.getPointOfSale().stylizedFlightBookingStatement))
            sliderPurchaseTotalText.onNext(Phrase.from(context, R.string.your_card_will_be_charged_template).put("dueamount", it.getTripTotalExcludingFee().formattedPrice).format())
        }

        checkoutParams.subscribe { params -> params as FlightCheckoutParams
            if (User.isLoggedIn(context)) {
                params.billingInfo.email = Db.getUser().primaryTraveler.email
            }
            builder.suppressFinalBooking(BookingSuppressionUtils.shouldSuppressFinalBooking(context, R.string.preference_suppress_flight_bookings))
            flightServices.checkout(params.toQueryMap()).subscribe(makeCheckoutResponseObserver())
            email = params.billingInfo.email
        }
    }

    fun makeCheckoutResponseObserver(): Observer<FlightCheckoutResponse> {
        return object : Observer<FlightCheckoutResponse> {
            override fun onNext(response: FlightCheckoutResponse) {
                if (response.hasErrors()) {
                    // see packagesCheckoutViewModel for error handling
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

