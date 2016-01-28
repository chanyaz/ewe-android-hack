package com.expedia.bookings.data.payment

import com.expedia.bookings.data.Money
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
    //API
    private val nullSubscription: Subscription? = null
    //Persist the last subscription to clean it up (if it is active) before creating a new subscription for currencyToPoints API
    private val currencyToPointsApiSubscriptions = BehaviorSubject.create<Subscription?>(nullSubscription)

    val currencyToPointsApiResponse = PublishSubject.create<CalculatePointsResponse>()
    val currencyToPointsApiError = PublishSubject.create<ApiError>()

    private fun makeCalculatePointsApiResponseObserver(): Observer<CalculatePointsResponse> {
        return object : Subscriber<CalculatePointsResponse>() {
            override fun onError(apiError: Throwable?) {
                if (!this.isUnsubscribed) {
                    currencyToPointsApiError.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
                }
            }

            override fun onNext(apiResponse: CalculatePointsResponse) {
                if (!apiResponse.hasErrors()) {
                    currencyToPointsApiResponse.onNext(apiResponse)
                } else {
                    currencyToPointsApiError.onNext(apiResponse.firstError)
                }
            }

            override fun onCompleted() {
            }
        }
    }

    //Inlet

    //Client can push CreateTrip, PriceChangeDuringCheckout, Coupon Apply/Remove responses on these streams,
    //and the business logic will emit updated PaymentSplits and relevant information on output streams
    val createTripSubject = PublishSubject.create<T>()
    val priceChangeDuringCheckoutSubject = PublishSubject.create<T>()
    val couponChangeSubject = PublishSubject.create<T>()

    //Merging to handle all 3 response types homogeneously
    val tripResponses = Observable.merge(createTripSubject, priceChangeDuringCheckoutSubject, couponChangeSubject)

    //Amount Chosen To Be Paid With Points
    val amountChosenToBePaidWithPointsSubject = PublishSubject.create<BigDecimal>()

    private val amountSelectedAndLatestTripResponse = amountChosenToBePaidWithPointsSubject.withLatestFrom(tripResponses, { amount, response ->
        object {
            val amount = amount
            val response = response
        }
    }).withLatestFrom(currencyToPointsApiSubscriptions, { amountAndResponse, subscription ->
        object {
            val amount = amountAndResponse.amount
            val response = amountAndResponse.response
            val subscription = subscription
        }
    })

    //If PwP is enabled, default to Max Payable With Points, otherwise default to Zero Payable With Points.
    private val defaultPaymentSplits: Observable<PaymentSplits> = createTripSubject.map {
        when (it.isExpediaRewardsRedeemable()) {
            true -> paymentSplitsWhenMaxPayableWithPoints(it)
            false -> paymentSplitsWhenZeroPayableWithPoints(it)
        }
    }

    fun expediaRewardsUserAccountDetails(response: T): PointsDetails = response.getPointDetails(ProgramName.ExpediaRewards)!!
    fun maxPayableWithPoints(response: T): BigDecimal = expediaRewardsUserAccountDetails(response).maxPayableWithPoints?.amount?.amount ?: BigDecimal.ZERO

    //Outlets
    data class PaymentSplitsAndTripResponse(val tripResponse: TripResponse, val paymentSplits: PaymentSplits)

    private fun paymentSplitsWhenZeroPayableWithPoints(response: T): PaymentSplits {
        val payingWithPoints = PointsAndCurrency(0, PointsType.BURN, Money("0", response.getTripTotal().currencyCode))
        val payingWithCards = PointsAndCurrency(response.expediaRewards.totalPointsToEarn, PointsType.EARN, response.getTripTotal())
        return PaymentSplits(payingWithPoints, payingWithCards)
    }

    private fun paymentSplitsWhenMaxPayableWithPoints(response: T): PaymentSplits {
        val expediaPointDetails = expediaRewardsUserAccountDetails(response)
        return PaymentSplits(expediaPointDetails.maxPayableWithPoints!!, expediaPointDetails.remainingPayableByCard!!)
    }

    //Payment Splits (amount payable by points and by card) to be consumed by clients for display purposes.
    val paymentSplits: Observable<PaymentSplits> = Observable.merge(
            defaultPaymentSplits,
            amountSelectedAndLatestTripResponse.filter { it.amount.equals(BigDecimal.ZERO) }.doOnNext { it.subscription?.unsubscribe() }.map { paymentSplitsWhenZeroPayableWithPoints(it.response) },
            currencyToPointsApiResponse.map { PaymentSplits(it.conversion!!, it.remainingPayableByCard!!) },
            couponChangeSubject.map { paymentSpitsWithPriceChange(it) },
            priceChangeDuringCheckoutSubject.map { paymentSpitsWithPriceChange(it) }
    )

    private fun paymentSpitsWithPriceChange(response: T): PaymentSplits {
        if (response.userPreferencePoints != null)
            return PaymentSplits( response.userPreferencePoints!!.getUserPreference(ProgramName.ExpediaRewards)!!, response.userPreferencePoints!!.remainingPayableByCard)
        else
            return paymentSplitsWhenZeroPayableWithPoints(response)
    }

    //Use this observable when tripResponse and
    public val paymentSplitsAndTripResponseObservable = paymentSplits.withLatestFrom(tripResponses, { paymentSplits, tripResponse ->
        PaymentSplitsAndTripResponse(tripResponse,paymentSplits)
    })

    //Conditions when Currency To Points Conversion can be locally handled without an API call
    fun canHandleCurrencyToPointsConversionLocally(amount: BigDecimal, response: T): Boolean {
        return amount.equals(BigDecimal.ZERO) || amount > response.getTripTotal().amount || amount > maxPayableWithPoints(response)
    }

    init {
        amountSelectedAndLatestTripResponse.filter { !canHandleCurrencyToPointsConversionLocally(it.amount, it.response) }
                .doOnNext { it.subscription?.unsubscribe() }
                .subscribe {
                    val calculatePointsParams = CalculatePointsParams.Builder()
                            .tripId(it.response.tripId)
                            .programName(ProgramName.ExpediaRewards)
                            .amount(it.amount.toString())
                            .rateId(expediaRewardsUserAccountDetails(it.response).rateID)
                            .build()

                    currencyToPointsApiSubscriptions.onNext(loyaltyServices.currencyToPoints(calculatePointsParams, makeCalculatePointsApiResponseObserver()))
                }
    }
}
