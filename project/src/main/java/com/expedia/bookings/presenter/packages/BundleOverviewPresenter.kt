package com.expedia.bookings.presenter.packages

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.PackageBundleHotelWidget
import com.expedia.bookings.widget.TextView
import com.expedia.ui.FlightPackageActivity
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.BundleHotelViewModel
import com.expedia.vm.BundleOverviewViewModel

public class BundleOverviewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val bundleContainer: ViewGroup by bindView(R.id.bundle_container)
    val toolbar: Toolbar by bindView(R.id.toolbar)

    val bundleHotelWidget: PackageBundleHotelWidget by bindView(R.id.packageBundleWidget)
    val flightLoadingBar: ProgressBar by bindView(R.id.flight_loading_bar)
    val selectDepartureButton: CardView by bindView(R.id.flight_departure_card_view)
    val selectArrivalButton: CardView by  bindView(R.id.flight_arrival_card_view)
    val destinationText: TextView by bindView(R.id.flight_departure_card_view_text)
    val arrivalText: TextView by bindView(R.id.flight_arrival_card_view_text)

    var viewModel: BundleOverviewViewModel by notNullAndObservable { vm ->
        vm.hotelParamsObservable.subscribe {
            bundleHotelWidget.viewModel.showLoadingStateObservable.onNext(true)
        }
        vm.hotelResultsObservable.subscribe {
            bundleHotelWidget.viewModel.showLoadingStateObservable.onNext(false)
        }
        vm.flightParamsObservable.subscribe {
            selectDepartureButton.isEnabled = false
            selectArrivalButton.isEnabled = false
            flightLoadingBar.visibility = View.VISIBLE
        }
        vm.flightResultsObservable.subscribe {
            selectDepartureButton.isEnabled = true
            selectArrivalButton.isEnabled = true
            flightLoadingBar.visibility = View.GONE
        }
        vm.destinationTextObservable.subscribeText(destinationText)
        vm.arrivalTextObservable.subscribeText(arrivalText)
    }

    init {
        View.inflate(context, R.layout.bundle_overview, this)
        bundleHotelWidget.viewModel = BundleHotelViewModel(context)
        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.packages_primary_color)
            val statusBar = Ui.setUpStatusBar(getContext(), toolbar, bundleContainer, color)
            addView(statusBar)
        }
        selectDepartureButton.isEnabled = false
        selectArrivalButton.isEnabled = false
        selectDepartureButton.setOnClickListener {
            openFlightsForDeparture()
        }
        selectArrivalButton.setOnClickListener {
            openFlightsForArrival()
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

    override fun back(): Boolean {
        bundleHotelWidget.collapseSelectedHotel()
        return super.back()
    }

}