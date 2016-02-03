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

public class PaymentModel<T : TripResponse>(loyaltyServices: LoyaltyServices) {
    val discardPendingCurrencyToPointsAPISubscription = PublishSubject.create<Unit>()

    data class PaymentSplitsAndTripResponse(val tripResponse: TripResponse, val paymentSplits: PaymentSplits)

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

    //Merging to handle all 3 response types homogeneously
    val tripResponses = Observable.merge(createTripSubject, priceChangeDuringCheckoutSubject, couponChangeSubject)

    //Facade to ensure there are no glitches!
    //Whenever you need Amount Chosen To Be Paid With Points and Latest Trip response and Latest Currency To Points Api Subscription, use this!
    private val burnAmountAndLatestTripResponse = burnAmountSubject.withLatestFrom(tripResponses, {
        burnAmount, latestTripResponse ->
        object {
            val burnAmount = burnAmount
            val latestTripResponse = latestTripResponse
        }
    })

    //If PwP is enabled, default to Max Payable With Points, otherwise default to Zero Payable With Points.
    private val startingPaymentSplitsFromCreateTrip: Observable<PaymentSplits> = createTripSubject.map {
        when (it.isExpediaRewardsRedeemable()) {
            true -> it.paymentSplitsWhenMaxPayableWithPoints()
            false -> it.paymentSplitsWhenZeroPayableWithPoints()
        }
    }

    //Payment Splits (amount payable by points and by card) to be consumed by clients.
    val paymentSplits: Observable<PaymentSplits> = Observable.merge(
            startingPaymentSplitsFromCreateTrip,
            couponChangeSubject.map { it.paymentSplitsForPriceChange() },
            priceChangeDuringCheckoutSubject.map { it.paymentSplitsForPriceChange() },
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
    public val paymentSplitsAndLatestTripResponse = paymentSplits.withLatestFrom(tripResponses, { paymentSplits, tripResponse ->
        PaymentSplitsAndTripResponse(tripResponse, paymentSplits)
    })

    //Conditions when Currency To Points Conversion can be locally handled without an API call
    fun canHandleCurrencyToPointsConversionLocally(burnAmount: BigDecimal, amountForMaxPayableWithPoints: BigDecimal): Boolean {
        return burnAmount.compareTo(BigDecimal.ZERO) == 0 || burnAmount.compareTo(amountForMaxPayableWithPoints) == 1
    }

    init {
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
