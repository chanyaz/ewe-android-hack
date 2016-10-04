package com.expedia.bookings.widget.rail

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.rail.RailLegSummaryViewModel
import rx.subjects.PublishSubject

class RailLegSummaryWidget(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {

    val travelTimes: TextView by bindView(R.id.times_view)
    val trainOperator: TextView by bindView(R.id.train_operator)
    val duration: TextView by bindView(R.id.layover_view)
    val timeline: RailResultsTimelineWidget by bindView(R.id.timeline_view)
    var outbound = false
    val legContainer: ViewGroup by bindView(R.id.rail_leg_container)
    val legDetailsIcon: ImageView by bindView(R.id.rail_leg_details_icon)
    val legDetailsWidget: RailDetailsTimeline by bindView(R.id.rail_leg_details)
    val fareDescription: TextView by bindView(R.id.fare_description)
    val fareDescriptionContainer: View by bindView(R.id.fare_description_container)
    val railCardName: TextView by bindView(R.id.rail_card_name)
    val overtakenMessage: android.widget.TextView by bindView(R.id.overtaken_message)
    val overtakenDivider: View by bindView(R.id.overtaken_message_divider)
    val legContainerClicked = PublishSubject.create<Unit>()

    var viewModel: RailLegSummaryViewModel by notNullAndObservable { vm ->
        vm.operatorObservable.subscribeText(trainOperator)
        vm.formattedStopsAndDurationObservable.subscribeText(duration)
        vm.formattedTimesObservable.subscribeText(travelTimes)
        vm.fareDescriptionLabelObservable.subscribeText(fareDescription)
        vm.legOptionObservable.subscribe { railLegOption ->
            timeline.updateLeg(railLegOption)
        }

        vm.legOptionObservable.subscribe(legDetailsWidget.railLegOptionObserver)

        legContainer.subscribeOnClick(legContainerClicked)

        legContainerClicked.withLatestFrom(vm.overtakenSubject, { clicked, overtaken ->
            if (!isLegDetailsExpanded()) {
                expandLegDetails(overtaken)
            } else {
                collapseLegDetails()
            }
        }).subscribe()

        vm.railCardAppliedNameSubject.subscribeTextAndVisibility(railCardName)
        fareDescriptionContainer.subscribeOnClick(viewModel.showLegInfoObservable)
    }

    init {
        View.inflate(getContext(), R.layout.rail_leg_summary, this)
        outbound = true //hardcoding for now until we handle round-trips
    }

    private fun expandLegDetails(overtaken: Boolean) {
        legDetailsWidget.visibility = Presenter.VISIBLE
        overtakenMessage.visibility = if (overtaken) View.VISIBLE else View.GONE
        overtakenDivider.visibility = if (overtaken) View.VISIBLE else View.GONE
        AnimUtils.rotate(legDetailsIcon)
    }

    private fun collapseLegDetails() {
        legDetailsWidget.visibility = Presenter.GONE
        overtakenMessage.visibility = View.GONE
        overtakenDivider.visibility = View.GONE
        AnimUtils.reverseRotate(legDetailsIcon)
        legDetailsIcon.clearAnimation()
    }

    private fun isLegDetailsExpanded(): Boolean {
        return legDetailsWidget.visibility == Presenter.VISIBLE
    }
}