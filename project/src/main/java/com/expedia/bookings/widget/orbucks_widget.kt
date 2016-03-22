package com.expedia.bookings.widget

import android.content.Context
import android.content.res.Resources
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

    var viewModel: IOrbucksViewModel by notNullAndObservable {
        it.orbucksWidgetVisibility.subscribeVisibility(this)
        it.orbucksMessage.subscribeText(orbucksMessage)
        it.enableOrbucksToggle.map { true }.subscribeChecked(orbucksSwitchView)
        orbucksSwitchView.subscribeOnCheckChanged(it.orbucksOpted)
        it.pointsAppliedMessageColor.subscribeTextColor(orbucksMessage)
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
    val enableOrbucksToggle: Observable<Unit>
    val pointsAppliedMessageColor: Observable<Int>
    val orbucksWidgetVisibility: Observable<Boolean>
}

class OrbucksViewModel<T : TripResponse>(paymentModel: PaymentModel<T>, val res: Resources) : IOrbucksViewModel {
    //MESSAGING
    private fun pointsAppliedMessage(paymentSplits: PaymentSplits, tripResponse: TripResponse): String {
        if (paymentSplits.payingWithPoints.points != 0f) {
            return Phrase.from(res, R.string.orbucks_applied_TEMPLATE)
                    .put("money", paymentSplits.payingWithPoints.amount.formattedMoneyFromAmountAndCurrencyCode)
                    .format().toString();
        } else {
            return Phrase.from(res, R.string.orbucks_available_TEMPLATE)
                    .put("money", tripResponse.getPointDetails()!!.totalAvailable.amount.formattedMoneyFromAmountAndCurrencyCode)
                    .format().toString();
        }
    }

    //Inlet
    override val orbucksOpted = PublishSubject.create<Boolean>()
    override val orbucksMessage = paymentModel.paymentSplitsWithLatestTripResponse.filter { ProgramName.Orbucks == it.tripResponse.getProgramName() }
            .map { pointsAppliedMessage(it.paymentSplits, it.tripResponse) }

    //Outlet
    override val enableOrbucksToggle = paymentModel.createTripSubject.filter { ProgramName.Orbucks == it.getProgramName() && it.isRewardsRedeemable() }.map { Unit }
    override val orbucksWidgetVisibility = paymentModel.tripResponses.map { ProgramName.Orbucks == it.getProgramName() && it.isRewardsRedeemable() }

    override val pointsAppliedMessageColor = paymentModel.paymentSplits.map {
        when (it.payingWithPoints.points != 0f) {
            true -> res.getColor((R.color.hotels_primary_color));
            false -> res.getColor(R.color.hotelsv2_checkout_text_color);
        }
    }

    init {
        orbucksOpted.subscribe(paymentModel.togglePaymentByPoints)
    }
}