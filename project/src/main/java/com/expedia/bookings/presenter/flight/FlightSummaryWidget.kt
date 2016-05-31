package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.packages.PackageInboundFlightWidget
import com.expedia.bookings.widget.packages.PackageOutboundFlightWidget
import com.expedia.vm.packages.BundleFlightViewModel

class FlightSummaryWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val title: TextView by bindView(R.id.title)
    val outboundFlightWidget: PackageOutboundFlightWidget by bindView(R.id.package_bundle_outbound_flight_widget)
    val inboundFlightWidget: PackageInboundFlightWidget by bindView(R.id.package_bundle_inbound_flight_widget)

    init {
        View.inflate(context, R.layout.flight_summary, this)
        orientation = VERTICAL
        outboundFlightWidget.viewModel = BundleFlightViewModel(context)
        inboundFlightWidget.viewModel = BundleFlightViewModel(context)
        outboundFlightWidget.flightIcon.setImageResource(R.drawable.packages_flight1_icon)
        inboundFlightWidget.flightIcon.setImageResource(R.drawable.packages_flight2_icon)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }
}