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
import android.widget.FrameLayout
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
import com.expedia.bookings.data.packages.PackagesPageUsableData
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.extensions.setAccessibilityHoverFocus
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.hotel.vm.PackageHotelResultsViewModel
import com.expedia.bookings.presenter.LeftToRightTransition
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
import com.expedia.bookings.utils.isBreadcrumbsMoveBundleOverviewPackagesEnabled
import com.expedia.bookings.utils.isMidAPIEnabled
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
        resultsMapView.visibility = View.VISIBLE
        removeView(resultsMapView)
        presenter.mapWidget.setMapView(resultsMapView)
        presenter.viewModel = PackageHotelResultsViewModel(context)
        presenter.hotelSelectedSubject.subscribe { hotel ->
            val params = Db.sharedInstance.packageParams
            params.latestSelectedOfferInfo.hotelId = hotel.hotelId
            params.latestSelectedOfferInfo.flightPIID = Db.getPackageResponse().getFlightPIIDFromSelectedHotel(hotel.hotelPid)
            params.latestSelectedOfferInfo.productOfferPrice = hotel.packageOfferModel.price

            PackagesTracking().trackHotelMapCarouselPropertyClick()
            hotelSelectedObserver.onNext(hotel)
        }
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
        detailsViewModel.reviewsDataObservable.subscribe(reviewsOfferObserver)
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

    private val slidingTabLayoutListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabSelected(tab: TabLayout.Tab) {
            PackagesTracking().trackHotelReviewCategoryChange(tab.position)
        }
    }

    val reviewsView: HotelReviewsView by lazy {
        val viewStub = findViewById<ViewStub>(R.id.reviews_stub)
        val presenter = viewStub.inflate() as HotelReviewsView
        presenter.reviewServices = reviewServices
        presenter.viewModel = HotelReviewsViewModel(getContext(), LineOfBusiness.PACKAGES)
        presenter.hotelReviewsTabbar.slidingTabLayout.addOnTabSelectedListener(slidingTabLayoutListener)
        presenter
    }

    val reviewsOfferObserver: Observer<HotelOffersResponse> = endlessObserver { offer ->
        reviewsView.viewModel.hotelOfferObserver.onNext(offer)
        show(reviewsView)
    }

    val REGULAR_ANIMATION_DURATION = 400
    val loadingOverlay: LoadingOverlayWidget by bindView(R.id.details_loading_overlay)
    var selectedPackageHotel: Hotel by Delegates.notNull()

    init {
        Ui.getApplication(context).packageComponent().inject(this)
        View.inflate(getContext(), R.layout.package_hotel_presenter, this)

        ObservableOld.combineLatest(dataAvailableSubject, trackEventSubject, { packageSearchResponse, _ -> packageSearchResponse }).subscribe {
            PackagesPageUsableData.HOTEL_RESULTS.pageUsableData.markAllViewsLoaded()
            trackSearchResult(it)
        }
    }

    fun cleanup() {
        reviewsView.hotelReviewsTabbar.slidingTabLayout.removeOnTabSelectedListener(slidingTabLayoutListener)
    }

    fun updateOverviewAnimationDuration(duration: Int) {
        resultsToOverview.animationDuration = duration
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addTransition(resultsToDetail)
        addTransition(resultsToOverview)
        addTransition(detailsToReview)
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

        if (isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)) {
            bundleSlidingWidget.bundlePriceWidget.viewModel.bundleTextLabelObservable.onNext(context.getString(R.string.search_bundle_total_text_new))
        } else if (PointOfSale.getPointOfSale().pointOfSaleId != PointOfSaleId.JAPAN) {
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
            PackagesTracking().trackBundleWidgetTap()
        }
        loadingOverlay.setBackground(R.color.packages_primary_color)
        val slidingBundleWidgetListener = SlidingBundleWidgetListener(bundleSlidingWidget, this)
        bundleSlidingWidget.bundlePriceWidget.setOnTouchListener(slidingBundleWidgetListener.onTouchListener)

        if (isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)) {
            resultsPresenter.bundlePriceWidgetTop.setOnClickListener {
                show(bundleSlidingWidget)
                PackagesTracking().trackBundleWidgetTap()
            }
            bundleSlidingWidget.bundlePriceWidget.viewModel.perPersonTextLabelObservable.subscribeVisibility(resultsPresenter.bundlePriceWidgetTop.bundlePerPersonText)
            bundleSlidingWidget.bundlePriceFooter.viewModel.totalPriceObservable.subscribeTextAndVisibility(resultsPresenter.bundlePriceWidgetTop.bundleTotalPrice)
            bundleSlidingWidget.bundlePriceWidget.viewModel.bundleTextLabelObservable.subscribeText(resultsPresenter.bundlePriceWidgetTop.bundleTitleText)
            bundleSlidingWidget.bundlePriceWidget.viewModel.pricePerPersonObservable.subscribeText(resultsPresenter.bundlePriceWidgetTop.bundleTotalPrice)
        }
    }

    val hotelSelectedObserver: Observer<Hotel> = endlessObserver { hotel ->
        PackagesPageUsableData.HOTEL_INFOSITE.pageUsableData.markPageLoadStarted()
        selectedPackageHotel = hotel
        val params = Db.sharedInstance.packageParams
        val packageRoomsObservable = if (isMidAPIEnabled(context)) {
            getMIDRoomSearch(params)
        } else {
            getPSSRoomSearch(hotel.packageOfferModel.piid, params.startDate.toString(), params.endDate.toString(), Db.sharedInstance.packageSelectedRoom?.ratePlanCode, Db.sharedInstance.packageSelectedRoom?.roomTypeCode, params.adults, params.children.firstOrNull())
        }
        getDetails(hotel.hotelId, packageRoomsObservable)
        bundleSlidingWidget.updateBundleViews(Constants.PRODUCT_HOTEL)
    }

    fun isShowingBundle(): Boolean {
        val isShowingBundle = Strings.equals(currentState, SlidingBundleWidget::class.java.name)
        return isShowingBundle
    }

    private val hideBundlePriceOverviewObserver: Observer<Boolean> = endlessObserver { hide ->
        if (!hide) {
            bundleSlidingWidget.visibility = VISIBLE
            if (isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)) {
                resultsPresenter.bundlePriceWidgetTop.visibility = View.VISIBLE
            }
            bundleSlidingWidget.startAnimation(slideUpAnimation)
        } else {
            bundleSlidingWidget.startAnimation(slideDownAnimation)
            if (isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)) {
                resultsPresenter.bundlePriceWidgetTop.visibility = View.GONE
            }
        }
    }

    private fun getPSSRoomSearch(piid: String, checkIn: String, checkOut: String, ratePlanCode: String?, roomTypeCode: String?, numberOfAdultTravelers: Int, childTravelerAge: Int?): Observable<BundleHotelRoomResponse> {
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

    private fun getDetails(hotelId: String, packageRoomsObservable: Observable<BundleHotelRoomResponse>) {
        loadingOverlay.visibility = View.VISIBLE
        AccessibilityUtil.delayedFocusToView(loadingOverlay, 0)
        loadingOverlay.setAccessibilityHoverFocus()
        loadingOverlay.animate(true)
        bundleSlidingWidget.bundlePriceWidget.disable()
        bundleSlidingWidget.bundlePriceWidget.setOnTouchListener(null)
        bundleSlidingWidget.bundlePriceWidget.setOnClickListener(null)
        if (isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)) {
            resultsPresenter.bundlePriceWidgetTop.setOnClickListener(null)
        }
        detailPresenter.hotelDetailView.viewmodel.paramsSubject.onNext(convertPackageToSearchParams(Db.sharedInstance.packageParams, resources.getInteger(R.integer.calendar_max_days_package_stay), resources.getInteger(R.integer.max_calendar_selectable_date_range)))
        val hotelInfoObservable = packageServices.hotelInfo(hotelId)

        ObservableOld.zip(packageRoomsObservable, hotelInfoObservable,
                { packageRoomsResponse, hotelInfoResponse ->
                    Pair(packageRoomsResponse, hotelInfoResponse)
                }
        ).subscribe(makeResponseObserver())
    }

    private fun handleRoomResponseErrors(errorCode: ApiError.Code) {
        val activity = (context as Activity)
        val resultIntent = Intent()
        resultIntent.putExtra(Constants.PACKAGE_HOTEL_OFFERS_ERROR, errorCode.name)
        activity.setResult(Activity.RESULT_OK, resultIntent)
        activity.finish()
    }

    private fun makeResponseObserver(): DisposableObserver<Pair<BundleHotelRoomResponse, HotelOffersResponse>> {
        return object : DisposableObserver<Pair<BundleHotelRoomResponse, HotelOffersResponse>>() {
            override fun onError(throwable: Throwable) {
                if (throwable is HttpException) {
                    try {
                        val response = throwable.response().errorBody()
                        val midError = Gson().fromJson(response?.charStream(), MultiItemApiSearchResponse::class.java)
                        handleRoomResponseErrors(midError.roomResponseFirstErrorCode)
                    } catch (e: Exception) {
                        handleRoomResponseErrors(ApiError.Code.PACKAGE_SEARCH_ERROR)
                    }
                } else {
                    handleError(throwable)
                }
            }

            override fun onNext(t: Pair<BundleHotelRoomResponse, HotelOffersResponse>) {
                val (packageRoomsResponse, hotelInfoResponse) = t
                if (hotelInfoResponse.hasErrors()) {
                    handleRoomResponseErrors(hotelInfoResponse.firstError?.errorCode
                            ?: ApiError.Code.PACKAGE_SEARCH_ERROR)
                    return
                }
                val hotelOffers = HotelOffersResponse.convertToHotelOffersResponse(hotelInfoResponse, packageRoomsResponse.getBundleRoomResponse(), packageRoomsResponse.getHotelCheckInDate(), packageRoomsResponse.getHotelCheckOutDate())

                PackageResponseUtils.saveHotelOfferResponse(context, hotelOffers, PackageResponseUtils.RECENT_PACKAGE_HOTEL_OFFER_FILE)
                loadingOverlay.animate(false)
                detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.onNext(hotelOffers)
                detailPresenter.hotelMapView.viewmodel.offersObserver.onNext(hotelOffers)
                show(detailPresenter)
                detailPresenter.showDefault()
                PackagesPageUsableData.HOTEL_INFOSITE.pageUsableData.markAllViewsLoaded()
                reviewsView.viewModel.resetTracking()
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
                    PackagesTracking().trackBundleWidgetTap()
                }
                if (isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)) {
                    resultsPresenter.bundlePriceWidgetTop.setOnClickListener {
                        show(bundleSlidingWidget)
                        PackagesTracking().trackBundleWidgetTap()
                    }
                }
            }
            DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
        }
    }

    private val detailsToReview = object : ScaleTransition(this, HotelDetailPresenter::class.java, HotelReviewsView::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            reviewsView.endTransition(forward)
            if (forward) {
                AccessibilityUtil.setFocusToToolbarNavigationIcon(reviewsView.toolbar)
            } else {
                AccessibilityUtil.setFocusToToolbarNavigationIcon(detailPresenter.hotelDetailView.hotelDetailsToolbar.toolbar)
            }
        }
    }

    private val defaultResultsTransition = object : Presenter.DefaultTransition(PackageHotelResultsPresenter::class.java.name) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            resultsPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            resultsPresenter.animationFinalize()
            AccessibilityUtil.setFocusToToolbarNavigationIcon(resultsPresenter.toolbar)
        }
    }

    private val resultsToDetail = object : LeftToRightTransition(this, PackageHotelResultsPresenter::class.java, HotelDetailPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            hideBundlePriceOverviewObserver.onNext(forward)
            if (!forward) {
                detailPresenter.hotelDetailView.resetViews()
                val countryCode = PointOfSale.getPointOfSale().threeLetterCountryCode
                val currencyCode = CurrencyUtils.currencyForLocale(countryCode)
                val zero = Money(BigDecimal(0), currencyCode)
                bundleSlidingWidget.bundlePriceWidget.viewModel.pricePerPersonObservable.onNext(Money(BigDecimal("0.00"), currencyCode).formattedMoney)
                bundleSlidingWidget.bundlePriceFooter.viewModel.total.onNext(zero)
                bundleSlidingWidget.bundlePriceFooter.viewModel.savings.onNext(zero)
                Db.sharedInstance.clearPackageHotelRoomSelection()
            } else {
                detailPresenter.hotelDetailView.refresh()
            }
            if (resultsPresenter.currentState == BaseHotelResultsPresenter.ResultsMap::class.java.name) {
                bundleSlidingWidget.visibility = if (forward) VISIBLE else GONE
            }
            super.startTransition(forward)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            resultsPresenter.animationFinalize()
            detailPresenter.animationFinalize(forward)
            loadingOverlay.visibility = View.GONE

            if (!forward) {
                enableSlidingWidget()
                AccessibilityUtil.setFocusToToolbarNavigationIcon(resultsPresenter.toolbar)
                trackEventSubject.onNext(Unit)
                if (isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)) {
                    resultsPresenter.bundlePriceWidgetTop.setOnClickListener {
                        show(bundleSlidingWidget)
                        PackagesTracking().trackBundleWidgetTap()
                    }
                }
            }
        }

        private fun enableSlidingWidget() {
            bundleSlidingWidget.bundlePriceWidget.enable()
            val slidingBundleWidgetListener = SlidingBundleWidgetListener(bundleSlidingWidget, this@PackageHotelPresenter)
            bundleSlidingWidget.bundlePriceWidget.setOnTouchListener(slidingBundleWidgetListener.onTouchListener)
            bundleSlidingWidget.bundlePriceWidget.setOnClickListener {
                show(bundleSlidingWidget)
                PackagesTracking().trackBundleWidgetTap()
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
        val params = Db.sharedInstance.packageParams
        params.packagePIID = offer.productKey
        params.latestSelectedOfferInfo.ratePlanCode = offer.ratePlanCode
        params.latestSelectedOfferInfo.roomTypeCode = offer.roomTypeCode
        params.latestSelectedOfferInfo.inventoryType = offer.supplierType
        params.latestSelectedOfferInfo.hotelCheckInDate = Db.getPackageResponse().getHotelCheckInDate()
        params.latestSelectedOfferInfo.hotelCheckOutDate = Db.getPackageResponse().getHotelCheckOutDate()
        params.latestSelectedOfferInfo.productOfferPrice = Db.getPackageResponse().getCurrentOfferPrice()
        val activity = (context as AppCompatActivity)
        activity.setResult(Activity.RESULT_OK)
        activity.finish()
    }

    private fun updatePackagePrice(offer: HotelOffersResponse.HotelRoomResponse) {
        val response = Db.getPackageResponse()
        val currentOfferPrice = PackageOfferModel.PackagePrice()
        currentOfferPrice.packageTotalPrice = offer.rateInfo.chargeableRateInfo.packageTotalPrice
        currentOfferPrice.tripSavings = offer.rateInfo.chargeableRateInfo.packageSavings
        currentOfferPrice.pricePerPerson = offer.rateInfo.chargeableRateInfo.packagePricePerPerson
        response.setCurrentOfferPrice(currentOfferPrice)
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
                resultsPresenter.viewModel.paramsSubject.onNext(convertPackageToSearchParams(Db.sharedInstance.packageParams, resources.getInteger(R.integer.calendar_max_days_hotel_stay), resources.getInteger(R.integer.max_calendar_selectable_date_range)))
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
        PackagesTracking().trackHotelSearchResultLoad(response, PackagesPageUsableData.HOTEL_RESULTS.pageUsableData)
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
}
