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
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.ui.FlightPackageActivity
import com.expedia.util.*
import com.expedia.vm.BundleFlightViewModel

public class PackageBundleFlightWidget(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {
    val flightLoadingBar: ImageView by bindView(R.id.flight_loading_bar)
    val flightInfoContainer: ViewGroup by bindView(R.id.flight_info_container)
    val flightCardText: TextView by bindView(R.id.flight_card_view_text)
    val travelInfoText: TextView by bindView(R.id.travel_info_view_text)
    val flightIcon: ImageView by bindView(R.id.package_flight_icon)
    val flightDetailsIcon: ImageView by bindView(R.id.package_flight_details_icon)
    val flightSelectIcon: ImageView by bindView(R.id.package_flight_select_icon)

    var isOutbound = false

    var viewModel: BundleFlightViewModel by notNullAndObservable { vm ->

        vm.flightTextObservable.subscribeText(flightCardText)
        vm.flightTextColorObservable.subscribeTextColor(flightCardText)
        vm.flightTextColorObservable.subscribeTextColor(travelInfoText)
        vm.travelInfoTextObservable.subscribeTextAndVisibility(travelInfoText)
        vm.flightDetailsIconObservable.subscribeVisibility(flightDetailsIcon)
        vm.showLoadingStateObservable.subscribeVisibility(flightLoadingBar)
        vm.showLoadingStateObservable.subscribeInverseVisibility(travelInfoText)
        vm.flightInfoContainerObservable.subscribeEnabled(flightInfoContainer)
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
    }

    init {
        View.inflate(getContext(), R.layout.bundle_flight_widget, this)
        flightInfoContainer.isEnabled = false
        flightInfoContainer.setOnClickListener {
            if (isOutbound) {
                openFlightsForDeparture()
            } else {
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
