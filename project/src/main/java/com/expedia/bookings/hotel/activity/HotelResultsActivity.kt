package com.expedia.bookings.hotel.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.hotel.util.HotelSearchManager
import com.expedia.bookings.hotel.util.HotelSearchParamsProvider
import com.expedia.bookings.hotel.vm.HotelResultsViewModel
import com.expedia.bookings.presenter.hotel.HotelResultsPresenter
import com.expedia.bookings.tracking.hotel.ClientLogTracker
import com.expedia.bookings.tracking.hotel.HotelSearchTrackingDataBuilder
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.ClientLogConstants
import com.expedia.bookings.utils.Constants.HOTELS_MAP_STATE
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.google.android.gms.maps.MapView
import javax.inject.Inject

class HotelResultsActivity : AppCompatActivity() {
    private val presenter by bindView<HotelResultsPresenter>(R.id.hotel_results_presenter)
    private val mapView: MapView by bindView<MapView>(R.id.map_view)

    lateinit var hotelSearchParamProvider: HotelSearchParamsProvider
        @Inject set

    lateinit var hotelSearchManager: HotelSearchManager
        @Inject set

    lateinit var searchTrackingBuilder: HotelSearchTrackingDataBuilder
        @Inject set

    lateinit var hotelClientLogTracker: ClientLogTracker
        @Inject set

    private lateinit var viewModel: HotelResultsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hotel_results_activity)
        Ui.getApplication(this).hotelComponent().inject(this)
        Ui.showTransparentStatusBar(this)
        val mapState = savedInstanceState?.getBundle(HOTELS_MAP_STATE)
        mapView.onCreate(mapState)

        initResultsPresenter()
    }

    override fun onStart() {
        mapView.onStart()
        super.onStart()
    }

    override fun onResume() {
        mapView.onResume()
        super.onResume()

        hotelSearchParamProvider.params?.let { params ->
            viewModel.paramsSubject.onNext(params)
        }
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        viewModel.clearSubscriptions()
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (presenter.back()) {
            return
        }
        super.onBackPressed()
    }

    override fun onLowMemory() {
        mapView.onLowMemory()
        super.onLowMemory()
    }

    private fun initResultsPresenter() {
        setUpMapView()
        viewModel = HotelResultsViewModel(this, hotelSearchManager)
        presenter.viewModel = viewModel

        setUpViewModelSubscriptions()
        subscribePresenterEvents()
        displayResultsPresenter()
    }

    private fun setUpMapView() {
        val viewGroup = mapView.parent as ViewGroup
        viewGroup.removeView(mapView)
        mapView.visibility = View.VISIBLE
        presenter.mapWidget.setMapView(mapView)
    }

    private fun setUpViewModelSubscriptions() {
        viewModel.searchRequestedObservable.subscribe {
            searchTrackingBuilder.markSearchApiCallMade()
        }
        viewModel.hotelResultsObservable.subscribe { hotelSearchResponse ->
            searchTrackingBuilder.markResultsProcessed()
            hotelSearchParamProvider.params?.let { params -> searchTrackingBuilder.searchParams(params) }
            searchTrackingBuilder.searchResponse(hotelSearchResponse)
        }
        viewModel.resultsReceivedObservable.subscribe {
            searchTrackingBuilder.markApiResponseReceived()
        }

        viewModel.searchApiErrorObservable.subscribe { apiError ->
            // todo https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/9179
        }

        viewModel.showHotelSearchViewObservable.subscribe {
            val intent = Intent(this, HotelSearchActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun subscribePresenterEvents() {
        presenter.adapter.allViewsLoadedTimeObservable.subscribe {
            searchTrackingBuilder.markResultsUsable()
            if (searchTrackingBuilder.isWorkComplete()) {
                val trackingData = searchTrackingBuilder.build()
                hotelClientLogTracker.trackResultsPerformance(trackingData.performanceData, ClientLogConstants.MATERIAL_HOTEL_SEARCH_PAGE,
                        eventName = ClientLogConstants.REGULAR_SEARCH_RESULTS)

                HotelTracking.trackHotelSearch(trackingData, viewModel.getSearchParams()!!)
            }
        }
        presenter.hotelSelectedSubject.subscribe { hotel ->
            //todo https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/9175
        }
    }

    private fun displayResultsPresenter() {
        presenter.visibility = View.VISIBLE
        presenter.showDefault()
        presenter.animationFinalize(enableLocation = true)
    }
}
