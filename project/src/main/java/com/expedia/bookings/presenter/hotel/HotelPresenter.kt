package com.expedia.bookings.presenter.hotel

import android.animation.ArgbEvaluator
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.animation.TransitionElement
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.HotelFavoriteHelper
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.clientlog.ClientLog
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.services.ClientLogServices
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.services.ReviewsServices
import com.expedia.bookings.tracking.HotelTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.ClientLogConstants
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.WalletUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.HotelMapCarouselAdapter
import com.expedia.bookings.widget.LoadingOverlayWidget
import com.expedia.ui.HotelActivity.Screen
import com.expedia.util.endlessObserver
import com.expedia.vm.GeocodeSearchModel
import com.expedia.vm.HotelCheckoutViewModel
import com.expedia.vm.HotelConfirmationViewModel
import com.expedia.vm.HotelCreateTripViewModel
import com.expedia.vm.HotelErrorViewModel
import com.expedia.vm.HotelMapViewModel
import com.expedia.vm.HotelPresenterViewModel
import com.expedia.vm.HotelReviewsViewModel
import com.expedia.vm.HotelSearchViewModel
import com.expedia.vm.hotel.FavoriteButtonViewModel
import com.expedia.vm.hotel.HotelDetailViewModel
import com.expedia.vm.hotel.HotelResultsViewModel
import com.google.android.gms.maps.MapView
import com.mobiata.android.Log
import org.joda.time.DateTime
import rx.Observer
import rx.subjects.PublishSubject
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

    var hotelDetailViewModel: HotelDetailViewModel by Delegates.notNull()
    var hotelSearchParams: HotelSearchParams by Delegates.notNull()
    val resultsMapView: MapView by bindView(R.id.map_view)
    val detailsMapView: MapView by bindView(R.id.details_map_view)

    val searchStub:ViewStub by bindView(R.id.search_stub)
    val searchPresenter: HotelSearchPresenter by lazy {
        var presenter = searchStub.inflate() as HotelSearchPresenter
        presenter.searchViewModel = HotelSearchViewModel(context)
        presenter.searchViewModel.searchParamsObservable.subscribe(searchObserver)
        presenter
    }

    val errorPresenter: HotelErrorPresenter by bindView(R.id.widget_hotel_errors)
    val resultsStub: ViewStub by bindView(R.id.results_stub)
    val resultsPresenter: HotelResultsPresenter by lazy {
        var presenter = resultsStub.inflate() as HotelResultsPresenter
        var resultsStub = presenter.findViewById(R.id.stub_map) as FrameLayout
        resultsMapView.visibility = View.VISIBLE
        removeView(resultsMapView);
        resultsStub.addView(resultsMapView)
        presenter.mapView = resultsMapView
        presenter.mapView.getMapAsync(presenter)
        presenter.viewmodel = HotelResultsViewModel(getContext(), hotelServices, LineOfBusiness.HOTELS)

        val hotelResultsDisplayClientLogBuilder: ClientLog.HotelResultBuilder = ClientLog.HotelResultBuilder()
        presenter.viewmodel.searchingForHotelsDateTime.subscribe(){
            hotelResultsDisplayClientLogBuilder.requestTime(DateTime.now())
        }
        presenter.viewmodel.hotelResultsObservable.subscribe {
            hotelResultsDisplayClientLogBuilder.processingTime(DateTime.now())
        }
        presenter.viewmodel.resultsReceivedDateTimeObservable.subscribe() { dateTime ->
            hotelResultsDisplayClientLogBuilder.responseTime(dateTime)
        }
        presenter.adapter.allViewsLoadedTimeObservable.subscribe() {
            hotelResultsDisplayClientLogBuilder.requestToUser(DateTime.now())
            val userBucketedForTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelResultsPerceivedInstantTest)
                hotelResultsDisplayClientLogBuilder.eventName(if (userBucketedForTest) ClientLogConstants.PERCIEVED_INSTANT_SEARCH_RESULTS else ClientLogConstants.REGULAR_SEARCH_RESULTS)
            hotelResultsDisplayClientLogBuilder.pageName(ClientLogConstants.MATERIAL_HOTEL_SEARCH_PAGE)
            hotelResultsDisplayClientLogBuilder.deviceName(android.os.Build.MODEL)
            clientLogServices.log(hotelResultsDisplayClientLogBuilder.build())
        }
        presenter.hotelSelectedSubject.subscribe(hotelSelectedObserver)
        presenter.viewmodel.errorObservable.subscribe(errorPresenter.getViewModel().apiErrorObserver)
        presenter.viewmodel.errorObservable.subscribe { show(errorPresenter) }
        presenter.viewmodel.showHotelSearchViewObservable.subscribe { show(searchPresenter, Presenter.FLAG_CLEAR_TOP) }
        presenter.viewmodel.hotelResultsObservable.subscribe({ hotelSearchResponse ->
            if (!Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelResultsPerceivedInstantTest)) {
                HotelTracking().trackHotelsSearch(hotelSearchParams, hotelSearchResponse)
            }
        })
        presenter.viewmodel.addHotelResultsObservable.subscribe({ hotelSearchResponse ->
            if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelResultsPerceivedInstantTest)) {
                HotelTracking().trackHotelsSearch(hotelSearchParams, hotelSearchResponse)
            }
        })
        presenter.searchOverlaySubject.subscribe(searchResultsOverlayObserver)
        presenter.showDefault()
        presenter
    }
    val detailsStub: ViewStub by bindView(R.id.details_stub)
    val detailPresenter: HotelDetailPresenter by lazy {
        var presenter = detailsStub.inflate() as HotelDetailPresenter
        var detailsStub = presenter.hotelMapView.findViewById(R.id.stub_map) as FrameLayout
        detailsMapView.visibility = View.VISIBLE
        removeView(detailsMapView);
        detailsStub.addView(detailsMapView)

        presenter.hotelMapView.mapView = detailsMapView
        presenter.hotelMapView.mapView.getMapAsync(presenter.hotelMapView);
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

        viewModel = HotelPresenterViewModel(checkoutPresenter.hotelCheckoutWidget.createTripViewmodel, checkoutPresenter.hotelCheckoutViewModel, presenter.hotelDetailView.viewmodel)
        viewModel.selectedRoomSoldOut.subscribe(presenter.hotelDetailView.viewmodel.selectedRoomSoldOut)
        viewModel.hotelSoldOutWithHotelId.subscribe ((resultsPresenter.mapCarouselRecycler.adapter as HotelMapCarouselAdapter).hotelSoldOut)
        viewModel.hotelSoldOutWithHotelId.subscribe (resultsPresenter.adapter.hotelSoldOut)
        viewModel.hotelSoldOutWithHotelId.subscribe (resultsPresenter.mapViewModel.hotelSoldOutWithIdObserver)
        viewModel.hotelFavoriteChange.subscribe(resultsPresenter.hotelFavoriteChangeObserver)

        presenter
    }

    val checkoutStub: ViewStub by bindView(R.id.checkout_stub)
    val checkoutPresenter: HotelCheckoutPresenter by lazy {
        var presenter = checkoutStub.inflate() as HotelCheckoutPresenter
        presenter.hotelCheckoutWidget.createTripViewmodel = HotelCreateTripViewModel(hotelServices, paymentModel)
        presenter.hotelCheckoutViewModel = HotelCheckoutViewModel(hotelServices, paymentModel)
        confirmationPresenter.hotelConfirmationViewModel = HotelConfirmationViewModel(presenter.hotelCheckoutViewModel.checkoutResponseObservable, context)
        presenter.hotelCheckoutViewModel.checkoutParams.subscribe { presenter.cvv.enableBookButton(false) }
        presenter.hotelCheckoutViewModel.checkoutResponseObservable.subscribe(endlessObserver { checkoutResponse ->
            checkoutDialog.dismiss()
            show(confirmationPresenter, Presenter.FLAG_CLEAR_BACKSTACK)
            WalletUtils.unbindFullWalletDataFromBillingInfo(Db.getWorkingBillingInfoManager().workingBillingInfo)
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

        presenter.hotelCheckoutWidget.createTripViewmodel.errorObservable.subscribe(errorPresenter.getViewModel().apiErrorObserver)
        presenter.hotelCheckoutWidget.createTripViewmodel.errorObservable.subscribe { show(errorPresenter) }
        presenter.hotelCheckoutWidget.createTripViewmodel.noResponseObservable.subscribe {
            val retryFun = fun() {
                presenter.hotelCheckoutWidget.doCreateTrip()
            }
            val cancelFun = fun() {
                show(detailPresenter)
            }
            DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
        }

        presenter.hotelCheckoutViewModel.priceChangeResponseObservable.subscribe(presenter.hotelCheckoutWidget.createTripResponseListener)
        presenter.hotelCheckoutViewModel.priceChangeResponseObservable.subscribe(endlessObserver { createTripResponse ->
            checkoutDialog.dismiss()
            show(presenter, Presenter.FLAG_CLEAR_TOP)
            presenter.show(presenter.hotelCheckoutWidget, Presenter.FLAG_CLEAR_TOP)
        })
        presenter.setSearchParams(hotelSearchParams)
        presenter.hotelCheckoutWidget.setSearchParams(hotelSearchParams)
        confirmationPresenter.hotelConfirmationViewModel.setSearchParams(hotelSearchParams)
        presenter.hotelCheckoutWidget.backPressedAfterUserWithEffectiveSwPAvailableSignedOut.subscribe(goToSearchScreen)
        presenter
    }

    val confirmationPresenter: HotelConfirmationPresenter by bindView(R.id.hotel_confirmation_presenter)

    val reviewsView: HotelReviewsView by lazy {
        var viewStub = findViewById(R.id.reviews_stub) as ViewStub
        var presenter = viewStub.inflate() as HotelReviewsView
        presenter.hotelReviewsTabbar.slidingTabLayout.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                HotelTracking().trackHotelReviewsCategories(tab.position)
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

        hotelDetailViewModel = HotelDetailViewModel(context, endlessObserver<HotelOffersResponse.HotelRoomResponse> {
            checkoutPresenter.hotelCheckoutWidget.couponCardView.viewmodel.hasDiscountObservable.onNext(false)
            checkoutPresenter.setSearchParams(hotelSearchParams)
            checkoutPresenter.hotelCheckoutWidget.setSearchParams(hotelSearchParams)
            checkoutPresenter.showCheckout(it)
            show(checkoutPresenter)
        })

        geoCodeSearchModel.geoResults.subscribe { geoResults ->
            fun triggerNewSearch(selectedResultIndex: Int) {
                val newHotelSearchParams = hotelSearchParams
                val geoLocation = geoResults[selectedResultIndex]
                newHotelSearchParams.suggestion.coordinates.lat = geoLocation.latitude
                newHotelSearchParams.suggestion.coordinates.lng = geoLocation.longitude
                newHotelSearchParams.suggestion.type = "GOOGLE_SUGGESTION_SEARCH"
                // trigger search with selected geoLocation
                searchObserver.onNext(newHotelSearchParams)
            }

            if (geoResults.count() > 0) {
                val freeformLocations = StrUtils.formatAddresses(geoResults)
                val builder = AlertDialog.Builder(context)
                builder.setTitle(R.string.ChooseLocation)
                val dialogItemClickListener = DialogInterface.OnClickListener { dialog, which ->
                    triggerNewSearch(which)
                    HotelTracking().trackGeoSuggestionClick()
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
        }
        else {
            Log.w("You can only set defaultTransition once. (default transition:" + getDefaultTransition() + ")")
        }

        if (screen != Screen.DETAILS && screen != Screen.RESULTS) {
            show(searchPresenter)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        addTransition(searchToResults)
        addTransition(searchToDetails)
        addTransition(resultsToDetail)
        addTransition(detailsToCheckout)
        addTransition(checkoutToConfirmation)
        addTransition(detailsToReview)
        addTransition(resultsToError)
        addTransition(searchToError)
        addTransition(checkoutToError)
        addTransition(detailsToError)
        addTransition(checkoutToSearch)

        errorPresenter.hotelDetailViewModel = hotelDetailViewModel
        errorPresenter.viewmodel = HotelErrorViewModel(context)
        errorPresenter.getViewModel().searchErrorObservable.subscribe {
            show(searchPresenter, Presenter.FLAG_CLEAR_TOP)
        }
        errorPresenter.viewmodel.defaultErrorObservable.subscribe {
            show(searchPresenter, Presenter.FLAG_CLEAR_TOP)
        }

        errorPresenter.viewmodel.checkoutCardErrorObservable.subscribe {
            show(checkoutPresenter, Presenter.FLAG_CLEAR_TOP)
            checkoutPresenter.hotelCheckoutWidget.slideWidget.resetSlider()
            checkoutPresenter.hotelCheckoutWidget.paymentInfoCardView.cardInfoContainer.performClick()
            checkoutPresenter.show(checkoutPresenter.hotelCheckoutWidget, Presenter.FLAG_CLEAR_TOP)
        }

        errorPresenter.viewmodel.checkoutPaymentFailedObservable.subscribe{
            show(checkoutPresenter, Presenter.FLAG_CLEAR_TOP)
            checkoutPresenter.hotelCheckoutWidget.slideWidget.resetSlider()
            checkoutPresenter.hotelCheckoutWidget.paymentInfoCardView.cardInfoContainer.performClick()
            checkoutPresenter.show(checkoutPresenter.hotelCheckoutWidget, Presenter.FLAG_CLEAR_TOP)
        }

        errorPresenter.viewmodel.checkoutAlreadyBookedObservable.subscribe {
            NavUtils.goToItin(context)
        }

        errorPresenter.viewmodel.soldOutObservable.subscribe {
            show(detailPresenter, Presenter.FLAG_CLEAR_TOP)
        }


        errorPresenter.viewmodel.sessionTimeOutObservable.subscribe {
            show(searchPresenter, Presenter.FLAG_CLEAR_TOP)
        }

        errorPresenter.viewmodel.checkoutTravelerErrorObservable.subscribe {
            show(checkoutPresenter, Presenter.FLAG_CLEAR_TOP)
            checkoutPresenter.hotelCheckoutWidget.slideWidget.resetSlider()
            checkoutPresenter.hotelCheckoutWidget.mainContactInfoCardView.setExpanded(true, true)
            checkoutPresenter.show(checkoutPresenter.hotelCheckoutWidget, Presenter.FLAG_CLEAR_TOP)
        }

        errorPresenter.viewmodel.checkoutUnknownErrorObservable.subscribe {
            show(checkoutPresenter, Presenter.FLAG_CLEAR_TOP)
            checkoutPresenter.hotelCheckoutWidget.slideWidget.resetSlider()
            checkoutPresenter.show(checkoutPresenter.hotelCheckoutWidget, Presenter.FLAG_CLEAR_TOP)
        }

        errorPresenter.viewmodel.productKeyExpiryObservable.subscribe {
            show(searchPresenter, Presenter.FLAG_CLEAR_TOP)
        }

        geoCodeSearchModel.errorObservable.subscribe(errorPresenter.getViewModel().apiErrorObserver)
        geoCodeSearchModel.errorObservable.subscribe { show(errorPresenter) }

        loadingOverlay.setBackground(R.color.hotels_primary_color)
    }

    private val defaultSearchTransition = object : Presenter.DefaultTransition(HotelSearchPresenter::class.java.name) {
        override fun endTransition(forward: Boolean) {
            searchPresenter.visibility = View.VISIBLE
            searchPresenter.showSuggestionState(selectOrigin = false)
            HotelTracking().trackHotelSearchBox((searchPresenter.getSearchViewModel() as HotelSearchViewModel).shopWithPointsViewModel.swpEffectiveAvailability.value)
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

    val searchBackgroundColor = TransitionElement(ContextCompat.getColor(context, R.color.search_anim_background), Color.TRANSPARENT)
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

    val searchArgbEvaluator = ArgbEvaluator()
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
            if(forward) {
                searchPresenter.setBackgroundColor(searchArgbEvaluator.evaluate(f, searchBackgroundColor.start, searchBackgroundColor.end) as Int)
            } else {
                searchPresenter.setBackgroundColor(searchArgbEvaluator.evaluate(f, searchBackgroundColor.end, searchBackgroundColor.start) as Int)
            }
            resultsPresenter.animationUpdate(f, !forward)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            searchPresenter.setBackgroundColor(if (forward) searchBackgroundColor.end else searchBackgroundColor.start)
            searchPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            resultsPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            resultsPresenter.animationFinalize(forward, true)
            searchPresenter.animationFinalize(forward)
            if (!forward) HotelTracking().trackHotelSearchBox((searchPresenter.getSearchViewModel() as HotelSearchViewModel).shopWithPointsViewModel.swpEffectiveAvailability.value)
        }
    }

      private val resultsToDetail = object : Presenter.Transition(HotelResultsPresenter::class.java.name, HotelDetailPresenter::class.java.name, DecelerateInterpolator(), ANIMATION_DURATION) {
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
            if (!forward) HotelTracking().trackHotelSearchBox((searchPresenter.getSearchViewModel() as HotelSearchViewModel).shopWithPointsViewModel.swpEffectiveAvailability.value)
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
            }
            else {
                HotelTracking().trackHotelSearchBox((searchPresenter.getSearchViewModel() as HotelSearchViewModel).shopWithPointsViewModel.swpEffectiveAvailability.value)
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
            if(forward) {
                searchPresenter.setBackgroundColor(searchArgbEvaluator.evaluate(f, searchBackgroundColor.start, searchBackgroundColor.end) as Int)
            } else {
                searchPresenter.setBackgroundColor(searchArgbEvaluator.evaluate(f, searchBackgroundColor.end, searchBackgroundColor.start) as Int)
            }
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
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

    val searchObserver: Observer<HotelSearchParams> = endlessObserver { params ->
        hotelSearchParams = params
        errorPresenter.getViewModel().paramsSubject.onNext(params)
        if (params.suggestion.hotelId != null) {
            // Hotel name search - go straight to details
            showDetails(params.suggestion.hotelId, true)
        } else if (params.suggestion.type.equals("RAW_TEXT_SEARCH")) {
            // fire off geo search to resolve raw text into lat/long
            geoCodeSearchModel.searchObserver.onNext(params)
        } else {
            // Hotel region search
            show(resultsPresenter, Presenter.FLAG_CLEAR_TOP)
            resultsPresenter.viewmodel.paramsSubject.onNext(params)
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

    private val hotelDetailsListener: Observer<HotelDetailsRequestMetadata> = endlessObserver {
        if (it.isOffersRequest) {
            loadingOverlay.animate(false)
        }

        if (it.hotelOffersResponse.hasErrors() && it.hotelOffersResponse.firstError.errorCode == ApiError.Code.HOTEL_ROOM_UNAVAILABLE && it.isOffersRequest) {
            //Just show the Hotel Details Screen in "Sold Out" state, fields being fetched from "/info/" API
            showDetails(it.hotelId, false)
        } else if (!it.hotelOffersResponse.hasErrors()) {
            //correct hotel name if it is a deeplink HOTEL search
            val intent = (context as Activity).intent
            if (intent.getBooleanExtra(Codes.FROM_DEEPLINK, false)) {
                intent.putExtra(Codes.FROM_DEEPLINK, false)
                if (hotelSearchParams.suggestion.type == "HOTEL") {
                    searchPresenter.getSearchViewModel().locationTextObservable.onNext(it.hotelOffersResponse.hotelName)
                }
            }
            detailPresenter.hotelDetailView.viewmodel.hotelOffersSubject.onNext(it.hotelOffersResponse)
            detailPresenter.hotelMapView.viewmodel.offersObserver.onNext(it.hotelOffersResponse)
            show(detailPresenter)
            detailPresenter.showDefault()
        }
    }

    val hotelSelectedObserver: Observer<Hotel> = endlessObserver { hotel ->
        //If hotel is known to be "Sold Out", simply show the Hotel Details Screen in "Sold Out" state, otherwise fetch Offers and show those as well
        showDetails(hotel.hotelId, if (hotel.isSoldOut) false else true)
        HotelTracking().trackHotelCarouselClick()
    }

    data class HotelDetailsRequestMetadata(val hotelId: String, val hotelOffersResponse: HotelOffersResponse, val isOffersRequest: Boolean)

    private fun showDetails(hotelId: String, fetchOffers: Boolean) {
        if (fetchOffers) {
            loadingOverlay.visibility = View.VISIBLE
            loadingOverlay.animate(true)
        }

        detailPresenter.hotelDetailView.viewmodel.paramsSubject.onNext(hotelSearchParams)
        if (HotelFavoriteHelper.showHotelFavoriteTest(true)) {
            detailPresenter.hotelDetailView.hotelId = hotelId
            val favoriteButtonViewModel = FavoriteButtonViewModel(context, hotelId, HotelTracking(), HotelTracking.PageName.INFOSITE)
            detailPresenter.hotelDetailView.hotelDetailsToolbar.heartIcon.viewModel = favoriteButtonViewModel
            favoriteButtonViewModel.favoriteChangeSubject.subscribe(viewModel.hotelFavoriteChange)
        }

        val subject = PublishSubject.create<HotelOffersResponse>()
        subject.subscribe(object : Observer<HotelOffersResponse> {
            override fun onNext(t: HotelOffersResponse?) {
                hotelDetailsListener.onNext(HotelDetailsRequestMetadata(hotelId, t!!, fetchOffers))
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                if (RetrofitUtils.isNetworkError(e)) {
                    val retryFun = fun() {
                        showDetails(hotelId, fetchOffers)
                    }
                    val cancelFun = fun() {
                        show(searchPresenter)
                    }
                    DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
                }
            }

        })
        if (fetchOffers) {
            hotelServices.offers(hotelSearchParams, hotelId, subject)
        } else {
            hotelServices.info(hotelSearchParams, hotelId, subject)
        }
    }

    override fun back(): Boolean {
        if (searchPresenter.back()) {
            return true
        }
        if (loadingOverlay.visibility != View.VISIBLE) {
            return super.back()
        }
        return true
    }

    val searchResultsOverlayObserver: Observer<Unit> = endlessObserver { params ->
        show(searchPresenter)
    }

    private fun trackHotelDetail() {
        val hotelOffersResponse = detailPresenter.hotelDetailView.viewmodel.hotelOffersResponse
        val hasEtpOffer = detailPresenter.hotelDetailView.viewmodel.hasEtpOffer(hotelOffersResponse)
        val hotelSoldOut = detailPresenter.hotelDetailView.viewmodel.hotelSoldOut.value
        detailPresenter.hotelDetailView.viewmodel.trackHotelDetailLoad(hotelOffersResponse, hotelSearchParams, hasEtpOffer, hotelSearchParams.suggestion.isCurrentLocationSearch, hotelSoldOut, viewModel.didLastCreateTripOrCheckoutResultInRoomSoldOut.value)
    }
}
