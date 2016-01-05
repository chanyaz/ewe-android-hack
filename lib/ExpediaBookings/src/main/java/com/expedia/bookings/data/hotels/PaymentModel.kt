package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.Money
import com.expedia.bookings.services.HotelServices
import rx.Observable
import rx.Observer
import rx.Subscription
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal

public class PaymentModel(hotelServices: HotelServices) {
    //API
    private val nullSubscription: Subscription? = null
    //Persist the last subscription to clean it up (if it is active) before creating a new subscription for currencyToPoints API
    private val currencyToPointsApiSubscriptions = BehaviorSubject.create<Subscription?>(nullSubscription)

    val currencyToPointsApiResponse = PublishSubject.create<CalculatePointsResponse>()
    val currencyToPointsApiError = PublishSubject.create<Unit>()

    private fun makeCalculatePointsApiResponseObserver(): Observer<CalculatePointsResponse> {
        return object : Observer<CalculatePointsResponse> {
            override fun onError(apiError: Throwable?) {
                currencyToPointsApiError.onNext(Unit)
            }

            override fun onNext(apiResponse: CalculatePointsResponse) {
                if (!apiResponse.hasErrors()) {
                    currencyToPointsApiResponse.onNext(apiResponse)
                } else {
                    currencyToPointsApiError.onNext(Unit)
                }
            }

            override fun onCompleted() {
            }
        }
    }

    //Inlet

    //Client can push CreateTrip, PriceChangeDuringCheckout, Coupon Apply/Remove responses on these streams,
    //and the business logic will emit updated PaymentSplits and relevant information on output streams
    val createTripSubject = PublishSubject.create<HotelCreateTripResponse>()
    val priceChangeDuringCheckoutSubject = PublishSubject.create<HotelCreateTripResponse>()
    val couponChangeSubject = PublishSubject.create<HotelCreateTripResponse>()

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
        when (it.isExpediaRewardsRedeemable) {
            true -> paymentSplitsWhenMaxPayableWithPoints(it)
            false -> paymentSplitsWhenZeroPayableWithPoints(it)
        }
    }

    fun expediaRewardsUserAccountDetails(response: HotelCreateTripResponse): PointsDetails = response.getPointDetails(PointsProgramType.EXPEDIA_REWARDS)
    fun maxPayableWithPoints(response: HotelCreateTripResponse): BigDecimal = expediaRewardsUserAccountDetails(response).maxPayableWithPoints?.amount?.amount ?: BigDecimal.ZERO

    //Outlets
    private fun paymentSplitsWhenZeroPayableWithPoints(response: HotelCreateTripResponse): PaymentSplits {
        val payingWithPoints = PointsAndCurrency(0, PointsType.BURN, Money("0", response.tripTotal.currencyCode))
        val payingWithCards = PointsAndCurrency(response.expediaRewards.totalPointsToEarn, PointsType.EARN, response.tripTotal)
        return PaymentSplits(payingWithPoints, payingWithCards)
    }

    private fun paymentSplitsWhenMaxPayableWithPoints(response: HotelCreateTripResponse): PaymentSplits {
        val expediaPointDetails = response.getPointDetails(PointsProgramType.EXPEDIA_REWARDS)
        return PaymentSplits(expediaPointDetails.maxPayableWithPoints!!, expediaPointDetails.remainingPayableByCard!!)
    }

    //Payment Splits (amount payable by points and by card) to be consumed by clients for display purposes.
    val paymentSplits: Observable<PaymentSplits> = Observable.merge(
            defaultPaymentSplits,
            amountSelectedAndLatestTripResponse.filter { it.amount.equals(BigDecimal.ZERO) }.doOnNext { it.subscription?.unsubscribe() }.map { paymentSplitsWhenZeroPayableWithPoints(it.response) },
            currencyToPointsApiResponse.map { PaymentSplits(it.conversion!!, it.remainingPayableByCard!!) }
    )

    //Conditions when Currency To Points Conversion can be locally handled without an API call
    private fun canHandleCurrencyToPointsConversionLocally(amount: BigDecimal, response: HotelCreateTripResponse): Boolean {
        return amount.equals(BigDecimal.ZERO) || amount > response.tripTotal.amount || amount > maxPayableWithPoints(response)
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

                    currencyToPointsApiSubscriptions.onNext(hotelServices.calculatePoints(calculatePointsParams, makeCalculatePointsApiResponseObserver()))
                }
    }
}
