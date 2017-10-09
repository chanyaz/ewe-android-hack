package com.expedia.bookings.hotel.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.hotel.util.HotelSearchManager
import com.expedia.bookings.hotel.vm.HotelResultsViewModel
import com.expedia.bookings.presenter.hotel.HotelResultsPresenter
import com.expedia.bookings.tracking.hotel.ClientLogTracker
import com.expedia.bookings.tracking.hotel.HotelSearchTrackingDataBuilder
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.ClientLogConstants
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.google.android.gms.maps.MapView
import javax.inject.Inject
import android.view.ViewGroup
import com.expedia.bookings.hotel.deeplink.HotelExtras


class HotelResultsActivity : AppCompatActivity() {
    val presenter: HotelResultsPresenter by lazy {
        findViewById(R.id.hotel_results_presenter) as HotelResultsPresenter
    }
    val resultsMapView: MapView by lazy {
        findViewById(R.id.results_map_view) as MapView
    }

    lateinit var hotelSearchManager: HotelSearchManager
        @Inject set

    lateinit var searchTrackingBuilder: HotelSearchTrackingDataBuilder
        @Inject set

    lateinit var hotelClientLogTracker: ClientLogTracker
        @Inject set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hotel_results_activity)
        Ui.getApplication(this).hotelComponent().inject(this)
        Ui.showTransparentStatusBar(this)
        val mapState = savedInstanceState?.getBundle(Constants.HOTELS_MAP_STATE)
        resultsMapView.onCreate(mapState)

        initResultsPresenter()
        hotelSearchManager.searchParams?.let { params ->
            presenter.resetListOffset()
            presenter.viewModel.paramsSubject.onNext(params)
        }
    }

    private fun initResultsPresenter() {
        //todo fix.
        val vg = resultsMapView.getParent() as ViewGroup
        vg.removeView(resultsMapView)
        resultsMapView.visibility = View.VISIBLE
        presenter.mapView = resultsMapView

        presenter.mapView.getMapAsync(presenter)
        presenter.viewModel = HotelResultsViewModel(this, hotelSearchManager)

        presenter.viewModel.searchingForHotelsDateTime.subscribe {
            searchTrackingBuilder.markSearchApiCallMade()
        }
        presenter.viewModel.hotelResultsObservable.subscribe { hotelSearchResponse ->
            searchTrackingBuilder.markResultsProcessed()
            hotelSearchManager.searchParams?.let { params -> searchTrackingBuilder.searchParams(params) }
            searchTrackingBuilder.searchResponse(hotelSearchResponse)
        }
        presenter.viewModel.resultsReceivedDateTimeObservable.subscribe { dateTime ->
            searchTrackingBuilder.markApiResponseReceived()
        }
        presenter.adapter.allViewsLoadedTimeObservable.subscribe {
            searchTrackingBuilder.markResultsUsable()
            if (searchTrackingBuilder.isWorkComplete()) {
                val trackingData = searchTrackingBuilder.build()
                hotelClientLogTracker.trackResultsPerformance(trackingData.performanceData, ClientLogConstants.MATERIAL_HOTEL_SEARCH_PAGE,
                        eventName = ClientLogConstants.REGULAR_SEARCH_RESULTS)
                HotelTracking.trackHotelSearch(trackingData)
            }
        }
        presenter.hotelSelectedSubject.subscribe { hotel ->
            val intent = Intent(this, HotelDetailsActivity::class.java)
            intent.putExtra(HotelExtras.EXTRA_HOTEL_SELECTED_ID, hotel.hotelId)
            startActivity(intent)
        }

        // todo errors
//        presenter.viewModel.searchApiErrorObservable.subscribe(errorPresenter.viewmodel.searchApiErrorObserver)
//        presenter.viewModel.searchApiErrorObservable.subscribe { show(errorPresenter) }

        //todo ..............
//        presenter.viewModel.showHotelSearchViewObservable.subscribe { show(searchPresenter, Presenter.FLAG_CLEAR_TOP) }
        presenter.visibility = View.VISIBLE
        presenter.showDefault()
    }
}