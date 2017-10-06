package com.expedia.bookings.hotel.activity

import android.os.Bundle
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.hotel.presenter.HotelPostSearchPresenter
import com.expedia.bookings.hotel.util.HotelSearchManager
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.ui.AbstractAppCompatActivity
import com.google.android.gms.maps.MapView
import javax.inject.Inject

class HotelPostSearchActivity : AbstractAppCompatActivity() {
    val hotelPresenter: HotelPostSearchPresenter by lazy {
        findViewById(R.id.hotel_presenter) as HotelPostSearchPresenter
    }

    val resultsMapView: MapView by lazy {
        hotelPresenter.findViewById<MapView>(R.id.map_view)
    }

    val detailsMapView: MapView by lazy {
        hotelPresenter.findViewById<MapView>(R.id.details_map_view)
    }

    lateinit var hotelSearchManager: HotelSearchManager
        @Inject set


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hotel_post_search_activity)
        Ui.getApplication(this).hotelComponent().inject(this)
        hotelSearchManager.searchParams?.let { params ->
            hotelPresenter.updateSearchParams(params)
            hotelPresenter.show()
        }

        Ui.showTransparentStatusBar(this)
        val mapState = savedInstanceState?.getBundle(Constants.HOTELS_MAP_STATE)
        resultsMapView.onCreate(mapState)
        detailsMapView.onCreate(mapState)
    }

    override fun onResume() {
        resultsMapView.onResume()
        detailsMapView.onResume()
        super.onResume()
    }

    override fun onPause() {
        resultsMapView.onPause()
        detailsMapView.onPause()
        super.onPause()

        if (isFinishing) {
            clearCCNumber()
            clearStoredCard()
        }
        else {
            Ui.hideKeyboard(this)
        }
    }

    override fun onDestroy() {
        resultsMapView.onDestroy()
        detailsMapView.onDestroy()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (!hotelPresenter.back()) {
            Db.setTemporarilySavedCard(null)
            super.onBackPressed()
        }
    }
}