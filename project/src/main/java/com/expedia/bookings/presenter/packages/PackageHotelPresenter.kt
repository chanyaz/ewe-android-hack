package com.expedia.bookings.presenter.packages

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.GestureDetector
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
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.PackageResponseUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.LoadingOverlayWidget
import com.expedia.bookings.widget.SlidingBundleWidget
import com.expedia.ui.PackageHotelActivity
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelMapViewModel
import com.expedia.vm.HotelReviewsViewModel
import com.expedia.vm.hotel.HotelResultsViewModel
import com.expedia.vm.packages.PackageHotelDetailViewModel
import com.google.android.gms.maps.MapView
import com.squareup.phrase.Phrase
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
    val bundleSlidingWidget: SlidingBundleWidget by bindView(R.id.sliding_bundle_widget)

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
        presenter.hotelDetailView.viewmodel = PackageHotelDetailViewModel(context, selectedRoomObserver)
        presenter.hotelDetailView.viewmodel.reviewsClickedWithHotelData.subscribe(reviewsObserver)
        presenter.hotelDetailView.viewmodel.mapClickedSubject.subscribe(presenter.hotelDetailsEmbeddedMapClickObserver)
        presenter.hotelDetailView.viewmodel.hotelDetailsBundleTotalObservable.subscribe { bundle ->
            bundleSlidingWidget.bundlePriceWidget.viewModel.setTextObservable.onNext(bundle)
            bundleSlidingWidget.bundlePriceFooter.viewModel.setTextObservable.onNext(bundle)
        }
        presenter.hotelMapView.viewmodel = HotelMapViewModel(context, presenter.hotelDetailView.viewmodel.scrollToRoom, presenter.hotelDetailView.viewmodel.hotelSoldOut, presenter.hotelDetailView.viewmodel.getLOB())
        presenter
    }

    val reviewsView: HotelReviewsView by lazy {
        var viewStub = findViewById(R.id.reviews_stub) as ViewStub
        var presenter = viewStub.inflate() as HotelReviewsView
        presenter.reviewServices = reviewServices
        presenter.hotelReviewsToolbar.slidingTabLayout.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                PackagesTracking().trackHotelReviewCategoryChange(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
        })
        setUpReviewsTransition(presenter)
        presenter
    }

    fun setUpReviewsTransition(view: View) {
        val transition = object : ScaleTransition(this, detailPresenter, view) {
            override fun endTransition(forward: Boolean) {
                super.endTransition(forward)
                if (forward) {
                    reviewsView.transitionFinished()
                }
            }
        }
        addTransition(transition)
    }

    val reviewsObserver: Observer<HotelOffersResponse> = endlessObserver { hotel ->
        reviewsView.viewModel = HotelReviewsViewModel(getContext(), LineOfBusiness.PACKAGES)
        reviewsView.viewModel.hotelObserver.onNext(hotel)
        show(reviewsView)
    }

    val SWIPE_MIN_DISTANCE = 10
    val SWIPE_THRESHOLD_VELOCITY = 300
    val FAST_ANIMATION_DURATION = 150
    val REGULAR_ANIMATION_DURATION = 400

    val loadingOverlay: LoadingOverlayWidget by bindView(R.id.details_loading_overlay)

    var selectedPackageHotel: Hotel by Delegates.notNull()

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (bundleSlidingWidget.isMoving || !bundleSlidingWidget.canMove) {
                return true
            }
            if (e1.y.minus(e2.y) > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                if (detailsToOverview.animationDuration != FAST_ANIMATION_DURATION) {
                    updateOverviewAnimationDuration(FAST_ANIMATION_DURATION)
                }
                if (!isShowingBundle()) {
                    show(bundleSlidingWidget)
                } else {
                    back()
                }
                return true
            } else if (e2.y.minus(e1.y) > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                if (isShowingBundle()) {
                    back()
                } else {
                    bundleSlidingWidget.closeBundleOverview()
                }
                return true
            }
            return false
        }

        override fun onDown(e: MotionEvent?): Boolean {
            return super.onDown(e)
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            if (bundleSlidingWidget.isMoving || !bundleSlidingWidget.canMove) {
                return true
            }
            if (detailsToOverview.animationDuration != REGULAR_ANIMATION_DURATION) {
                updateOverviewAnimationDuration(REGULAR_ANIMATION_DURATION)
            }
            bundleSlidingWidget.translateBundleOverview(e2.rawY)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            updateOverviewAnimationDuration(REGULAR_ANIMATION_DURATION)
            val isShowingBundle = isShowingBundle()
            if (isShowingBundle) {
                back()
            } else {
                show(bundleSlidingWidget)
            }
            return true
        }
    }
    val gestureDetector: GestureDetector = GestureDetector(context, gestureListener)

    init {
        Ui.getApplication(context).packageComponent().inject(this)
        View.inflate(getContext(), R.layout.package_hotel_presenter, this)
        bundleSlidingWidget.setupBundleViews()
        bundleSlidingWidget.animationFinished.subscribe {
            resultsPresenter.viewmodel.hotelResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(Db.getPackageResponse()))
        }
        bundleSlidingWidget.bundlePriceWidget.viewModel.bundleTextLabelObservable.onNext(context.getString(R.string.search_bundle_total_text))
        val currencyCode = Db.getPackageResponse().packageResult.packageOfferModels[0].price.packageTotalPrice.currencyCode
        val total = Money(BigDecimal(0), currencyCode)
        var packageSavings = Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                .put("savings", total.getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL))
                .format().toString()
        bundleSlidingWidget.bundlePriceWidget.viewModel.setTextObservable.onNext(Pair(total.getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL), packageSavings))
        bundleSlidingWidget.bundlePriceFooter.viewModel.setTextObservable.onNext(Pair(total.getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL), packageSavings))
    }

    fun updateOverviewAnimationDuration(duration: Int) {
        detailsToOverview.animationDuration = duration
        resultsToOverview.animationDuration = duration
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addTransition(resultsToDetail)
        addTransition(resultsToOverview)
        addTransition(detailsToOverview)
        addTransition(reviewsToOverview)
        bundleSlidingWidget.bundleOverViewWidget.bundleHotelWidget.rowContainer.setOnClickListener {
            back()
        }
        bundleSlidingWidget.bundlePriceWidget.setOnClickListener {
            show(bundleSlidingWidget)
        }
        loadingOverlay.setBackground(R.color.packages_primary_color)
        bundleSlidingWidget.bundlePriceWidget.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (bundleSlidingWidget.isMoving) {
                    return true
                }
                if (!gestureDetector.onTouchEvent(event)) {
                    when (event.action) {
                        (MotionEvent.ACTION_DOWN) -> {
                            bundleSlidingWidget.canMove = true
                        }
                        (MotionEvent.ACTION_UP) -> {
                            bundleSlidingWidget.canMove = false
                            val distance = Math.abs(bundleSlidingWidget.translationY)
                            val distanceMax = height.toFloat() - bundleSlidingWidget.bundlePriceWidget.height
                            val upperThreshold = distanceMax / 3
                            val lowerThreshold = (distanceMax / 3) * 2
                            if (distance > Math.abs(lowerThreshold) && !isShowingBundle()) {
                                // currentState !=  BundleWidget, from BOTTOM to TOP but distance moved less than threshold hence close widget.
                                bundleSlidingWidget.closeBundleOverview()
                            } else if (distance <= Math.abs(lowerThreshold) && !isShowingBundle()) {
                                // currentState !=  BundleWidget, from BOTTOM to TOP and distance moved greater than threshold hence show widget again.
                                show(bundleSlidingWidget)
                            } else if (distance <= Math.abs(upperThreshold)) {
                                // currentState ==  BundleWidget, from TOP to BOTTOM but distance moved less than threshold hence show widget again.
                                bundleSlidingWidget.openBundleOverview()
                            } else if (distance > Math.abs(upperThreshold)) {
                                // currentState ==  BundleWidget, from TOP to BOTTOM and distance moved greater than threshold hence close widget i.e. hitBack()
                                back()
                            }
                        }
                    }
                }
                return true
            }
        })
    }

    val hotelSelectedObserver: Observer<Hotel> = endlessObserver { hotel ->
        selectedPackageHotel = hotel
        getDetails(hotel.packageOfferModel.piid, hotel.hotelId, Db.getPackageParams().checkIn.toString(), Db.getPackageParams().checkOut.toString(), Db.getPackageSelectedRoom()?.ratePlanCode, Db.getPackageSelectedRoom()?.roomTypeCode)
        PackagesTracking().trackHotelMapCarouselPropertyClick()
    }

    val hideBundlePriceOverviewObserver: Observer<Boolean> = endlessObserver { hide ->
        bundleSlidingWidget.visibility = if (hide) GONE else VISIBLE
    }

    private fun getDetails(piid: String, hotelId: String, checkIn: String, checkOut: String, ratePlanCode: String?, roomTypeCode: String?) {
        loadingOverlay.visibility = View.VISIBLE
        loadingOverlay.animate(true)
        detailPresenter.hotelDetailView.viewmodel.paramsSubject.onNext(convertPackageToSearchParams(Db.getPackageParams(), resources.getInteger(R.integer.calendar_max_days_hotel_stay), resources.getInteger(R.integer.calendar_max_selectable_date_range)))
        val packageHotelOffers = packageServices.hotelOffer(piid, checkIn, checkOut, ratePlanCode, roomTypeCode)
        val info = packageServices.hotelInfo(hotelId)
        packageHotelOffers.subscribe()
        Observable.zip(packageHotelOffers, info, { packageHotelOffers, info ->
            if (packageHotelOffers.hasErrors()) {
                val activity = (context as AppCompatActivity)
                val resultIntent = Intent()
                resultIntent.putExtra(Constants.PACKAGE_HOTEL_OFFERS_ERROR, packageHotelOffers.firstError.errorCode.name)
                activity.setResult(Activity.RESULT_OK, resultIntent)
                activity.finish()
                return@zip
            }

            val hotelOffers = HotelOffersResponse.convertToHotelOffersResponse(info, packageHotelOffers, Db.getPackageParams())
            PackageResponseUtils.saveHotelOfferResponse(context, hotelOffers, PackageResponseUtils.RECENT_PACKAGE_HOTEL_OFFER_FILE)
            loadingOverlay.animate(false)
            detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.onNext(hotelOffers)
            detailPresenter.hotelMapView.viewmodel.offersObserver.onNext(hotelOffers)
            show(detailPresenter)
            detailPresenter.showDefault()
        }).subscribe()
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
            resultsPresenter.animationFinalize(forward)
        }
    }

    private val resultsToDetail = object : Presenter.Transition(PackageHotelResultsPresenter::class.java.name, HotelDetailPresenter::class.java.name, DecelerateInterpolator(), REGULAR_ANIMATION_DURATION) {
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
            detailPresenter.translationY = 0f
            resultsPresenter.animationFinalize(!forward)
            detailPresenter.animationFinalize()
            loadingOverlay.visibility = View.GONE
            if (forward) {
                detailPresenter.hotelDetailView.viewmodel.addViewsAfterTransition()
            } else {
                resultsPresenter.viewmodel.paramsSubject.onNext(convertPackageToSearchParams(Db.getPackageParams(), resources.getInteger(R.integer.calendar_max_days_hotel_stay),  resources.getInteger(R.integer.calendar_max_package_selectable_date_range)))
                resultsPresenter.viewmodel.hotelResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(Db.getPackageResponse()))
                resultsPresenter.recyclerView.adapter.notifyDataSetChanged()
                trackSearchResult()
            }
        }
    }

    private val detailsToOverview = object : Presenter.Transition(HotelDetailPresenter::class.java.name, SlidingBundleWidget::class.java.name, AccelerateDecelerateInterpolator(), REGULAR_ANIMATION_DURATION) {
        override fun startTransition(forward: Boolean) {
            bundleSlidingWidget.startBundleTransition(forward)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            bundleSlidingWidget.updateBundleTransition(f, forward)
        }

        override fun endTransition(forward: Boolean) {
            bundleSlidingWidget.finalizeBundleTransition(forward)
        }
    }

    private val resultsToOverview = object : Presenter.Transition(PackageHotelResultsPresenter::class.java.name, SlidingBundleWidget::class.java.name, AccelerateDecelerateInterpolator(), REGULAR_ANIMATION_DURATION) {
        override fun startTransition(forward: Boolean) {
            bundleSlidingWidget.startBundleTransition(forward)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            bundleSlidingWidget.updateBundleTransition(f, forward)
        }

        override fun endTransition(forward: Boolean) {
            bundleSlidingWidget.finalizeBundleTransition(forward)
        }
    }

    private val reviewsToOverview = object : Presenter.Transition(HotelReviewsView::class.java.name, SlidingBundleWidget::class.java.name, AccelerateDecelerateInterpolator(), REGULAR_ANIMATION_DURATION) {
        override fun startTransition(forward: Boolean) {
            bundleSlidingWidget.startBundleTransition(forward)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            bundleSlidingWidget.updateBundleTransition(f, forward)
        }

        override fun endTransition(forward: Boolean) {
            bundleSlidingWidget.finalizeBundleTransition(forward)
        }
    }

    val selectedRoomObserver = endlessObserver<HotelOffersResponse.HotelRoomResponse> { offer ->
        Db.setPackageSelectedHotel(selectedPackageHotel, offer)
        val params = Db.getPackageParams();
        params.packagePIID = offer.productKey;
        val activity = (context as AppCompatActivity)
        activity.setResult(Activity.RESULT_OK)
        activity.finish()
    }

    val defaultTransitionObserver: Observer<PackageHotelActivity.Screen> = endlessObserver {
        when (it) {
            PackageHotelActivity.Screen.DETAILS -> {
                addDefaultTransition(defaultResultsTransition)
                show(resultsPresenter)
                resultsPresenter.showDefault()
                show(detailPresenter)
                detailPresenter.showDefault()
            }
            PackageHotelActivity.Screen.RESULTS -> {
                addDefaultTransition(defaultResultsTransition)
                show(resultsPresenter)
                resultsPresenter.showDefault()
                resultsPresenter.viewmodel.paramsSubject.onNext(convertPackageToSearchParams(Db.getPackageParams(), resources.getInteger(R.integer.calendar_max_days_hotel_stay),  resources.getInteger(R.integer.calendar_max_package_selectable_date_range)))
                trackSearchResult()
            }
            PackageHotelActivity.Screen.DETAILS_ONLY -> {
                //change hotel room
                addDefaultTransition(defaultDetailsTransition)
            }

        }
    }

    private fun trackSearchResult() {
        PackagesTracking().trackHotelSearchResultLoad(Db.getPackageResponse())
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

    private fun isShowingBundle(): Boolean {
        val isShowingBundle = Strings.equals(currentState, SlidingBundleWidget::class.java.name)
        return isShowingBundle
    }
}
