package com.expedia.bookings.widget.packages

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.AccessibleCardView
import com.expedia.bookings.widget.FlightSegmentBreakdownView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeContentDescription
import com.expedia.util.subscribeEnabled
import com.expedia.util.subscribeInverseVisibility
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeTextColor
import com.expedia.util.subscribeVisibility
import com.expedia.vm.FlightSegmentBreakdown
import com.expedia.vm.FlightSegmentBreakdownViewModel
import com.expedia.vm.flights.FlightViewModel
import com.expedia.vm.packages.BundleFlightViewModel
import com.squareup.phrase.Phrase

abstract class BaseBundleFlightWidget(context: Context, attrs: AttributeSet?) : AccessibleCardView(context, attrs) {
    abstract fun showLoading()
    abstract fun handleResultsLoaded()
    abstract fun enable()
    abstract fun disable()
    abstract fun rowClicked()
    abstract fun isInboundFlight(): Boolean
    var showFlightCabinClass = false
    var showCollapseIcon: Boolean = false
    val isUserBucketedForRateDetailExpansionTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightRateDetailExpansion)

    protected val opacity: Float = 0.25f

    val flightLoadingBar: ImageView by bindView(R.id.flight_loading_bar)
    lateinit var rowContainer: ViewGroup
    val flightInfoContainer: ViewGroup by bindView(R.id.flight_info_container)
    val flightCardText: TextView by bindView(R.id.flight_card_view_text)
    val travelInfoText: TextView by bindView(R.id.travel_info_view_text)
    val flightIcon: ImageView by bindView(R.id.package_flight_icon)
    lateinit var flightDetailsIcon: ImageView
    var flightCollapseIcon: ImageView? = null

    val forwardArrow: ImageView by bindView(R.id.flight_forward_arrow_icon)
    val flightDetailsContainer: ViewGroup by bindView(R.id.flight_details_container)
    val flightSegmentWidget: FlightSegmentBreakdownView by bindView(R.id.segment_breakdown)
    val totalDurationText: TextView by bindView(R.id.flight_total_duration)

    var viewModel: BundleFlightViewModel by notNullAndObservable { vm ->
        vm.showRowContainerWithMoreInfo.subscribe {
            when (it) {
                true -> {
                    rowContainer = this.findViewById(R.id.detailed_row_container) as ViewGroup
                    rowContainer.visibility = VISIBLE
                    val flightCell = FlightCellWidget(context, 0, false)
                    rowContainer.addView(flightCell)
                    flightDetailsContainer.setOnClickListener {
                        if (isFlightSegmentDetailsExpanded()) {
                            collapseFlightDetails()
                        }
                    }
                    flightDetailsIcon = rowContainer.findViewById(R.id.flight_overview_expand_icon) as ImageView
                }
                false -> {
                    rowContainer = this.findViewById(R.id.row_container) as ViewGroup
                    rowContainer.visibility = VISIBLE
                    flightDetailsIcon = this.findViewById(R.id.package_flight_details_icon) as ImageView
                }
            }
            showCollapseIcon = it
        }
        vm.flightTextObservable.subscribeText(flightCardText)
        vm.flightTextColorObservable.subscribeTextColor(flightCardText)
        vm.flightTravelInfoColorObservable.subscribeTextColor(travelInfoText)
        vm.travelInfoTextObservable.subscribeTextAndVisibility(travelInfoText)
        vm.flightDetailsIconObservable.subscribeVisibility(flightDetailsIcon)
        vm.showLoadingStateObservable.subscribeVisibility(flightLoadingBar)
        vm.showLoadingStateObservable.subscribeInverseVisibility(travelInfoText)
        vm.flightInfoContainerObservable.subscribeEnabled(flightInfoContainer)
        vm.totalDurationObserver.subscribeText(totalDurationText)
        vm.totalDurationContDescObserver.subscribeContentDescription(totalDurationText)
        vm.flightIconImageObservable.subscribe { pair: Pair<Int, Int> ->
            flightIcon.setImageResource(pair.first)
            flightIcon.setColorFilter(pair.second)
        }

        vm.showLoadingStateObservable.subscribe { showLoading ->
            this.loadingStateObservable.onNext(showLoading)
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
            showCollapseIcon = vm.showRowContainerWithMoreInfo.value
            this.selectedCardObservable.onNext(Unit)
            var segmentBreakdowns = arrayListOf<FlightSegmentBreakdown>()
            for (segment in selectedFlight.flightSegments) {
                segmentBreakdowns.add(FlightSegmentBreakdown(segment, selectedFlight.hasLayover, showFlightCabinClass, showCollapseIcon))
                showCollapseIcon = false
            }
            flightSegmentWidget.viewmodel.addSegmentRowsObserver.onNext(segmentBreakdowns)

            rowContainer.setOnClickListener {
                if (!isFlightSegmentDetailsExpanded()) {
                    expandFlightDetails()
                } else {
                    collapseFlightDetails(true)
                }
                this.selectedCardObservable.onNext(Unit)
            }
            if (vm.showRowContainerWithMoreInfo.value) {
                (rowContainer.getChildAt(0) as FlightCellWidget).bind(FlightViewModel(context, selectedFlight))
                flightCollapseIcon = flightSegmentWidget.linearLayout.getChildAt(0).findViewById(R.id.flight_overview_collapse_icon) as ImageView
            }
            if (isUserBucketedForRateDetailExpansionTest) {
                expandFlightDetails(false)
            }
        }
    }

    fun cancel() {
        flightLoadingBar.clearAnimation()
        flightLoadingBar.visibility = View.GONE
        travelInfoText.visibility = View.VISIBLE
    }

    init {
        View.inflate(getContext(), R.layout.bundle_flight_widget, this)
        isEnabled = false
        flightSegmentWidget.viewmodel = FlightSegmentBreakdownViewModel(context)
    }

    fun expandFlightDetails(trackClick: Boolean = true) {
        viewModel.flightsRowExpanded.onNext(Unit)
        if (viewModel.showRowContainerWithMoreInfo.value && flightCollapseIcon != null) {
            rowContainer.visibility = Presenter.GONE
            AnimUtils.rotate(flightCollapseIcon)
        }
        flightDetailsContainer.visibility = Presenter.VISIBLE
        AnimUtils.rotate(flightDetailsIcon)
        if (trackClick) {
            trackBundleOverviewFlightExpandClick(true)
        }
    }

    fun collapseFlightDetails(trackClick: Boolean = false) {
        flightDetailsContainer.visibility = Presenter.GONE
        if (viewModel.showRowContainerWithMoreInfo.value && flightCollapseIcon != null) {
            rowContainer.visibility = Presenter.VISIBLE
            AnimUtils.reverseRotate(flightCollapseIcon)
        }
        AnimUtils.reverseRotate(flightDetailsIcon)
        if (trackClick) {
            trackBundleOverviewFlightExpandClick(false)
        }
    }

    fun isFlightSegmentDetailsExpanded(): Boolean {
        return flightDetailsContainer.visibility == Presenter.VISIBLE
    }

    fun backButtonPressed() {
        if (isUserBucketedForRateDetailExpansionTest && !isFlightSegmentDetailsExpanded()) {
            expandFlightDetails()
        } else if (isFlightSegmentDetailsExpanded()) {
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
        if (!isEnabled) {
            this.disabledStateObservable.onNext(Unit)
        }
    }

    fun getFlightWidgetExpandedState(): String {
        if (isFlightSegmentDetailsExpanded()) {
            return context.getString(R.string.accessibility_cont_desc_role_button_collapse)
        } else {
            return context.getString(R.string.accessibility_cont_desc_role_button_expand)
        }
    }

    override fun contentDescription(): String {
        val searchParams = viewModel.searchParams.value
        return Phrase.from(context, R.string.select_flight_cont_desc_TEMPLATE)
                .put("flight", StrUtils.formatAirportCodeCityName(if (isInboundFlight()) searchParams.origin else searchParams.destination))
                .put("date", DateUtils.localDateToMMMd(if (isInboundFlight()) searchParams.endDate else searchParams.startDate))
                .put("travelers", StrUtils.formatTravelerString(context, searchParams.guests))
                .format()
                .toString()
    }

    override fun disabledContentDescription(): String {
        val searchParams = viewModel.searchParams.value
        return Phrase.from(context, R.string.select_flight_disabled_cont_desc_TEMPLATE)
                .put("flight", StrUtils.formatAirportCodeCityName(if (isInboundFlight()) searchParams.origin else searchParams.destination))
                .put("date", DateUtils.localDateToMMMd(if (isInboundFlight()) searchParams.endDate else searchParams.startDate))
                .put("travelers", StrUtils.formatTravelerString(context, searchParams.guests))
                .put("previous", if (isInboundFlight()) context.getString(R.string.select_flight_disabled_choose_outbound) else context.getString(R.string.select_flight_disabled_choose_hotel))
                .format()
                .toString()
    }

    override fun getRowInfoContainer(): ViewGroup {
        return rowContainer
    }

    override fun loadingContentDescription(): String {
        val searchParams = viewModel.searchParams.value
        return Phrase.from(context, R.string.select_flight_searching_cont_desc_TEMPLATE)
                .put("flight", StrUtils.formatAirportCodeCityName(if (isInboundFlight()) searchParams.origin else searchParams.destination))
                .put("date", DateUtils.localDateToMMMd(if (isInboundFlight()) searchParams.endDate else searchParams.startDate))
                .put("travelers", StrUtils.formatTravelerString(context, searchParams.guests))
                .format()
                .toString()
    }

    override fun selectedCardContentDescription(): String {
        val searchParams = viewModel.searchParams.value
        val expandState = if (flightDetailsContainer.visibility == Presenter.VISIBLE) context.getString(R.string.accessibility_cont_desc_role_button_collapse) else context.getString(R.string.accessibility_cont_desc_role_button_expand)
        return Phrase.from(context, R.string.select_flight_selected_cont_desc_TEMPLATE)
                .put("flight", StrUtils.formatAirportCodeCityName(if (isInboundFlight()) searchParams.origin else searchParams.destination))
                .put("datetraveler", viewModel.travelInfoTextObservable.value)
                .put("expandstate", expandState)
                .format()
                .toString()
    }

    fun trackBundleOverviewFlightExpandClick(isExpanding: Boolean) {
        if (viewModel.lob == LineOfBusiness.PACKAGES) {
            PackagesTracking().trackBundleOverviewFlightExpandClick(isExpanding)
        } else if (viewModel.lob == LineOfBusiness.FLIGHTS_V2) {
            FlightsV2Tracking.trackOverviewFlightExpandClick(isExpanding)
        }
    }

}
