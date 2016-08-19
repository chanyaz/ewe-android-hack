package com.expedia.bookings.presenter.shared

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FlightSegmentBreakdownView
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeVisibility
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeTextAndVisibilityInvisible
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.vm.AbstractFlightOverviewViewModel
import com.expedia.vm.FlightSegmentBreakdown
import com.expedia.vm.FlightSegmentBreakdownViewModel
import rx.subjects.PublishSubject

class FlightOverviewPresenter(context: Context, attrs: AttributeSet?) : Presenter(context, attrs) {

    val bundlePriceTextView: TextView by bindView(R.id.bundle_price)
    val bundlePriceLabelTextView: TextView by bindView(R.id.bundle_price_label)
    val selectFlightButton: Button by bindView(R.id.select_flight_button)
    val urgencyMessagingText: TextView by bindView(R.id.flight_overview_urgency_messaging)
    val totalDurationText: TextView by bindView(R.id.flight_total_duration)
    val flightSegmentWidget: FlightSegmentBreakdownView by bindView(R.id.segment_breakdown)
    val showBaggageFeesButton: Button by bindView(R.id.show_baggage_fees)
    val paymentFeesMayApplyTextView: Button by bindView(R.id.show_payment_fees)
    val baggageFeeShowSubject = PublishSubject.create<String>()
    val showPaymentFeesObservable = PublishSubject.create<Unit>()
    val e3EndpointUrl = Ui.getApplication(getContext()).appComponent().endpointProvider().e3EndpointUrl

    init {
        View.inflate(getContext(), R.layout.widget_flight_overview, this)
        flightSegmentWidget.viewmodel = FlightSegmentBreakdownViewModel(context)
    }

    @Override
    override fun onFinishInflate() {
        super.onFinishInflate()
        val filter = PorterDuffColorFilter(ContextCompat.getColor(this.context,R.color.flight_overview_color_filter), PorterDuff.Mode.SRC_ATOP);
        showBaggageFeesButton.compoundDrawables[0].colorFilter = filter
        paymentFeesMayApplyTextView.compoundDrawables[0].colorFilter = filter
    }

    var vm: AbstractFlightOverviewViewModel by notNullAndObservable {
        vm.chargesObFeesTextSubject.subscribeTextAndVisibility(paymentFeesMayApplyTextView)
        vm.bundlePriceSubject.subscribeText(bundlePriceTextView)
        vm.showBundlePriceSubject.subscribeVisibility(bundlePriceLabelTextView)
        vm.showBundlePriceSubject.subscribeVisibility(bundlePriceTextView)
        vm.urgencyMessagingSubject.subscribeTextAndVisibilityInvisible(urgencyMessagingText)
        vm.totalDurationSubject.subscribeText(totalDurationText)
        vm.selectedFlightLegSubject.subscribe { selectedFlight ->
            val segmentbreakdowns = arrayListOf<FlightSegmentBreakdown>()
            for (segment in selectedFlight.flightSegments) {
                segmentbreakdowns.add(FlightSegmentBreakdown(segment, selectedFlight.hasLayover))
            }
            showBaggageFeesButton.setOnClickListener {
                if(selectedFlight.baggageFeesUrl.contains("http")){
                    baggageFeeShowSubject.onNext(selectedFlight.baggageFeesUrl)
                }
                else {
                    baggageFeeShowSubject.onNext(e3EndpointUrl + selectedFlight.baggageFeesUrl)
                }
            }
            paymentFeesMayApplyTextView.setOnClickListener {
                showPaymentFeesObservable.onNext(Unit)
            }
            flightSegmentWidget.viewmodel.addSegmentRowsObserver.onNext(segmentbreakdowns)
            selectFlightButton.subscribeOnClick(vm.selectFlightClickObserver)
        }
    }
}
