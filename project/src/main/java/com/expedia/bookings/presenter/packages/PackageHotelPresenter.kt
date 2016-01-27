package com.expedia.bookings.presenter.packages

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.hotels.convertPackageToSearchParams
import com.expedia.bookings.data.packages.PackageOffersResponse
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.hotel.HotelDetailPresenter
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.LoadingOverlayWidget
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelDetailViewModel
import com.expedia.vm.HotelMapViewModel
import com.expedia.vm.HotelResultsViewModel
import com.google.android.gms.maps.MapView
import rx.Observable
import rx.Observer
import javax.inject.Inject
import kotlin.properties.Delegates

public class PackageHotelPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    lateinit var packageServices: PackageServices
        @Inject set

    val resultsMapView: MapView by bindView(R.id.map_view)
    val detailsMapView: MapView by bindView(R.id.details_map_view)
    val resultsPresenter: PackageHotelResultsPresenter by lazy {
        var viewStub = findViewById(R.id.results_stub) as ViewStub
        var presenter = viewStub.inflate() as PackageHotelResultsPresenter
        var resultsStub = presenter.findViewById(R.id.stub_map) as FrameLayout
        resultsMapView.visibility = View.VISIBLE
        removeView(resultsMapView);
        resultsStub.addView(resultsMapView)
        presenter.mapView = resultsMapView
        presenter.mapView.getMapAsync(presenter)
        presenter.viewmodel = HotelResultsViewModel(context, null)
        presenter.hotelSelectedSubject.subscribe(hotelSelectedObserver)
        presenter
    }
    val detailPresenter: HotelDetailPresenter by lazy {
        var viewStub = findViewById(R.id.details_stub) as ViewStub
        var presenter = viewStub.inflate() as HotelDetailPresenter
        var detailsStub = presenter.hotelMapView.findViewById(R.id.stub_map) as FrameLayout
        detailsMapView.visibility = View.VISIBLE
        removeView(detailsMapView);
        detailsStub.addView(detailsMapView)
        presenter.hotelMapView.mapView = detailsMapView
        presenter.hotelMapView.mapView.getMapAsync(presenter.hotelMapView);
        presenter.hotelDetailView.viewmodel = HotelDetailViewModel(context, null, selectedRoomObserver)
        presenter.hotelMapView.viewmodel = HotelMapViewModel(context, presenter.hotelDetailView.viewmodel.scrollToRoom, presenter.hotelDetailView.viewmodel.hotelSoldOut)
        presenter
    }
    val loadingOverlay: LoadingOverlayWidget by bindView(R.id.details_loading_overlay)
    var selectedPackageHotel: Hotel by Delegates.notNull()

    init {
        Ui.getApplication(context).packageComponent().inject(this)
        View.inflate(getContext(), R.layout.package_hotel_presenter, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        addTransition(resultsToDetail)
        show(resultsPresenter)
        resultsPresenter.showDefault()
        resultsPresenter.viewmodel.paramsSubject.onNext(convertPackageToSearchParams(Db.getPackageParams(), resources.getInteger(R.integer.calendar_max_days_hotel_stay)))
        resultsPresenter.viewmodel.hotelResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(Db.getPackageResponse()))
        loadingOverlay.setBackground(R.color.packages_primary_color)
    }

    val hotelSelectedObserver: Observer<Hotel> = endlessObserver { hotel ->
        selectedPackageHotel = hotel
        getDetails(hotel.packageOfferModel.piid, hotel.hotelId, Db.getPackageParams().checkIn.toString(), Db.getPackageParams().checkOut.toString())
    }

    private fun getDetails(piid: String, hotelId: String, checkIn: String, checkOut: String) {
        loadingOverlay.visibility = View.VISIBLE
        loadingOverlay.animate(true)
        detailPresenter.hotelDetailView.viewmodel.paramsSubject.onNext(convertPackageToSearchParams(Db.getPackageParams(), resources.getInteger(R.integer.calendar_max_days_hotel_stay)))
        val packageHotelOffers = packageServices.hotelOffer(piid, checkIn, checkOut)
        val info = packageServices.hotelInfo(hotelId)
        Observable.zip(packageHotelOffers, info, { packageHotelOffers, info ->
            println("zip success")
            val hotelOffers = HotelOffersResponse.convertToHotelOffersResponse(info, packageHotelOffers, Db.getPackageParams())
            loadingOverlay.animate(false)
            detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.onNext(hotelOffers)
            detailPresenter.hotelMapView.viewmodel.offersObserver.onNext(hotelOffers)
            show(detailPresenter)
            detailPresenter.showDefault()
        }).subscribe()
        packageHotelOffers.subscribe(getOfferObserver())
        info.subscribe(getInfoObserver())
    }

    fun getOfferObserver() :  Observer<PackageOffersResponse> {
        return object : Observer<PackageOffersResponse> {
            override fun onNext(response: PackageOffersResponse) {
                println("offers success, Hotels:" + response.packageHotelOffers.size);
            }

            override fun onCompleted() {
                println("offers completed")
            }

            override fun onError(e: Throwable?) {
                println("offers error: " + e?.message)
            }
        }
    }

    fun getInfoObserver() :  Observer<HotelOffersResponse> {
        return object : Observer<HotelOffersResponse> {
            override fun onNext(response: HotelOffersResponse) {
                println("info success, Hotel:" + response.hotelName);
            }

            override fun onCompleted() {
                println("info completed")
            }

            override fun onError(e: Throwable?) {
                println("info error: " + e?.message)
            }
        }
    }

    private val defaultTransition = object : Presenter.DefaultTransition(PackageHotelResultsPresenter::class.java.name) {

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            loadingOverlay.visibility = View.GONE
            resultsPresenter.visibility = View.VISIBLE
            resultsPresenter.animationStart()
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            resultsPresenter.animationUpdate(f, !forward)
        }

        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            resultsPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            resultsPresenter.animationFinalize(forward)
        }
    }

    private val resultsToDetail = object : Presenter.Transition(PackageHotelResultsPresenter::class.java.name, HotelDetailPresenter::class.java.name, DecelerateInterpolator(), 400) {
        private var detailsHeight: Int = 0

        override fun startTransition(forward: Boolean) {
            if (!forward) {
                detailPresenter.hotelDetailView.resetViews()
            }
            else {
                detailPresenter.hotelDetailView.refresh()
            }
            val parentHeight = height
            detailsHeight = parentHeight - Ui.getStatusBarHeight(getContext())
            val pos = (if (forward) detailsHeight else 0).toFloat()
            detailPresenter.translationY = pos
            detailPresenter.visibility = View.VISIBLE
            detailPresenter.animationStart()
            resultsPresenter.visibility = View.VISIBLE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            val pos = if (forward) (detailsHeight - (f * detailsHeight)) else (f * detailsHeight)
            detailPresenter.translationY = pos
            detailPresenter.animationUpdate(f, !forward)
        }

        override fun finalizeTransition(forward: Boolean) {
            detailPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            resultsPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            detailPresenter.translationY = 0f
            resultsPresenter.animationFinalize(!forward)
            detailPresenter.animationFinalize()
            loadingOverlay.visibility = View.GONE
            if (forward) {
                detailPresenter.hotelDetailView.viewmodel.addViewsAfterTransition()
            } else {
                resultsPresenter.recyclerView.adapter.notifyDataSetChanged()
            }
        }
    }

    val selectedRoomObserver = object : Observer<HotelOffersResponse.HotelRoomResponse> {
        override fun onNext(offer: HotelOffersResponse.HotelRoomResponse) {
            Db.setPackageSelectedHotel(selectedPackageHotel, offer)
            val params = Db.getPackageParams();
            params.packagePIID = offer.productKey;
            val activity = (context as AppCompatActivity)
            activity.setResult(Activity.RESULT_OK)
            activity.finish()
        }

        override fun onCompleted() {

        }

        override fun onError(e: Throwable) {

        }
    }
}
