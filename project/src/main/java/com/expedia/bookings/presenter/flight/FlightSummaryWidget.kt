package com.expedia.bookings.presenter.flight

import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.packages.InboundFlightWidget
import com.expedia.bookings.widget.packages.OutboundFlightWidget
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.packages.BundleFlightViewModel
import com.expedia.vm.packages.FlightOverviewSummaryViewModel
import rx.subjects.PublishSubject

class FlightSummaryWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val outboundFlightTitle: TextView by bindView(R.id.outbound_flight_title)
    val inboundFlightTitle: TextView by bindView(R.id.inbound_flight_title)
    val outboundFlightWidget: OutboundFlightWidget by bindView(R.id.package_bundle_outbound_flight_widget)
    val inboundFlightWidget: InboundFlightWidget by bindView(R.id.package_bundle_inbound_flight_widget)
    val freeCancellationInfoTextView: TextView by bindView(R.id.free_cancellation_info)
    val freeCancellationMoreInfoIcon: android.widget.ImageView by bindView(R.id.free_cancellation_more_info_icon)
    val freeCancellationInfoContainer: LinearLayout by bindView(R.id.free_cancellation_layout)
    val splitTicketBaggageFeesTextView: android.widget.TextView by bindView(R.id.split_ticket_baggage_fee_links)
    val splitTicketInfoContainer: View by bindView(R.id.split_ticket_info_container)
    val scrollSpaceView: View by bindView(R.id.scroll_space_flight)
    val airlineFeeWarningTextView: android.widget.TextView by bindView(R.id.airline_fee_warning_text)
    val basicEconomyMessageTextView: View by bindView(R.id.basic_economy_info)
    val basicEconomyInfoClickedSubject: PublishSubject<Unit> = PublishSubject.create<Unit>()

    init {
        View.inflate(context, R.layout.flight_summary, this)
        orientation = VERTICAL
        outboundFlightWidget.viewModel = BundleFlightViewModel(context, LineOfBusiness.FLIGHTS_V2)
        inboundFlightWidget.viewModel = BundleFlightViewModel(context, LineOfBusiness.FLIGHTS_V2)
        outboundFlightWidget.flightIcon.setImageResource(R.drawable.packages_flight1_icon)
        inboundFlightWidget.flightIcon.setImageResource(R.drawable.packages_flight2_icon)
        outboundFlightWidget.showFlightCabinClass = true
        inboundFlightWidget.showFlightCabinClass = true
        basicEconomyMessageTextView.subscribeOnClick(basicEconomyInfoClickedSubject)
    }

    var viewmodel: FlightOverviewSummaryViewModel by notNullAndObservable { vm ->
        vm.outboundFlightTitle.subscribeText(outboundFlightTitle)
        vm.inboundFlightTitle.subscribeTextAndVisibility(inboundFlightTitle)
        vm.outboundBundleWidgetClassObservable.subscribe(outboundFlightWidget.viewModel.updateUpsellClassPreference)
        vm.inboundBundleWidgetClassObservable.subscribe(inboundFlightWidget.viewModel.updateUpsellClassPreference)
        freeCancellationInfoContainer.subscribeOnClick(vm.freeCancellationInfoClickSubject)
        vm.freeCancellationInfoSubject.subscribe {
            if (it){
                freeCancellationInfoTextView.visibility = View.VISIBLE
                AnimUtils.rotate(freeCancellationMoreInfoIcon)
            } else {
                freeCancellationInfoTextView.visibility = View.GONE
                AnimUtils.reverseRotate(freeCancellationMoreInfoIcon)
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        splitTicketBaggageFeesTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    fun collapseFlightWidgets() {
        inboundFlightWidget.backButtonPressed()
        outboundFlightWidget.backButtonPressed()
    }
}