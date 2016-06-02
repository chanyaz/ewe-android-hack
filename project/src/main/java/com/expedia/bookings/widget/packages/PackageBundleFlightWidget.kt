package com.expedia.bookings.widget.packages

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FlightSegmentBreakdownView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeEnabled
import com.expedia.util.subscribeInverseVisibility
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeTextColor
import com.expedia.util.subscribeVisibility
import com.expedia.vm.packages.BundleFlightViewModel
import com.expedia.vm.FlightSegmentBreakdown
import com.expedia.vm.FlightSegmentBreakdownViewModel

abstract class PackageBundleFlightWidget(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {
    abstract fun showLoading()
    abstract fun handleResultsLoaded()
    abstract fun enable()
    abstract fun disable()
    abstract fun rowClicked()

    protected val opacity: Float = 0.25f

    val flightLoadingBar: ImageView by bindView(R.id.flight_loading_bar)
    val rowContainer: ViewGroup by bindView(R.id.row_container)
    val flightInfoContainer: ViewGroup by bindView(R.id.flight_info_container)
    val flightCardText: TextView by bindView(R.id.flight_card_view_text)
    val travelInfoText: TextView by bindView(R.id.travel_info_view_text)
    val flightIcon: ImageView by bindView(R.id.package_flight_icon)
    val flightDetailsIcon: ImageView by bindView(R.id.package_flight_details_icon)
    val forwardArrow: ImageView by bindView(R.id.flight_forward_arrow_icon)
    val flightDetailsContainer: ViewGroup by bindView(R.id.flight_details_container)
    val flightSegmentWidget: FlightSegmentBreakdownView by bindView(R.id.segment_breakdown)
    val totalDurationText: TextView by bindView(R.id.flight_total_duration)

    var viewModel: BundleFlightViewModel by notNullAndObservable { vm ->

        vm.flightTextObservable.subscribeText(flightCardText)
        vm.flightTextColorObservable.subscribeTextColor(flightCardText)
        vm.flightTravelInfoColorObservable.subscribeTextColor(travelInfoText)
        vm.travelInfoTextObservable.subscribeTextAndVisibility(travelInfoText)
        vm.flightDetailsIconObservable.subscribeVisibility(flightDetailsIcon)
        vm.showLoadingStateObservable.subscribeVisibility(flightLoadingBar)
        vm.showLoadingStateObservable.subscribeInverseVisibility(travelInfoText)
        vm.flightInfoContainerObservable.subscribeEnabled(flightInfoContainer)
        vm.totalDurationObserver.subscribeText(totalDurationText)
        vm.flightIconImageObservable.subscribe { pair: Pair<Int, Int> ->
            flightIcon.setImageResource(pair.first)
            flightIcon.setColorFilter(pair.second)
        }

        vm.showLoadingStateObservable.subscribe { showLoading ->
            if (showLoading) {
                rowContainer.isEnabled = false
                flightInfoContainer.isEnabled = false
                AnimUtils.progressForward(flightLoadingBar)
                flightCardText.setTextColor(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
            } else {
                rowContainer.isEnabled = true
                flightInfoContainer.isEnabled = true
                flightLoadingBar.clearAnimation()
                flightIcon.setColorFilter(Ui.obtainThemeColor(context, R.attr.primary_color))
                rowContainer.setOnClickListener {
                    rowClicked()
                }
            }
        }
        vm.flightSelectIconObservable.subscribe { showing ->
            if (showing) {
                forwardArrow.visibility = View.VISIBLE
            } else {
                forwardArrow.visibility = View.GONE
            }
        }

        vm.selectedFlightLegObservable.subscribe { selectedFlight ->
            var segmentBreakdowns = arrayListOf<FlightSegmentBreakdown>()
            for (segment in selectedFlight.flightSegments) {
                segmentBreakdowns.add(FlightSegmentBreakdown(segment, selectedFlight.hasLayover))
            }
            flightSegmentWidget.viewmodel.addSegmentRowsObserver.onNext(segmentBreakdowns)

            rowContainer.setOnClickListener {
                if (!isFlightSegmentDetailsExpanded()) {
                    expandFlightDetails()
                } else {
                    collapseFlightDetails()
                }
            }
        }
    }

    init {
        View.inflate(getContext(), R.layout.bundle_flight_widget, this)
        isEnabled = false
        flightSegmentWidget.viewmodel = FlightSegmentBreakdownViewModel(context)
    }

    private fun expandFlightDetails() {
        flightDetailsContainer.visibility = Presenter.VISIBLE
        AnimUtils.rotate(flightDetailsIcon)
        PackagesTracking().trackBundleOverviewFlightExpandClick()
    }

    private fun collapseFlightDetails() {
        flightDetailsContainer.visibility = Presenter.GONE
        AnimUtils.reverseRotate(flightDetailsIcon)
        flightDetailsIcon.clearAnimation()
    }

    private fun isFlightSegmentDetailsExpanded(): Boolean {
        return flightDetailsContainer.visibility == Presenter.VISIBLE
    }

    fun backButtonPressed() {
        if (isFlightSegmentDetailsExpanded()) {
            collapseFlightDetails()
        }
    }

    fun toggleFlightWidget(alpha: Float, isEnabled: Boolean) {
        travelInfoText.alpha = alpha
        flightCardText.alpha = alpha
        flightIcon.alpha = alpha
        flightDetailsIcon.alpha = alpha
        this.isEnabled = isEnabled
        flightDetailsIcon.isEnabled = isEnabled
        rowContainer.isEnabled = isEnabled
    }
}
