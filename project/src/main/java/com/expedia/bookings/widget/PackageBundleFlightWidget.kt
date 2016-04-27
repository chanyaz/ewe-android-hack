package com.expedia.bookings.widget

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.ui.PackageFlightActivity
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

class PackageBundleFlightWidget(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {
    val flightLoadingBar: ImageView by bindView(R.id.flight_loading_bar)
    val flightInfoContainer: ViewGroup by bindView(R.id.flight_info_container)
    val flightCardText: TextView by bindView(R.id.flight_card_view_text)
    val travelInfoText: TextView by bindView(R.id.travel_info_view_text)
    val flightIcon: ImageView by bindView(R.id.package_flight_icon)
    val flightDetailsIcon: ImageView by bindView(R.id.package_flight_details_icon)
    val flightSelectIcon: ImageView by bindView(R.id.package_flight_select_icon)
    val flightDetailsContainer: ViewGroup by bindView(R.id.flight_details_container)
    val flightSegmentWidget: FlightSegmentBreakdownView by bindView(R.id.segment_breakdown)
    val totalDurationText: TextView by bindView(R.id.flight_total_duration)

    var isOutbound = false

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
                flightInfoContainer.isEnabled = false
                AnimUtils.progressForward(flightLoadingBar)
                flightCardText.setTextColor(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
            } else {
                flightInfoContainer.isEnabled = true
                flightLoadingBar.clearAnimation()
                flightIcon.setColorFilter(Ui.obtainThemeColor(context, R.attr.primary_color))
                setOnClickListener {
                    if (isOutbound) {
                        openFlightsForDeparture()
                    } else {
                        openFlightsForArrival()
                    }
                }
            }
        }
        vm.flightSelectIconObservable.subscribe { showing ->
            if (showing) {
                flightSelectIcon.visibility = View.VISIBLE
                AnimUtils.getFadeInRotateAnim(flightSelectIcon).start()
            } else {
                flightSelectIcon.clearAnimation()
                flightSelectIcon.visibility = View.GONE
            }
        }

        vm.selectedFlightLegObservable.subscribe { selectedFlight ->
            var segmentBreakdowns = arrayListOf<FlightSegmentBreakdown>()
            for (segment in selectedFlight.flightSegments) {
                segmentBreakdowns.add(FlightSegmentBreakdown(segment, selectedFlight.hasLayover))
            }
            flightSegmentWidget.viewmodel.addSegmentRowsObserver.onNext(segmentBreakdowns)
        }
    }

    init {
        View.inflate(getContext(), R.layout.bundle_flight_widget, this)
        isEnabled = false
        flightDetailsIcon.setOnClickListener {
            if (flightDetailsContainer.visibility == Presenter.GONE) {
                expandFlightDetails()
            } else {
                collapseFlightDetails()
            }
        }

        flightSegmentWidget.viewmodel = FlightSegmentBreakdownViewModel(context)
    }

    fun openFlightsForDeparture() {
        val intent = Intent(context, PackageFlightActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        (context as AppCompatActivity).startActivityForResult(intent, Constants.PACKAGE_FLIGHT_OUTBOUND_REQUEST_CODE, null)
    }

    fun openFlightsForArrival() {
        val intent = Intent(context, PackageFlightActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        (context as AppCompatActivity).startActivityForResult(intent, Constants.PACKAGE_FLIGHT_RETURN_REQUEST_CODE, null)
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
    }
}
