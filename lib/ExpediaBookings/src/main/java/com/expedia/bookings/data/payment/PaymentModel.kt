package com.expedia.bookings.data.payment

import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.services.LoyaltyServices
import rx.Observable
import rx.Observer
import rx.Subscriber
import rx.Subscription
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal

class PaymentModel<T : TripResponse>(loyaltyServices: LoyaltyServices) {
    val discardPendingCurrencyToPointsAPISubscription = PublishSubject.create<Unit>()

    class PaymentSplitsAndTripResponse(val tripResponse: TripResponse, val paymentSplits: PaymentSplits) {
        fun isCardRequired(): Boolean = if (this.tripResponse.isExpediaRewardsRedeemable()) this.paymentSplits.paymentSplitsType() != PaymentSplitsType.IS_FULL_PAYABLE_WITH_POINT else this.tripResponse.isCardDetailsRequiredForBooking()
    }

    //API
    private val nullSubscription: Subscription? = null
    //Persist the last subscription to clean it up (if it is active) before creating a new subscription for currencyToPoints API
    val burnAmountToPointsApiSubscriptions = BehaviorSubject.create<Subscription?>(nullSubscription)

    val burnAmountToPointsApiResponse = PublishSubject.create<CalculatePointsResponse>()
    val burnAmountToPointsApiError = PublishSubject.create<ApiError>()

    private fun makeCalculatePointsApiResponseObserver(): Observer<CalculatePointsResponse> {
        return object : Subscriber<CalculatePointsResponse>() {
            override fun onError(apiError: Throwable?) {
                if (!this.isUnsubscribed) {
                    burnAmountToPointsApiError.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
                }
            }

            override fun onNext(apiResponse: CalculatePointsResponse) {
                if (!apiResponse.hasErrors()) {
                    burnAmountToPointsApiResponse.onNext(apiResponse)
                } else {
                    burnAmountToPointsApiError.onNext(apiResponse.firstError)
                }
            }

            override fun onCompleted() {
            }
        }
    }

    //INLETS

    //Clients can push CreateTrip, PriceChangeDuringCheckout, Coupon Apply/Remove responses on these streams,
    //and the business logic will emit updated PaymentSplits and relevant information on output streams
    val createTripSubject = PublishSubject.create<T>()
    val priceChangeDuringCheckoutSubject = PublishSubject.create<T>()
    val couponChangeSubject = PublishSubject.create<T>()

    //Amount Chosen To Be Paid With Points
    val burnAmountSubject = PublishSubject.create<BigDecimal>()

    //OUTLETS

    //Merging to handle all 3 Trip Response Types homogeneously
    val tripResponses = PublishSubject.create<T>()

    //Facade to ensure there are no glitches!
    //Whenever you need Amount Chosen To Be Paid With Points and Latest Trip response and Latest Currency To Points Api Subscription, use this!
    private val burnAmountAndLatestTripResponse = burnAmountSubject.withLatestFrom(tripResponses, {
        burnAmount, latestTripResponse ->
        object {
            val burnAmount = burnAmount
            val latestTripResponse = latestTripResponse
        }
    })

    val pwpOpted = BehaviorSubject.create<Boolean>(true)

    //If PwP is enabled, default to Max Payable With Points, otherwise default to Zero Payable With Points.
    private val createTripResponsePaymentSplits = PublishSubject.create<PaymentSplits>()

    //If PwP is disabled, API may still return Max-Payable-By-Points-Splits, but ignore those and fallback to Zero-Payable-With-Points
    //If PwP is enabled, simply take the Payment-Splits-For-Price-Change which have all fallbacks in place
    val priceChangeResponsePaymentSplits = PublishSubject.create<PaymentSplits>()

    //Boolean in the Pair indicates whether the Suggestion Updates are from Create-Trip (true) or from Price-Change (false)
    val paymentSplitsSuggestionUpdates = PublishSubject.create<Pair<PaymentSplits, Boolean>>()

    //Payment Splits (amount payable by points and by card) to be consumed by clients.
    val paymentSplits: Observable<PaymentSplits> = Observable.merge(
            createTripResponsePaymentSplits,
            priceChangeResponsePaymentSplits,
            burnAmountAndLatestTripResponse.filter { it.burnAmount.compareTo(BigDecimal.ZERO) == 0 }.map { it.latestTripResponse.paymentSplitsWhenZeroPayableWithPoints() },
            burnAmountToPointsApiResponse.map { PaymentSplits(it.conversion!!, it.remainingPayableByCard!!) }
    )

    val restoredPaymentSplitsInCaseOfDiscardedApiCall = PublishSubject.create<PaymentSplits>()
    //Intermediate Stream to ensure side-effects like `doOnNext` execute only once even if the stream is subscribe to multiple times!
    //This Intermediate Stream is ultimately poured into `restoredPaymentSplitsInCaseOfDiscardedApiCall` which the clients can absorb.
    private val restoredPaymentSplitsInCaseOfDiscardedApiCallIntermediateStream = discardPendingCurrencyToPointsAPISubscription
            .withLatestFrom(burnAmountToPointsApiSubscriptions, { unit, burnAmountToPointsApiSubscription -> burnAmountToPointsApiSubscription })
            .doOnNext { it?.unsubscribe() }
            .withLatestFrom(paymentSplits, { unit, paymentSplits -> paymentSplits })

    //Facade to ensure there are no glitches!
    //Whenever you need Payment Splits and Latest Trip response, use this!
    public val paymentSplitsWithLatestTripResponse = paymentSplits.withLatestFrom(tripResponses, { paymentSplits, tripResponse ->
        PaymentSplitsAndTripResponse(tripResponse, paymentSplits)
    })


    //Conditions when Currency To Points Conversion can be locally handled without an API call
    fun canHandleCurrencyToPointsConversionLocally(burnAmount: BigDecimal, amountForMaxPayableWithPoints: BigDecimal): Boolean {
        return burnAmount.compareTo(BigDecimal.ZERO) == 0 || burnAmount.compareTo(amountForMaxPayableWithPoints) == 1
    }

    init {
        createTripSubject.subscribe {
            //Explicitly ensuring that tripResponses has the latest Trip Response onloaded to it before a Payment-Split is onloaded to paymentSplits (via createTripResponsePaymentSplits)
            tripResponses.onNext(it)
            createTripResponsePaymentSplits.onNext(it.newTripPaymentSplits())
            paymentSplitsSuggestionUpdates.onNext(Pair(it.newTripPaymentSplits(), true))
        }

        Observable.merge(priceChangeDuringCheckoutSubject, couponChangeSubject).withLatestFrom(pwpOpted, { priceChangeResponse, pwpOpted -> Pair(priceChangeResponse, pwpOpted) }).subscribe {
            //Explicitly ensuring that tripResponses has the latest Trip Response onloaded to it before a Payment-Split is onloaded to paymentSplits (via priceChangeResponsePaymentSplits)
            tripResponses.onNext(it.first)
            priceChangeResponsePaymentSplits.onNext(it.first.paymentSplitsForPriceChange(it.second))
            paymentSplitsSuggestionUpdates.onNext(Pair(it.first.paymentSplitsSuggestions(it.second), false))
        }

        burnAmountAndLatestTripResponse
                .withLatestFrom(burnAmountToPointsApiSubscriptions, { burnAmountAndLatestTripResponse, burnAmountToPointsApiSubscription -> Pair(burnAmountAndLatestTripResponse, burnAmountToPointsApiSubscription) })
                .doOnNext { it.second?.unsubscribe() }
                .filter { !canHandleCurrencyToPointsConversionLocally(it.first.burnAmount, it.first.latestTripResponse.maxPayableWithExpediaRewardPoints().amount) }
                .map { it.first }
                .subscribe {
                    val calculatePointsParams = CalculatePointsParams.Builder()
                            .tripId(it.latestTripResponse.tripId)
                            .programName(ProgramName.ExpediaRewards)
                            .amount(it.burnAmount.toString())
                            .rateId(it.latestTripResponse.expediaRewardsUserAccountDetails().rateID)
                            .build()

                    burnAmountToPointsApiSubscriptions.onNext(loyaltyServices.currencyToPoints(calculatePointsParams, makeCalculatePointsApiResponseObserver()))
                }

        restoredPaymentSplitsInCaseOfDiscardedApiCallIntermediateStream.subscribe {
            restoredPaymentSplitsInCaseOfDiscardedApiCall.onNext(it)
        }
    }
}
