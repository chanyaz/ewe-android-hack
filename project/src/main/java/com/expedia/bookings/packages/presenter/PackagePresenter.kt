package com.expedia.bookings.packages.presenter

import android.animation.ArgbEvaluator
import android.app.Activity
import android.app.AlertDialog
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
import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.BaseApiResponse
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.MIDItinDetailsResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.packages.PackagesPageUsableData
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.enums.TwoScreenOverviewState
import com.expedia.bookings.extensions.safeSubscribeOptional
import com.expedia.bookings.presenter.BaseTwoScreenOverviewPresenter
import com.expedia.bookings.presenter.IntentPresenter
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.services.ItinTripServices
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.tracking.ApiCallFailing
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.tracking.hotel.PageUsableData
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.SearchParamsHistoryUtil
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.TravelerManager
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isMidAPIEnabled
import com.expedia.bookings.utils.CrashlyticsLoggingUtil.logWhenNotAutomation
import com.expedia.bookings.widget.shared.WebCheckoutView
import com.expedia.bookings.packages.activity.PackageActivity
import com.expedia.bookings.packages.vm.PackageWebCheckoutViewViewModel
import com.expedia.bookings.packages.vm.BundleOverviewViewModel
import com.expedia.bookings.packages.vm.PackageConfirmationViewModel
import com.expedia.bookings.packages.vm.PackageErrorViewModel
import com.expedia.bookings.packages.vm.PackageSearchViewModel
import com.mobiata.android.Log
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal
import java.util.Date
import javax.inject.Inject

class PackagePresenter(context: Context, attrs: AttributeSet) : IntentPresenter(context, attrs) {
    lateinit var packageServices: PackageServices
        @Inject set

    var travelerManager: TravelerManager
    lateinit var tripResponse: PackageCreateTripResponse

    val itinTripServices: ItinTripServices by lazy {
        Ui.getApplication(context).packageComponent().itinTripServices()
    }

    var isCrossSellPackageOnFSREnabled = false
    val bundlePresenterViewStub: ViewStub by bindView(R.id.widget_bundle_overview_view_stub)
    val confirmationViewStub: ViewStub by bindView(R.id.widget_package_confirmation_view_stub)
    val errorViewStub: ViewStub by bindView(R.id.widget_package_error_view_stub)
    val pageUsableData = PageUsableData()
    val midAPIEnabled = isMidAPIEnabled()

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
        presenter.bundleWidget.viewModel.showBundleTotalObservable.filter { !isMidAPIEnabled() || it }.subscribe {
            val packagePrice = Db.getPackageResponse().getCurrentOfferPrice()
            if (packagePrice != null) {
                val packageSavings = Money(BigDecimal(packagePrice.tripSavings.amount.toDouble()),
                        packagePrice.tripSavings.currencyCode)
                presenter.totalPriceWidget.visibility = View.VISIBLE
                presenter.totalPriceWidget.viewModel.total.onNext(Money(BigDecimal(packagePrice.packageTotalPrice.amount.toDouble()),
                        packagePrice.packageTotalPrice.currencyCode))
                presenter.totalPriceWidget.viewModel.savings.onNext(packageSavings)
            }
        }
        checkoutPresenter.getCreateTripViewModel().createTripResponseObservable.safeSubscribeOptional { trip ->
            tripResponse = trip as PackageCreateTripResponse
            expediaRewards = trip.rewards?.totalPointsToEarn?.toString()
        }
        checkoutPresenter.getCheckoutViewModel().bookingSuccessResponse.subscribe { pair: Pair<BaseApiResponse, String> ->
            val response = pair.first as PackageCheckoutResponse
            response.packageDetails = tripResponse.packageDetails
            show(confirmationPresenter)
            pageUsableData.markAllViewsLoaded(Date().time)
            confirmationPresenter.viewModel.showConfirmation.onNext(Pair(response.newTrip?.itineraryNumber, pair.second))
            expediaRewards?.let {
                confirmationPresenter.viewModel.setRewardsPoints.onNext(it)
            }
            PackagesTracking().trackCheckoutPaymentConfirmation(response, Strings.capitalizeFirstLetter(Db.sharedInstance.packageSelectedRoom.supplierType), pageUsableData, Db.sharedInstance.packageParams)
        }
        checkoutPresenter.getCreateTripViewModel().createTripErrorObservable.subscribe(errorPresenter.getViewModel().checkoutApiErrorObserver)
        checkoutPresenter.getCheckoutViewModel().checkoutErrorObservable.subscribe(errorPresenter.getViewModel().checkoutApiErrorObserver)
        checkoutPresenter.getCreateTripViewModel().createTripErrorObservable.subscribe {
            if (bundlePresenter.webCheckoutView.visibility == View.VISIBLE) {
                bundlePresenter.back()
            }
            show(errorPresenter)
        }
        checkoutPresenter.getCheckoutViewModel().checkoutErrorObservable.subscribe { show(errorPresenter) }
        presenter.bundleWidget.viewModel.toolbarTitleObservable.subscribe(presenter.bundleOverviewHeader.toolbar.viewModel.toolbarTitle)
        presenter.bundleWidget.viewModel.toolbarTitleObservable.subscribe(presenter.bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel.cityTitle)
        presenter.bundleWidget.viewModel.toolbarSubtitleObservable.subscribe(presenter.bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel.datesTitle)
        presenter.bundleWidget.viewModel.toolbarSubtitleObservable.subscribe(presenter.bundleOverviewHeader.toolbar.viewModel.toolbarSubtitle)
        presenter.bundleWidget.viewModel.errorObservable.subscribe(errorPresenter.getViewModel().packageSearchApiErrorObserver)
        presenter.bundleWidget.viewModel.errorObservable.subscribe { show(errorPresenter) }
        if (midAPIEnabled) {
            (presenter.webCheckoutView.viewModel as PackageWebCheckoutViewViewModel).fetchItinObservable.subscribe { bookedTripID ->
                pageUsableData.markPageLoadStarted(System.currentTimeMillis())
                itinTripServices.getTripDetails(bookedTripID, makeNewItinResponseObserver())
            }
        }
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
            bundlePresenter.showCheckout()
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
            bundlePresenter.showCheckout()
            bundlePresenter.getCheckoutPresenter().openTravelerPresenter()
        }

        presenter.getViewModel().checkoutCardErrorObservable.subscribe {
            show(bundlePresenter, Presenter.FLAG_CLEAR_TOP)
            bundlePresenter.showCheckout()
            bundlePresenter.getCheckoutPresenter().paymentWidget.showPaymentForm(fromPaymentError = true)
        }
        presenter
    }
    val hotelOffersErrorObservable = PublishSubject.create<Pair<ApiError.Code, ApiCallFailing>>()

    private val ANIMATION_DURATION = 400

    var expediaRewards: String? = null

    val bookingSuccessDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.booking_successful))
        builder.setMessage(context.getString(R.string.check_your_email_for_itin))
        builder.setPositiveButton(context.getString(R.string.ok), { dialog, _ ->
            if (currentState == WebCheckoutView::class.java.name) {
                (context as Activity).finish()
            }
            dialog.dismiss()
        })
        val dialog = builder.create()
        dialog.setOnShowListener {
            OmnitureTracking.trackMIDBookingConfirmationDialog(Db.sharedInstance.packageSelectedRoom.supplierType, pageUsableData)
        }
        dialog
    }

    init {
        if (context is PackageActivity) {
            isCrossSellPackageOnFSREnabled = context.intent.getBooleanExtra(Constants.INTENT_PERFORM_HOTEL_SEARCH, false)
        }

        Ui.getApplication(getContext()).packageComponent().inject(this)
        travelerManager = Ui.getApplication(getContext()).travelerComponent().travelerManager()
        View.inflate(context, R.layout.package_presenter, this)

        searchPresenter.searchViewModel = PackageSearchViewModel(context)
        searchPresenter.searchViewModel.searchParamsObservable.subscribe { params ->
            PackagesPageUsableData.HOTEL_RESULTS.pageUsableData.markPageLoadStarted()
            // Starting a new search clear previous selection
            Db.sharedInstance.clearPackageSelection()
            travelerManager.updateDbTravelers(params)
            errorPresenter.getViewModel().paramsSubject.onNext(params)
            logWhenNotAutomation("onNext() called on hotelParamsObservable in PackagePresenter.")
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
        if (midAPIEnabled) {
            addTransition(webCheckoutViewToConfirmation)
        }

        if (isCrossSellPackageOnFSREnabled) {
            addDefaultTransition(defaultOverviewTransition)
            performHotelSearch()
        } else {
            addDefaultTransition(defaultSearchTransition)
            show(searchPresenter)
            addTransition(searchToBundle)
        }

        SearchParamsHistoryUtil.loadPreviousPackageSearchParams(context, loadSuccess)
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
            PackagesPageUsableData.SEARCH.pageUsableData.markAllViewsLoaded()
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
                bundlePresenter.bundleWidget.splitTicketInfoContainer.visibility = View.GONE
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
            searchPresenter.animationFinalize()
            searchPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            bundlePresenter.visibility = if (forward) View.VISIBLE else View.GONE
            if (!forward) {
                trackSearchPageLoad()
                AccessibilityUtil.setFocusToToolbarNavigationIcon(searchPresenter.toolbar)
                bundlePresenter.getCheckoutPresenter().getCreateTripViewModel().reset()
            } else {
                trackViewBundlePageLoad(true)
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
            searchPresenter.animationFinalize()
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

    private val webCheckoutViewToConfirmation = object : Transition(WebCheckoutView::class.java, PackageConfirmationPresenter::class.java) {
        override fun endTransition(forward: Boolean) {
            if (forward) {
                confirmationPresenter.visibility = View.VISIBLE
                bundlePresenter.webCheckoutView.visibility = View.GONE
            }
        }
    }

    fun trackSearchPageLoad() {
        PackagesTracking().trackDestinationSearchInit(PackagesPageUsableData.SEARCH.pageUsableData)
    }

    fun trackViewBundlePageLoad(isFirstBundleLaunch: Boolean = false) {
        PackagesTracking().trackViewBundlePageLoad(isFirstBundleLaunch)
    }

    fun showBundleOverView() {
        show(bundlePresenter)
        bundlePresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault(), FLAG_CLEAR_BACKSTACK)
        bundlePresenter.trackShowBundleOverview()
    }

    fun makeNewItinResponseObserver(): Observer<AbstractItinDetailsResponse> {
        confirmationPresenter.viewModel = PackageConfirmationViewModel(context, isWebCheckout = true)
        return object : DisposableObserver<AbstractItinDetailsResponse>() {
            override fun onComplete() {
            }

            override fun onNext(itinDetailsResponse: AbstractItinDetailsResponse) {
                if (itinDetailsResponse.errors != null) {
                    bookingSuccessDialog.show()
                } else {
                    val response = itinDetailsResponse as MIDItinDetailsResponse
                    confirmationPresenter.viewModel.itinDetailsResponseObservable.onNext(response)
                    show(confirmationPresenter, FLAG_CLEAR_BACKSTACK)
                    pageUsableData.markAllViewsLoaded(System.currentTimeMillis())
                    OmnitureTracking.trackMIDConfirmation(response, Db.sharedInstance.packageSelectedRoom.supplierType, pageUsableData)
                }
            }

            override fun onError(e: Throwable) {
                Log.d("Error fetching itin:" + e.stackTrace)
                bookingSuccessDialog.show()
            }
        }
    }

    override fun handleBack(flags: Int, currentChild: Any): Boolean {
        if (currentChild is Intent) {
            bundlePresenter.bundleWidget.viewModel.cancelSearchObservable.onNext(Unit)
        }
        return super.handleBack(flags, currentChild)
    }

    override fun back(): Boolean {
        if (midAPIEnabled && Db.sharedInstance.packageParams != null && bundlePresenter.webCheckoutView.visibility == View.VISIBLE) {
            bundlePresenter.webCheckoutView.back()
            return true
        }
        return super.back()
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
