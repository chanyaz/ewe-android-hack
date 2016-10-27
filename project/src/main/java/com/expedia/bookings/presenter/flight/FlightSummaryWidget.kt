package com.expedia.bookings.presenter.flight

import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.packages.InboundFlightWidget
import com.expedia.bookings.widget.packages.OutboundFlightWidget
import com.expedia.vm.packages.BundleFlightViewModel

class FlightSummaryWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val title: TextView by bindView(R.id.title)
    val outboundFlightWidget: OutboundFlightWidget by bindView(R.id.package_bundle_outbound_flight_widget)
    val inboundFlightWidget: InboundFlightWidget by bindView(R.id.package_bundle_inbound_flight_widget)
    val freeCancellationLabelTextView: TextView by bindView(R.id.free_cancellation_text)
    val splitTicketBaggageFeesTextView: android.widget.TextView by bindView(R.id.split_ticket_baggage_fee_links)
    val splitTicketInfoContainer: View by bindView(R.id.split_ticket_info_container)
    val scrollSpaceView: View by bindView(R.id.scroll_space_flight)
    val airlineFeeWarningTextView: android.widget.TextView by bindView(R.id.airline_fee_warning_text)

    init {
        View.inflate(context, R.layout.flight_summary, this)
        orientation = VERTICAL
        outboundFlightWidget.viewModel = BundleFlightViewModel(context, LineOfBusiness.FLIGHTS_V2)
        inboundFlightWidget.viewModel = BundleFlightViewModel(context, LineOfBusiness.FLIGHTS_V2)
        outboundFlightWidget.flightIcon.setImageResource(R.drawable.packages_flight1_icon)
        inboundFlightWidget.flightIcon.setImageResource(R.drawable.packages_flight2_icon)
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