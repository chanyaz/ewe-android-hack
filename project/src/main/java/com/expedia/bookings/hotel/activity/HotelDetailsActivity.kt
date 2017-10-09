package com.expedia.bookings.hotel.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.hotel.util.HotelInfoManager
import com.expedia.bookings.hotel.util.HotelSearchManager
import com.expedia.bookings.presenter.hotel.HotelDetailPresenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.LoadingOverlayWidget
import com.expedia.vm.HotelMapViewModel
import com.expedia.vm.HotelWebCheckoutViewViewModel
import com.expedia.vm.hotel.HotelDetailViewModel
import com.google.android.gms.maps.MapView
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

class HotelDetailsActivity : AppCompatActivity() {
    val presenter: HotelDetailPresenter by lazy {
        findViewById(R.id.hotel_detail_presenter) as HotelDetailPresenter
    }

    val detailsMapView: MapView by lazy {
        findViewById(R.id.details_map_view) as MapView
    }

    val loadingOverlay: LoadingOverlayWidget by lazy {
        findViewById(R.id.details_loading_overlay) as LoadingOverlayWidget
    }

    lateinit var hotelSearchManager: HotelSearchManager
        @Inject set

    lateinit var hotelInfoManager: HotelInfoManager
        @Inject set

    private lateinit var hotelDetailViewModel: HotelDetailViewModel

    private var subscriptions = CompositeSubscription()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hotel_details_activity)
        Ui.getApplication(this).hotelComponent().inject(this)
        Ui.showTransparentStatusBar(this)

        hotelDetailViewModel = HotelDetailViewModel(this, hotelInfoManager)
        initDetailsPresenter()
        initViewModel()

        val intent = intent;
        if (intent != null) {
            val hotelId = intent.getStringExtra(HotelExtras.EXTRA_HOTEL_SELECTED_ID)

            hotelSearchManager.searchParams?.let { params ->
                presenter.hotelDetailView.viewmodel.hotelSelectedObservable.onNext(Unit)
                hotelDetailViewModel.fetchOffers(params, hotelId)
            }
        } else {
            //todo run away
        }
    }

    override fun onDestroy() {
        subscriptions.clear()
        hotelDetailViewModel.clearSubscriptions()
        super.onDestroy()
    }
    /*
            val detailsStub = presenter.hotelMapView.findViewById<FrameLayout>(R.id.stub_map)
        detailsMapView.visibility = View.VISIBLE
        removeView(detailsMapView)
        detailsStub.addView(detailsMapView)

        presenter.hotelMapView.mapView = detailsMapView
        presenter.hotelMapView.mapView.getMapAsync(presenter.hotelMapView)
        presenter.hotelDetailView.viewmodel = hotelDetailViewModel
        presenter.hotelDetailView.viewmodel.depositInfoContainerClickObservable.subscribe { pair: Pair<String, HotelOffersResponse.HotelRoomResponse> ->
            presenter.hotelDepositInfoObserver.onNext(pair)
        }
        presenter.hotelDetailView.viewmodel.reviewsClickedWithHotelData.subscribe(reviewsObserver)
        presenter.hotelDetailView.viewmodel.hotelRenovationObservable.subscribe(presenter.hotelRenovationObserver)
        presenter.hotelDetailView.viewmodel.hotelPayLaterInfoObservable.subscribe { pair: Pair<String, List<HotelOffersResponse.HotelRoomResponse>> ->
            presenter.hotelPayLaterInfoObserver.onNext(pair)
        }

        presenter.hotelDetailView.viewmodel.vipAccessInfoObservable.subscribe(presenter.hotelVIPAccessInfoObserver)
        presenter.hotelDetailView.viewmodel.mapClickedSubject.subscribe(presenter.hotelDetailsEmbeddedMapClickObserver)
        presenter.hotelMapView.viewmodel = HotelMapViewModel(context, presenter.hotelDetailView.viewmodel.scrollToRoom, presenter.hotelDetailView.viewmodel.hotelSoldOut, presenter.hotelDetailView.viewmodel.getLOB())

//todo make less shitty
//        presenter.hotelDetailView.viewmodel.changeDates.subscribe(goToSearchScreen)

        if (shouldUseWebCheckout()) {
            viewModel = HotelPresenterViewModel((webCheckoutView.viewModel as HotelWebCheckoutViewViewModel).createTripViewModel, null, presenter.hotelDetailView.viewmodel)
        } else {
            viewModel = HotelPresenterViewModel(checkoutPresenter.hotelCheckoutWidget.createTripViewmodel, checkoutPresenter.hotelCheckoutViewModel, presenter.hotelDetailView.viewmodel)
        }
        viewModel.selectedRoomSoldOut.subscribe(presenter.hotelDetailView.viewmodel.selectedRoomSoldOut)
        //ResultsPresenter doesn't inflate with roboelectric due to missing shadows for google map


//        lol wuuuuut
//        if (!ExpediaBookingApp.isRobolectric()) {
//            viewModel.hotelSoldOutWithHotelId.subscribe((resultsPresenter.mapCarouselRecycler.adapter as HotelMapCarouselAdapter).hotelSoldOut)
//            viewModel.hotelSoldOutWithHotelId.subscribe(resultsPresenter.adapter.hotelSoldOut)
//            viewModel.hotelSoldOutWithHotelId.subscribe(resultsPresenter.mapViewModel.hotelSoldOutWithIdObserver)
//        }

     */
    private fun initDetailsPresenter() {
        val mapStub = presenter.hotelMapView.findViewById<FrameLayout>(R.id.stub_map)
        detailsMapView.visibility = View.VISIBLE
        val parent = detailsMapView.parent as ViewGroup
        parent.removeView(detailsMapView)
        mapStub.addView(detailsMapView)

        presenter.hotelMapView.mapView = detailsMapView
        presenter.hotelMapView.mapView.getMapAsync(presenter.hotelMapView)


        presenter.hotelDetailView.viewmodel = hotelDetailViewModel
        presenter.hotelDetailView.viewmodel.depositInfoContainerClickObservable.subscribe { pair: Pair<String, HotelOffersResponse.HotelRoomResponse> ->
            presenter.hotelDepositInfoObserver.onNext(pair)
        }
//        presenter.hotelDetailView.viewmodel.reviewsClickedWithHotelData.subscribe(reviewsObserver)
        presenter.hotelDetailView.viewmodel.hotelRenovationObservable.subscribe(presenter.hotelRenovationObserver)
        presenter.hotelDetailView.viewmodel.hotelPayLaterInfoObservable.subscribe { pair: Pair<String, List<HotelOffersResponse.HotelRoomResponse>> ->
            presenter.hotelPayLaterInfoObserver.onNext(pair)
        }

        presenter.hotelDetailView.viewmodel.vipAccessInfoObservable.subscribe(presenter.hotelVIPAccessInfoObserver)
        presenter.hotelDetailView.viewmodel.mapClickedSubject.subscribe(presenter.hotelDetailsEmbeddedMapClickObserver)
        presenter.hotelMapView.viewmodel = HotelMapViewModel(this, presenter.hotelDetailView.viewmodel.scrollToRoom,
                presenter.hotelDetailView.viewmodel.hotelSoldOut, presenter.hotelDetailView.viewmodel.getLOB())

//todo make less shitty
//        presenter.hotelDetailView.viewmodel.changeDates.subscribe(goToSearchScreen)

//        if (shouldUseWebCheckout()) {
//            viewModel = HotelPresenterViewModel((webCheckoutView.viewModel as HotelWebCheckoutViewViewModel).createTripViewModel, null, presenter.hotelDetailView.viewmodel)
//        } else {
//            viewModel = HotelPresenterViewModel(checkoutPresenter.hotelCheckoutWidget.createTripViewmodel, checkoutPresenter.hotelCheckoutViewModel, presenter.hotelDetailView.viewmodel)
//        }
//        viewModel.selectedRoomSoldOut.subscribe(presenter.hotelDetailView.viewmodel.selectedRoomSoldOut)

    }

    private fun initViewModel() {
        subscriptions.add(hotelDetailViewModel.fetchInProgressSubject.subscribe {
            loadingOverlay.visibility = View.VISIBLE
            loadingOverlay.animate(true)
        })

        subscriptions.add(hotelDetailViewModel.fetchCancelledSubject.subscribe {
//            loadingOverlay.visibility = View.GONE
            //todo handle
//            show(searchPresenter)
        })

        subscriptions.add(hotelDetailViewModel.hotelOffersSubject.subscribe { response ->
            loadingOverlay.animate(false)
            loadingOverlay.visibility = View.GONE

            presenter.showDefault()
            // change dates just update the views.  todo this is terrible fix eventually
            hotelDetailViewModel.addViewsAfterTransition()
            presenter.animationFinalize()
            presenter.hotelMapView.viewmodel.offersObserver.onNext(response)
        })


        subscriptions.add(hotelDetailViewModel.roomSelectedSubject.subscribe { offer ->
            //todo go to checkout
//            checkoutPresenter.hotelCheckoutWidget.markRoomSelected()
//            if (shouldUseWebCheckout()) {
//                val webCheckoutViewModel = webCheckoutView.viewModel as HotelWebCheckoutViewViewModel
//                webCheckoutViewModel.hotelSearchParamsObservable.onNext(hotelSearchParams)
//                webCheckoutViewModel.offerObservable.onNext(offer)
//                show(webCheckoutView)
//            } else {
//                checkoutPresenter.hotelCheckoutWidget.couponCardView.viewmodel.hasDiscountObservable.onNext(false)
//                checkoutPresenter.setSearchParams(hotelSearchParams)
//                checkoutPresenter.hotelCheckoutWidget.setSearchParams(hotelSearchParams)
//                checkoutPresenter.showCheckout(offer)
//                show(checkoutPresenter)
//            }
        })
    }
}