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
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.ui.HotelPackageActivity
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.BundleOverviewViewModel

public class BundleOverviewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val HOTEL_REQUEST_CODE = 101
    val FLIGHT_REQUEST_CODE = 102

    val bundleContainer: ViewGroup by bindView(R.id.bundle_container)
    val toolbar: Toolbar by bindView(R.id.toolbar)

    val selectHotelsButton: CardView by bindView(R.id.hotels_card_view)
    val selectDepartureButton: CardView by bindView(R.id.flight_departure_card_view)
    val selectArrivalButton: CardView by  bindView(R.id.flight_arrival_card_view)
    val hotelsText: TextView by bindView(R.id.hotels_card_view_text)
    val destinationText: TextView by bindView(R.id.flight_departure_card_view_text)
    val arrivalText: TextView by bindView(R.id.flight_arrival_card_view_text)

    var viewModel: BundleOverviewViewModel by notNullAndObservable { vm ->
        vm.hotelTextObservable.subscribeText(hotelsText)
        vm.destinationTextObservable.subscribeText(destinationText)
        vm.arrivalTextObservable.subscribeText(arrivalText)
    }

    init {
        View.inflate(context, R.layout.bundle_overview, this)
        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.packages_primary_color)
            val statusBar = Ui.setUpStatusBar(getContext(), toolbar, bundleContainer, color)
            addView(statusBar)
        }
        selectHotelsButton.setOnClickListener {
            openHotels()
        }
        selectDepartureButton.setOnClickListener {
            openFlights()
        }
        selectArrivalButton.setOnClickListener {
            openFlights()
        }
    }

    fun openHotels() {
        val intent = Intent(context, HotelPackageActivity::class.java)
        (context as AppCompatActivity).startActivityForResult(intent, HOTEL_REQUEST_CODE, null)
    }

    fun openFlights() {
        val intent = Intent(context, HotelPackageActivity::class.java)
        (context as AppCompatActivity).startActivityForResult(intent, FLIGHT_REQUEST_CODE, null)
    }




}