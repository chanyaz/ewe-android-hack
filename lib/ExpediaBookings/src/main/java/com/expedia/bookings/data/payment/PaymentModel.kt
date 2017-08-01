package com.expedia.bookings.data.payment

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.withLatestFrom
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal

class PaymentModel<T : TripResponse>(loyaltyServices: LoyaltyServices) {
    val discardPendingCurrencyToPointsAPISubscription = PublishSubject.create<Unit>()

    class PaymentSplitsWithTripTotalAndTripResponse<T : TripResponse>(val tripResponse: T, val paymentSplits: PaymentSplits, val tripTotalPayableIncludingFee: Money) {
        fun isCardRequired(): Boolean = if (this.tripResponse.isRewardsRedeemable()) this.paymentSplits.paymentSplitsType() != PaymentSplitsType.IS_FULL_PAYABLE_WITH_POINT else this.tripResponse.isCardDetailsRequiredForBooking()
    }

    //API
    private val nullSubscription: Disposable? = null
    //Persist the last subscription to clean it up (if it is active) before creating a new subscription for currencyToPoints API
    val burnAmountToPointsApiSubscriptions = BehaviorSubject.createDefault<Disposable?>(nullSubscription)

    val burnAmountToPointsApiResponse = PublishSubject.create<CalculatePointsResponse>()
    val burnAmountToPointsApiError = PublishSubject.create<ApiError>()

    private fun makeCalculatePointsApiResponseObserver(): Observer<CalculatePointsResponse> {
        return object: DisposableObserver<CalculatePointsResponse>() {
            override fun onError(apiError: Throwable) {
                if (!this.isDisposed) {
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

            override fun onComplete() {
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

    val tripTotalPayable = BehaviorSubject.create<Money>()

    //OUTLETS

    //Merging to handle all 3 Trip Response Types homogeneously
    val tripResponses = PublishSubject.create<T>()

    //Facade to ensure there are no glitches!
    //Whenever you need Amount Chosen To Be Paid With Points and Latest Trip response and Latest Currency To Points Api Disposable, use this!
    private val burnAmountAndLatestTripResponse = burnAmountSubject.withLatestFrom(tripResponses, {
        burnAmount, latestTripResponse ->
        object {
            val burnAmount = burnAmount
            val latestTripResponse = latestTripResponse
        }
    })

    val pwpOpted = BehaviorSubject.createDefault<Boolean>(true)
    val swpOpted = BehaviorSubject.createDefault<Boolean>(true)

    val togglePaymentByPoints = PublishSubject.create<Boolean>()
    val togglePaymentByPointsIntermediateStream = PublishSubject.create<PaymentSplits>()

    //If PwP is enabled, default to Max Payable With Points, otherwise default to Zero Payable With Points.
    private val paymentSplitsFromCreateTripResponse = PublishSubject.create<PaymentSplits>()

    //If PwP is disabled, API may still return Max-Payable-By-Points-Splits, but ignore those and fallback to Zero-Payable-With-Points
    //If PwP is enabled, simply take the Payment-Splits-For-Price-Change which have all fallbacks in place
    val paymentSplitsFromPriceChangeResponse = PublishSubject.create<PaymentSplits>()

    //If PwP is enabled, default to Max Payable With Points, otherwise default to Zero Payable With Points.
    private val paymentSplitsFromBurnAmountUpdates = PublishSubject.create<PaymentSplits>()

    //Boolean in the Pair indicates whether the Suggestion Updates are from Create-Trip (true) or from Price-Change (false)
    val paymentSplitsSuggestionUpdates = PublishSubject.create<Pair<PaymentSplits, Boolean>>()

    //Payment Splits (amount payable by points and by card) to be consumed by clients.
    val paymentSplits: Observable<PaymentSplits> = Observable.merge(
            paymentSplitsFromCreateTripResponse,
            paymentSplitsFromPriceChangeResponse,
            paymentSplitsFromBurnAmountUpdates,
            togglePaymentByPointsIntermediateStream
    )

    val restoredPaymentSplitsInCaseOfDiscardedApiCall = PublishSubject.create<PaymentSplits>()
    //Intermediate Stream to ensure side-effects like `doOnNext` execute only once even if the stream is subscribe to multiple times!
    //This Intermediate Stream is ultimately poured into `restoredPaymentSplitsInCaseOfDiscardedApiCall` which the clients can absorb.
    private val restoredPaymentSplitsInCaseOfDiscardedApiCallIntermediateStream = discardPendingCurrencyToPointsAPISubscription
            .withLatestFrom(burnAmountToPointsApiSubscriptions, { unit, burnAmountToPointsApiSubscription -> burnAmountToPointsApiSubscription })
            .doOnNext { it?.dispose() }
            .withLatestFrom(paymentSplits, { unit, paymentSplits -> paymentSplits })

    //Facade to ensure there are no glitches!
    //Whenever you need Payment Splits and Latest Trip response and TripTotal including fee paid at hotel, use this!
    val paymentSplitsWithLatestTripTotalPayableAndTripResponse = paymentSplits.withLatestFrom(tripTotalPayable, tripResponses,
            { paymentSplits, tripTotalPayable, tripResponses ->
                PaymentSplitsWithTripTotalAndTripResponse(tripResponses, paymentSplits, tripTotalPayable)
            })

    //Conditions when Currency To Points Conversion can be locally handled without an API call
    fun canHandleCurrencyToPointsConversionLocally(burnAmount: BigDecimal, amountForMaxPayableWithPoints: BigDecimal): Boolean {
        return burnAmount.compareTo(BigDecimal.ZERO) == 0 || burnAmount.compareTo(amountForMaxPayableWithPoints) == 1
    }

    init {
        burnAmountAndLatestTripResponse.filter { it.burnAmount.compareTo(BigDecimal.ZERO) == 0 }.map { it.latestTripResponse}.subscribe{
            tripTotalPayable.onNext(it.tripTotalPayableIncludingFeeIfZeroPayableByPoints())
            paymentSplitsFromBurnAmountUpdates.onNext(it.paymentSplitsWhenZeroPayableWithPoints() )
        }

        burnAmountToPointsApiResponse.subscribe {
            tripTotalPayable.onNext(it.tripTotalPayable!!)//TODO PUK
            paymentSplitsFromBurnAmountUpdates.onNext(PaymentSplits(it.conversion!!, it.remainingPayableByCard!!))
        }

        togglePaymentByPoints.withLatestFrom(tripResponses, { togglePaymentByPoints, latestTripResponse ->
            object {
                val togglePaymentByPoints = togglePaymentByPoints
                val latestTripResponse = latestTripResponse
            }
        }).subscribe {
            if (it.togglePaymentByPoints) {
                tripTotalPayable.onNext(it.latestTripResponse.rewardsUserAccountDetails().tripTotalPayable!!)//TODO PUK
                togglePaymentByPointsIntermediateStream.onNext(it.latestTripResponse.paymentSplitsWhenMaxPayableWithPoints())
            } else {
                tripTotalPayable.onNext(it.latestTripResponse.tripTotalPayableIncludingFeeIfZeroPayableByPoints())
                togglePaymentByPointsIntermediateStream.onNext(it.latestTripResponse.paymentSplitsWhenZeroPayableWithPoints())
            }
        }

        createTripSubject.withLatestFrom(swpOpted, { createTripResponse, swpOpted ->
            object {
                val createTripResponse = createTripResponse;
                val swpOpted = swpOpted;
            }
        }).subscribe {
            //Explicitly ensuring that tripResponses has the latest Trip Response onloaded to it before a Payment-Split is onloaded to paymentSplits (via createTripResponsePaymentSplits)
            tripResponses.onNext(it.createTripResponse)
            tripTotalPayable.onNext(it.createTripResponse.getTripTotalIncludingFeeForCreateTrip(it.swpOpted))
            paymentSplitsFromCreateTripResponse.onNext(it.createTripResponse.paymentSplitsForNewCreateTrip(it.swpOpted))
            paymentSplitsSuggestionUpdates.onNext(Pair(it.createTripResponse.paymentSplitsSuggestionsForNewCreateTrip(), true))
        }

        Observable.merge(priceChangeDuringCheckoutSubject, couponChangeSubject).withLatestFrom(pwpOpted, { priceChangeResponse, pwpOpted -> Pair(priceChangeResponse, pwpOpted) }).subscribe {
            //Explicitly ensuring that tripResponses has the latest Trip Response onloaded to it before a Payment-Split is onloaded to paymentSplits (via priceChangeResponsePaymentSplits)
            tripResponses.onNext(it.first)
            tripTotalPayable.onNext(it.first.getTripTotalIncludingFeeForPriceChange(it.second))
            paymentSplitsFromPriceChangeResponse.onNext(it.first.paymentSplitsForPriceChange(it.second))
            paymentSplitsSuggestionUpdates.onNext(Pair(it.first.paymentSplitsSuggestionsForPriceChange(it.second), false))
        }

        burnAmountAndLatestTripResponse
                .withLatestFrom(burnAmountToPointsApiSubscriptions, { burnAmountAndLatestTripResponse, burnAmountToPointsApiSubscription -> Pair(burnAmountAndLatestTripResponse, burnAmountToPointsApiSubscription) })
                .doOnNext { it.second?.dispose() }
                .filter { !canHandleCurrencyToPointsConversionLocally(it.first.burnAmount, it.first.latestTripResponse.maxPayableWithRewardPoints().amount) }
                .map { it.first }
                .subscribe {
                    val calculatePointsParams = CalculatePointsParams.Builder()
                            .tripId(it.latestTripResponse.tripId)
                            .programName(it.latestTripResponse.getProgramName())
                            .amount(it.burnAmount.toString())
                            .rateId(it.latestTripResponse.rewardsUserAccountDetails().rateID)
                            .build()

                    burnAmountToPointsApiSubscriptions.onNext(loyaltyServices.currencyToPoints(calculatePointsParams, makeCalculatePointsApiResponseObserver()))
                }

        restoredPaymentSplitsInCaseOfDiscardedApiCallIntermediateStream.subscribe {
            restoredPaymentSplitsInCaseOfDiscardedApiCall.onNext(it)
        }
    }
}
