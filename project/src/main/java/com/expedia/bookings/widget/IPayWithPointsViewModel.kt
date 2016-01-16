package com.expedia.bookings.widget

import android.content.res.Resources
import com.expedia.bookings.R
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplits
import com.expedia.bookings.data.payment.PointsProgramType
import com.expedia.bookings.utils.Strings
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.PublishSubject
import java.math.BigDecimal

public interface IPayWithPointsViewModel {
    //Inlets
    val amountSubmittedByUser: PublishSubject<String>
    val pwpStateChange: PublishSubject<Boolean>
    val clearButtonClick: PublishSubject<Unit>

    //Outlets
    val updateAmountOfEditText: Observable<String>
    val pwpWidgetVisibility: Observable<Boolean>
    val totalPointsAndAmountAvailableToRedeem: Observable<String>
    val currencySymbol: Observable<String>
    val pwpConversionResponse: Observable<String>
}

public class PayWithPointsViewModel<T : TripResponse>(val paymentModel: PaymentModel<T>, val resources: Resources) : IPayWithPointsViewModel {
    private val userEntersMoreThanTripTotalString = resources.getString(R.string.user_enters_more_than_trip)
    private val calculatingPointsString = resources.getString(R.string.pwp_calculating_points)
    private val calculationApiErrorString = resources.getString(R.string.pwp_calculation_api_error)

    private fun userEntersMoreThanPointsMessage(response: TripResponse) = Phrase.from(resources, R.string.user_enters_more_than_points_TEMPLATE)
            .put("money", response.getPointDetails(PointsProgramType.EXPEDIA_REWARDS)!!.totalAvailable.amount.formattedMoneyFromAmountAndCurrencyCode)
            .format().toString()

    private fun pointsAppliedMessage(paymentSplits: PaymentSplits) = Phrase.from(resources, R.string.pwp_points_applied_TEMPLATE)
            .put("points", paymentSplits.payingWithPoints.points)
            .format().toString()

    //Inlets
    override val amountSubmittedByUser = PublishSubject.create<String>()
    override val pwpStateChange = PublishSubject.create<Boolean>()
    override val clearButtonClick = PublishSubject.create<Unit>()

    //Outlets
    private val defaultAmount = paymentModel.createTripSubject.filter { it.isExpediaRewardsRedeemable() }.map { paymentModel.maxPayableWithPoints(it).toString() }
    override val updateAmountOfEditText = Observable.merge(
            defaultAmount,
            clearButtonClick.map { "" })

    override val pwpWidgetVisibility = paymentModel.createTripSubject.map { it.isExpediaRewardsRedeemable() }

    override val totalPointsAndAmountAvailableToRedeem = paymentModel.tripResponses.filter { it.isExpediaRewardsRedeemable() }.map {
        Phrase.from(resources.getString(R.string.pay_with_point_total_available_TEMPLATE))
                .put("money", it.getPointDetails(PointsProgramType.EXPEDIA_REWARDS)!!.totalAvailable.amount.formattedMoneyFromAmountAndCurrencyCode)
                .put("points", it.getPointDetails(PointsProgramType.EXPEDIA_REWARDS)!!.totalAvailable.points)
                .format().toString()
    }

    override val currencySymbol = paymentModel.tripResponses.filter { it.isExpediaRewardsRedeemable() }.map {
        it.getPointDetails(PointsProgramType.EXPEDIA_REWARDS)!!.totalAvailable.amount.currencySymbol
    }

    private val calculatingPointsMessage = PublishSubject.create<Unit>()

    private fun toBigDecimal(string: String): BigDecimal = if (Strings.isEmpty(string)) BigDecimal.ZERO else BigDecimal(string)

    private val amountSelectedAndLatestTripResponse = Observable.merge(
            amountSubmittedByUser.distinctUntilChanged().map { toBigDecimal(it) },
            pwpStateChange.filter { it }.withLatestFrom(amountSubmittedByUser.startWith(defaultAmount), { x, y -> y }).map { toBigDecimal(it) },
            pwpStateChange.filter { !it }.map { BigDecimal.ZERO },
            clearButtonClick.map { BigDecimal.ZERO })
            .withLatestFrom(paymentModel.tripResponses, { amount, response ->
                object {
                    val amount = amount
                    val response = response
                }
            })

    private val pointsAppliedAndErrorMessages = Observable.merge(
            amountSelectedAndLatestTripResponse.filter { it.amount.compareTo(it.response.getTripTotal().amount) == 1 }.map { userEntersMoreThanTripTotalString },
            amountSelectedAndLatestTripResponse.filter { it.amount.compareTo(it.response.getTripTotal().amount) == -1 && it.amount.compareTo(paymentModel.maxPayableWithPoints(it.response)) == 1 }.map { userEntersMoreThanPointsMessage(it.response) },
            paymentModel.currencyToPointsApiError.map { calculationApiErrorString },
            paymentModel.paymentSplits.map { pointsAppliedMessage(it) }
    )

    override val pwpConversionResponse = Observable.merge(calculatingPointsMessage.map { calculatingPointsString }, pointsAppliedAndErrorMessages)

    init {
        amountSelectedAndLatestTripResponse.doOnNext {
            if (!paymentModel.canHandleCurrencyToPointsConversionLocally(it.amount, it.response)) calculatingPointsMessage.onNext(Unit)
        }.map { it.amount }.subscribe(paymentModel.amountChosenToBePaidWithPointsSubject)
    }
}
