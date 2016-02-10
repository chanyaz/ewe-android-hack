package com.expedia.vm

import android.content.res.Resources
import android.util.Log
import com.expedia.bookings.R
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplits
import com.expedia.bookings.data.payment.ProgramName
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.tracking.PayWithPointsErrorTrackingEnum
import com.expedia.bookings.utils.NumberUtils
import com.expedia.bookings.utils.Strings
import com.expedia.vm.interfaces.IPayWithPointsViewModel
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal
import java.text.NumberFormat

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

    private fun amountToPointsConversionAPIErrorTracking(apiError: ApiError): PayWithPointsErrorTrackingEnum {
        when (apiError.errorCode) {
            ApiError.Code.POINTS_CONVERSION_UNAUTHENTICATED_ACCESS -> return PayWithPointsErrorTrackingEnum.UNAUTHENTICATED_ACCESS
            ApiError.Code.TRIP_SERVICE_ERROR -> return PayWithPointsErrorTrackingEnum.TRIP_SERVICE_ERROR
        }
        return PayWithPointsErrorTrackingEnum.UNKNOWN_ERROR
    }

    private fun userEntersMoreThanAvailableBurnAmountMessage(amountForMaxPayableByPoints: String) = Phrase.from(resources, R.string.user_enters_more_than_available_points_TEMPLATE)
            .put("money", amountForMaxPayableByPoints)
            .format().toString()

    //POINTS APPLIED MESSAGING
    private fun pointsAppliedMessage(paymentSplits: PaymentSplits) = Phrase.from(resources, R.string.pwp_points_applied_TEMPLATE)
            .put("points", NumberFormat.getInstance().format(paymentSplits.payingWithPoints.points))
            .format().toString()

    //INLETS
    override val userEnteredBurnAmount = PublishSubject.create<String>()
    override val pwpOpted = BehaviorSubject.create<Boolean>(true)
    override val hasPwpEditBoxFocus = PublishSubject.create<Boolean>()
    override val clearUserEnteredBurnAmount = PublishSubject.create<Unit>()
    override val userToggledPwPSwitchWithUserEnteredBurnedAmountSubject = PublishSubject.create<Pair<Boolean, String>>()


    //OUTLETS
    //MESSAGING START
    override val totalPointsAndAmountAvailableToRedeem = paymentModel.tripResponses.filter { it.isExpediaRewardsRedeemable() }.map {
        Phrase.from(resources.getString(R.string.pay_with_point_total_available_TEMPLATE))
                .put("money", it.getPointDetails(ProgramName.ExpediaRewards)!!.totalAvailable.amount.formattedMoneyFromAmountAndCurrencyCode)
                .put("points", NumberFormat.getInstance().format(it.getPointDetails(ProgramName.ExpediaRewards)!!.totalAvailable.points))
                .format().toString()
    }

    override val currencySymbol = paymentModel.tripResponses.filter { it.isExpediaRewardsRedeemable() }.map {
        it.getPointDetails(ProgramName.ExpediaRewards)!!.totalAvailable.amount.currencySymbol
    }
    //MESSAGING END

    private val enablePwPToggleOnRedeemableNewTrip = paymentModel.createTripSubject.filter { it.isExpediaRewardsRedeemable() }.map { true }
    override val userSignedIn = PublishSubject.create<Boolean>()
    override val enablePwPToggle = Observable.merge(userSignedIn, enablePwPToggleOnRedeemableNewTrip)

    override val navigatingOutOfPaymentOptions = PublishSubject.create<Unit>()

    //Critical to absorb tripResponses here as a trip can shift from redeemable to non-redeemable and vice-versa in case of Apply/Remove Coupon and Price Change on Checkout!
    override val pwpWidgetVisibility = paymentModel.tripResponses.map { it.isExpediaRewardsRedeemable() }

    private val burnAmountUpdatesFromPaymentSplitsSuggestions = paymentModel.paymentSplitsSuggestionUpdates.map { it.payingWithPoints.amount.amount }

    private val burnAmountForComparison = PublishSubject.create<BigDecimal>()

    override val burnAmountUpdate = Observable.merge(
            burnAmountUpdatesFromPaymentSplitsSuggestions.map { it.toString() },
            clearUserEnteredBurnAmount.map { "" },
            userEnteredBurnAmount.filter { !Strings.isEmpty(it) && !Strings.equals(toBigDecimalWithScale2(it).toString(), it) }.map { toBigDecimalWithScale2(it).toString() },
            //Update textbox to restoredPaymentSplits in case of discarded api call and toggle is on
            paymentModel.restoredPaymentSplitsInCaseOfDiscardedApiCall
                    .withLatestFrom(pwpOpted, { paymentSplits, pwpOpted -> Pair(paymentSplits, pwpOpted) })
                    .filter {
                        it.second
                    }
                    .map { it.first.payingWithPoints.amount.amount }
                    .map { if (it.compareTo(BigDecimal.ZERO) == 0) "" else it.toString() }
    )

    private val distinctBurnAmountEntered = PublishSubject.create<BigDecimal>()
    //Intermediate Stream to ensure side-effects like `doOnNext` execute only once even if the stream is subscribe to multiple times!
    //This Intermediate Stream is ultimately poured into `burnAmountEntered` which the clients can absorb.
    private val distinctBurnAmountEnteredIntermediateStream = Observable.merge(
            userEnteredBurnAmount.map { toBigDecimalWithScale2(it) },
            pwpOpted.filter { !it }.map { toBigDecimalWithScale2("") },
            clearUserEnteredBurnAmount.map { toBigDecimalWithScale2("") })
            .withLatestFrom(burnAmountForComparison, { burnAmount, burnAmountForComparison ->
                object {
                    val burnAmount = burnAmount
                    val burnAmountForComparison = burnAmountForComparison
                }
            })
            .filter { it.burnAmountForComparison == null || it.burnAmount.compareTo(it.burnAmountForComparison) != 0 }
            .map { it.burnAmount }
            .doOnNext { burnAmountForComparison.onNext(it) }

    private val burnAmountAndLatestTripTotalFacade = distinctBurnAmountEntered
            .withLatestFrom(paymentModel.tripResponses, { burnAmount, tripResponse ->
                object {
                    val burnAmount = burnAmount
                    val tripTotal = tripResponse.getTripTotal()
                    val maxPayableWithPoints = tripResponse.maxPayableWithExpediaRewardPoints()
                    val totalAvailableBurnAmount = tripResponse.totalAvailableBurnAmount(ProgramName.ExpediaRewards)
                }
            })

    //Locally Handled Errors - Burn Amount > Trip Total
    private val burnAmountGreaterThanTripTotalToBeHandledLocallyError = burnAmountAndLatestTripTotalFacade.filter { it.burnAmount.compareTo(it.tripTotal.amount) == 1 }

    //Locally Handled Errors - Burn Amount <= Trip Total AND Burn Amount > Total Burn Amount Available
    private val burnAmountLessThanTripTotalAndGreaterThanAvailableInAccountToBeHandledLocallyError = burnAmountAndLatestTripTotalFacade.filter { it.burnAmount.compareTo(it.tripTotal.amount) != 1 && it.burnAmount.compareTo(it.totalAvailableBurnAmount.amount) == 1 }

    private val pointsAppliedAndErrorMessages: Observable<Pair<String, Boolean>> = Observable.merge(
            //Points received from Payment Splits
            paymentModel.paymentSplits.map { Pair(pointsAppliedMessage(it), true) },
            burnAmountGreaterThanTripTotalToBeHandledLocallyError.map { Pair(userEntersMoreThanTripTotalString, false) },
            burnAmountLessThanTripTotalAndGreaterThanAvailableInAccountToBeHandledLocallyError.map { Pair(userEntersMoreThanAvailableBurnAmountMessage(it.maxPayableWithPoints.formattedMoneyFromAmountAndCurrencyCode), false) },
            //API Errors
            paymentModel.burnAmountToPointsApiError.map { Pair(amountToPointsConversionAPIErrorString(it), false) },
            //In case of back reset points applied
            paymentModel.restoredPaymentSplitsInCaseOfDiscardedApiCall.map { Pair(pointsAppliedMessage(it), true) }
    )

    private val showCalculatingPointsMessage = burnAmountAndLatestTripTotalFacade.filter { !paymentModel.canHandleCurrencyToPointsConversionLocally(it.burnAmount, it.maxPayableWithPoints.amount) }.map { Unit }
    override val pointsAppliedMessage = Observable.merge(showCalculatingPointsMessage.map { Pair(calculatingPointsString, true) }, pointsAppliedAndErrorMessages)

    override val pointsAppliedMessageColor = pointsAppliedMessage.map {
        when (it.second) {
            true -> resources.getColor((R.color.hotels_primary_color));
            false -> resources.getColor(R.color.cvv_error);
        }
    }

    override val enablePwpEditBox = Observable.merge(
            pointsAppliedAndErrorMessages.map { true },
            showCalculatingPointsMessage.map { false } )

    init {
        burnAmountUpdatesFromPaymentSplitsSuggestions
                .withLatestFrom(pwpOpted, { burnAmountUpdateFromPaymentSplitsSuggestion, pwpOpted -> Pair(burnAmountUpdateFromPaymentSplitsSuggestion, pwpOpted) })
                .map { if (!it.second) BigDecimal.ZERO else it.first }
                .subscribe(burnAmountForComparison)
        paymentModel.burnAmountToPointsApiError.map { null }.subscribe(burnAmountForComparison)
        paymentModel.restoredPaymentSplitsInCaseOfDiscardedApiCall.map { it.payingWithPoints.amount.amount }.subscribe(burnAmountForComparison)

        //Send it off to the Model!
        pwpOpted.subscribe(paymentModel.pwpOpted)
        distinctBurnAmountEntered.subscribe(paymentModel.burnAmountSubject)
        navigatingOutOfPaymentOptions.subscribe(paymentModel.discardPendingCurrencyToPointsAPISubscription)

        distinctBurnAmountEnteredIntermediateStream.subscribe(distinctBurnAmountEntered)

        setupTracking()
    }


    private fun setupTracking() {
        // User decides to change the pay with points amount
        paymentModel.burnAmountToPointsApiResponse.map {
            val payingWithPointsAmount = it.conversion!!.amount.amount
            val totalAmount = payingWithPointsAmount.plus(it.remainingPayableByCard!!.amount.amount)
            NumberUtils.getPercentagePaidWithPointsForOmniture(payingWithPointsAmount, totalAmount)
        }.subscribe {
            HotelV2Tracking().trackPayWithPointsAmountUpdateSuccess(it)
        }

        distinctBurnAmountEntered.filter { pwpOpted.value && it.compareTo(BigDecimal.ZERO) == 0}.subscribe {
            HotelV2Tracking().trackPayWithPointsAmountUpdateSuccess(0)
        }

        //User decides to toggle pay with points widget
        userToggledPwPSwitchWithUserEnteredBurnedAmountSubject.withLatestFrom(paymentModel.tripResponses, { pwpSwitchStateWithUserBurnAmount, tripResponse ->
            object {
                val pwpSwitchState = pwpSwitchStateWithUserBurnAmount.first
                val userEnteredBurnAmount = pwpSwitchStateWithUserBurnAmount.second
                val trip = tripResponse
            }
        }).subscribe {
            if (!it.pwpSwitchState) {
                HotelV2Tracking().trackPayWithPointsDisabled()
            }
            else {
                val percentage  = if(Strings.isNotEmpty(it.userEnteredBurnAmount)) NumberUtils.getPercentagePaidWithPointsForOmniture(BigDecimal(it.userEnteredBurnAmount), it.trip.getTripTotal().amount) else 0
                HotelV2Tracking().trackPayWithPointsReEnabled(percentage)
            }
        }

        // User enters wrong pay with points amount or calculate points api gives error.
        burnAmountGreaterThanTripTotalToBeHandledLocallyError.subscribe { HotelV2Tracking().trackPayWithPointsError(PayWithPointsErrorTrackingEnum.AMOUNT_ENTERED_GREATER_THAN_TRIP_TOTAL) }
        burnAmountLessThanTripTotalAndGreaterThanAvailableInAccountToBeHandledLocallyError.subscribe { HotelV2Tracking().trackPayWithPointsError(PayWithPointsErrorTrackingEnum.AMOUNT_ENTERED_GREATER_THAN_TRIP_TOTAL) }
        paymentModel.burnAmountToPointsApiError.map { amountToPointsConversionAPIErrorTracking(it) }.subscribe {HotelV2Tracking().trackPayWithPointsError(it) }
        userToggledPwPSwitchWithUserEnteredBurnedAmountSubject.filter { it.first }.map { it.second }.subscribe(userEnteredBurnAmount)
    }
    private fun toBigDecimalWithScale2(string: String): BigDecimal = (if (Strings.isEmpty(string)) BigDecimal.ZERO else BigDecimal(string)).setScale(2)
}