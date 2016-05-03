package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Switch
import com.expedia.bookings.R
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplits
import com.expedia.bookings.data.payment.ProgramName
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.NumberUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.withLatestFrom
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeChecked
import com.expedia.util.subscribeOnCheckChanged
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextColor
import com.expedia.util.subscribeVisibility
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.BehaviorSubject
import java.math.BigDecimal
import javax.inject.Inject

class OrbucksWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val orbucksMessage: TextView by bindView(R.id.orbucks_message_view)
    val orbucksSwitchView: Switch by bindView(R.id.orbucks_switch)
    val payWithRewardMessage: TextView by bindView(R.id.pay_with_reward_message)

    var viewModel: IOrbucksViewModel by notNullAndObservable {
        it.orbucksWidgetVisibility.subscribeVisibility(this)
        it.orbucksMessage.subscribeText(orbucksMessage)
        orbucksSwitchView.subscribeOnCheckChanged(it.orbucksOpted)
        it.pointsAppliedMessageColor.subscribeTextColor(orbucksMessage)
        it.payWithRewardsMessage.subscribeText(payWithRewardMessage)
        it.updateToggle.subscribeChecked(orbucksSwitchView)
    }
        @Inject set

    init {
        View.inflate(getContext(), R.layout.orbucks_widget, this)
        Ui.getApplication(context).hotelComponent().inject(this)
    }
}

interface IOrbucksViewModel {
    //Inlets
    val orbucksOpted: BehaviorSubject<Boolean>

    //Outlet
    val orbucksMessage: Observable<String>
    val pointsAppliedMessageColor: Observable<Int>
    val orbucksWidgetVisibility: Observable<Boolean>
    val payWithRewardsMessage: Observable<String>
    val updateToggle: Observable<Boolean>
}

class OrbucksViewModel<T : TripResponse>(paymentModel: PaymentModel<T>, val context: Context) : IOrbucksViewModel {
    //MESSAGING
    private fun pointsAppliedMessage(paymentSplits: PaymentSplits, tripResponse: TripResponse): String {
        if (paymentSplits.payingWithPoints.points != 0f) {
            return Phrase.from(context, R.string.orbucks_applied_TEMPLATE)
                    .put("money", paymentSplits.payingWithPoints.amount.formattedMoneyFromAmountAndCurrencyCode)
                    .format().toString();
        } else {
            return Phrase.from(context, R.string.orbucks_available_TEMPLATE)
                    .put("money", tripResponse.getPointDetails()!!.totalAvailable.amount.formattedMoneyFromAmountAndCurrencyCode)
                    .format().toString();
        }
    }

    private val programmaticToggle = BehaviorSubject.create<Boolean>(false)

    //Inlet
    override val orbucksOpted = BehaviorSubject.create<Boolean>(true)
    override val orbucksMessage = paymentModel.paymentSplitsWithLatestTripTotalPayableAndTripResponse.filter { ProgramName.Orbucks == it.tripResponse.getProgramName() }
            .map { pointsAppliedMessage(it.paymentSplits, it.tripResponse) }

    //Outlet
    override val orbucksWidgetVisibility = paymentModel.tripResponses.map { ProgramName.Orbucks == it.getProgramName() && it.isRewardsRedeemable() }
    override val payWithRewardsMessage = orbucksOpted.map {
        when (it) {
            true -> context.resources.getString(R.string.paying_with_rewards)
            false -> context.resources.getString(R.string.pay_with_rewards)
        }
    }

    override val pointsAppliedMessageColor = paymentModel.paymentSplits.map {
        when (it.payingWithPoints.points != 0f) {
            true -> ContextCompat.getColor(context, R.color.hotels_primary_color);
            false -> ContextCompat.getColor(context, R.color.hotelsv2_checkout_text_color);
        }
    }

    override val updateToggle = paymentModel.paymentSplitsSuggestionUpdates
            .withLatestFrom(paymentModel.swpOpted, paymentModel.tripResponses, orbucksOpted, {
                paymentSplitsSuggestionUpdate, isSwpOpted, tripResponse, orbucksOpted ->
                object {
                    val isCreateTrip = paymentSplitsSuggestionUpdate.second
                    val isSwpOpted = isSwpOpted
                    val isRewardsRedeemable = tripResponse.isRewardsRedeemable()
                    val orbucksOpted = orbucksOpted
                }
            })
            .filter { it.isCreateTrip && it.isRewardsRedeemable }
            .doOnNext { if (it.orbucksOpted != it.isSwpOpted) programmaticToggle.onNext(true) }
            .map { it.isSwpOpted }

    init {
        orbucksOpted.subscribe(paymentModel.togglePaymentByPoints)

        orbucksOpted.withLatestFrom(paymentModel.tripResponses, programmaticToggle, {
            orbucksOpted, tripResponses, programmaticToggle ->
            object {
                val orbucksOpted = orbucksOpted
                val tripResponses = tripResponses
                val programmaticToggle = programmaticToggle
            }
        }).doOnNext { programmaticToggle.onNext(false) }.filter { !it.programmaticToggle }.subscribe {
            if (it.orbucksOpted) {
                val newPaymentSplits = it.tripResponses.paymentSplitsWhenMaxPayableWithPoints()
                val tripTotal = it.tripResponses.getTripTotalExcludingFee().amount;
                val percentage = NumberUtils.getPercentagePaidWithPointsForOmniture(newPaymentSplits.payingWithPoints.amount.amount, tripTotal)
                HotelV2Tracking().trackPayWithPointsReEnabled(percentage)
            } else {
                HotelV2Tracking().trackPayWithPointsDisabled()
            }
        }
    }
}