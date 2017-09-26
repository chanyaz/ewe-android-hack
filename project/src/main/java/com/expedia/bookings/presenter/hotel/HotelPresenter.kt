package com.expedia.bookings.presenter.hotel

import android.animation.ArgbEvaluator
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.animation.TransitionElement
import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.HotelItinDetailsResponse
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.hotel.deeplink.HotelDeepLinkHandler
import com.expedia.bookings.hotel.deeplink.HotelLandingPage
import com.expedia.bookings.hotel.util.HotelInfoManager
import com.expedia.bookings.hotel.util.HotelSearchManager
import com.expedia.bookings.hotel.util.HotelSuggestionManager
import com.expedia.bookings.hotel.vm.HotelResultsViewModel
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.services.ClientLogServices
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.services.ItinTripServices
import com.expedia.bookings.services.ReviewsServices
import com.expedia.bookings.tracking.hotel.ClientLogTracker
import com.expedia.bookings.tracking.hotel.HotelSearchTrackingDataBuilder
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.tracking.hotel.PageUsableData
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.ClientLogConstants
import com.expedia.bookings.utils.ProWizardBucketCache
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.HotelMapCarouselAdapter
import com.expedia.bookings.widget.LoadingOverlayWidget
import com.expedia.bookings.widget.shared.WebCheckoutView
import com.expedia.ui.HotelActivity.Screen
import com.expedia.util.endlessObserver
import com.expedia.util.setInverseVisibility
import com.expedia.vm.GeocodeSearchModel
import com.expedia.vm.HotelCheckoutViewModel
import com.expedia.vm.HotelConfirmationViewModel
import com.expedia.vm.HotelCreateTripViewModel
import com.expedia.vm.HotelErrorViewModel
import com.expedia.vm.HotelMapViewModel
import com.expedia.vm.HotelPresenterViewModel
import com.expedia.vm.HotelReviewsViewModel
import com.expedia.vm.HotelSearchViewModel
import com.expedia.vm.HotelWebCheckoutViewViewModel
import com.expedia.vm.hotel.HotelDetailViewModel
import com.google.android.gms.maps.MapView
import com.mobiata.android.Log
import rx.Observable
import rx.Observer
import rx.Subscription
import java.util.Date
import javax.inject.Inject
import kotlin.properties.Delegates

// declared open for mocking purposes in tests (see: HotelDeeplinkHandlerTest)
open class HotelPresenter(context: Context, attrs: AttributeSet?) : Presenter(context, attrs) {

    lateinit var reviewServices: ReviewsServices
        @Inject set

    lateinit var hotelServices: HotelServices
        @Inject set

    lateinit var clientLogServices: ClientLogServices
        @Inject set

    lateinit var paymentModel: PaymentModel<HotelCreateTripResponse>
        @Inject set

    lateinit var hotelClientLogTracker: ClientLogTracker
        @Inject set

    lateinit var searchTrackingBuilder: HotelSearchTrackingDataBuilder
        @Inject set

    lateinit var itinTripServices: ItinTripServices
        @Inject set

    lateinit var hotelSearchManager: HotelSearchManager
        @Inject set

    lateinit var hotelInfoManager: HotelInfoManager
        @Inject set

    val eventName = ClientLogConstants.REGULAR_SEARCH_RESULTS

    var hotelDetailViewModel: HotelDetailViewModel by Delegates.notNull()
    var hotelSearchParams: HotelSearchParams by Delegates.notNull()
    val resultsMapView: MapView by bindView(R.id.map_view)
    val detailsMapView: MapView by bindView(R.id.details_map_view)
    val pageUsableData = PageUsableData()

    val bookingSuccessDialog: android.app.AlertDialog by lazy {
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.booking_successful))
        builder.setMessage(context.getString(R.string.check_your_email_for_itin))
        builder.setPositiveButton(context.getString(R.string.ok), { dialog, which ->
            (context as Activity).finish()
            dialog.dismiss()
        })
        builder.create()
    }

    val searchStub: ViewStub by bindView(R.id.search_stub)
    val searchPresenter: HotelSearchPresenter by lazy {
        val presenter = searchStub.inflate() as HotelSearchPresenter
        val searchViewModel = HotelSearchViewModel(context, hotelSearchManager)
        presenter.searchViewModel = searchViewModel

        searchViewModel.genericSearchSubject.subscribe { params -> handleGenericSearch(params) }
        searchViewModel.hotelIdSearchSubject.subscribe { params ->
            HotelTracking.trackPinnedSearch()
            handleHotelIdSearch(params, goToResults = AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelPinnedSearch))
        }
        searchViewModel.rawTextSearchSubject.subscribe { params -> handleGeoSearch(params) }

        presenter
    }

    val webCheckoutViewStub: ViewStub by bindView(R.id.web_checkout_view_stub)
    val webCheckoutView: WebCheckoutView by lazy {
        val webCheckoutView = webCheckoutViewStub.inflate() as WebCheckoutView
        val hotelWebCheckoutViewViewModel = HotelWebCheckoutViewViewModel(context)
        hotelWebCheckoutViewViewModel.createTripViewModel = HotelCreateTripViewModel(hotelServices, paymentModel)
        setUpCreateTripErrorHandling(hotelWebCheckoutViewViewModel.createTripViewModel)
        webCheckoutView.viewModel = hotelWebCheckoutViewViewModel

        hotelWebCheckoutViewViewModel.closeView.subscribe {
            webCheckoutView.clearHistory()
            hotelWebCheckoutViewViewModel.webViewURLObservable.onNext("about:blank")
        }

        hotelWebCheckoutViewViewModel.backObservable.subscribe {
            back()
        }

        hotelWebCheckoutViewViewModel.blankViewObservable.subscribe {
            super.back()
        }
        hotelWebCheckoutViewViewModel.fetchItinObservable.subscribe { bookedTripID ->
            itinTripServices.getTripDetails(bookedTripID, makeNewItinResponseObserver())
        }

        webCheckoutView
    }
    
    val errorPresenter: HotelErrorPresenter by bindView(R.id.widget_hotel_errors)
    val resultsStub: ViewStub by bindView(R.id.results_stub)
    val resultsPresenter: HotelResultsPresenter by lazy {
        val presenter = resultsStub.inflate() as HotelResultsPresenter
        val resultsStub = presenter.findViewById<FrameLayout>(R.id.stub_map)
        resultsMapView.visibility = View.VISIBLE
        removeView(resultsMapView)
        resultsStub.addView(resultsMapView)
        presenter.mapView = resultsMapView
        presenter.mapView.getMapAsync(presenter)
        presenter.viewModel = HotelResultsViewModel(getContext(), hotelSearchManager)

        presenter.viewModel.searchingForHotelsDateTime.subscribe {
            searchTrackingBuilder.markSearchApiCallMade()
        }
        presenter.viewModel.hotelResultsObservable.subscribe { hotelSearchResponse ->
            searchTrackingBuilder.markResultsProcessed()
            searchTrackingBuilder.searchParams(hotelSearchParams)
            searchTrackingBuilder.searchResponse(hotelSearchResponse)
        }
        presenter.viewModel.resultsReceivedDateTimeObservable.subscribe { dateTime ->
            searchTrackingBuilder.markApiResponseReceived()
        }
        presenter.adapter.allViewsLoadedTimeObservable.subscribe {
            searchTrackingBuilder.markResultsUsable()
            if (searchTrackingBuilder.isWorkComplete()) {
                val trackingData = searchTrackingBuilder.build()
                hotelClientLogTracker.trackResultsPerformance(trackingData.performanceData, ClientLogConstants.MATERIAL_HOTEL_SEARCH_PAGE, eventName)
                HotelTracking.trackHotelSearch(trackingData)
            }
        }
        presenter.hotelSelectedSubject.subscribe(hotelSelectedObserver)
        presenter.viewModel.searchApiErrorObservable.subscribe(errorPresenter.viewmodel.searchApiErrorObserver)
        presenter.viewModel.searchApiErrorObservable.subscribe { show(errorPresenter) }
        presenter.viewModel.showHotelSearchViewObservable.subscribe { show(searchPresenter, Presenter.FLAG_CLEAR_TOP) }
        presenter.showDefault()
        presenter
    }
    val detailsStub: ViewStub by bindView(R.id.details_stub)
    val detailPresenter: HotelDetailPresenter by lazy {
        val presenter = detailsStub.inflate() as HotelDetailPresenter
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
        presenter.hotelDetailView.viewmodel.changeDates.subscribe(goToSearchScreen)

        if (shouldUseWebCheckout()) {
            viewModel = HotelPresenterViewModel((webCheckoutView.viewModel as HotelWebCheckoutViewViewModel).createTripViewModel, null, presenter.hotelDetailView.viewmodel)
        } else {
            viewModel = HotelPresenterViewModel(checkoutPresenter.hotelCheckoutWidget.createTripViewmodel, checkoutPresenter.hotelCheckoutViewModel, presenter.hotelDetailView.viewmodel)
        }
        viewModel.selectedRoomSoldOut.subscribe(presenter.hotelDetailView.viewmodel.selectedRoomSoldOut)
        //ResultsPresenter doesn't inflate with roboelectric due to missing shadows for google map
        if (!ExpediaBookingApp.isRobolectric()) {
            viewModel.hotelSoldOutWithHotelId.subscribe((resultsPresenter.mapCarouselRecycler.adapter as HotelMapCarouselAdapter).hotelSoldOut)
            viewModel.hotelSoldOutWithHotelId.subscribe(resultsPresenter.adapter.hotelSoldOut)
            viewModel.hotelSoldOutWithHotelId.subscribe(resultsPresenter.mapViewModel.hotelSoldOutWithIdObserver)
        }

        presenter
    }

    val checkoutStub: ViewStub by bindView(R.id.checkout_stub)
    val checkoutPresenter: HotelCheckoutPresenter by lazy {
        val presenter = checkoutStub.inflate() as HotelCheckoutPresenter
        presenter.hotelCheckoutWidget.createTripViewmodel = HotelCreateTripViewModel(hotelServices, paymentModel)
        presenter.hotelCheckoutViewModel = HotelCheckoutViewModel(hotelServices, paymentModel)
        confirmationPresenter.hotelConfirmationViewModel = HotelConfirmationViewModel(context)
        presenter.hotelCheckoutViewModel.checkoutRequestStartTimeObservable.subscribe { startTime ->
            pageUsableData.markPageLoadStarted(startTime)
        }

        presenter.hotelCheckoutViewModel.checkoutResponseObservable.subscribe(confirmationPresenter.hotelConfirmationViewModel.checkoutResponseObservable)
        presenter.hotelCheckoutViewModel.checkoutParams.subscribe { presenter.cvv.enableBookButton(false) }
        presenter.hotelCheckoutViewModel.checkoutResponseObservable.subscribe(endlessObserver { checkoutResponse ->
            checkoutDialog.dismiss()
            show(confirmationPresenter, Presenter.FLAG_CLEAR_BACKSTACK)
        })

        presenter.hotelCheckoutViewModel.errorObservable.subscribe(errorPresenter.getViewModel().apiErrorObserver)
        presenter.hotelCheckoutViewModel.errorObservable.subscribe {
            checkoutDialog.dismiss()
            show(errorPresenter)
        }
        presenter.hotelCheckoutViewModel.noResponseObservable.subscribe {
            val retryFun = fun() {
                presenter.hotelCheckoutWidget.slideAllTheWayObservable.onNext(Unit)
            }
            val cancelFun = fun() {
                show(detailPresenter)
            }
            DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
        }
        presenter.hotelCheckoutViewModel.checkoutParams.subscribe {
            checkoutDialog.show()
        }
        presenter.hotelCheckoutWidget.slideAllTheWayObservable.withLatestFrom(paymentModel.paymentSplitsWithLatestTripTotalPayableAndTripResponse) { unit, paymentSplitsAndLatestTripResponse ->
            paymentSplitsAndLatestTripResponse.isCardRequired()
        }.filter { it }.subscribe {
            checkoutDialog.hide()
        }

        setUpCreateTripErrorHandling(presenter.hotelCheckoutWidget.createTripViewmodel)

        presenter.hotelCheckoutViewModel.priceChangeResponseObservable.subscribe(presenter.hotelCheckoutWidget.createTripResponseListener)
        presenter.hotelCheckoutViewModel.priceChangeResponseObservable.subscribe(endlessObserver { createTripResponse ->
            checkoutDialog.dismiss()
            show(presenter, Presenter.FLAG_CLEAR_TOP)
            presenter.show(presenter.hotelCheckoutWidget, Presenter.FLAG_CLEAR_TOP)
        })
        presenter.setSearchParams(hotelSearchParams)
        presenter.hotelCheckoutWidget.setSearchParams(hotelSearchParams)
        confirmationPresenter.hotelConfirmationViewModel.setSearchParams(hotelSearchParams)

        Observable.combineLatest(confirmationPresenter.hotelConfirmationViewModel.hotelConfirmationDetailsSetObservable, confirmationPresenter.hotelConfirmationViewModel.hotelConfirmationUISetObservable, {
            confirmationDetailsSet, confirmationUISet ->
            if (confirmationDetailsSet && confirmationUISet) {
                pageUsableData.markAllViewsLoaded(Date().time)
                HotelTracking.trackHotelPurchaseConfirmation(confirmationPresenter.hotelConfirmationViewModel.hotelCheckoutResponseObservable.value, confirmationPresenter.hotelConfirmationViewModel.percentagePaidWithPointsObservable.value,
                        confirmationPresenter.hotelConfirmationViewModel.totalAppliedRewardCurrencyObservable.value, hotelSearchParams.guests, confirmationPresenter.hotelConfirmationViewModel.couponCodeObservable.value, pageUsableData)
            }
        }).subscribe()

        presenter.hotelCheckoutWidget.backPressedAfterUserWithEffectiveSwPAvailableSignedOut.subscribe(goToSearchScreen)
        presenter
    }

    private fun setUpCreateTripErrorHandling(createTripViewModel: HotelCreateTripViewModel) {
        createTripViewModel.errorObservable.subscribe(errorPresenter.getViewModel().apiErrorObserver)
        createTripViewModel.errorObservable.subscribe { show(errorPresenter) }
        createTripViewModel.noResponseObservable.subscribe {
            val retryFun = fun() {
                if (shouldUseWebCheckout()) {
                    (webCheckoutView.viewModel as HotelWebCheckoutViewViewModel).fireCreateTripObservable.onNext(Unit)
                } else {
                    checkoutPresenter.hotelCheckoutWidget.doCreateTrip()
                }
            }
            val cancelFun = fun() {
                show(detailPresenter)
            }
            DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
        }
    }

    fun makeNewItinResponseObserver(): Observer<AbstractItinDetailsResponse> {
        confirmationPresenter.hotelConfirmationViewModel = HotelConfirmationViewModel(context, true)
        return object : Observer<AbstractItinDetailsResponse> {
            override fun onCompleted() {

            }

            override fun onNext(itinDetailsResponse: AbstractItinDetailsResponse) {
                confirmationPresenter.hotelConfirmationViewModel.itinDetailsResponseObservable.onNext(itinDetailsResponse as HotelItinDetailsResponse)
                show(confirmationPresenter, FLAG_CLEAR_BACKSTACK)
            }

            override fun onError(e: Throwable) {
                Log.d("Error fetching itin:" + e.stackTrace)
                bookingSuccessDialog.show()
            }

        }
    }

    val confirmationPresenter: HotelConfirmationPresenter by bindView(R.id.hotel_confirmation_presenter)

    val reviewsView: HotelReviewsView by lazy {
        val viewStub = findViewById<ViewStub>(R.id.reviews_stub)
        val presenter = viewStub.inflate() as HotelReviewsView
        presenter.hotelReviewsTabbar.slidingTabLayout.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                HotelTracking.trackHotelReviewsCategories(tab.position)
            }
        })
        presenter.reviewServices = reviewServices
        presenter
    }

    val loadingOverlay: LoadingOverlayWidget by bindView(R.id.details_loading_overlay)
    val ANIMATION_DURATION = 400
    val geoCodeSearchModel = GeocodeSearchModel(context)
    private val checkoutDialog = ProgressDialog(context)
    var viewModel: HotelPresenterViewModel by Delegates.notNull()

    init {
        Ui.getApplication(getContext()).hotelComponent().inject(this)

        initDetailViewModel()

        geoCodeSearchModel.geoResults.subscribe { geoResults ->
            fun triggerNewSearch(selectedResultIndex: Int) {
                val newHotelSearchParams = hotelSearchParams
                val geoLocation = geoResults[selectedResultIndex]
                newHotelSearchParams.suggestion.coordinates.lat = geoLocation.latitude
                newHotelSearchParams.suggestion.coordinates.lng = geoLocation.longitude
                newHotelSearchParams.suggestion.type = "GOOGLE_SUGGESTION_SEARCH"
                // trigger search with selected geoLocation
                handleGenericSearch(newHotelSearchParams)
            }

            if (geoResults.count() > 0) {
                val freeformLocations = StrUtils.formatAddresses(geoResults)
                val builder = AlertDialog.Builder(context)
                builder.setTitle(R.string.ChooseLocation)
                val dialogItemClickListener = DialogInterface.OnClickListener { dialog, which ->
                    triggerNewSearch(which)
                    HotelTracking.trackGeoSuggestionClick()
                }
                builder.setItems(freeformLocations, dialogItemClickListener)
                val alertDialog = builder.create()
                alertDialog.show()
            }
        }
        checkoutDialog.setMessage(resources.getString(R.string.booking_loading))
        checkoutDialog.setCancelable(false)
        checkoutDialog.isIndeterminate = true
    }

    private fun shouldUseWebCheckout() = PointOfSale.getPointOfSale().shouldShowWebCheckout() ||
                                        (PointOfSale.getPointOfSale().isHotelsWebCheckoutABTestEnabled
                                                && AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelsWebCheckout))

    fun setDefaultTransition(screen: Screen) {
        val defaultTransition = when (screen) {
            Screen.DETAILS -> defaultDetailsTransition
            Screen.RESULTS -> defaultResultsTransition
            else -> defaultSearchTransition
        }

        // #6626: protects us from deeplink logic adding different default transition when app is
        // already running (and has state) in background
        if (!hasDefaultTransition()) {
            addDefaultTransition(defaultTransition)
        } else {
            Log.w("You can only set defaultTransition once. (default transition:" + getDefaultTransition() + ")")
        }

        if (screen != Screen.DETAILS && screen != Screen.RESULTS) {
            show(searchPresenter)
        }
    }

    fun handleDeepLink(params: HotelSearchParams?, landingPage: HotelLandingPage?) {
        deepLinkHandler.handleNavigationViaDeepLink(params, landingPage)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        addTransition(searchToResults)
        addTransition(searchToDetails)
        addTransition(resultsToDetail)
        addTransition(detailsToReview)
        addTransition(resultsToError)
        addTransition(searchToError)
        addTransition(detailsToError)

        if (shouldUseWebCheckout()) {
            addTransition(detailsToWebCheckoutView)
            addTransition(webCheckoutViewToConfirmation)
            addTransition(webCheckoutViewToError)
        } else {
            addTransition(detailsToCheckout)
            addTransition(checkoutToConfirmation)
            addTransition(checkoutToError)
            addTransition(checkoutToSearch)
        }

        setUpErrorPresenter()
        loadingOverlay.setBackgroundColor(ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color)))
    }

    private fun setUpErrorPresenter() {
        errorPresenter.hotelDetailViewModel = hotelDetailViewModel
        errorPresenter.viewmodel = HotelErrorViewModel(context)
        errorPresenter.getViewModel().searchErrorObservable.subscribe {
            searchPresenter.resetSearchOptions()
            show(searchPresenter, FLAG_CLEAR_BACKSTACK)
            searchPresenter.showDefault()
        }
        errorPresenter.viewmodel.defaultErrorObservable.subscribe {
            show(searchPresenter, FLAG_CLEAR_BACKSTACK)
            searchPresenter.showDefault()
        }
        errorPresenter.getViewModel().filterNoResultsObservable.subscribe {
            resultsPresenter.showFilterCachedResults()
            show(resultsPresenter, FLAG_CLEAR_TOP)
        }
        errorPresenter.getViewModel().pinnedNotFoundToNearByHotelObservable.subscribe {
            resultsPresenter.showCachedResults()
            show(resultsPresenter, FLAG_CLEAR_TOP)
        }

        errorPresenter.viewmodel.checkoutCardErrorObservable.subscribe {
            show(checkoutPresenter, FLAG_CLEAR_TOP)
            checkoutPresenter.show(checkoutPresenter.hotelCheckoutWidget, FLAG_CLEAR_TOP)
            checkoutPresenter.hotelCheckoutWidget.paymentInfoCardView.cardInfoContainer.performClick()
        }

        errorPresenter.viewmodel.checkoutPaymentFailedObservable.subscribe {
            show(checkoutPresenter, FLAG_CLEAR_TOP)
            checkoutPresenter.show(checkoutPresenter.hotelCheckoutWidget, FLAG_CLEAR_TOP)
            checkoutPresenter.hotelCheckoutWidget.paymentInfoCardView.cardInfoContainer.performClick()
        }

        errorPresenter.viewmodel.checkoutAlreadyBookedObservable.subscribe {
            NavUtils.goToItin(context)
        }

        errorPresenter.viewmodel.soldOutObservable.subscribe {
            show(detailPresenter, FLAG_CLEAR_TOP)
        }


        errorPresenter.viewmodel.sessionTimeOutObservable.subscribe {
            show(searchPresenter, FLAG_CLEAR_BACKSTACK)
            searchPresenter.showDefault()
        }

        errorPresenter.viewmodel.checkoutTravelerErrorObservable.subscribe {
            show(checkoutPresenter, FLAG_CLEAR_TOP)
            checkoutPresenter.hotelCheckoutWidget.slideWidget.resetSlider()
            checkoutPresenter.hotelCheckoutWidget.mainContactInfoCardView.setExpanded(true, true)
            checkoutPresenter.show(checkoutPresenter.hotelCheckoutWidget, FLAG_CLEAR_TOP)
        }

        errorPresenter.viewmodel.checkoutUnknownErrorObservable.subscribe {
            show(checkoutPresenter, FLAG_CLEAR_TOP)
            checkoutPresenter.hotelCheckoutWidget.slideWidget.resetSlider()
            checkoutPresenter.show(checkoutPresenter.hotelCheckoutWidget, FLAG_CLEAR_TOP)
        }

        errorPresenter.viewmodel.productKeyExpiryObservable.subscribe {
            show(searchPresenter, FLAG_CLEAR_TOP)
        }

        geoCodeSearchModel.errorObservable.subscribe(errorPresenter.getViewModel().apiErrorObserver)
        geoCodeSearchModel.errorObservable.subscribe { show(errorPresenter) }
    }

    private val defaultSearchTransition = object : Presenter.DefaultTransition(HotelSearchPresenter::class.java.name) {
        override fun endTransition(forward: Boolean) {
            searchPresenter.visibility = View.VISIBLE
            if (!ProWizardBucketCache.isBucketed(context)) {
                searchPresenter.showSuggestionState(selectOrigin = false)
            }
            searchPresenter.resetSuggestionTracking()
            HotelTracking.trackHotelSearchBox((searchPresenter.getSearchViewModel() as HotelSearchViewModel).shopWithPointsViewModel.swpEffectiveAvailability.value)
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
                backStack.push(searchPresenter)
            }
        }
    }

    private val searchBackgroundColor = TransitionElement(ContextCompat.getColor(context, R.color.search_anim_background), Color.TRANSPARENT)
    private val defaultResultsTransition = object : Presenter.DefaultTransition(HotelResultsPresenter::class.java.name) {

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
            backStack.push(searchPresenter)
        }
    }

    private val searchArgbEvaluator = ArgbEvaluator()
    private val searchToResults = object : Presenter.Transition(HotelSearchPresenter::class.java, HotelResultsPresenter::class.java, AccelerateDecelerateInterpolator(), 500) {

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            loadingOverlay.visibility = View.GONE
            searchPresenter.visibility = View.VISIBLE
            resultsPresenter.visibility = View.VISIBLE
            searchPresenter.setBackgroundColor(searchBackgroundColor.start)
            searchPresenter.animationStart(!forward)
            resultsPresenter.animationStart()
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            searchPresenter.animationUpdate(f, !forward)
            if (forward) {
                searchPresenter.setBackgroundColor(searchArgbEvaluator.evaluate(f, searchBackgroundColor.start, searchBackgroundColor.end) as Int)
            } else {
                searchPresenter.setBackgroundColor(searchArgbEvaluator.evaluate(f, searchBackgroundColor.end, searchBackgroundColor.start) as Int)
            }
            resultsPresenter.animationUpdate(f, !forward)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            searchPresenter.setBackgroundColor(if (forward) searchBackgroundColor.end else searchBackgroundColor.start)
            if (!forward) searchPresenter.resetSuggestionTracking()
            searchPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            resultsPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            resultsPresenter.animationFinalize(forward, true)
            searchPresenter.animationFinalize(forward)
            if (!forward) HotelTracking.trackHotelSearchBox((searchPresenter.getSearchViewModel() as HotelSearchViewModel).shopWithPointsViewModel.swpEffectiveAvailability.value)
        }
    }

    private val resultsToDetail = object : Presenter.Transition(HotelResultsPresenter::class.java.name, HotelDetailPresenter::class.java.name, DecelerateInterpolator(), ANIMATION_DURATION) {
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
                resultsPresenter.recyclerView.adapter.notifyDataSetChanged()
            }
        }
    }

    private val webCheckoutViewToError = object : Presenter.Transition(WebCheckoutView::class.java.name, HotelErrorPresenter::class.java.name, DecelerateInterpolator(), ANIMATION_DURATION) {

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            webCheckoutView.visibility = View.VISIBLE
            errorPresenter.visibility = View.VISIBLE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            errorPresenter.animationUpdate(f, !forward)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            webCheckoutView.setInverseVisibility(forward)
            errorPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            errorPresenter.animationFinalize()
        }
    }

    private val checkoutToError = object : Presenter.Transition(HotelCheckoutPresenter::class.java.name, HotelErrorPresenter::class.java.name, DecelerateInterpolator(), ANIMATION_DURATION) {

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            checkoutPresenter.visibility = View.VISIBLE
            errorPresenter.visibility = View.VISIBLE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            errorPresenter.animationUpdate(f, !forward)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            checkoutPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            errorPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            errorPresenter.animationFinalize()
        }
    }

    private val detailsToWebCheckoutView = object : Transition(HotelDetailPresenter::class.java, WebCheckoutView::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            detailPresenter.setInverseVisibility(forward)
            webCheckoutView.toolbar.visibility = if (forward) View.VISIBLE else View.GONE
            webCheckoutView.visibility = if (forward) View.VISIBLE else View.GONE
            AccessibilityUtil.setFocusToToolbarNavigationIcon(webCheckoutView.toolbar)
        }
    }

    private val webCheckoutViewToConfirmation = object : Transition(WebCheckoutView::class.java, HotelConfirmationPresenter::class.java) {
        override fun endTransition(forward: Boolean) {
            if (forward) {
                confirmationPresenter.visibility = View.VISIBLE
                webCheckoutView.visibility = View.GONE
                AccessibilityUtil.delayFocusToToolbarNavigationIcon(confirmationPresenter.toolbar, 300)
            }
        }
    }

    private val searchToError = object : Presenter.Transition(HotelSearchPresenter::class.java.name, HotelErrorPresenter::class.java.name, DecelerateInterpolator(), ANIMATION_DURATION) {

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            loadingOverlay.visibility = View.GONE
            searchPresenter.visibility = View.VISIBLE
            errorPresenter.visibility = View.VISIBLE
            searchPresenter.animationStart(!forward)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            searchPresenter.animationUpdate(f, !forward)
            errorPresenter.animationUpdate(f, !forward)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            searchPresenter.setBackgroundColor(if (forward) searchBackgroundColor.end else searchBackgroundColor.start)
            searchPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            errorPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            searchPresenter.animationFinalize(forward)
            errorPresenter.animationFinalize()
            if (!forward) searchPresenter.resetSuggestionTracking()
            if (!forward) HotelTracking.trackHotelSearchBox((searchPresenter.getSearchViewModel() as HotelSearchViewModel).shopWithPointsViewModel.swpEffectiveAvailability.value)
        }
    }

    private val searchToDetails = object : ScaleTransition(this, HotelSearchPresenter::class.java, HotelDetailPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            if (!forward) {
                detailPresenter.hotelDetailView.resetViews()
            }
            loadingOverlay.visibility = View.GONE
            searchPresenter.animationStart(!forward)
            searchPresenter.animationFinalize(forward)
            searchPresenter.setBackgroundColor(searchBackgroundColor.start)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            searchPresenter.animationUpdate(f, !forward)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (forward) {
                detailPresenter.hotelDetailView.refresh()
                detailPresenter.hotelDetailView.viewmodel.addViewsAfterTransition()
            } else {
                searchPresenter.resetSuggestionTracking()
                HotelTracking.trackHotelSearchBox((searchPresenter.getSearchViewModel() as HotelSearchViewModel).shopWithPointsViewModel.swpEffectiveAvailability.value)
            }
        }
    }

    private val resultsToError = ScaleTransition(this, HotelResultsPresenter::class.java, HotelErrorPresenter::class.java)

    private val detailsToCheckout = object : ScaleTransition(this, HotelDetailPresenter::class.java, HotelCheckoutPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            checkoutDialog.hide()
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) {
                trackHotelDetail()
            }
        }

    }

    private val checkoutToSearch = object : Presenter.Transition(HotelSearchPresenter::class.java, HotelCheckoutPresenter::class.java, AccelerateDecelerateInterpolator(), 500) {

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            if (!forward) {
                detailPresenter.hotelDetailView.resetViews()
            }
            loadingOverlay.visibility = View.GONE
            searchPresenter.visibility = View.VISIBLE
            checkoutPresenter.visibility = View.VISIBLE
            searchPresenter.setBackgroundColor(searchBackgroundColor.start)
            searchPresenter.animationStart(!forward)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            searchPresenter.animationUpdate(f, !forward)
            if (forward) {
                searchPresenter.setBackgroundColor(searchArgbEvaluator.evaluate(f, searchBackgroundColor.start, searchBackgroundColor.end) as Int)
            } else {
                searchPresenter.setBackgroundColor(searchArgbEvaluator.evaluate(f, searchBackgroundColor.end, searchBackgroundColor.start) as Int)
            }
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) searchPresenter.resetSuggestionTracking()
            searchPresenter.setBackgroundColor(if (forward) searchBackgroundColor.end else searchBackgroundColor.start)
            searchPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            checkoutPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            searchPresenter.animationFinalize(forward)
        }
    }

    private val detailsToError = object : ScaleTransition(this, HotelDetailPresenter::class.java, HotelErrorPresenter::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) {
                trackHotelDetail()
            }
        }
    }

    private val checkoutToConfirmation = object : ScaleTransition(this, HotelCheckoutPresenter::class.java, HotelConfirmationPresenter::class.java) {
        override fun endTransition(forward: Boolean) {
            if (forward) {
                checkoutPresenter.visibility = GONE
                AccessibilityUtil.delayFocusToToolbarNavigationIcon(confirmationPresenter.toolbar, 300)
            }
        }
    }
    private val detailsToReview = object : ScaleTransition(this, HotelDetailPresenter::class.java, HotelReviewsView::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (forward) {
                reviewsView.transitionFinished()
            } else {
                trackHotelDetail()
            }
        }
    }

    val goToSearchScreen: Observer<Unit> = endlessObserver {
        show(searchPresenter, Presenter.FLAG_CLEAR_TOP)
    }

    val reviewsObserver: Observer<HotelOffersResponse> = endlessObserver { hotel ->
        reviewsView.viewModel = HotelReviewsViewModel(getContext())
        reviewsView.viewModel.hotelObserver.onNext(hotel)
        show(reviewsView)
    }

    val hotelSelectedObserver: Observer<Hotel> = endlessObserver { hotel ->
        detailPresenter.hotelDetailView.viewmodel.hotelSelectedObservable.onNext(Unit)
        //If hotel is known to be "Sold Out", simply show the Hotel Details Screen in "Sold Out" state, otherwise fetch Offers and show those as well
        showDetails(hotel.hotelId)
        HotelTracking.trackHotelCarouselClick()
    }

    fun handleGenericSearch(params: HotelSearchParams) {
        updateSearchParams(params)

        resultsPresenter.resetListOffset()
        show(resultsPresenter, Presenter.FLAG_CLEAR_TOP)
        resultsPresenter.viewModel.paramsSubject.onNext(params)
    }

    private fun handleHotelIdSearch(params: HotelSearchParams, goToResults: Boolean = false) {
        updateSearchParams(params)

        if (goToResults) {
            setDefaultTransition(Screen.RESULTS)
            resultsPresenter.resetListOffset()
            show(resultsPresenter, Presenter.FLAG_CLEAR_TOP)
            resultsPresenter.viewModel.paramsSubject.onNext(params)
        } else {
            setDefaultTransition(Screen.DETAILS)
            showDetails(params.suggestion.hotelId)
        }
    }

    private fun handleGeoSearch(params: HotelSearchParams) {
        updateSearchParams(params)
        geoCodeSearchModel.searchObserver.onNext(params)
    }

    private fun updateSearchParams(params: HotelSearchParams) {
        hotelSearchParams = params
        errorPresenter.getViewModel().paramsSubject.onNext(params)
    }

    private fun initDetailViewModel() {
        hotelDetailViewModel = HotelDetailViewModel(context, hotelInfoManager)

        hotelDetailViewModel.fetchInProgressSubject.subscribe {
            loadingOverlay.visibility = View.VISIBLE
            loadingOverlay.animate(true)
        }

        hotelDetailViewModel.fetchCancelledSubject.subscribe {
            loadingOverlay.visibility = View.GONE
            show(searchPresenter)
        }

        hotelDetailViewModel.hotelOffersSubject.subscribe { response ->
            loadingOverlay.animate(false)
            loadingOverlay.visibility = View.GONE
            if (currentState != detailPresenter::class.java.name) {
                show(detailPresenter)
                detailPresenter.showDefault()
            } else {
                // change dates just update the views.  todo this is terrible fix eventually
                hotelDetailViewModel.addViewsAfterTransition()
            }
            detailPresenter.hotelMapView.viewmodel.offersObserver.onNext(response)
        }


        hotelDetailViewModel.roomSelectedSubject.subscribe { offer ->
            checkoutPresenter.hotelCheckoutWidget.markRoomSelected()
            if (shouldUseWebCheckout()) {
                val webCheckoutViewModel = webCheckoutView.viewModel as HotelWebCheckoutViewViewModel
                webCheckoutViewModel.hotelSearchParamsObservable.onNext(hotelSearchParams)
                webCheckoutViewModel.offerObservable.onNext(offer)
                show(webCheckoutView)
            } else {
                checkoutPresenter.hotelCheckoutWidget.couponCardView.viewmodel.hasDiscountObservable.onNext(false)
                checkoutPresenter.setSearchParams(hotelSearchParams)
                checkoutPresenter.hotelCheckoutWidget.setSearchParams(hotelSearchParams)
                checkoutPresenter.showCheckout(offer)
                show(checkoutPresenter)
            }
        }
    }

    private fun showDetails(hotelId: String) {
        hotelDetailViewModel.fetchOffers(hotelSearchParams, hotelId)
    }

    private val deepLinkHandler: HotelDeepLinkHandler by lazy {
        val manager = HotelSuggestionManager(Ui.getApplication(context).hotelComponent().suggestionsService())

        val handler = HotelDeepLinkHandler(context, manager)
        handler.hotelSearchDeepLinkSubject.subscribe { params ->
            updateSearchForDeepLink(params)
            setDefaultTransition(Screen.RESULTS)
            handleGenericSearch(params)
        }
        handler.hotelIdToResultsSubject.subscribe { params ->
            updateSearchForDeepLink(params)
            handleHotelIdSearch(params, goToResults = true)
        }

        var subscription: Subscription? = null
        handler.hotelIdToDetailsSubject.subscribe { params ->
            updateSearchForDeepLink(params)
            subscription = hotelInfoManager.infoSuccessSubject.subscribe { offerResponse ->
                if (hotelSearchParams.suggestion.type == "HOTEL") {
                    searchPresenter.getSearchViewModel().locationTextObservable.onNext(offerResponse.hotelName)
                }
                subscription?.unsubscribe()
            }
            handleHotelIdSearch(params, goToResults = false)
        }

        handler.deepLinkInvalidSubject.subscribe {
            setDefaultTransition(Screen.SEARCH)
        }
        handler
    }

    private fun updateSearchForDeepLink(params: HotelSearchParams) {
        searchPresenter.searchViewModel.destinationLocationObserver.onNext(params.suggestion)
        searchPresenter.selectTravelers(TravelerParams(params.adults, params.children, emptyList(), emptyList()))
        searchPresenter.searchViewModel.datesUpdated(params.checkIn, params.checkOut)
        searchPresenter.selectDates(params.checkIn, params.checkOut)
    }

    override fun back(): Boolean {
        if (currentState == WebCheckoutView::class.java.name) {
            webCheckoutView.back()
            return true
        }
        if (searchPresenter.back()) {
            return true
        }
        if (loadingOverlay.visibility != View.VISIBLE) {
            return super.back()
        }
        return true
    }

    private fun trackHotelDetail() {
        detailPresenter.hotelDetailView.viewmodel.trackHotelDetailLoad(viewModel.didLastCreateTripOrCheckoutResultInRoomSoldOut.value)
    }
}
