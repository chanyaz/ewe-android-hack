package com.expedia.ui

import android.os.Bundle
import android.os.PersistableBundle
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.presenter.packages.PackageHotelPresenter
import com.expedia.bookings.utils.Ui
import com.google.android.gms.maps.MapView

class PackageHotelActivity : AbstractAppCompatActivity() {
    val hotelsPresenter: PackageHotelPresenter by lazy {
        findViewById(R.id.package_hotel_presenter) as PackageHotelPresenter
    }

    val resultsMapView: MapView by lazy {
        hotelsPresenter.findViewById(R.id.map_view) as MapView
    }

    val detailsMapView: MapView by lazy {
        hotelsPresenter.findViewById(R.id.details_map_view) as MapView
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.package_hotel_activity)
        Ui.showTransparentStatusBar(this)
        resultsMapView.onCreate(savedInstanceState)
        detailsMapView.onCreate(savedInstanceState)

        if (intent.hasExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS)) {
            hotelsPresenter.defaultTransitionObserver.onNext(Screen.DETAILS)
            hotelsPresenter.hotelSelectedObserver.onNext(Db.getPackageSelectedHotel())
        } else {
            hotelsPresenter.defaultTransitionObserver.onNext(Screen.RESULTS)
        }
    }

    override fun onPause() {
        resultsMapView.onPause()
        detailsMapView.onPause()
        super.onPause()
    }

    override fun onResume() {
        resultsMapView.onResume()
        detailsMapView.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        resultsMapView.onDestroy()
        detailsMapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        resultsMapView.onLowMemory()
        detailsMapView.onLowMemory()
        super.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        resultsMapView.onSaveInstanceState(outState)
        detailsMapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onBackPressed() {
        if (!hotelsPresenter.back()) {
            super.onBackPressed()
        }
    }

    enum class Screen {
        DETAILS,
        RESULTS
    }
}