package com.expedia.bookings.presenter.packages

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.MotionEvent
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
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.presenter.hotel.HotelDetailPresenter
import com.expedia.bookings.presenter.hotel.HotelReviewsView
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.services.ReviewsServices
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.LoadingOverlayWidget
import com.expedia.bookings.widget.TotalPriceWidget
import com.expedia.ui.PackageHotelActivity
import com.expedia.util.endlessObserver
import com.expedia.util.subscribeText
import com.expedia.vm.BundleOverviewViewModel
import com.expedia.vm.BundlePriceViewModel
import com.expedia.vm.HotelDetailViewModel
import com.expedia.vm.HotelMapViewModel
import com.expedia.vm.HotelResultsViewModel
import com.expedia.vm.HotelReviewsViewModel
import com.google.android.gms.maps.MapView
import rx.Observable
import rx.Observer
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.properties.Delegates

class PackageHotelPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    lateinit var reviewServices: ReviewsServices
        @Inject set

    lateinit var packageServices: PackageServices
        @Inject set

    val resultsMapView: MapView by bindView(R.id.map_view)
    val detailsMapView: MapView by bindView(R.id.details_map_view)
    val bundlePriceWidget: TotalPriceWidget by bindView(R.id.bundle_price_widget)
    val bundleOverViewWidget: BundleWidget by bindView(R.id.bundle_widget)

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
        presenter.hotelDetailView.viewmodel.reviewsClickedWithHotelData.subscribe(reviewsObserver)
        presenter.hotelDetailView.viewmodel.mapClickedSubject.subscribe(presenter.hotelDetailsEmbeddedMapClickObserver)
        presenter.hotelDetailView.viewmodel.hotelDetailsBundleTotalObservable.subscribe { bundle ->
            bundlePriceWidget.viewModel.setTextObservable.onNext(bundle)
        }
        presenter.hotelMapView.viewmodel = HotelMapViewModel(context, presenter.hotelDetailView.viewmodel.scrollToRoom, presenter.hotelDetailView.viewmodel.hotelSoldOut)
        presenter
    }

    val reviewsView: HotelReviewsView by lazy {
        var viewStub = findViewById(R.id.reviews_stub) as ViewStub
        var presenter = viewStub.inflate() as HotelReviewsView
        presenter.reviewServices = reviewServices
        presenter
    }

    val reviewsObserver: Observer<HotelOffersResponse> = endlessObserver { hotel ->
        reviewsView.viewModel = HotelReviewsViewModel(getContext())
        reviewsView.viewModel.hotelObserver.onNext(hotel)
        show(reviewsView)
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
        addTransition(detailsToReview)
        loadingOverlay.setBackground(R.color.packages_primary_color)
        bundlePriceWidget.setOnTouchListener(object : View.OnTouchListener {
            internal var originY: Float = 0.toFloat()
            internal var hasMoved: Boolean = false
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    (MotionEvent.ACTION_DOWN) -> {
                        originY = height.toFloat() - (bundlePriceWidget.height / 2f)
                        hasMoved = false
                    }
                    (MotionEvent.ACTION_UP) -> {
                        val distance = bundlePriceWidget.translationY
                        val distanceMax = -height.toFloat() + bundlePriceWidget.height + Ui.getStatusBarHeight(context)
                        val upperThreshold = distanceMax / 3
                        val isShowingBundle = Strings.equals(currentState, BundleWidget::class.java.name)
                        if (isShowingBundle) {
                            back()
                        } else if (!hasMoved || (distance > distanceMax && distance <= upperThreshold)) {
                            show(bundleOverViewWidget)
                        } else {
                            closeBundleOverview(distanceMax)
                        }
                        originY = 0f
                        hasMoved = false

                    }
                    (MotionEvent.ACTION_MOVE) -> {
                        val diff = event.rawY - originY
                        translateBundleOverview(diff)
                        hasMoved = true
                    }
                }
                return true
            }
        })
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

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
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

        override fun endTransition(forward: Boolean) {
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

    private val detailsToReview = object : ScaleTransition(this, HotelDetailPresenter::class.java, HotelReviewsView::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (forward) {
                reviewsView.transitionFinished()
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

        override fun endTransition(forward: Boolean) {
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

        override fun endTransition(forward: Boolean) {
            finalizeBundleTransition(forward)
        }
    }


    var translationDistance = 0f
    val statusBarHeight = Ui.getStatusBarHeight(context)

    private fun startBundleTransition(forward: Boolean) {
        val lp = bundleOverViewWidget.layoutParams
        lp.height = height - statusBarHeight
        translationDistance = bundlePriceWidget.translationY
        bundleOverViewWidget.translationY = height.toFloat() - bundlePriceWidget.height + translationDistance
        resultsPresenter.visibility = View.VISIBLE
        bundleOverViewWidget.visibility = View.VISIBLE
        bundlePriceWidget.visibility = View.VISIBLE
        bundlePriceWidget.bundleTitle.visibility = View.VISIBLE
        bundlePriceWidget.bundleSubtitle.visibility = View.VISIBLE
        bundlePriceWidget.setBackgroundColor(if (forward) Color.WHITE else ContextCompat.getColor(context, R.color.packages_primary_color))
    }

    private fun updateBundleTransition(f: Float, forward: Boolean) {
        var distance = -height - translationDistance + bundlePriceWidget.height + statusBarHeight
        val pos = if (forward) translationDistance + (f * distance) else (1 - f) * translationDistance
        translateBundleOverview(pos)
    }

    private fun finalizeBundleTransition(forward: Boolean) {
        resultsPresenter.visibility = View.VISIBLE
        bundleOverViewWidget.visibility = if (forward) View.VISIBLE else View.GONE
        bundlePriceWidget.bundleTitle.visibility = if (forward) View.VISIBLE else View.GONE
        bundlePriceWidget.bundleSubtitle.visibility = if (forward) View.VISIBLE else View.GONE
        bundlePriceWidget.setBackgroundColor(if (forward) ContextCompat.getColor(context, R.color.packages_primary_color) else Color.WHITE)
        bundleOverViewWidget.translationY = if (forward) statusBarHeight.toFloat() else height.toFloat()
        bundlePriceWidget.translationY = if (forward) -height + bundlePriceWidget.height + statusBarHeight.toFloat() else 0f
    }

    private fun translateBundleOverview(distance: Float) {
        val distanceMax = - height.toFloat() + bundlePriceWidget.height + Ui.getStatusBarHeight(context)
        val f = distance / distanceMax
        if (distance > distanceMax && distance < 0) {
            bundleOverViewWidget.visibility = View.VISIBLE
            bundlePriceWidget.translationY = distance
            bundleOverViewWidget.translationY = height.toFloat() - bundlePriceWidget.height + distance
            bundlePriceWidget.animateBundleWidget(f, true)
        }
    }

    private fun closeBundleOverview(distanceMax: Float) {
        val animator = ObjectAnimator.ofFloat(bundlePriceWidget, "translationY", bundlePriceWidget.translationY, 0f)
        animator.duration = 400L
        animator.addUpdateListener(ValueAnimator.AnimatorUpdateListener
        {
            anim ->
            bundleOverViewWidget.translationY = height.toFloat() - bundlePriceWidget.height + bundlePriceWidget.translationY
            bundlePriceWidget.animateBundleWidget(Math.abs((-distanceMax + bundlePriceWidget.translationY) / distanceMax), false)

        })
        animator.start()
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
        bundleOverViewWidget.viewModel.hotelParamsObservable.onNext(Db.getPackageParams())
        bundleOverViewWidget.viewModel.hotelResultsObservable.onNext(Unit)
        bundleOverViewWidget.viewModel.toolbarTitleObservable.subscribeText(bundlePriceWidget.bundleTitle)
        bundleOverViewWidget.viewModel.toolbarSubtitleObservable.subscribeText(bundlePriceWidget.bundleSubtitle)
        bundleOverViewWidget.bundleHotelWidget.setOnClickListener {
            back()
        }
        bundlePriceWidget.viewModel = BundlePriceViewModel(context)
        bundlePriceWidget.bundleChevron.visibility = View.VISIBLE
        bundlePriceWidget.setOnClickListener {
            show(bundleOverViewWidget)
        }
        bundleOverViewWidget.setPadding(0, Ui.getToolbarSize(context), 0, 0)
        val icon = ContextCompat.getDrawable(context, R.drawable.read_more).mutate()
        icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
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
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            loadingOverlay.visibility = View.GONE
            detailPresenter.visibility = View.VISIBLE
            if (forward) {
                detailPresenter.hotelDetailView.refresh()
                detailPresenter.hotelDetailView.viewmodel.addViewsAfterTransition()
            }
        }
    }

}
