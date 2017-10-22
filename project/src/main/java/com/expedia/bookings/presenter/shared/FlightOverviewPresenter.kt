package com.expedia.bookings.presenter.shared

import android.app.AlertDialog
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.FlightSegmentBreakdownView
import com.expedia.bookings.widget.BasicEconomyToolTipView
import com.expedia.util.subscribeVisibility
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeContentDescription
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeTextAndVisibilityInvisible
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.vm.AbstractFlightOverviewViewModel
import com.expedia.vm.FlightSegmentBreakdown
import com.expedia.vm.FlightSegmentBreakdownViewModel
import com.expedia.vm.flights.BasicEconomyTooltipViewModel
import rx.subjects.PublishSubject
import rx.Observable

class FlightOverviewPresenter(context: Context, attrs: AttributeSet?) : Presenter(context, attrs) {

    val bundlePriceTextView: TextView by bindView(R.id.bundle_price)
    val bundlePriceLabelTextView: TextView by bindView(R.id.bundle_price_label)
    val earnMessageTextView: TextView by bindView(R.id.earn_message)
    val selectFlightButton: Button by bindView(R.id.select_flight_button)
    val urgencyMessagingText: TextView by bindView(R.id.flight_overview_urgency_messaging)
    val basicEconomyTooltip: TextView by bindView(R.id.flight_basic_economy_tooltip)
    val totalDurationText: TextView by bindView(R.id.flight_total_duration)
    val flightSegmentWidget: FlightSegmentBreakdownView by bindView(R.id.segment_breakdown)
    val showBaggageFeesButton: Button by bindView(R.id.show_baggage_fees)
    val paymentFeesMayApplyTextView: Button by bindView(R.id.show_payment_fees)
    val airlineFeeWarningTextView: TextView by bindView(R.id.show_airline_fee_warning_text)
    val baggageFeeShowSubject = PublishSubject.create<String>()
    val showPaymentFeesObservable = PublishSubject.create<Boolean>()
    val e3EndpointUrl = Ui.getApplication(getContext()).appComponent().endpointProvider().e3EndpointUrl

    val basicEconomyToolTipInfoView = BasicEconomyToolTipView(context, null)
    val dialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setView(basicEconomyToolTipInfoView)
        builder.setPositiveButton(context.getString(R.string.DONE), { dialog, which -> dialog.dismiss() })
        builder.create()
    }

    init {
        View.inflate(getContext(), R.layout.widget_flight_overview, this)
        flightSegmentWidget.viewmodel = FlightSegmentBreakdownViewModel(context)
        basicEconomyToolTipInfoView.viewmodel = BasicEconomyTooltipViewModel()
        basicEconomyTooltip.text = HtmlCompat.fromHtml(context.getString(R.string.flight_details_basic_economy_message))
    }

    @Override
    override fun onFinishInflate() {
        super.onFinishInflate()
        val filter = PorterDuffColorFilter(ContextCompat.getColor(this.context,R.color.flight_overview_color_filter), PorterDuff.Mode.SRC_ATOP)
        showBaggageFeesButton.compoundDrawables[0].colorFilter = filter
        paymentFeesMayApplyTextView.compoundDrawables[0].colorFilter = filter
        basicEconomyTooltip.compoundDrawables[2].colorFilter = filter
    }

    var vm: AbstractFlightOverviewViewModel by notNullAndObservable {
        vm.chargesObFeesTextSubject.subscribeTextAndVisibility(paymentFeesMayApplyTextView)
        vm.airlineFeesWarningTextSubject.subscribeTextAndVisibility(airlineFeeWarningTextView)
        vm.bundlePriceSubject.subscribeText(bundlePriceTextView)
        vm.earnMessage.subscribeText(earnMessageTextView)
        vm.showEarnMessage.subscribeVisibility(earnMessageTextView)
        vm.showBundlePriceSubject.subscribeVisibility(bundlePriceLabelTextView)
        Observable.combineLatest(vm.showEarnMessage, vm.showBundlePriceSubject, {
            showEarnMessage, showBundlePrice ->
            showEarnMessage || showBundlePrice
        }).subscribeVisibility(bundlePriceTextView)
        vm.urgencyMessagingSubject.subscribeTextAndVisibilityInvisible(urgencyMessagingText)
        vm.showBasicEconomyTooltip.subscribeVisibility(basicEconomyTooltip)
        basicEconomyToolTipInfoView.viewmodel.basicEconomyTooltipTitle.subscribe { title ->
            dialog.setTitle(title)
        }
        vm.basicEconomyMessagingToolTipInfo.subscribe(basicEconomyToolTipInfoView.viewmodel.basicEconomyTooltipInfo)
        vm.totalDurationSubject.subscribeText(totalDurationText)
        vm.totalDurationContDescSubject.subscribeContentDescription(totalDurationText)
        vm.selectedFlightLegSubject.subscribe { selectedFlight ->
            val segmentbreakdowns = arrayListOf<FlightSegmentBreakdown>()
            val segmentSeatClassAndBookingCode = selectedFlight.packageOfferModel.segmentsSeatClassAndBookingCode
            for ((index, segment) in selectedFlight.flightSegments.withIndex()) {
                if (segmentSeatClassAndBookingCode != null) {
                    segment.seatClass = segmentSeatClassAndBookingCode[index].seatClass
                    segment.bookingCode = segmentSeatClassAndBookingCode[index].bookingCode
                }
                segmentbreakdowns.add(FlightSegmentBreakdown(segment, selectedFlight.hasLayover, vm.showSeatClassAndBookingCode.value))
            }
            showBaggageFeesButton.setOnClickListener {
                if(selectedFlight.baggageFeesUrl.contains("http")){
                    baggageFeeShowSubject.onNext(selectedFlight.baggageFeesUrl)
                }
                else {
                    baggageFeeShowSubject.onNext(e3EndpointUrl + selectedFlight.baggageFeesUrl)
                }
            }
            basicEconomyTooltip.setOnClickListener {
                dialog.show()
            }
            flightSegmentWidget.viewmodel.addSegmentRowsObserver.onNext(segmentbreakdowns)
            selectFlightButton.subscribeOnClick(vm.selectFlightClickObserver)
            selectFlight()
        }
    }

    private fun selectFlight() {
        vm.selectFlightClickObserver.onNext(Unit)
    }
}
