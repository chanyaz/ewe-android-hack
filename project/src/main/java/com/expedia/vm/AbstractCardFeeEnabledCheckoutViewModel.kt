package com.expedia.vm

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.SpannableStringBuilder
import android.text.SpannedString
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.data.CardFeeResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.CardFeeService
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.isFlexEnabled
import com.squareup.phrase.Phrase
import rx.Observer
import rx.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class AbstractCardFeeEnabledCheckoutViewModel(context: Context) : AbstractCheckoutViewModel(context) {

    var cardFeeService: CardFeeService? = null
        @Inject set

    abstract fun resetCardFees()
    abstract fun getCardFeesCallback(): Observer<CardFeeResponse>

    val selectedCardFeeObservable = BehaviorSubject.create<Money>()
    val selectedFlightChargesFees = BehaviorSubject.create<String>("")
    val obFeeDetailsUrlSubject = BehaviorSubject.create<String>("")
    private var lastFetchedCardFeeKeyPair: Pair<String, String>? = null
    val cardFeeFlexStatus = BehaviorSubject.create<String>()

    init {
        compositeDisposable?.add(paymentViewModel.resetCardFees.subscribe {
            lastFetchedCardFeeKeyPair = null
            resetCardFees()
        })

        compositeDisposable?.add(paymentViewModel.cardBIN
                .debounce(1, TimeUnit.SECONDS, getScheduler())
                .subscribe { fetchCardFees(cardId = it, tripId = getTripId()) })
        compositeDisposable?.add(createTripResponseObservable
                .subscribe {
                    val cardId = paymentViewModel.cardBIN.value
                    fetchCardFees(cardId, getTripId())
                })

        setupCardFeeSubjects()
    }

    private fun fetchCardFees(cardId: String, tripId: String) {
        if (tripId.isNotBlank() && cardId.length >= 6) {
            val lastFetchedTripId = lastFetchedCardFeeKeyPair?.first
            val lastFetchedCardId = lastFetchedCardFeeKeyPair?.second
            val fetchFreshCardFee = !(tripId == lastFetchedTripId && cardId == lastFetchedCardId)
            if (fetchFreshCardFee) {
                lastFetchedCardFeeKeyPair = Pair(tripId, cardId)
                cardFeeService?.getCardFees(tripId, cardId, isFlexEnabled(context), getCardFeesCallback())
            }
        }
    }

    private fun setupCardFeeSubjects() {
        ObservableOld.combineLatest(selectedFlightChargesFees, obFeeDetailsUrlSubject, {
            flightChargesFees, obFeeDetailsUrl ->
            cardFeeWarningTextSubject.onNext(getAirlineMayChargeFeeText(flightChargesFees, obFeeDetailsUrl))
            showCardFeeWarningText.onNext(!flightChargesFees.isNullOrBlank())
        }).subscribe()

        compositeDisposable?.add(paymentViewModel.resetCardFees.subscribe {
            cardFeeService?.cancel()
            paymentTypeSelectedHasCardFee.onNext(false)
            cardFeeTextSubject.onNext(SpannedString(""))
            cardFeeWarningTextSubject.onNext(getAirlineMayChargeFeeText(selectedFlightChargesFees.value, obFeeDetailsUrlSubject.value))
        })

        selectedCardFeeObservable
                .debounce(1, TimeUnit.SECONDS, getScheduler()) // subscribe on ui thread as we're affecting ui elements
                .subscribe {
                    selectedCardFee ->
                    if (selectedCardFee.isZero) {
                        showCardFeeWarningText.onNext(false)
                        paymentTypeSelectedHasCardFee.onNext(false)
                        cardFeeTextSubject.onNext(SpannedString(""))
                        cardFeeWarningTextSubject.onNext(SpannedString(""))
                    } else {
                        val cardFeeText = Phrase.from(context, R.string.payment_method_processing_fee_TEMPLATE)
                                .put("card_fee", selectedCardFee.formattedPrice)
                                .format().toString()
                        val cardFeeWarningText = Phrase.from(context, R.string.flights_payment_method_fee_warning_TEMPLATE)
                                .put("card_fee", selectedCardFee.formattedPrice)
                                .format().toString()
                        showCardFeeWarningText.onNext(true)
                        paymentTypeSelectedHasCardFee.onNext(true)
                        cardFeeTextSubject.onNext(HtmlCompat.fromHtml(cardFeeText))
                        cardFeeWarningTextSubject.onNext(HtmlCompat.fromHtml(cardFeeWarningText))
                    }
                }
    }

    private fun getAirlineMayChargeFeeText(flightChargesFeesTxt: String, obFeeUrl: String): SpannableStringBuilder {
        if (Strings.isNotEmpty(flightChargesFeesTxt)) {
            if (!obFeeUrl.isNullOrBlank()) {
                val resId = if (PointOfSale.getPointOfSale().showAirlinePaymentMethodFeeLegalMessage()) {
                    R.string.flights_there_maybe_additional_fee_TEMPLATE
                } else {
                    R.string.flights_fee_added_based_on_payment_TEMPLATE
                }
                val airlineFeeWithLink =
                        Phrase.from(context, resId)
                                .put("airline_fee_url", obFeeUrl)
                                .format().toString()
                return StrUtils.getSpannableTextByColor(airlineFeeWithLink, ContextCompat.getColor(context, R.color.flight_primary_color), true)
            } else {
                val resId = if (PointOfSale.getPointOfSale().showAirlinePaymentMethodFeeLegalMessage()) {
                    R.string.flights_there_maybe_additional_fee
                } else {
                    R.string.flights_fee_added_based_on_payment
                }
                val airlineFee = context.resources.getString(resId)
                return SpannableStringBuilder(airlineFee)
            }
        }
        return SpannableStringBuilder()
    }
}
