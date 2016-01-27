package com.expedia.bookings.widget

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.bindView
import com.expedia.ui.FlightPackageActivity
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeVisibility
import com.expedia.vm.BundleFlightViewModel

public class PackageBundleFlightWidget(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {
    val flightLoadingBar: ProgressBar by bindView(R.id.flight_loading_bar)
    val flightInfoContainer: ViewGroup by bindView(R.id.flight_info_container)
    val flightCardText: TextView by bindView(R.id.flight_card_view_text)
    val travelInfoText: TextView by bindView(R.id.travel_info_view_text)
    val flightIcon: ImageView by bindView(R.id.package_flight_icon)
    val flightArrowIcon: ImageView by bindView(R.id.package_flight_arrow_icon)

    var isOutbound = false

    var viewModel : BundleFlightViewModel by notNullAndObservable { vm ->
        vm.flightTextObservable.subscribeText(flightCardText)

        vm.showLoadingStateObservable.subscribe { showLoading ->
            if (showLoading) {
                flightInfoContainer.isEnabled = false
                flightLoadingBar.visibility = View.VISIBLE
            } else {
                flightInfoContainer.isEnabled = true
                flightLoadingBar.visibility = View.GONE
            }
        }

        vm.flightTextObservable.subscribeText(flightCardText)
        vm.travelInfoTextObservable.subscribeTextAndVisibility(travelInfoText)
        vm.flightArrowIconObservable.subscribeVisibility(flightArrowIcon)
    }

    init {
        View.inflate(getContext(), R.layout.bundle_flight_widget, this)
        flightInfoContainer.isEnabled = false
        flightInfoContainer.setOnClickListener {
            if (isOutbound) {
                openFlightsForDeparture()
            }
            else {
                openFlightsForArrival()
            }
        }
    }

    fun openFlightsForDeparture() {
        val intent = Intent(context, FlightPackageActivity::class.java)
        (context as AppCompatActivity).startActivityForResult(intent, Constants.PACKAGE_FLIGHT_DEPARTURE_REQUEST_CODE, null)
    }

    fun openFlightsForArrival() {
        val intent = Intent(context, FlightPackageActivity::class.java)
        (context as AppCompatActivity).startActivityForResult(intent, Constants.PACKAGE_FLIGHT_ARRIVAL_REQUEST_CODE, null)
    }
}
