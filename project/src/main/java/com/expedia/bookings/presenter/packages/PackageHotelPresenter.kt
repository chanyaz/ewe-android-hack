package com.expedia.bookings.presenter.packages

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.animation.AnimationUtils
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
import com.expedia.bookings.presenter.hotel.BaseHotelResultsPresenter
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
import com.expedia.bookings.widget.SlidingBundleWidgetListener
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

    val slideDownAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_down)
    val slideUpAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_up)

    val resultsPresenter: PackageHotelResultsPresenter by lazy {
        val viewStub = findViewById(R.id.results_stub) as ViewStub
        val presenter = viewStub.inflate() as PackageHotelResultsPresenter
        val resultsStub = presenter.findViewById(R.id.stub_map) as FrameLayout
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
        val viewStub = findViewById(R.id.details_stub) as ViewStub
        val presenter = viewStub.inflate() as HotelDetailPresenter
        val detailsStub = presenter.hotelMapView.findViewById(R.id.stub_map) as FrameLayout
        detailsMapView.visibility = View.VISIBLE
        removeView(detailsMapView);
        detailsStub.addView(detailsMapView)
        presenter.hotelMapView.mapView = detailsMapView
        presenter.hotelMapView.mapView.getMapAsync(presenter.hotelMapView);
        presenter.hotelDetailView.viewmodel = PackageHotelDetailViewModel(context, selectedRoomObserver)
        presenter.hotelDetailView.viewmodel.reviewsClickedWithHotelData.subscribe(reviewsObserver)
        presenter.hotelDetailView.viewmodel.vipAccessInfoObservable.subscribe(presenter.hotelVIPAccessInfoObserver)
        presenter.hotelDetailView.viewmodel.mapClickedSubject.subscribe(presenter.hotelDetailsEmbeddedMapClickObserver)
        presenter.hotelDetailView.viewmodel.bundlePricePerPersonObservable.subscribe { bundle ->
            bundleSlidingWidget.bundlePriceWidget.viewModel.setTextObservable.onNext(bundle)
        }
        presenter.hotelDetailView.viewmodel.bundleTotalPriceObservable.subscribe { bundle ->
            bundleSlidingWidget.bundlePriceFooter.viewModel.setTextObservable.onNext(bundle)
        }
        presenter.hotelMapView.viewmodel = HotelMapViewModel(context, presenter.hotelDetailView.viewmodel.scrollToRoom, presenter.hotelDetailView.viewmodel.hotelSoldOut, presenter.hotelDetailView.viewmodel.getLOB())
        presenter
    }

    val reviewsView: HotelReviewsView by lazy {
        val viewStub = findViewById(R.id.reviews_stub) as ViewStub
        val presenter = viewStub.inflate() as HotelReviewsView
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
                bundleSlidingWidget.setVisibility(!forward)
            }
        }
        addTransition(transition)
    }

    val reviewsObserver: Observer<HotelOffersResponse> = endlessObserver { hotel ->
        reviewsView.viewModel = HotelReviewsViewModel(getContext(), LineOfBusiness.PACKAGES)
        reviewsView.viewModel.hotelObserver.onNext(hotel)
        show(reviewsView)
    }
    val REGULAR_ANIMATION_DURATION = 400
    val loadingOverlay: LoadingOverlayWidget by bindView(R.id.details_loading_overlay)
    var selectedPackageHotel: Hotel by Delegates.notNull()

    init {
        Ui.getApplication(context).packageComponent().inject(this)
        View.inflate(getContext(), R.layout.package_hotel_presenter, this)
        bundleSlidingWidget.setupBundleViews(Constants.PRODUCT_HOTEL)
        bundleSlidingWidget.animationFinished.subscribe {
            resultsPresenter.viewmodel.hotelResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(Db.getPackageResponse()))
        }
        val currencyCode = Db.getPackageResponse().packageResult.packageOfferModels[0].price.packageTotalPrice.currencyCode
        val total = Money(BigDecimal(0), currencyCode)
        bundleSlidingWidget.bundlePriceWidget.viewModel.bundleTextLabelObservable.onNext(context.getString(R.string.search_bundle_total_text))
        val packageSavings = Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
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
        bundleSlidingWidget.bundleOverViewWidget.bundleHotelWidget.rowContainer.setOnClickListener {
            back()
        }
        bundleSlidingWidget.bundlePriceWidget.setOnClickListener {
            show(bundleSlidingWidget)
        }
        loadingOverlay.setBackground(R.color.packages_primary_color)
        val slidingBundleWidgetListener = SlidingBundleWidgetListener(bundleSlidingWidget, this)
        bundleSlidingWidget.bundlePriceWidget.setOnTouchListener(slidingBundleWidgetListener.onTouchListener)
    }

    val hotelSelectedObserver: Observer<Hotel> = endlessObserver { hotel ->
        selectedPackageHotel = hotel
        val params = Db.getPackageParams()
        getDetails(hotel.packageOfferModel.piid, hotel.hotelId, params.checkIn.toString(), params.checkOut.toString(), Db.getPackageSelectedRoom()?.ratePlanCode, Db.getPackageSelectedRoom()?.roomTypeCode, params.adults, params.children.firstOrNull())
        PackagesTracking().trackHotelMapCarouselPropertyClick()
        bundleSlidingWidget.updateBundleViews(Constants.PRODUCT_HOTEL)
    }

    fun isShowingBundle(): Boolean {
        val isShowingBundle = Strings.equals(currentState, SlidingBundleWidget::class.java.name)
        return isShowingBundle
    }

    private val hideBundlePriceOverviewObserver: Observer<Boolean> = endlessObserver { hide ->
        bundleSlidingWidget.visibility = if (hide) {
            bundleSlidingWidget.startAnimation(slideDownAnimation)
            GONE
        } else {
            bundleSlidingWidget.startAnimation(slideUpAnimation)
            VISIBLE
        }
    }

    private fun getDetails(piid: String, hotelId: String, checkIn: String, checkOut: String, ratePlanCode: String?, roomTypeCode: String?, numberOfAdultTravelers: Int, childTravelerAge: Int?) {
        loadingOverlay.visibility = View.VISIBLE
        loadingOverlay.animate(true)
        detailPresenter.hotelDetailView.viewmodel.paramsSubject.onNext(convertPackageToSearchParams(Db.getPackageParams(), resources.getInteger(R.integer.calendar_max_days_hotel_stay), resources.getInteger(R.integer.calendar_max_selectable_date_range)))
        val packageHotelOffers = packageServices.hotelOffer(piid, checkIn, checkOut, ratePlanCode, roomTypeCode, numberOfAdultTravelers, childTravelerAge)
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
            if (resultsPresenter.currentState == BaseHotelResultsPresenter.ResultsMap::class.java.name) {
                bundleSlidingWidget.visibility = if (forward) VISIBLE else GONE
            }
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
                trackSearchResult()
            }
        }
    }

    private val detailsToOverview = bundleSlidingWidget.addBundleTransitionFrom(HotelDetailPresenter::class.java.name)
    private val resultsToOverview = bundleSlidingWidget.addBundleTransitionFrom(PackageHotelResultsPresenter::class.java.name)

    private val selectedRoomObserver = endlessObserver<HotelOffersResponse.HotelRoomResponse> { offer ->
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
                resultsPresenter.viewmodel.paramsSubject.onNext(convertPackageToSearchParams(Db.getPackageParams(), resources.getInteger(R.integer.calendar_max_days_hotel_stay), resources.getInteger(R.integer.calendar_max_package_selectable_date_range)))
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

    private fun View.setVisibility(forward: Boolean) {
        this.visibility = if (forward) View.VISIBLE else View.GONE
    }
}