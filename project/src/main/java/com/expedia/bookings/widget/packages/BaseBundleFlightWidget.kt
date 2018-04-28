package com.expedia.bookings.widget.packages

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.extensions.setFocusForView
import com.expedia.bookings.extensions.subscribeContentDescription
import com.expedia.bookings.extensions.subscribeEnabled
import com.expedia.bookings.extensions.subscribeInverseVisibility
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.extensions.subscribeTextColor
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.packages.vm.BundleFlightViewModel
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isRichContentShowRouteScoreEnabled
import com.expedia.bookings.widget.AccessibleCardView
import com.expedia.bookings.widget.FlightSegmentBreakdownView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.FlightSegmentBreakdown
import com.expedia.vm.FlightSegmentBreakdownViewModel
import com.expedia.vm.flights.BaggageInfoView
import com.expedia.vm.flights.BaggageInfoViewModel
import com.squareup.phrase.Phrase

abstract class BaseBundleFlightWidget(context: Context, attrs: AttributeSet?) : AccessibleCardView(context, attrs) {
    abstract fun showLoading()
    abstract fun handleResultsLoaded()
    abstract fun enable()
    abstract fun disable()
    abstract fun rowClicked()
    abstract fun isInboundFlight(): Boolean
    var showFlightCabinClass = false

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
    val urgencyMessageText: TextView by bindView(R.id.urgency_message_cont)
    val flightMessageContainer: View by bindView(R.id.flight_message_container)
    val routeScoreText: TextView by bindView(R.id.textView_route_score)
    val totalDurationText: TextView by bindView(R.id.flight_total_duration)

    val baggagePaymentDivider: View by bindView(R.id.baggage_payment_divider)
    val baggageFeesButton: View by bindView(R.id.show_baggage_fees_button)
    lateinit var baggageInfoView: BaggageInfoView

    var viewModel: BundleFlightViewModel by notNullAndObservable { vm ->
        vm.flightTextObservable.subscribeText(flightCardText)
        vm.flightTextColorObservable.subscribeTextColor(flightCardText)
        vm.flightTravelInfoColorObservable.subscribeTextColor(travelInfoText)
        vm.travelInfoTextObservable.subscribeTextAndVisibility(travelInfoText)
        vm.flightDetailsIconObservable.subscribe {
            flightDetailsIcon.clearAnimation()
            flightDetailsIcon.visibility = if (it) View.VISIBLE else View.GONE
        }

        vm.showLoadingStateObservable.subscribeVisibility(flightLoadingBar)
        vm.showLoadingStateObservable.subscribeInverseVisibility(travelInfoText)
        vm.flightInfoContainerObservable.subscribeEnabled(flightInfoContainer)
        vm.totalDurationObserver.subscribeText(totalDurationText)
        vm.urgencyMessageObservable.subscribeTextAndVisibility(urgencyMessageText)
        vm.totalDurationContDescObserver.subscribeContentDescription(totalDurationText)
        vm.flightIconImageObservable.subscribe { pair: Pair<Int, Int> ->
            flightIcon.setImageResource(pair.first)
            flightIcon.setColorFilter(pair.second)
        }

        vm.showBaggageInfoLinkObservable.subscribeVisibility(baggagePaymentDivider)
        vm.showBaggageInfoLinkObservable.subscribeVisibility(baggageFeesButton)
        baggageFeesButton.subscribeOnClick(vm.baggageInfoClickSubject)

        vm.showLoadingStateObservable.subscribe { showLoading ->
            this.loadingStateObservable.onNext(showLoading)
            if (showLoading) {
                postDelayed({ flightCardText.setFocusForView() }, 600L)
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
            val segmentBreakdowns = arrayListOf<FlightSegmentBreakdown>()
            for (segment in selectedFlight.flightSegments) {
                segmentBreakdowns.add(FlightSegmentBreakdown(segment, selectedFlight.hasLayover, showFlightCabinClass))
            }
            flightSegmentWidget.viewmodel.addSegmentRowsObserver.onNext(segmentBreakdowns)

            rowContainer.setOnClickListener {
                if (!isFlightSegmentDetailsExpanded()) {
                    expandFlightDetails()
                } else {
                    collapseFlightDetails(true)
                }
            }
            this.selectedCardObservable.onNext(Unit)
        }

        if (viewModel.lob == LineOfBusiness.FLIGHTS_V2) {
            baggageInfoView = BaggageInfoView(context)
            baggageInfoView.baggageInfoViewModel = BaggageInfoViewModel(context)
            baggageInfoView.baggageInfoViewModel.showBaggageInfoWebViewSubject.subscribe {
                viewModel.openBaggageFeeWebView()
            }
            vm.showBaggageInfoSubject.subscribe { flight ->
                baggageInfoView.baggageInfoViewModel.showLoaderSubject.onNext(true)
                baggageInfoView.getBaggageInfo(flight)
            }
        }

        if (isRichContentShowRouteScoreEnabled()) {
            vm.flightMessageContainerStream.subscribeVisibility(flightMessageContainer)
            vm.routeScoreStream.subscribeTextAndVisibility(routeScoreText)
            vm.isFareFamilyUpgraded.subscribe(flightSegmentWidget.viewmodel.isFareFamilyUpgraded)
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
        flightDetailsContainer.visibility = Presenter.VISIBLE
        if (flightDetailsIcon.visibility == View.VISIBLE) {
            AnimUtils.rotate(flightDetailsIcon)
        }
        if (trackClick) {
            trackBundleOverviewFlightExpandClick(true)
        }
        this.selectedCardObservable.onNext(Unit)
    }

    fun collapseFlightDetails(trackClick: Boolean = false) {
        flightDetailsContainer.visibility = Presenter.GONE
        if (flightDetailsIcon.visibility == View.VISIBLE) {
            AnimUtils.reverseRotate(flightDetailsIcon)
        }
        if (trackClick) {
            trackBundleOverviewFlightExpandClick(false)
        }
        this.selectedCardObservable.onNext(Unit)
    }

    fun isFlightSegmentDetailsExpanded(): Boolean {
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
                .put("date", LocaleBasedDateFormatUtils.localDateToMMMd(if (isInboundFlight()) searchParams.endDate!! else searchParams.startDate))
                .put("travelers", StrUtils.formatTravelerString(context, searchParams.guests))
                .format()
                .toString()
    }

    override fun disabledContentDescription(): String {
        val searchParams = viewModel.searchParams.value
        return Phrase.from(context, R.string.select_flight_disabled_cont_desc_TEMPLATE)
                .put("flight", StrUtils.formatAirportCodeCityName(if (isInboundFlight()) searchParams.origin else searchParams.destination))
                .put("date", LocaleBasedDateFormatUtils.localDateToMMMd(if (isInboundFlight()) searchParams.endDate!! else searchParams.startDate))
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
                .put("date", LocaleBasedDateFormatUtils.localDateToMMMd(if (isInboundFlight()) searchParams.endDate!! else searchParams.startDate))
                .put("travelers", StrUtils.formatTravelerString(context, searchParams.guests))
                .format()
                .toString()
    }

    override fun selectedCardContentDescription(): String {
        val searchParams = viewModel.searchParams.value
        val travelInfoText = viewModel.travelInfoTextObservable.value
        if (searchParams != null && travelInfoText != null) {
            val expandState = if (flightDetailsContainer.visibility == Presenter.VISIBLE) context.getString(R.string.accessibility_cont_desc_role_button_collapse) else context.getString(R.string.accessibility_cont_desc_role_button_expand)
            return Phrase.from(context, R.string.select_flight_selected_cont_desc_TEMPLATE)
                    .put("flight", StrUtils.formatAirportCodeCityName(if (isInboundFlight()) searchParams.origin else searchParams.destination))
                    .put("datetraveler", travelInfoText)
                    .put("expandstate", expandState)
                    .format()
                    .toString()
        } else return ""
    }

    fun trackBundleOverviewFlightExpandClick(isExpanding: Boolean) {
        if (viewModel.lob == LineOfBusiness.PACKAGES) {
            PackagesTracking().trackBundleOverviewFlightExpandClick(isExpanding)
        } else if (viewModel.lob == LineOfBusiness.FLIGHTS_V2) {
            FlightsV2Tracking.trackOverviewFlightExpandClick(isExpanding)
        }
    }
}
