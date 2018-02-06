package com.expedia.bookings.hotel.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.hotel.util.HotelInfoManager
import com.expedia.bookings.hotel.util.HotelSearchManager
import com.expedia.bookings.hotel.util.HotelSearchParamsProvider
import com.expedia.bookings.presenter.hotel.HotelDetailPresenter
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.LoadingOverlayWidget
import com.expedia.vm.HotelMapViewModel
import com.expedia.vm.hotel.HotelDetailViewModel
import com.google.android.gms.maps.MapView
import javax.inject.Inject

class HotelDetailsActivity : AppCompatActivity() {
    private val container by bindView<FrameLayout>(R.id.hotel_detail_activity_container)
    private val presenter by bindView<HotelDetailPresenter>(R.id.hotel_details_presenter)
    private val loadingOverlay by bindView<LoadingOverlayWidget>(R.id.details_loading_overlay)
    private val mapView: MapView by bindView<MapView>(R.id.details_map_view)

    private lateinit var hotelDetailViewModel: HotelDetailViewModel

    lateinit var hotelSearchParamProvider: HotelSearchParamsProvider
        @Inject set

    lateinit var hotelSearchManager: HotelSearchManager
        @Inject set

    lateinit var hotelInfoManager: HotelInfoManager
        @Inject set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hotel_details_activity)
        Ui.getApplication(this).hotelComponent().inject(this)
        Ui.showTransparentStatusBar(this)
        val mapState = savedInstanceState?.getBundle(Constants.HOTELS_MAP_STATE)
        mapView.onCreate(mapState)

        initDetailViewModel()
        initPresenter()

        val intent = intent
        if (intent != null) {
            val hotelId = intent.getStringExtra(HotelExtras.EXTRA_HOTEL_SELECTED_ID)

            hotelSearchParamProvider.params?.let { params ->
                presenter.hotelDetailView.viewmodel.hotelSelectedObservable.onNext(Unit)
                hotelDetailViewModel.fetchOffers(params, hotelId)
            }
        } else {
            returnToSearch()
        }
    }

    override fun onStart() {
        mapView.onStart()
        super.onStart()
    }

    override fun onResume() {
        mapView.onResume()
        super.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
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

    private fun initDetailViewModel() {
        hotelDetailViewModel = HotelDetailViewModel(this, hotelInfoManager, hotelSearchManager)

        hotelDetailViewModel.fetchInProgressSubject.subscribe {
            loadingOverlay.visibility = View.VISIBLE
            loadingOverlay.animate(true)
        }

        hotelDetailViewModel.fetchCancelledSubject.subscribe {
            loadingOverlay.visibility = View.GONE
            returnToSearch()
        }

        hotelDetailViewModel.infositeApiErrorSubject.subscribe { error ->
            loadingOverlay.animate(false)
            loadingOverlay.visibility = View.GONE

            //todo https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/9180
        }

        hotelDetailViewModel.hotelOffersSubject.subscribe { response ->
            loadingOverlay.animate(false)
            loadingOverlay.visibility = View.GONE

            presenter.animationFinalize(true)
            presenter.showDefault()
            presenter.hotelDetailView.refresh()
            hotelDetailViewModel.addViewsAfterTransition()
            presenter.hotelMapView.viewmodel.offersObserver.onNext(response)

//            reviewsView.viewModel.resetTracking() todo https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/10380
        }

        hotelDetailViewModel.roomSelectedSubject.subscribe { offer ->
            //todo go to checkout https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/9176
        }
    }

    private fun initPresenter() {
        setUpMapView()

        presenter.hotelDetailView.viewmodel = hotelDetailViewModel
        hotelDetailViewModel.depositInfoContainerClickObservable.subscribe { pair: Pair<String, HotelOffersResponse.HotelRoomResponse> ->
            presenter.hotelDepositInfoObserver.onNext(pair)
        }
        hotelDetailViewModel.hotelRenovationObservable.subscribe(presenter.hotelRenovationObserver)
        hotelDetailViewModel.hotelPayLaterInfoObservable.subscribe { pair: Pair<String, List<HotelOffersResponse.HotelRoomResponse>> ->
            presenter.hotelPayLaterInfoObserver.onNext(pair)
        }

        hotelDetailViewModel.vipAccessInfoObservable.subscribe(presenter.hotelVIPAccessInfoObserver)
        hotelDetailViewModel.mapClickedSubject.subscribe(presenter.hotelDetailsEmbeddedMapClickObserver)
        presenter.hotelMapView.viewmodel = HotelMapViewModel(this, hotelDetailViewModel.scrollToRoom, hotelDetailViewModel.hotelSoldOut, hotelDetailViewModel.getLOB())
        hotelDetailViewModel.returnToSearchSubject.subscribe {
            returnToSearch()
        }

        hotelDetailViewModel.newDatesSelected.subscribe { dates ->
            //todo https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/10379
//            searchPresenter.searchViewModel.datesUpdated(dates.first, dates.second)
        }

        // todo https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/10380
//        hotelDetailViewModel.reviewsDataObservable.subscribe(reviewsOfferObserver)

        presenter
    }

    private fun setUpMapView() {
        container.removeView(mapView)
        mapView.visibility = View.VISIBLE
        val mapStub = presenter.hotelMapView.findViewById<FrameLayout>(R.id.stub_map)
        mapStub.addView(mapView)
        presenter.hotelMapView.mapView = mapView
        presenter.hotelMapView.mapView.getMapAsync(presenter.hotelMapView)
    }

    private fun returnToSearch() {
        val intent = Intent(this, HotelSearchActivity::class.java)
        startActivity(intent)
        finish()
    }
}
