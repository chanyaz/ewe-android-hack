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
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeChecked
import com.expedia.util.subscribeOnCheckChanged
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextColor
import com.expedia.util.subscribeVisibility
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.PublishSubject
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
    val orbucksOpted: PublishSubject<Boolean>

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

    //Inlet
    override val orbucksOpted = PublishSubject.create<Boolean>()
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
            .withLatestFrom(paymentModel.swpOpted, {
                paymentSplitsSuggestionUpdate, isSwpOpted ->
                object {
                    val isCreateTrip = paymentSplitsSuggestionUpdate.second
                    val isSwpOpted = isSwpOpted
                }
            })
            .withLatestFrom(paymentModel.tripResponses, {
                isCreateTripAndIsSwpOpted, tripResponse ->
                object {
                    val isCreateTrip = isCreateTripAndIsSwpOpted.isCreateTrip
                    val isSwpOpted = isCreateTripAndIsSwpOpted.isSwpOpted
                    val isRewardsRedeemable = tripResponse.isRewardsRedeemable()
                }
            })
            .filter { it.isCreateTrip && it.isRewardsRedeemable }
            .map { it.isSwpOpted }

    init {
        orbucksOpted.subscribe(paymentModel.togglePaymentByPoints)
    }
}