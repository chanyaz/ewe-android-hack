package com.expedia.bookings.rail.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.tracking.RailTracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeContentDescription
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeTextAndVisibilityInvisible
import com.expedia.vm.rail.RailLegSummaryViewModel
import com.squareup.phrase.Phrase
import rx.subjects.PublishSubject

class RailLegSummaryWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val legInfoContainer: LinearLayout by bindView(R.id.rail_leg_info_container)
    val travelTimes: TextView by bindView(R.id.times_view)
    val trainOperator: TextView by bindView(R.id.train_operator)
    val duration: TextView by bindView(R.id.layover_view)
    val timeline: RailResultsTimelineWidget by bindView(R.id.timeline_view)
    val legContainer: ViewGroup by bindView(R.id.rail_leg_container)
    val legDetailsIcon: ImageView by bindView(R.id.rail_leg_details_icon)
    val legDetailsWidget: RailDetailsTimeline by bindView(R.id.rail_leg_details)
    val fareDescription: TextView by bindView(R.id.fare_description)
    val railCardName: TextView by bindView(R.id.rail_card_name)
    val overtakenMessage: android.widget.TextView by bindView(R.id.overtaken_message)
    val overtakenDivider: View by bindView(R.id.overtaken_message_divider)
    val legContainerClicked = PublishSubject.create<Unit>()

    init {
        View.inflate(getContext(), R.layout.rail_leg_summary, this)
    }

    fun reset() {
        railCardName.visibility = View.GONE
    }

    fun bindViewModel(vm: RailLegSummaryViewModel) {
        vm.operatorObservable.subscribeText(trainOperator)
        vm.formattedStopsAndDurationObservable.subscribeText(duration)
        vm.formattedTimesObservable.subscribeText(travelTimes)
        vm.fareDescriptionObservable.subscribe { text ->
            if(!text?.toString().isNullOrBlank()) {
                fareDescription.text = text
                fareDescription.contentDescription = Phrase.from(context, R.string.a11y_button_TEMPLATE)
                        .put("description", text)
                        .format().toString()
            }
            fareDescription.visibility = if(text?.toString().isNullOrBlank()) View.INVISIBLE else View.VISIBLE
        }
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

        vm.railCardNameObservable.subscribe { text ->
            if(!text?.toString().isNullOrBlank()) {
                railCardName.text = text
                railCardName.contentDescription = Phrase.from(context, R.string.rail_railcard_applied_cont_desc_TEMPLATE)
                        .put("railcards", text)
                        .format().toString()
            }
            railCardName.visibility = if(text?.toString().isNullOrBlank()) View.GONE else View.VISIBLE
        }

        vm.railSummaryContentDescription.subscribeContentDescription(legInfoContainer)

        fareDescription.subscribeOnClick(vm.showLegInfoObservable)
    }

    private fun expandLegDetails(overtaken: Boolean) {
        legDetailsWidget.visibility = Presenter.VISIBLE
        overtakenMessage.visibility = if (overtaken) View.VISIBLE else View.GONE
        overtakenDivider.visibility = if (overtaken) View.VISIBLE else View.GONE
        AnimUtils.rotate(legDetailsIcon)
        legDetailsIcon.contentDescription = context.getString(R.string.accessibility_cont_desc_role_button_collapse)
        RailTracking().trackRailTripOverviewDetailsExpand()
    }

    private fun collapseLegDetails() {
        legDetailsWidget.visibility = Presenter.GONE
        overtakenMessage.visibility = View.GONE
        overtakenDivider.visibility = View.GONE
        AnimUtils.reverseRotate(legDetailsIcon)
        legDetailsIcon.contentDescription = context.getString(R.string.accessibility_cont_desc_role_button_expand)
        legDetailsIcon.clearAnimation()
    }

    private fun isLegDetailsExpanded(): Boolean {
        return legDetailsWidget.visibility == Presenter.VISIBLE
    }
}