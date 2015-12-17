package com.expedia.ui

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.presenter.hotel.HotelPresenter
import com.expedia.bookings.utils.Ui
import com.google.android.gms.maps.MapView
import kotlin.properties.Delegates

public class HotelActivity : AppCompatActivity() {

    val hotelPresenter: HotelPresenter by Delegates.lazy {
        findViewById(R.id.hotel_presenter) as HotelPresenter
    }

    val mapView: MapView by Delegates.lazy {
        hotelPresenter.findViewById(R.id.widget_hotel_results).findViewById(R.id.map_view) as MapView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultHotelComponents()
        setContentView(R.layout.activity_hotel)
        Ui.showTransparentStatusBar(this)
        mapView.onCreate(savedInstanceState)
    }

    override fun onBackPressed() {
        if (!hotelPresenter.back()) {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onResume() {
        mapView.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        mapView.onLowMemory()
        super.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        mapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState, outPersistentState)
    }

}

