package com.expedia.vm

import android.content.res.Resources
import com.expedia.bookings.R
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplits
import com.expedia.bookings.data.payment.ProgramName
import com.expedia.bookings.utils.Strings
import com.expedia.vm.interfaces.IPayWithPointsViewModel
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.PublishSubject
import java.math.BigDecimal

public class PayWithPointsViewModel<T : TripResponse>(val paymentModel: PaymentModel<T>, val resources: Resources) : IPayWithPointsViewModel {
    //ERROR MESSAGING
    private val userEntersMoreThanTripTotalString = resources.getString(R.string.user_enters_more_than_trip)
    private val calculatingPointsString = resources.getString(R.string.pwp_calculating_points)
    private val pointsConversionUnauthenticatedAccess = resources.getString(R.string.pwp_points_conversion_unauthenticated_access)
    private val tripServiceError = resources.getString(R.string.pwp_trip_service_error)
    private val pwpUnknownError = resources.getString(R.string.pwp_unknown_error)

    private fun amountToPointsConversionAPIErrorString(apiError: ApiError): String {
        when (apiError.errorCode) {
            ApiError.Code.POINTS_CONVERSION_UNAUTHENTICATED_ACCESS -> return pointsConversionUnauthenticatedAccess
            ApiError.Code.TRIP_SERVICE_ERROR -> return tripServiceError
        }
        return pwpUnknownError
    }

    private fun userEntersMoreThanAvailableBurnAmountMessage(amountForMaxPayableByPoints: String) = Phrase.from(resources, R.string.user_enters_more_than_available_points_TEMPLATE)
            .put("money", amountForMaxPayableByPoints)
            .format().toString()

    //POINTS APPLIED MESSAGING
    private fun pointsAppliedMessage(paymentSplits: PaymentSplits) = Phrase.from(resources, R.string.pwp_points_applied_TEMPLATE)
            .put("points", paymentSplits.payingWithPoints.points)
            .format().toString()

    //INLETS
    override val userEnteredBurnAmount = PublishSubject.create<String>()
    override val pwpOpted = PublishSubject.create<Boolean>()
    override val clearUserEnteredBurnAmount = PublishSubject.create<Unit>()

    //OUTLETS
    //MESSAGING START
    override val totalPointsAndAmountAvailableToRedeem = paymentModel.tripResponses.filter { it.isExpediaRewardsRedeemable() }.map {
        Phrase.from(resources.getString(R.string.pay_with_point_total_available_TEMPLATE))
                .put("money", it.getPointDetails(ProgramName.ExpediaRewards)!!.totalAvailable.amount.formattedMoneyFromAmountAndCurrencyCode)
                .put("points", it.getPointDetails(ProgramName.ExpediaRewards)!!.totalAvailable.points)
                .format().toString()
    }

    override val currencySymbol = paymentModel.tripResponses.filter { it.isExpediaRewardsRedeemable() }.map {
        it.getPointDetails(ProgramName.ExpediaRewards)!!.totalAvailable.amount.currencySymbol
    }
    //MESSAGING END

    private val updatePwPToggleOnNewTrip = paymentModel.createTripSubject.map { it.isExpediaRewardsRedeemable() }
    override val userSignedIn = PublishSubject.create<Boolean>()
    override val enablePwPToggle = Observable.merge(userSignedIn, updatePwPToggleOnNewTrip)

    //Critical to absorb tripResponses here as a trip can shift from redeemable to non-redeemable and vice-versa in case of Apply/Remove Coupon and Price Change on Checkout!
    override val pwpWidgetVisibility = paymentModel.tripResponses.map { it.isExpediaRewardsRedeemable() }

    private val startingPaymentSplits = paymentModel.tripResponses.filter {
        it.isExpediaRewardsRedeemable()
    }.map { it.maxPayableWithExpediaRewardPoints().amount }

    private val pointsForComparison = PublishSubject.create<BigDecimal>()

    override val burnAmountUpdate = Observable.merge(
            startingPaymentSplits.map { it.toString() },
            clearUserEnteredBurnAmount.map { "" },
            paymentModel.restoredPaymentSplitsInCaseOfDiscardedApiCall.map { it.payingWithPoints.amount.amount.toString() })

    private val burnAmountEntered = Observable.merge(
            userEnteredBurnAmount.map { toBigDecimal(it) },
            pwpOpted.filter { !it }.map { BigDecimal.ZERO },
            clearUserEnteredBurnAmount.map { BigDecimal.ZERO })
            .withLatestFrom(pointsForComparison, { burnAmount, pointsForComparison ->
                object {
                    val burnAmount = burnAmount
                    val pointsForComparison = pointsForComparison
                }
            })
            .filter { it.pointsForComparison == null || it.burnAmount.compareTo(it.pointsForComparison) != 0 }
            .map { it.burnAmount }
            .doOnNext { pointsForComparison.onNext(it) }

    private val distinctBurnAmount = PublishSubject.create<BigDecimal>()

    private val burnAmountAndLatestTripTotalFacade = distinctBurnAmount
            .withLatestFrom(paymentModel.tripResponses, { burnAmount, tripResponse ->
                object {
                    val burnAmount = burnAmount
                    val tripTotal = tripResponse.getTripTotal()
                    val maxPayableWithPoints = tripResponse.maxPayableWithExpediaRewardPoints()
                    val totalAvailableBurnAmount = tripResponse.totalAvailableBurnAmount(ProgramName.ExpediaRewards)
                }
            })

    private val pointsAppliedAndErrorMessages = Observable.merge(
            //Points received from Payment Splits
            paymentModel.paymentSplits.map { Pair(pointsAppliedMessage(it), true) },
            //Locally Handled Errors - Burn Amount > Trip Total
            burnAmountAndLatestTripTotalFacade.filter { it.burnAmount.compareTo(it.tripTotal.amount) == 1 }.map { Pair(userEntersMoreThanTripTotalString, false) },
            //Locally Handled Errors - Burn Amount <= Trip Total AND Burn Amount > Total Burn Amount Available
            burnAmountAndLatestTripTotalFacade.filter { it.burnAmount.compareTo(it.tripTotal.amount) != 1 && it.burnAmount.compareTo(it.totalAvailableBurnAmount.amount) == 1 }.map { Pair(userEntersMoreThanAvailableBurnAmountMessage(it.maxPayableWithPoints.formattedMoneyFromAmountAndCurrencyCode), false) },
            //API Errors
            paymentModel.burnAmountToPointsApiError.map { Pair(amountToPointsConversionAPIErrorString(it), false) },
            //In case of back reset points applied
            paymentModel.restoredPaymentSplitsInCaseOfDiscardedApiCall.map { Pair(pointsAppliedMessage(it), true) }
    )

    private val showCalculatingPointsMessage = burnAmountAndLatestTripTotalFacade.filter { !paymentModel.canHandleCurrencyToPointsConversionLocally(it.burnAmount, it.maxPayableWithPoints.amount) }.map { Unit }
    override val pointsAppliedMessage = Observable.merge(showCalculatingPointsMessage.map { Pair(calculatingPointsString, true) }, pointsAppliedAndErrorMessages)

    override val pointsAppliedMessageColor = pointsAppliedMessage.map {
        when (it.second) {
            true -> resources.getColor(android.R.color.black);
            false -> resources.getColor(R.color.cvv_error);
        }
    }

    init {
        startingPaymentSplits.subscribe(pointsForComparison)
        paymentModel.burnAmountToPointsApiError.map { null }.subscribe(pointsForComparison)
        paymentModel.restoredPaymentSplitsInCaseOfDiscardedApiCall.map { it.payingWithPoints.amount.amount }.subscribe(pointsForComparison)

        //Send it off to the Model!
        distinctBurnAmount.subscribe(paymentModel.burnAmountSubject)
        burnAmountEntered.subscribe(distinctBurnAmount)
    }

    private fun toBigDecimal(string: String): BigDecimal = if (Strings.isEmpty(string)) BigDecimal.ZERO else BigDecimal(string)
}