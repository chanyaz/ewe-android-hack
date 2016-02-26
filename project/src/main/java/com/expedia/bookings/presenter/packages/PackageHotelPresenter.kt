package com.expedia.bookings.presenter.packages

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.hotels.convertPackageToSearchParams
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.hotel.HotelDetailPresenter
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.LoadingOverlayWidget
import com.expedia.bookings.widget.PackageBundlePriceWidget
import com.expedia.ui.PackageHotelActivity
import com.expedia.util.endlessObserver
import com.expedia.vm.BundleOverviewViewModel
import com.expedia.vm.BundlePriceViewModel
import com.expedia.vm.HotelDetailViewModel
import com.expedia.vm.HotelMapViewModel
import com.expedia.vm.HotelResultsViewModel
import com.google.android.gms.maps.MapView
import com.mobiata.android.util.AndroidUtils
import rx.Observable
import rx.Observer
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.properties.Delegates

class PackageHotelPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    lateinit var packageServices: PackageServices
        @Inject set

    val resultsMapView: MapView by bindView(R.id.map_view)
    val detailsMapView: MapView by bindView(R.id.details_map_view)
    val bundlePriceWidget: PackageBundlePriceWidget by bindView(R.id.bundle_price_widget)
    val bundleOverViewWidget: BundleWidget by bindView(R.id.bundle_widget)
    val bundleToolbar: Toolbar by bindView(R.id.bundle_toolbar)

    val resultsPresenter: PackageHotelResultsPresenter by lazy {
        var viewStub = findViewById(R.id.results_stub) as ViewStub
        var presenter = viewStub.inflate() as PackageHotelResultsPresenter
        var resultsStub = presenter.findViewById(R.id.stub_map) as FrameLayout
        resultsMapView.visibility = View.VISIBLE
        removeView(resultsMapView);
        resultsStub.addView(resultsMapView)
        presenter.mapView = resultsMapView
        presenter.mapView.getMapAsync(presenter)
        presenter.viewmodel = HotelResultsViewModel(context, null, LineOfBusiness.PACKAGES, null)
        presenter.hotelSelectedSubject.subscribe(hotelSelectedObserver)
        presenter.hideBundlePriceOverviewSubject.subscribe(hideBundlePriceOverviewObserver)
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
        presenter.hotelDetailView.viewmodel.hotelDetailsBundleTotalObservable.subscribe { bundle ->
            bundlePriceWidget.viewModel.setTextObservable.onNext(bundle)
        }
        presenter.hotelMapView.viewmodel = HotelMapViewModel(context, presenter.hotelDetailView.viewmodel.scrollToRoom, presenter.hotelDetailView.viewmodel.hotelSoldOut)
        presenter
    }
    val loadingOverlay: LoadingOverlayWidget by bindView(R.id.details_loading_overlay)
    var selectedPackageHotel: Hotel by Delegates.notNull()

    init {
        Ui.getApplication(context).packageComponent().inject(this)
        View.inflate(getContext(), R.layout.package_hotel_presenter, this)
        setupBundleViews()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addTransition(resultsToDetail)
        addTransition(resultsToOverview)
        addTransition(detailsToOverview)
        loadingOverlay.setBackground(R.color.packages_primary_color)
    }

    val hotelSelectedObserver: Observer<Hotel> = endlessObserver { hotel ->
        selectedPackageHotel = hotel
        getDetails(hotel.packageOfferModel.piid, hotel.hotelId, Db.getPackageParams().checkIn.toString(), Db.getPackageParams().checkOut.toString(), Db.getPackageSelectedRoom()?.ratePlanCode, Db.getPackageSelectedRoom()?.roomTypeCode)
    }

    val hideBundlePriceOverviewObserver: Observer<Boolean> = endlessObserver { hide ->
        bundlePriceWidget.visibility = if (hide) GONE else VISIBLE
    }

    private fun getDetails(piid: String, hotelId: String, checkIn: String, checkOut: String, ratePlanCode: String?, roomTypeCode: String?) {
        loadingOverlay.visibility = View.VISIBLE
        loadingOverlay.animate(true)
        detailPresenter.hotelDetailView.viewmodel.paramsSubject.onNext(convertPackageToSearchParams(Db.getPackageParams(), resources.getInteger(R.integer.calendar_max_days_hotel_stay)))
        val packageHotelOffers = packageServices.hotelOffer(piid, checkIn, checkOut, ratePlanCode, roomTypeCode)
        val info = packageServices.hotelInfo(hotelId)
        Observable.zip(packageHotelOffers, info, { packageHotelOffers, info ->
            val hotelOffers = HotelOffersResponse.convertToHotelOffersResponse(info, packageHotelOffers, Db.getPackageParams())
            loadingOverlay.animate(false)
            detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.onNext(hotelOffers)
            detailPresenter.hotelMapView.viewmodel.offersObserver.onNext(hotelOffers)
            show(detailPresenter)
            detailPresenter.showDefault()
        }).subscribe()
        packageHotelOffers.subscribe()
        info.subscribe()
    }

    private val defaultResultsTransition = object : Presenter.DefaultTransition(PackageHotelResultsPresenter::class.java.name) {

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
            bundlePriceWidget.visibility = View.VISIBLE
            bundlePriceWidget.viewModel.bundleTextLabelObservable.onNext(context.getString(R.string.search_bundle_total_text))
            bundlePriceWidget.viewModel.perPersonTextLabelObservable.onNext(true)
            bundlePriceWidget.viewModel.setTextObservable.onNext(Pair(Money(BigDecimal(0),
                    Db.getPackageResponse().packageResult.packageOfferModels[0].price.packageTotalPrice.currencyCode).formattedMoney, ""))
            resultsPresenter.animationFinalize(forward)
        }
    }

    private val resultsToDetail = object : Presenter.Transition(PackageHotelResultsPresenter::class.java.name, HotelDetailPresenter::class.java.name, DecelerateInterpolator(), 400) {
        private var detailsHeight: Int = 0

        override fun startTransition(forward: Boolean) {
            if (!forward) {
                detailPresenter.hotelDetailView.resetViews()
            } else {
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
            bundlePriceWidget.visibility = View.VISIBLE
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

    private val detailsToOverview = object : Presenter.Transition(HotelDetailPresenter::class.java.name, BundleWidget::class.java.name, AccelerateDecelerateInterpolator(), 800) {

        override fun startTransition(forward: Boolean) {
            startBundleTransition(forward)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            updateBundleTransition(f, forward)
        }

        override fun finalizeTransition(forward: Boolean) {
            finalizeBundleTransition(forward)
        }
    }

    private val resultsToOverview = object : Presenter.Transition(PackageHotelResultsPresenter::class.java.name, BundleWidget::class.java.name, AccelerateDecelerateInterpolator(), 600) {

        override fun startTransition(forward: Boolean) {
            startBundleTransition(forward)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            updateBundleTransition(f, forward)
        }

        override fun finalizeTransition(forward: Boolean) {
            finalizeBundleTransition(forward)
        }
    }

    private fun startBundleTransition(forward: Boolean) {
        val lp = bundleOverViewWidget.layoutParams
        lp.height = AndroidUtils.getScreenSize(context).y - Ui.getStatusBarHeight(context)
        bundleOverViewWidget.translationY = if (forward) resultsPresenter.height.toFloat() else Ui.getStatusBarHeight(context).toFloat()
        bundlePriceWidget.translationY = if (forward) 0f else - resultsPresenter.height.toFloat()
        resultsPresenter.visibility = View.VISIBLE
        bundleOverViewWidget.visibility = View.VISIBLE
        bundlePriceWidget.visibility = View.VISIBLE
        bundlePriceWidget.alpha = if (forward) 1f else 0f
        bundlePriceWidget.rotateChevron(!forward)
    }

    private fun updateBundleTransition(f: Float, forward: Boolean) {
        val height = resultsPresenter.height
        val pos = if (forward) (f * height) else (height - (f * height))
        bundleOverViewWidget.translationY = Math.max(Ui.getStatusBarHeight(context).toFloat(), if (forward) (height - (f * height)) else (f * height))
        bundlePriceWidget.translationY = Math.max(-height + Ui.getStatusBarHeight(context).toFloat(), -pos)
        bundlePriceWidget.alpha = if (forward) (1 - f) else f
    }

    private fun finalizeBundleTransition(forward: Boolean) {
        resultsPresenter.visibility = View.VISIBLE
        bundleOverViewWidget.visibility = if (forward) View.VISIBLE else View.GONE
        bundlePriceWidget.visibility = if (forward) View.GONE else View.VISIBLE
        bundlePriceWidget.alpha = if (forward) 0f else 1f
        bundleOverViewWidget.translationY = if (forward) Ui.getStatusBarHeight(context).toFloat() else resultsPresenter.height.toFloat()
        bundlePriceWidget.translationY =  if (forward) - resultsPresenter.height.toFloat() + Ui.getStatusBarHeight(context).toFloat() else 0f
    }

    val selectedRoomObserver = endlessObserver<HotelOffersResponse.HotelRoomResponse> { offer ->
        Db.setPackageSelectedHotel(selectedPackageHotel, offer)
        val params = Db.getPackageParams();
        params.packagePIID = offer.productKey;
        val activity = (context as AppCompatActivity)
        activity.setResult(Activity.RESULT_OK)
        activity.finish()
    }

    private fun setupBundleViews() {
        bundleOverViewWidget.viewModel = BundleOverviewViewModel(context, null)
        bundleOverViewWidget.viewModel.toolbarTitleObservable.subscribe {
            bundleToolbar.title = it
        }
        bundleOverViewWidget.viewModel.toolbarSubtitleObservable.subscribe {
            bundleToolbar.subtitle = it
        }
        bundleOverViewWidget.bundleHotelWidget.setOnClickListener {
            back()
        }
        bundleToolbar.setOnClickListener {
            back()
        }
        bundlePriceWidget.viewModel = BundlePriceViewModel(context)
        bundlePriceWidget.bundleChevron.visibility = View.VISIBLE
        bundlePriceWidget.setOnClickListener {
            bundleOverViewWidget.viewModel.hotelParamsObservable.onNext(Db.getPackageParams())
            bundleOverViewWidget.viewModel.hotelResultsObservable.onNext(Unit)
            show(bundleOverViewWidget)
        }
        bundleOverViewWidget.bundleContainer.setPadding(0, Ui.getToolbarSize(context), 0, 0)
        val icon = ContextCompat.getDrawable(context, R.drawable.read_more).mutate()
        icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        bundleToolbar.navigationIcon = icon
    }

    val defaultTransitionObserver: Observer<PackageHotelActivity.Screen> = endlessObserver {
        when (it) {
            PackageHotelActivity.Screen.DETAILS -> {
                addDefaultTransition(defaultDetailsTransition)
            }
            PackageHotelActivity.Screen.RESULTS -> {
                addDefaultTransition(defaultResultsTransition)
                show(resultsPresenter)
                resultsPresenter.showDefault()
                resultsPresenter.viewmodel.paramsSubject.onNext(convertPackageToSearchParams(Db.getPackageParams(), resources.getInteger(R.integer.calendar_max_days_hotel_stay)))
                resultsPresenter.viewmodel.hotelResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(Db.getPackageResponse()))
            }
        }
    }

    private val defaultDetailsTransition = object : Presenter.DefaultTransition(HotelDetailPresenter::class.java.name) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            loadingOverlay.visibility = View.GONE
            detailPresenter.visibility = View.VISIBLE
            if (forward) {
                detailPresenter.hotelDetailView.refresh()
                detailPresenter.hotelDetailView.viewmodel.addViewsAfterTransition()
            }
        }
    }

}
