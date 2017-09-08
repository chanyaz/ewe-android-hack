package com.expedia.bookings.presenter.packages

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.animation.AnimationListenerAdapter
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.hotels.convertPackageToSearchParams
import com.expedia.bookings.data.multiitem.BundleHotelRoomResponse
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.multiitem.MultiItemApiSearchResponse
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.hotel.vm.PackageHotelResultsViewModel
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.presenter.hotel.BaseHotelResultsPresenter
import com.expedia.bookings.presenter.hotel.HotelDetailPresenter
import com.expedia.bookings.presenter.hotel.HotelReviewsView
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.services.ReviewsServices
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.PackageResponseUtils
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isMidAPIEnabled
import com.expedia.bookings.utils.setAccessibilityHoverFocus
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.LoadingOverlayWidget
import com.expedia.bookings.widget.SlidingBundleWidget
import com.expedia.bookings.widget.SlidingBundleWidgetListener
import com.expedia.ui.PackageHotelActivity
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelMapViewModel
import com.expedia.vm.HotelReviewsViewModel
import com.expedia.vm.packages.PackageHotelDetailViewModel
import com.google.android.gms.maps.MapView
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
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

    val dataAvailableSubject = PublishSubject.create<BundleSearchResponse>()
    val trackEventSubject = PublishSubject.create<Unit>()

    val slideDownAnimation by lazy {
        val anim = AnimationUtils.loadAnimation(context, R.anim.slide_down)
        anim.duration = 500
        anim.setAnimationListener(object : AnimationListenerAdapter() {
            override fun onAnimationEnd(animation: Animation?) {
                bundleSlidingWidget.visibility = GONE
            }
        })
        anim
    }
    val slideUpAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_up)

    val resultsPresenter: PackageHotelResultsPresenter by lazy {
        val viewStub = findViewById<ViewStub>(R.id.results_stub)
        val presenter = viewStub.inflate() as PackageHotelResultsPresenter
        val resultsStub = presenter.findViewById<FrameLayout>(R.id.stub_map)
        resultsMapView.visibility = View.VISIBLE
        removeView(resultsMapView)
        resultsStub.addView(resultsMapView)
        presenter.mapView = resultsMapView
        presenter.mapView.getMapAsync(presenter)
        presenter.viewModel = PackageHotelResultsViewModel(context)
        presenter.hotelSelectedSubject.subscribe(hotelSelectedObserver)
        presenter.hideBundlePriceOverviewSubject.subscribe(hideBundlePriceOverviewObserver)
        presenter
    }

    val detailPresenter: HotelDetailPresenter by lazy {
        val viewStub = findViewById<ViewStub>(R.id.details_stub)
        val presenter = viewStub.inflate() as HotelDetailPresenter
        val detailsStub = presenter.hotelMapView.findViewById<FrameLayout>(R.id.stub_map)
        detailsMapView.visibility = View.VISIBLE
        removeView(detailsMapView)
        detailsStub.addView(detailsMapView)
        presenter.hotelMapView.mapView = detailsMapView
        presenter.hotelMapView.mapView.getMapAsync(presenter.hotelMapView)

        val detailsViewModel = PackageHotelDetailViewModel(context)

        detailsViewModel.roomSelectedSubject.subscribe(selectedRoomObserver)
        detailsViewModel.reviewsClickedWithHotelData.subscribe(reviewsObserver)
        detailsViewModel.vipAccessInfoObservable.subscribe(presenter.hotelVIPAccessInfoObserver)
        detailsViewModel.hotelRenovationObservable.subscribe(presenter.hotelRenovationObserver)
        detailsViewModel.mapClickedSubject.subscribe(presenter.hotelDetailsEmbeddedMapClickObserver)
        detailsViewModel.bundlePricePerPersonObservable.subscribe { pricePerPerson ->
            bundleSlidingWidget.bundlePriceWidget.viewModel.pricePerPerson.onNext(pricePerPerson)
        }
        detailsViewModel.bundleTotalPriceObservable.subscribe { totalPrice ->
            bundleSlidingWidget.bundlePriceFooter.viewModel.total.onNext(totalPrice)
        }
        detailsViewModel.bundleSavingsObservable.subscribe { savings ->
            bundleSlidingWidget.bundlePriceFooter.viewModel.savings.onNext(savings)
        }

        presenter.hotelDetailView.viewmodel = detailsViewModel
        presenter.hotelMapView.viewmodel = HotelMapViewModel(context, detailsViewModel.scrollToRoom, detailsViewModel.hotelSoldOut, detailsViewModel.getLOB())

        presenter
    }

    val reviewsView: HotelReviewsView by lazy {
        val viewStub = findViewById<ViewStub>(R.id.reviews_stub)
        val presenter = viewStub.inflate() as HotelReviewsView
        presenter.reviewServices = reviewServices
        presenter.hotelReviewsTabbar.slidingTabLayout.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                PackagesTracking().trackHotelReviewCategoryChange(tab.position)
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
                    AccessibilityUtil.setFocusToToolbarNavigationIcon(reviewsView.toolbar)
                } else {
                    AccessibilityUtil.setFocusToToolbarNavigationIcon(detailPresenter.hotelDetailView.hotelDetailsToolbar.toolbar)
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
    val REGULAR_ANIMATION_DURATION = 400
    val loadingOverlay: LoadingOverlayWidget by bindView(R.id.details_loading_overlay)
    var selectedPackageHotel: Hotel by Delegates.notNull()

    init {
        Ui.getApplication(context).packageComponent().inject(this)
        View.inflate(getContext(), R.layout.package_hotel_presenter, this)

        ObservableOld.combineLatest(dataAvailableSubject, trackEventSubject, { packageSearchResponse, trackEvent -> packageSearchResponse }).subscribe {
            trackSearchResult(it)
        }
    }

    fun updateOverviewAnimationDuration(duration: Int) {
        resultsToOverview.animationDuration = duration
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addTransition(resultsToDetail)
        addTransition(resultsToOverview)
        bundleSlidingWidget.setupBundleViews(Constants.PRODUCT_HOTEL)
        
        resultsPresenter.viewModel.hotelResultsObservable.subscribe {
            bundleSlidingWidget.bundleOverViewWidget.viewModel.hotelResultsObservable.onNext(Unit)
            bindData()
        }

        bundleSlidingWidget.animationFinished.subscribe {
            resultsPresenter.viewModel.hotelResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(Db.getPackageResponse()))
        }
    }

    private fun bindData() {
        dataAvailableSubject.onNext(Db.getPackageResponse())
        val currencyCode = Db.getPackageResponse().getCurrencyCode()
        if (PointOfSale.getPointOfSale().pointOfSaleId != PointOfSaleId.JAPAN) {
            bundleSlidingWidget.bundlePriceWidget.viewModel.bundleTextLabelObservable.onNext(context.getString(R.string.search_bundle_total_text))
        }
        val zero = Money(BigDecimal(0), currencyCode)
        bundleSlidingWidget.bundlePriceWidget.viewModel.pricePerPerson.onNext(zero)
        bundleSlidingWidget.bundlePriceFooter.viewModel.total.onNext(zero)
        bundleSlidingWidget.bundlePriceFooter.viewModel.savings.onNext(zero)
        if (ProductFlavorFeatureConfiguration.getInstance().shouldShowPackageIncludesView()) {
            bundleSlidingWidget.bundlePriceWidget.viewModel.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_flights_hotel))
            bundleSlidingWidget.bundlePriceFooter.viewModel.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_flights_hotel))
        }
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
        params.hotelId = hotel.hotelId
        params.latestSelectedProductTotalPrice = hotel.packageOfferModel.price.packageTotalPrice
        val packageHotelOffers = if (isMidAPIEnabled(context)) {
            getMIDRoomSearch(params)
        } else {
            getPSSRoomSearch(hotel.packageOfferModel.piid, hotel.hotelId, params.startDate.toString(), params.endDate.toString(), Db.getPackageSelectedRoom()?.ratePlanCode, Db.getPackageSelectedRoom()?.roomTypeCode, params.adults, params.children.firstOrNull())
        }
        getDetails(hotel.hotelId, packageHotelOffers)
        PackagesTracking().trackHotelMapCarouselPropertyClick()
        bundleSlidingWidget.updateBundleViews(Constants.PRODUCT_HOTEL)
    }

    fun isShowingBundle(): Boolean {
        val isShowingBundle = Strings.equals(currentState, SlidingBundleWidget::class.java.name)
        return isShowingBundle
    }

    private val hideBundlePriceOverviewObserver: Observer<Boolean> = endlessObserver { hide ->
        if (!hide) {
            bundleSlidingWidget.visibility = VISIBLE
            bundleSlidingWidget.startAnimation(slideUpAnimation)
        } else {
            bundleSlidingWidget.startAnimation(slideDownAnimation)
        }
    }

    private fun getPSSRoomSearch(piid: String, hotelId: String, checkIn: String, checkOut: String, ratePlanCode: String?, roomTypeCode: String?, numberOfAdultTravelers: Int, childTravelerAge: Int?): Observable<BundleHotelRoomResponse> {
        return packageServices
                .hotelOffer(piid, checkIn, checkOut, ratePlanCode, roomTypeCode, numberOfAdultTravelers, childTravelerAge)
                .map { packageHotelOffers ->
                    packageHotelOffers.setCheckInDate(checkIn)
                    packageHotelOffers.setCheckOutDate(checkOut)
                    packageHotelOffers
                }
    }

    private fun getMIDRoomSearch(params: PackageSearchParams): Observable<BundleHotelRoomResponse> {
        return packageServices
                .multiItemRoomSearch(params)
                .map { it }
    }

    private fun getDetails(hotelId: String, packageHotelOffers: Observable<BundleHotelRoomResponse>) {
        loadingOverlay.visibility = View.VISIBLE
        AccessibilityUtil.delayedFocusToView(loadingOverlay, 0)
        loadingOverlay.setAccessibilityHoverFocus()
        loadingOverlay.animate(true)
        bundleSlidingWidget.bundlePriceWidget.disable()
        bundleSlidingWidget.bundlePriceWidget.setOnTouchListener(null)
        bundleSlidingWidget.bundlePriceWidget.setOnClickListener(null)
        detailPresenter.hotelDetailView.viewmodel.paramsSubject.onNext(convertPackageToSearchParams(Db.getPackageParams(), resources.getInteger(R.integer.calendar_max_days_package_stay), resources.getInteger(R.integer.max_calendar_selectable_date_range)))
        val info = packageServices.hotelInfo(hotelId)

        ObservableOld.zip(packageHotelOffers.doOnError {}, info.doOnError {},
                { packageHotelOffers, info ->
                    if (packageHotelOffers.hasRoomResponseErrors()) {
                        handleRoomResponseErrors(packageHotelOffers.roomResponseFirstErrorCode)
                        return@zip
                    }
                    val hotelOffers = HotelOffersResponse.convertToHotelOffersResponse(info, packageHotelOffers.getBundleRoomResponse(), packageHotelOffers.getHotelCheckInDate(), packageHotelOffers.getHotelCheckOutDate())

                    PackageResponseUtils.saveHotelOfferResponse(context, hotelOffers, PackageResponseUtils.RECENT_PACKAGE_HOTEL_OFFER_FILE)
                    loadingOverlay.animate(false)
                    detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.onNext(hotelOffers)
                    detailPresenter.hotelMapView.viewmodel.offersObserver.onNext(hotelOffers)
                    show(detailPresenter)
                    detailPresenter.showDefault()
                }
        ).subscribe(makeErrorSubscriber(true))
    }

    private fun handleRoomResponseErrors(errorCode: ApiError.Code) {
        val activity = (context as AppCompatActivity)
        val resultIntent = Intent()
        resultIntent.putExtra(Constants.PACKAGE_HOTEL_OFFERS_ERROR, errorCode.name)
        activity.setResult(Activity.RESULT_OK, resultIntent)
        activity.finish()
    }

    private fun makeErrorSubscriber(showErrorDialog: Boolean): DisposableObserver<Any> {
        return object : DisposableObserver<Any>() {
            override fun onError(throwable: Throwable) {
                if (throwable is HttpException) {
                    try {
                        val response = throwable.response().errorBody()
                        val midError = Gson().fromJson(response?.charStream(), MultiItemApiSearchResponse::class.java)
                        handleRoomResponseErrors(midError.roomResponseFirstErrorCode)
                    } catch (e: Exception) {
                        handleRoomResponseErrors(ApiError.Code.PACKAGE_SEARCH_ERROR)
                    }
                } else if (showErrorDialog) {
                    handleError(throwable)
                }
            }

            override fun onNext(t: Any) {
            }

            override fun onComplete() {
            }
        }
    }

    private fun handleError(e: Throwable) {
        if (RetrofitUtils.isNetworkError(e)) {
            val retryFun = fun() {
                hotelSelectedObserver.onNext(selectedPackageHotel)
            }
            val cancelFun = fun() {
                back()
                loadingOverlay.visibility = View.GONE
                val slidingBundleWidgetListener = SlidingBundleWidgetListener(bundleSlidingWidget, this@PackageHotelPresenter)
                bundleSlidingWidget.bundlePriceWidget.enable()
                bundleSlidingWidget.bundlePriceWidget.setOnTouchListener(slidingBundleWidgetListener.onTouchListener)
                bundleSlidingWidget.bundlePriceWidget.setOnClickListener {
                    show(bundleSlidingWidget)
                }
            }
            DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
        }
    }

    private val defaultResultsTransition = object : Presenter.DefaultTransition(PackageHotelResultsPresenter::class.java.name) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            resultsPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            resultsPresenter.animationFinalize(forward)
            AccessibilityUtil.setFocusToToolbarNavigationIcon(resultsPresenter.toolbar)
        }
    }

    private val resultsToDetail = object : Presenter.Transition(PackageHotelResultsPresenter::class.java.name, HotelDetailPresenter::class.java.name, DecelerateInterpolator(), REGULAR_ANIMATION_DURATION) {
        private var detailsHeight: Int = 0

        override fun startTransition(forward: Boolean) {
            hideBundlePriceOverviewObserver.onNext(forward)
            if (!forward) {
                detailPresenter.hotelDetailView.resetViews()
                val countryCode = PointOfSale.getPointOfSale().threeLetterCountryCode
                val currencyCode = CurrencyUtils.currencyForLocale(countryCode)
                bundleSlidingWidget.bundlePriceWidget.viewModel.pricePerPersonObservable.onNext(Money(BigDecimal("0.00"), currencyCode).formattedMoney)
                Db.clearPackageHotelRoomSelection()
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
            if (!forward) {
                bundleSlidingWidget.bundlePriceWidget.enable()
                val slidingBundleWidgetListener = SlidingBundleWidgetListener(bundleSlidingWidget, this@PackageHotelPresenter)
                bundleSlidingWidget.bundlePriceWidget.setOnTouchListener(slidingBundleWidgetListener.onTouchListener)
                bundleSlidingWidget.bundlePriceWidget.setOnClickListener {
                    show(bundleSlidingWidget)
                }
            }
            if (forward) {
                detailPresenter.hotelDetailView.viewmodel.addViewsAfterTransition()
                AccessibilityUtil.setFocusToToolbarNavigationIcon(detailPresenter.hotelDetailView.hotelDetailsToolbar.toolbar)
            } else {
                AccessibilityUtil.setFocusToToolbarNavigationIcon(resultsPresenter.toolbar)
                trackEventSubject.onNext(Unit)
            }
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

    private val selectedRoomObserver = endlessObserver<HotelOffersResponse.HotelRoomResponse> { offer ->
        Db.setPackageSelectedHotel(selectedPackageHotel, offer)
        updatePackagePrice(offer)
        val params = Db.getPackageParams()
        params.packagePIID = offer.productKey
        params.ratePlanCode = offer.ratePlanCode
        params.roomTypeCode = offer.roomTypeCode
        params.latestSelectedProductTotalPrice = offer.rateInfo.chargeableRateInfo.packageTotalPrice
        val activity = (context as AppCompatActivity)
        activity.setResult(Activity.RESULT_OK)
        activity.finish()
    }

    private fun updatePackagePrice(offer: HotelOffersResponse.HotelRoomResponse) {
        var response = Db.getPackageResponse()
        val currentOffer = PackageOfferModel()
        currentOffer.price = PackageOfferModel.PackagePrice()
        currentOffer.price.packageTotalPrice = offer.rateInfo.chargeableRateInfo.packageTotalPrice
        currentOffer.price.tripSavings = offer.rateInfo.chargeableRateInfo.packageSavings
        response.setCurrentOfferModel(currentOffer)
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
                resultsPresenter.viewModel.paramsSubject.onNext(convertPackageToSearchParams(Db.getPackageParams(), resources.getInteger(R.integer.calendar_max_days_hotel_stay), resources.getInteger(R.integer.max_calendar_selectable_date_range)))
                trackEventSubject.onNext(Unit)
            }
            PackageHotelActivity.Screen.DETAILS_ONLY -> {
                //change hotel room
                resultsPresenter.hideBundlePriceOverview(true)
                addDefaultTransition(defaultDetailsTransition)
            }
        }
    }

    private fun trackSearchResult(response: BundleSearchResponse) {
        PackagesTracking().trackHotelSearchResultLoad(response)
    }

    private val defaultDetailsTransition = object : Presenter.DefaultTransition(HotelDetailPresenter::class.java.name) {
        override fun endTransition(forward: Boolean) {
            loadingOverlay.visibility = View.GONE
            detailPresenter.visibility = View.VISIBLE
            detailPresenter.translationY = 0f
            bundleSlidingWidget.visibility = View.GONE
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