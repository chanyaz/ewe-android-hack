package com.expedia.bookings.packages.presenter

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.support.annotation.VisibleForTesting
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.multiitem.MultiItemApiSearchResponse
import com.expedia.bookings.data.packages.PackageCostSummaryBreakdownModel
import com.expedia.bookings.data.packages.PackageHotelFilterOptions
import com.expedia.bookings.data.packages.PackagesPageUsableData
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.enums.TwoScreenOverviewState
import com.expedia.bookings.extensions.setInverseVisibility
import com.expedia.bookings.extensions.setVisibility
import com.expedia.bookings.packages.activity.PackageHotelActivity
import com.expedia.bookings.packages.vm.MultiItemBottomCheckoutContainerViewModel
import com.expedia.bookings.packages.vm.OverviewHeaderData
import com.expedia.bookings.packages.vm.PackageCheckoutOverviewViewModel
import com.expedia.bookings.packages.vm.PackageCostSummaryBreakdownViewModel
import com.expedia.bookings.packages.vm.PackageCreateTripViewModel
import com.expedia.bookings.packages.vm.PackageOverviewViewModel
import com.expedia.bookings.packages.vm.PackageTotalPriceViewModel
import com.expedia.bookings.packages.vm.PackageWebCheckoutViewViewModel
import com.expedia.bookings.packages.widget.BundleWidget
import com.expedia.bookings.packages.widget.MultiItemBottomCheckoutContainer
import com.expedia.bookings.presenter.BaseTwoScreenOverviewPresenter
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isBetterSavingsOnRDScreenEnabledForPackages
import com.expedia.bookings.widget.BundleOverviewHeader
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.flights.PaymentFeeInfoWebView
import com.expedia.bookings.widget.shared.WebCheckoutView
import com.expedia.util.PackageUtil
import com.expedia.vm.WebViewViewModel
import com.squareup.phrase.Phrase
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

class PackageOverviewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    private val ANIMATION_DURATION = 400

    val bundleOverviewHeader: BundleOverviewHeader by bindView(R.id.coordinator_layout)
    val bottomCheckoutContainer: MultiItemBottomCheckoutContainer by bindView(R.id.bottom_checkout_container)
    val bundleWidget: BundleWidget by bindView(R.id.bundle_widget)
    val changeHotel by lazy { bundleOverviewHeader.toolbar.menu.findItem(R.id.package_change_hotel) }
    val changeHotelRoom by lazy { bundleOverviewHeader.toolbar.menu.findItem(R.id.package_change_hotel_room) }
    val changeFlight by lazy { bundleOverviewHeader.toolbar.menu.findItem(R.id.package_change_flight) }

    val viewModel: PackageOverviewViewModel by lazy {
        PackageOverviewViewModel(context)
    }

    val totalPriceWidget by lazy {
        bottomCheckoutContainer.totalPriceWidget
    }

    val createTripViewModel: PackageCreateTripViewModel by lazy {
        PackageCreateTripViewModel(Ui.getApplication(context).packageComponent().packageServices(), context)
    }

    lateinit var webCheckoutViewModel: PackageWebCheckoutViewViewModel
        @Inject set
    val webCheckoutView: WebCheckoutView by lazy {
        val viewStub = findViewById<ViewStub>(R.id.package_web_checkout_stub)
        val webCheckoutView = viewStub.inflate() as WebCheckoutView
        webCheckoutViewModel.packageCreateTripViewModel = createTripViewModel
        webCheckoutViewModel.packageCreateTripViewModel.midCreateTripErrorObservable.subscribe { error ->
            PackagesTracking().trackMidCreateTripError(error)
            if (webCheckoutView.visibility == View.VISIBLE) {
                back()
            }
            midCreateTripErrorDialog.show()
        }

        webCheckoutView.viewModel = webCheckoutViewModel

        webCheckoutViewModel.closeView.subscribe {
            super.back()
        }

        webCheckoutViewModel.backObservable.subscribe {
            webCheckoutView.back()
        }

        webCheckoutViewModel.blankViewObservable.subscribe {
            webCheckoutView.toggleLoading(true)
        }
        webCheckoutViewModel.packageCreateTripViewModel.multiItemResponseSubject.subscribe {
            fireCheckoutOverviewTracking(Db.getPackageResponse().getCurrentOfferPrice()?.packageTotalPrice?.amount?.toDouble())
            if (webCheckoutView.visibility == View.VISIBLE) {
                webCheckoutViewModel.showWebViewObservable.onNext(true)
            }
        }
        webCheckoutView
    }

    val paymentFeeInfoWebView: PaymentFeeInfoWebView by lazy {
        val viewStub = findViewById<ViewStub>(R.id.payment_fee_info_webview_stub)
        val airlineFeeWebview = viewStub.inflate() as PaymentFeeInfoWebView
        airlineFeeWebview.setExitButtonOnClickListener(View.OnClickListener { this.back() })
        airlineFeeWebview.viewModel = WebViewViewModel()
        viewModel.obFeeDetailsUrlSubject.subscribe(airlineFeeWebview.viewModel.webViewURLObservable)
        airlineFeeWebview
    }

    private val overviewToPaymentFeeWebView = object : Transition(BaseTwoScreenOverviewPresenter.BundleDefault::class.java, PaymentFeeInfoWebView::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            bundleOverviewHeader.visibility = if (forward) View.GONE else View.VISIBLE
            paymentFeeInfoWebView.visibility = if (!forward) View.GONE else View.VISIBLE
        }
    }

    private val defaultTransition = object : DefaultTransition(BaseTwoScreenOverviewPresenter.BundleDefault::class.java.name) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            bundleOverviewHeader.toolbar.menu.setGroupVisible(R.id.package_change_menu, false)
            bundleOverviewHeader.toggleCollapsingToolBar(!forward)
            bundleOverviewHeader.nestedScrollView.visibility = VISIBLE
            bundleOverviewHeader.nestedScrollView.foreground?.alpha = 0
        }
    }

    init {
        View.inflate(context, R.layout.package_overview, this)

        addTransition(overviewToPaymentFeeWebView)
        addDefaultTransition(defaultTransition)

        setupCheckoutViewModelSubscriptions()
        setupClickListeners()
        setupBundleOverviewHeader()
        setupViewModels()
    }

    private val midCreateTripErrorDialog: Dialog by lazy {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.mid_createtrip_error_dialog)
        val changeFlight = dialog.findViewById<TextView>(R.id.change_flight)
        changeFlight.setOnClickListener {
            onFlightChange()
            dialog.dismiss()
        }

        val retry = dialog.findViewById<TextView>(R.id.retry)
        retry.setOnClickListener {
            (webCheckoutView.viewModel as PackageWebCheckoutViewViewModel).packageCreateTripViewModel.performMultiItemCreateTripSubject.onNext(Unit)
            dialog.dismiss()
        }
        dialog.setCanceledOnTouchOutside(false)
        dialog
    }

    init {
        Ui.getApplication(context).packageComponent().inject(this)
        bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel = PackageCheckoutOverviewViewModel(context)
        bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel = PackageCheckoutOverviewViewModel(context)
        viewModel.toolbarNavIconContDescSubject.subscribe(bundleOverviewHeader.toolbar.viewModel.toolbarNavIconContentDesc)
        viewModel.toolbarNavIcon.subscribe(bundleOverviewHeader.toolbar.viewModel.toolbarNavIcon)
//        scrollSpaceView = bundleWidget.scrollSpaceView
        if (PointOfSale.getPointOfSale().pointOfSaleId == PointOfSaleId.JAPAN) {
            bottomCheckoutContainer.totalPriceWidget.bundleTotalText.text = StrUtils.bundleTotalWithTaxesString(context)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        removeView(bundleWidget)

        createTripViewModel.bundleDatesObservable.subscribe(bundleWidget.bundleHotelWidget.viewModel.hotelDatesGuestObservable)

        bundleOverviewHeader.nestedScrollView.addView(bundleWidget)
        bundleOverviewHeader.toolbar.inflateMenu(R.menu.menu_package_checkout)
        bundleWidget.toggleMenuObservable.subscribe(bundleOverviewHeader.toolbar.toggleMenuObserver)

        bundleOverviewHeader.toolbar.viewModel.overflowClicked.subscribe {
            PackagesTracking().trackBundleEditClick()
        }

        changeHotel.setOnMenuItemClickListener({
            cancelMIDCreateTripCall()
            onChangeHotelClicked()
            true
        })

        changeHotelRoom.setOnMenuItemClickListener({
            cancelMIDCreateTripCall()
            onChangeHotelRoomClicked()
            true
        })

        changeFlight.setOnMenuItemClickListener({
            cancelMIDCreateTripCall()
            onFlightChange()
            PackagesTracking().trackBundleEditItemClick("Flight")

            true
        })

        addTransition(overviewToWebCheckoutView)

        viewModel.performMIDCreateTripSubject.subscribe {
            webCheckoutView.clearHistory()
            webCheckoutView.webView.clearHistory()
            webCheckoutView.viewModel.webViewURLObservable.onNext(context.getString(R.string.clear_webview_url))
            (webCheckoutView.viewModel as PackageWebCheckoutViewViewModel).doCreateTrip()
            setupOverviewPresenterForMID()
        }
    }

    private fun cancelMIDCreateTripCall() {
        val vm = webCheckoutView.viewModel
        if (vm is PackageWebCheckoutViewViewModel) {
            vm.packageCreateTripViewModel.cancelMultiItemCreateTripSubject.onNext(Unit)
        }
    }

    private fun onFlightChange() {
        Db.setCachedPackageResponse(Db.getPackageResponse())

        resetBundleOverview()
        bundleOverviewHeader.toggleOverviewHeader(false)

        val params = Db.sharedInstance.packageParams
        params.pageType = Constants.PACKAGE_CHANGE_FLIGHT
        params.selectedLegId = null

        bundleWidget.viewModel.flightParamsObservable.onNext(params)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun onChangeHotelClicked() {
        Db.setCachedPackageResponse(Db.getPackageResponse())

        resetBundleOverview()
        bundleOverviewHeader.toggleOverviewHeader(false)

        val params = Db.sharedInstance.packageParams
        params.pageType = Constants.PACKAGE_CHANGE_HOTEL
        params.filterOptions = PackageHotelFilterOptions()

        bundleWidget.viewModel.hotelParamsObservable.onNext(params)
        PackagesTracking().trackBundleEditItemClick("Hotel")
    }

    private fun resetBundleOverview() {
        bottomCheckoutContainer.totalPriceWidget.resetPriceWidget()
        bottomCheckoutContainer.totalPriceWidget.toggleBundleTotalCompoundDrawable(false)
        resetBundleTotalTax()
        bundleWidget.collapseBundleWidgets()
        bundleWidget.viewModel.showSplitTicketMessagingObservable.onNext(false)
        bundleWidget.toggleMenuObservable.onNext(false)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun onChangeHotelRoomClicked() {
        Db.setCachedPackageResponse(Db.getPackageResponse())

        resetBundleOverview()

        val params = Db.sharedInstance.packageParams
        params.pageType = Constants.PACKAGE_CHANGE_HOTEL

        val intent = Intent(context, PackageHotelActivity::class.java)
        intent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, true)
        (context as AppCompatActivity).startActivityForResult(intent, Constants.HOTEL_REQUEST_CODE, null)
        PackagesTracking().trackBundleEditItemClick("Room")
    }

    private fun resetBundleTotalTax() {
        if (PointOfSale.getPointOfSale().pointOfSaleId == PointOfSaleId.JAPAN) {
            bottomCheckoutContainer.totalPriceWidget.bundleTotalText.text = StrUtils.bundleTotalWithTaxesString(context)
        }
    }

    private fun setCheckoutHeaderOverviewDates() {
        val params = Db.sharedInstance.packageParams
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")

        //set the package start and end date
        val startDateFormatted = params.startDate.toString(formatter)
        val endDateFormatted = params.endDate?.toString(formatter)

        if (endDateFormatted != null) {
            bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel.checkInAndCheckOutDate.onNext(Pair(startDateFormatted, endDateFormatted))
            bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel.checkInAndCheckOutDate.onNext(Pair(startDateFormatted, endDateFormatted))
        } else {
            bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel.checkInWithoutCheckoutDate.onNext(startDateFormatted)
            bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel.checkInWithoutCheckoutDate.onNext(startDateFormatted)
        }
    }

    override fun back(): Boolean {
        bundleWidget.viewModel.cancelSearchObservable.onNext(Unit)

        if (currentState == BaseTwoScreenOverviewPresenter.BundleDefault::class.java.name && bundleOverviewHeader.appBarLayout.isActivated) {
            showBackToSearchDialog()
            return true
        }
        bundleWidget.collapseBundleWidgets()
        return super.back()
    }

    private fun showBackToSearchDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.package_checkout_back_dialog_title)
        builder.setMessage(R.string.package_checkout_back_dialog_message)
        builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
        builder.setPositiveButton(context.getString(R.string.start_over)) { _, _ ->
            cancelMIDCreateTripCall()
            bundleWidget.viewModel.showSearchObservable.onNext(Unit)
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun setToolbarNavIcon(forward: Boolean) {
        if (forward) {
            viewModel.toolbarNavIconContDescSubject.onNext(resources.getString(R.string.toolbar_nav_icon_cont_desc))
            viewModel.toolbarNavIcon.onNext(ArrowXDrawableUtil.ArrowDrawableType.BACK)
        } else {
            viewModel.toolbarNavIconContDescSubject.onNext(resources.getString(R.string.toolbar_nav_icon_close_cont_desc))
            viewModel.toolbarNavIcon.onNext(ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
        }
    }

    private fun fireCheckoutOverviewTracking(amount: Double?) {
        PackagesPageUsableData.RATE_DETAILS.pageUsableData.markAllViewsLoaded()
        PackagesTracking().trackBundleOverviewPageLoad(amount, PackagesPageUsableData.RATE_DETAILS.pageUsableData)
    }

    private val overviewToWebCheckoutView = object : Transition(BaseTwoScreenOverviewPresenter.BundleDefault::class.java, WebCheckoutView::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            bundleOverviewHeader.setInverseVisibility(forward)
            webCheckoutView.setVisibility(forward)
            webCheckoutView.viewModel.showWebViewObservable.onNext(forward)
        }
    }

    private fun bundleWidgetSetup() {
        bundleWidget.outboundFlightWidget.toggleFlightWidget(1f, true)
        bundleWidget.inboundFlightWidget.toggleFlightWidget(1f, true)
        bundleWidget.bundleHotelWidget.toggleHotelWidget(1f, true)

        if (currentState == BaseTwoScreenOverviewPresenter.BundleDefault::class.java.name) {
            bundleWidget.toggleMenuObservable.onNext(true)
            setToolbarNavIcon(false)
        }
        bundleWidget.setPadding(0, 0, 0, 0)
        bundleWidget.bundleHotelWidget.collapseSelectedHotel()
        bundleWidget.outboundFlightWidget.collapseFlightDetails()
        bundleWidget.inboundFlightWidget.collapseFlightDetails()
    }

    private fun resetCheckoutState() {
        if (currentState == BaseTwoScreenOverviewPresenter.BundleDefault::class.java.name) {
            bundleOverviewHeader.toggleOverviewHeader(true)
            viewModel.bottomCheckoutContainerStateObservable.onNext(TwoScreenOverviewState.BUNDLE)
        }
    }

    private fun setupOverviewPresenterForMID() {
        resetCheckoutState()
        bundleWidgetSetup()
        val searchResponse = Db.getPackageResponse() as MultiItemApiSearchResponse
        val hotel = Db.getPackageSelectedHotel()
        bundleWidget.viewModel.setUpAirlineFeeTextAndSplitTicketMessagingOnBundleOverview()
        val cityName = StrUtils.formatCity(Db.sharedInstance.packageParams.destination)

        val headerData = OverviewHeaderData(cityName, searchResponse.getHotelCheckOutDate(),
                searchResponse.getHotelCheckInDate(), hotel.largeThumbnailUrl)
        (bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel
                as PackageCheckoutOverviewViewModel).tripResponseSubject.onNext(headerData)

        (bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel
                as PackageCheckoutOverviewViewModel).tripResponseSubject.onNext(headerData)
        setCheckoutHeaderOverviewDates()
        setMandatoryFee(searchResponse)
        setHotelBundleWidgetGuestsAndDatesText(searchResponse)
        searchResponse.getCurrentOfferPrice()?.let {
            val tripSavings = it.tripSavings
            val shouldShowTripSavings = it.showTripSavings
            bottomCheckoutContainer.totalPriceWidget.viewModel.savings.onNext(tripSavings)
            bottomCheckoutContainer.totalPriceWidget.viewModel.shouldShowSavings.onNext(shouldShowTripSavings)
            if (isBetterSavingsOnRDScreenEnabledForPackages(context)) {
                val packageReferenceTotalPrice = it.packageReferenceTotalPrice
                val totalPrice = it.packageTotalPrice?.formattedMoneyFromAmountAndCurrencyCode
                bottomCheckoutContainer.totalPriceWidget.viewModel.referenceTotalPrice.onNext(packageReferenceTotalPrice)
                var totalPriceContainerContDesc: String
                if (shouldShowTripSavings) {
                    bottomCheckoutContainer.totalPriceWidget.viewModel.betterSavingsObservable.onNext(true)
                    bottomCheckoutContainer.totalPriceWidget.bundleSavings.visibility = View.GONE
                    val flightPIID = Db.sharedInstance.packageParams.latestSelectedOfferInfo.flightPIID
                    val standaloneHotelPrice = searchResponse.getSelectedHotelReferenceTotalPriceFromID(hotel.hotelId)?.formattedMoneyFromAmountAndCurrencyCode
                    val standaloneFlightPrice = searchResponse.getSelectedFlightReferenceTotalPriceFromPIID(flightPIID)?.formattedMoneyFromAmountAndCurrencyCode
                    val referenceTotalPrice = packageReferenceTotalPrice?.formattedMoneyFromAmountAndCurrencyCode
                    val savings = tripSavings?.formattedMoneyFromAmountAndCurrencyCode
                    val costSummaryBreakdown = PackageCostSummaryBreakdownModel(standaloneHotelPrice, standaloneFlightPrice, referenceTotalPrice, savings, totalPrice)
                    val costSummaryViewModel = (bottomCheckoutContainer.totalPriceWidget.breakdown.viewmodel as PackageCostSummaryBreakdownViewModel)
                    costSummaryViewModel.packageCostSummaryObservable.onNext(costSummaryBreakdown)
                    totalPriceContainerContDesc = Phrase.from(context, R.string.bundle_total_price_widget_cost_breakdown_variant_cont_desc_TEMPLATE)
                            .put("total_price", totalPrice)
                            .put("reference_total_price", referenceTotalPrice)
                            .put("savings", savings)
                            .format().toString()
                } else {
                    totalPriceContainerContDesc = Phrase.from(context, R.string.bundle_overview_price_widget_cont_desc_TEMPLATE)
                            .put("total_price", totalPrice)
                            .format().toString()
                }
                bottomCheckoutContainer.totalPriceWidget.viewModel.totalPriceContainerDescription.onNext(totalPriceContainerContDesc)
                bottomCheckoutContainer.totalPriceWidget.toggleBundleTotalCompoundDrawable(shouldShowTripSavings)
            }
        }
    }

    //We have decided to not add mandatory fees in the bundle total for now. This will be worked on again.

    private fun setMandatoryFee(packageResponse: BundleSearchResponse) {
        val packageTotal = packageResponse.getCurrentOfferPrice()?.packageTotalPrice ?: return
        bottomCheckoutContainer.totalPriceWidget.viewModel.setBundleTotalPrice(packageTotal)
    }

    private fun setHotelBundleWidgetGuestsAndDatesText(packageResponse: BundleSearchResponse) {
        bundleWidget.bundleHotelWidget.viewModel.hotelDatesGuestObservable.onNext(
                PackageUtil.getBundleHotelDatesAndGuestsText(context, packageResponse.getHotelCheckInDate(), packageResponse.getHotelCheckOutDate(), Db.sharedInstance.packageParams.guests))
    }

    fun resetToLoadedOutboundFlights() {
        Db.sharedInstance.packageParams.currentFlights = Db.sharedInstance.packageParams.defaultFlights

        //revert bundle view to be the state loaded outbound flights
        bundleWidget.revertBundleViewToSelectOutbound()
        bundleWidget.outboundFlightWidget.viewModel.showLoadingStateObservable.onNext(false)

        val rate = Db.sharedInstance.packageSelectedRoom.rateInfo.chargeableRateInfo
        bottomCheckoutContainer.totalPriceWidget.viewModel.setPriceValues(rate.packageTotalPrice, rate.packageSavings)
    }

    fun resetToLoadedHotels() {
        Db.sharedInstance.packageParams.currentFlights = Db.sharedInstance.packageParams.defaultFlights

        //revert bundle view to be the state loaded hotels
        bottomCheckoutContainer.totalPriceWidget.resetPriceWidget()
        bundleWidget.revertBundleViewToSelectHotel()
        bundleWidget.bundleHotelWidget.viewModel.showLoadingStateObservable.onNext(false)
    }

    private fun toggleBottomContainerViews(state: TwoScreenOverviewState) {
        bottomCheckoutContainer.toggleCheckoutButton(state)
    }

    private fun setupCheckoutViewModelSubscriptions() {
        viewModel.bottomCheckoutContainerStateObservable.subscribe { currentState ->
            toggleBottomContainerViews(currentState)
        }
    }

    private fun setupClickListeners() {
        bottomCheckoutContainer.checkoutButton.setOnClickListener {
            if (currentState == BaseTwoScreenOverviewPresenter.BundleDefault::class.java.name) {
                show(webCheckoutView)
            }
        }
    }

    private fun setupBundleOverviewHeader() {
        bundleOverviewHeader.setUpCollapsingToolbar()
        bundleOverviewHeader.toolbar.overflowIcon = ContextCompat.getDrawable(context, R.drawable.ic_create_white_24dp)
    }

    private fun setupViewModels() {
        bottomCheckoutContainer.viewModel = MultiItemBottomCheckoutContainerViewModel()
        bottomCheckoutContainer.totalPriceViewModel = PackageTotalPriceViewModel(context)
        bottomCheckoutContainer.baseCostSummaryBreakdownViewModel = PackageCostSummaryBreakdownViewModel(context)
    }

    fun resetAndShowTotalPriceWidget() {
        bottomCheckoutContainer.viewModel.resetPriceWidgetObservable.onNext(Unit)
    }
}
