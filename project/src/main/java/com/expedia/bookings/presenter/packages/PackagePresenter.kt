package com.expedia.bookings.presenter.packages

import android.animation.ArgbEvaluator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.animation.TransitionElement
import com.expedia.bookings.data.*
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.enums.TwoScreenOverviewState
import com.expedia.bookings.presenter.BaseTwoScreenOverviewPresenter
import com.expedia.bookings.presenter.IntentPresenter
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.tracking.hotel.PageUsableData
import com.expedia.bookings.utils.*
import com.expedia.ui.PackageActivity
import com.expedia.util.safeSubscribeOptional
import com.expedia.vm.packages.BundleOverviewViewModel
import com.expedia.vm.packages.PackageConfirmationViewModel
import com.expedia.vm.packages.PackageErrorViewModel
import com.expedia.vm.packages.PackageSearchViewModel
import rx.subjects.PublishSubject
import java.math.BigDecimal
import java.util.Date
import javax.inject.Inject

class PackagePresenter(context: Context, attrs: AttributeSet) : IntentPresenter(context, attrs) {
    lateinit var packageServices: PackageServices
        @Inject set

    lateinit var travelerManager: TravelerManager

    var isCrossSellPackageOnFSREnabled = false
    val bundlePresenterViewStub: ViewStub by bindView(R.id.widget_bundle_overview_view_stub)
    val confirmationViewStub: ViewStub by bindView(R.id.widget_package_confirmation_view_stub)
    val errorViewStub: ViewStub by bindView(R.id.widget_package_error_view_stub)
    val pageUsableData = PageUsableData()

    val searchPresenter: PackageSearchPresenter by lazy {
        if (displayFlightDropDownRoutes()) {
            val viewStub = findViewById<View>(R.id.package_search_restricted_airport_dropdown_presenter) as ViewStub
            viewStub.inflate() as PackageSearchAirportDropdownPresenter
        } else {
            val viewStub = findViewById<View>(R.id.widget_package_search_presenter) as ViewStub
            viewStub.inflate() as PackageSearchPresenter
        }
    }

    val bundleLoadingView: View by lazy {
        val bundleLoadingView = bundlePresenter.findViewById<View>(R.id.bundle_loading_view)
        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            bundleLoadingView.setPadding(0, statusBarHeight, 0, 0)
        }
        bundleLoadingView
    }
    val bundlePresenter: PackageOverviewPresenter by lazy {
        val presenter = bundlePresenterViewStub.inflate() as PackageOverviewPresenter
        val checkoutPresenter = presenter.getCheckoutPresenter()
        presenter.bundleWidget.viewModel = BundleOverviewViewModel(context, packageServices)
        presenter.bundleWidget.viewModel.searchParamsChangeObservable.subscribe {
            checkoutPresenter.getCheckoutViewModel().bottomCheckoutContainerStateObservable.onNext(TwoScreenOverviewState.OTHER)
        }
        presenter.bundleWidget.viewModel.showSearchObservable.subscribe {
            if (isCrossSellPackageOnFSREnabled) {
                (context as AppCompatActivity).finish()
            } else {
                show(searchPresenter, FLAG_CLEAR_BACKSTACK)
                searchPresenter.showDefault()
            }
        }
        checkoutPresenter.getCheckoutViewModel().checkoutRequestStartTimeObservable.subscribe { startTime ->
            pageUsableData.markPageLoadStarted(startTime)
        }
        presenter.bundleWidget.viewModel.showBundleTotalObservable.subscribe { visible ->
            val packagePrice = Db.getPackageResponse().getCurrentOfferModel().price

            val packageSavings = Money(BigDecimal(packagePrice.tripSavings.amount.toDouble()),
                    packagePrice.tripSavings.currencyCode)
            presenter.totalPriceWidget.visibility = if (visible) View.VISIBLE else View.GONE
            presenter.totalPriceWidget.viewModel.total.onNext(Money(BigDecimal(packagePrice.packageTotalPrice.amount.toDouble()),
                    packagePrice.packageTotalPrice.currencyCode))
            presenter.totalPriceWidget.viewModel.savings.onNext(packageSavings)
        }
        checkoutPresenter.getCreateTripViewModel().createTripResponseObservable.safeSubscribeOptional { trip ->
            trip!!
            expediaRewards = trip.rewards?.totalPointsToEarn?.toString()
        }
        checkoutPresenter.getCheckoutViewModel().bookingSuccessResponse.subscribe { pair: Pair<BaseApiResponse, String> ->
            val response = pair.first as PackageCheckoutResponse
            show(confirmationPresenter)
            val newTrip = response.newTrip
            if (newTrip != null) {
                val tripID = newTrip!!.tripId

                val hotelSearchParams = DeeplinkCreatorUtils.hotelSearchParams!!
                val bookmark = Bookmark("Trip to " + hotelSearchParams.destination, hotelSearchParams.startDate, hotelSearchParams.endDate, 1, DeeplinkCreatorUtils.generateDeeplinkForCurrentPath(LineOfBusiness.PACKAGES), LineOfBusiness.PACKAGES)

                BookmarkUtils.saveTripBooked(context, tripID!!, bookmark)
            }
            pageUsableData.markAllViewsLoaded(Date().time)
            confirmationPresenter.viewModel.showConfirmation.onNext(Pair(response.newTrip?.itineraryNumber, pair.second))
            confirmationPresenter.viewModel.setRewardsPoints.onNext(expediaRewards)
            PackagesTracking().trackCheckoutPaymentConfirmation(response, Strings.capitalizeFirstLetter(Db.getPackageSelectedRoom().supplierType), pageUsableData, Db.getPackageParams())
        }
        checkoutPresenter.getCreateTripViewModel().createTripErrorObservable.subscribe(errorPresenter.getViewModel().checkoutApiErrorObserver)
        checkoutPresenter.getCheckoutViewModel().checkoutErrorObservable.subscribe(errorPresenter.getViewModel().checkoutApiErrorObserver)
        checkoutPresenter.getCreateTripViewModel().createTripErrorObservable.subscribe { show(errorPresenter) }
        checkoutPresenter.getCheckoutViewModel().checkoutErrorObservable.subscribe { show(errorPresenter) }
        presenter.bundleWidget.viewModel.toolbarTitleObservable.subscribe(presenter.bundleOverviewHeader.toolbar.viewModel.toolbarTitle)
        presenter.bundleWidget.viewModel.toolbarTitleObservable.subscribe(presenter.bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel.cityTitle)
        presenter.bundleWidget.viewModel.toolbarSubtitleObservable.subscribe(presenter.bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel.datesTitle)
        presenter.bundleWidget.viewModel.toolbarSubtitleObservable.subscribe(presenter.bundleOverviewHeader.toolbar.viewModel.toolbarSubtitle)
        presenter.bundleWidget.viewModel.errorObservable.subscribe(errorPresenter.getViewModel().packageSearchApiErrorObserver)
        presenter.bundleWidget.viewModel.errorObservable.subscribe { show(errorPresenter) }
        presenter
    }

    val confirmationPresenter: PackageConfirmationPresenter by lazy {
        val presenter = confirmationViewStub.inflate() as PackageConfirmationPresenter
        presenter.viewModel = PackageConfirmationViewModel(context)
        presenter
    }

    val errorPresenter: PackageErrorPresenter by lazy {
        val presenter = errorViewStub.inflate() as PackageErrorPresenter
        presenter.viewmodel = PackageErrorViewModel(context)
        presenter.getViewModel().defaultErrorObservable.subscribe {
            bundlePresenter.bundleWidget.revertBundleViewToSelectHotel()
            bundlePresenter.bundleWidget.outboundFlightWidget.viewModel.showLoadingStateObservable.onNext(false)
            bundlePresenter.bundleWidget.inboundFlightWidget.viewModel.showLoadingStateObservable.onNext(false)

            if (isCrossSellPackageOnFSREnabled) {
                (context as AppCompatActivity).finish()
            } else {
                show(searchPresenter, FLAG_CLEAR_BACKSTACK)
                searchPresenter.showDefault()
            }
        }

        hotelOffersErrorObservable.subscribe(presenter.getViewModel().hotelOffersApiErrorObserver)
        presenter.getViewModel().checkoutUnknownErrorObservable.subscribe {
            show(bundlePresenter, Presenter.FLAG_CLEAR_TOP)
            bundlePresenter.bottomCheckoutContainer.slideToPurchase.resetSlider()
        }

        presenter.viewmodel.createTripUnknownErrorObservable.subscribe {
            if (isCrossSellPackageOnFSREnabled) {
                (context as AppCompatActivity).finish()
            } else {
                show(searchPresenter, FLAG_CLEAR_BACKSTACK)
                searchPresenter.showDefault()
            }
        }

        presenter.viewmodel.checkoutTravelerErrorObservable.subscribe {
            show(bundlePresenter, Presenter.FLAG_CLEAR_TOP)
            bundlePresenter.show(bundlePresenter.getCheckoutPresenter())
            bundlePresenter.bottomCheckoutContainer.slideToPurchase.resetSlider()
            bundlePresenter.getCheckoutPresenter().openTravelerPresenter()
        }

        presenter.getViewModel().checkoutCardErrorObservable.subscribe {
            show(bundlePresenter, Presenter.FLAG_CLEAR_TOP)
            bundlePresenter.showCheckout()
            bundlePresenter.getCheckoutPresenter().paymentWidget.showPaymentForm(fromPaymentError = true)
        }
        presenter
    }
    val hotelOffersErrorObservable = PublishSubject.create<ApiError.Code>()

    private val ANIMATION_DURATION = 400

    var expediaRewards: String? = null

    init {
        if (context is PackageActivity) {
            isCrossSellPackageOnFSREnabled = context.intent.getBooleanExtra(Constants.INTENT_PERFORM_HOTEL_SEARCH, false)
        }

        Ui.getApplication(getContext()).packageComponent().inject(this)
        travelerManager = Ui.getApplication(getContext()).travelerComponent().travelerManager()
        View.inflate(context, R.layout.package_presenter, this)

        searchPresenter.searchViewModel = PackageSearchViewModel(context)
        searchPresenter.searchViewModel.searchParamsObservable.subscribe { params ->
            // Starting a new search clear previous selection
            Db.clearPackageSelection()
            travelerManager.updateDbTravelers(params)
            errorPresenter.getViewModel().paramsSubject.onNext(params)
            bundlePresenter.bundleWidget.viewModel.hotelParamsObservable.onNext(params)
            showBundleOverView()
        }

        hotelOffersErrorObservable.subscribe { show(errorPresenter) }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        addTransition(bundleToConfirmation)
        addTransition(bundleOverviewToError)
        addTransition(errorToSearch)

        if (isCrossSellPackageOnFSREnabled) {
            addDefaultTransition(defaultOverviewTransition)
            performHotelSearch()
        } else {
            addDefaultTransition(defaultSearchTransition)
            show(searchPresenter)
            addTransition(searchToBundle)
        }

        SearchParamsHistoryUtil.loadPreviousPackageSearchParams(context, loadSuccess)

        //TODO: If inflated from deeplink, pass real HotelSearchParams from sharedPreferences
        val hotelSearchParams = DeeplinkSharedPrefParserUtils.getHotelSearchDeeplinkParams(context)
        if (hotelSearchParams != null) {
            searchPresenter.searchViewModel.hotelSearchParamsObservable.onNext(hotelSearchParams)
        }
    }

    private val loadSuccess: (PackageSearchParams) -> Unit = { params ->
        (context as Activity).runOnUiThread {
            searchPresenter.searchViewModel.previousSearchParamsObservable.onNext(params)
        }
    }

    private fun performHotelSearch() {
        val flightSearchParams = Db.getFlightSearchParams()
        val packageParams = searchPresenter.searchViewModel.getParamsBuilder()
                .infantSeatingInLap(flightSearchParams.infantSeatingInLap)
                .origin(flightSearchParams.origin)
                .destination(flightSearchParams.destination)
                .startDate(flightSearchParams.startDate)
                .endDate(flightSearchParams.endDate)
                .adults(flightSearchParams.adults)
                .children(flightSearchParams.children)
                .build() as PackageSearchParams
        searchPresenter.searchViewModel.performSearchObserver.onNext(packageParams)
    }

    private val defaultOverviewTransition = object : Presenter.DefaultTransition(PackageOverviewPresenter::class.java.name) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            bundlePresenter.visibility = View.VISIBLE
            bundlePresenter.bundleWidget.collapseBundleWidgets()
            searchPresenter.animationStart(!forward)
            bundlePresenter.bundleWidget.collapseBundleWidgets()
            bundlePresenter.bundleOverviewHeader.checkoutOverviewHeaderToolbar.visibility = View.GONE
            bundlePresenter.bundleOverviewHeader.toggleOverviewHeader(false)
            bundlePresenter.resetAndShowTotalPriceWidget()
            bundlePresenter.setToolbarNavIcon(true)
            bundlePresenter.scrollSpaceView?.viewTreeObserver?.addOnGlobalLayoutListener(bundlePresenter.overviewLayoutListener)
            bundlePresenter.getCheckoutPresenter().getCheckoutViewModel().bottomCheckoutContainerStateObservable.onNext(TwoScreenOverviewState.OTHER)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            bundlePresenter.visibility = View.VISIBLE
            trackViewBundlePageLoad()
            val params = bundlePresenter.bundleOverviewHeader.appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior as AppBarLayout.Behavior
            behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
                override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                    return false
                }
            })
        }
    }

    private val defaultSearchTransition = object : Presenter.DefaultTransition(getDefaultSearchPresenterClassName()) {
        override fun startTransition(forward: Boolean) {
            searchPresenter.visibility = View.VISIBLE
            super.startTransition(forward)
        }

        override fun endTransition(forward: Boolean) {
            searchPresenter.visibility = View.VISIBLE
            trackSearchPageLoad()
        }
    }

    val searchArgbEvaluator = ArgbEvaluator()
    val searchBackgroundColor = TransitionElement(ContextCompat.getColor(context, R.color.search_anim_background), Color.TRANSPARENT)

    private val searchToBundle = object : Transition(searchPresenter.javaClass, PackageOverviewPresenter::class.java, AccelerateDecelerateInterpolator(), 500) {

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            searchPresenter.visibility = View.VISIBLE
            bundlePresenter.visibility = View.VISIBLE
            bundlePresenter.bundleWidget.collapseBundleWidgets()
            searchPresenter.animationStart(!forward)
            if (forward) {
                bundlePresenter.bundleWidget.collapseBundleWidgets()
                bundlePresenter.bundleOverviewHeader.checkoutOverviewHeaderToolbar.visibility = View.GONE
                bundlePresenter.bundleWidget.packageAirlineFeeWarningTextView.visibility = View.GONE
                bundlePresenter.bundleOverviewHeader.toggleOverviewHeader(false)
                bundlePresenter.resetAndShowTotalPriceWidget()
                bundlePresenter.setToolbarNavIcon(true)
                bundlePresenter.scrollSpaceView?.viewTreeObserver?.addOnGlobalLayoutListener(bundlePresenter.overviewLayoutListener)
            } else {
                bundlePresenter.scrollSpaceView?.viewTreeObserver?.removeOnGlobalLayoutListener(bundlePresenter.overviewLayoutListener)
            }
            bundlePresenter.getCheckoutPresenter().getCheckoutViewModel().bottomCheckoutContainerStateObservable.onNext(TwoScreenOverviewState.OTHER)
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
            searchPresenter.setBackgroundColor(if (forward) searchBackgroundColor.end else searchBackgroundColor.start)
            searchPresenter.animationFinalize(forward)
            searchPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            bundlePresenter.visibility = if (forward) View.VISIBLE else View.GONE
            if (!forward) {
                trackSearchPageLoad()
                AccessibilityUtil.setFocusToToolbarNavigationIcon(searchPresenter.toolbar)
                bundlePresenter.getCheckoutPresenter().getCreateTripViewModel().reset()
            } else {
                trackViewBundlePageLoad()
            }

            val params = bundlePresenter.bundleOverviewHeader.appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior as AppBarLayout.Behavior
            behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
                override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                    return false
                }
            })
        }
    }

    private val bundleToConfirmation = ScaleTransition(this, PackageOverviewPresenter::class.java, PackageConfirmationPresenter::class.java)

    private val bundleOverviewToError = object : Presenter.Transition(PackageOverviewPresenter::class.java, PackageErrorPresenter::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            errorPresenter.visibility = View.VISIBLE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            errorPresenter.animationUpdate(f, !forward)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            bundlePresenter.visibility = if (forward) View.GONE else View.VISIBLE
            errorPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            errorPresenter.animationFinalize()
            if (!forward) {
                trackViewBundlePageLoad()
            } else {
                AccessibilityUtil.setFocusToToolbarNavigationIcon(errorPresenter.standardToolbar)
            }
        }
    }

    private val errorToSearch = object : Presenter.Transition(PackageErrorPresenter::class.java, searchPresenter.javaClass, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            searchPresenter.visibility = View.VISIBLE
            searchPresenter.animationStart(forward)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            searchPresenter.animationUpdate(f, forward)
            if (forward) {
                searchPresenter.setBackgroundColor(searchArgbEvaluator.evaluate(f, searchBackgroundColor.start, searchBackgroundColor.end) as Int)
            } else {
                searchPresenter.setBackgroundColor(searchArgbEvaluator.evaluate(f, searchBackgroundColor.end, searchBackgroundColor.start) as Int)
            }
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            searchPresenter.setBackgroundColor(if (!forward) searchBackgroundColor.end else searchBackgroundColor.start)
            searchPresenter.animationFinalize(!forward)
            errorPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            searchPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            if (forward) {
                trackSearchPageLoad()
                searchPresenter.showDefault()
                if (AccessibilityUtil.isTalkBackEnabled(context)) {
                    searchPresenter.searchButton.isEnabled = false
                }
                bundlePresenter.getCheckoutPresenter().getCreateTripViewModel().reset()
                AccessibilityUtil.setFocusToToolbarNavigationIcon(searchPresenter.toolbar)
            }
        }
    }

    fun trackSearchPageLoad() {
        PackagesTracking().trackDestinationSearchInit()
    }

    fun trackViewBundlePageLoad() {
        PackagesTracking().trackViewBundlePageLoad()
    }

    fun showBundleOverView() {
        show(bundlePresenter)
        bundlePresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault(), FLAG_CLEAR_BACKSTACK)
        bundlePresenter.trackShowBundleOverview()
    }

    override fun handleBack(flags: Int, currentChild: Any): Boolean {
        if (currentChild is Intent) {
            bundlePresenter.bundleWidget.viewModel.cancelSearchObservable.onNext(Unit)
        }
        return super.handleBack(flags, currentChild)
    }

    private fun displayFlightDropDownRoutes(): Boolean {
        return PointOfSale.getPointOfSale().displayFlightDropDownRoutes()
    }

    private fun getDefaultSearchPresenterClassName(): String {
        return if (displayFlightDropDownRoutes()) {
            PackageSearchAirportDropdownPresenter::class.java.name
        } else {
            PackageSearchPresenter::class.java.name
        }
    }

}
