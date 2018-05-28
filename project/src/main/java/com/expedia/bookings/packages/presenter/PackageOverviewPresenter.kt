package com.expedia.bookings.packages.presenter

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.support.annotation.VisibleForTesting
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.multiitem.MultiItemApiSearchResponse
import com.expedia.bookings.data.packages.PackageCostSummaryBreakdownModel
import com.expedia.bookings.data.packages.PackagesPageUsableData
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.extensions.safeSubscribeOptional
import com.expedia.bookings.extensions.setInverseVisibility
import com.expedia.bookings.extensions.setVisibility
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.packages.activity.PackageHotelActivity
import com.expedia.bookings.packages.vm.AbstractUniversalCKOTotalPriceViewModel
import com.expedia.bookings.packages.vm.OverviewHeaderData
import com.expedia.bookings.packages.vm.PackageCheckoutOverviewViewModel
import com.expedia.bookings.packages.vm.PackageCostSummaryBreakdownViewModel
import com.expedia.bookings.packages.vm.PackageTotalPriceViewModel
import com.expedia.bookings.packages.vm.PackageWebCheckoutViewViewModel
import com.expedia.bookings.packages.widget.BundleWidget
import com.expedia.bookings.presenter.BaseTwoScreenOverviewPresenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isBetterSavingsOnRDScreenEnabledForPackages
import com.expedia.bookings.utils.isMidAPIEnabled
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.shared.WebCheckoutView
import com.expedia.util.PackageUtil
import io.reactivex.subjects.PublishSubject
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

class PackageOverviewPresenter(context: Context, attrs: AttributeSet) : BaseTwoScreenOverviewPresenter(context, attrs) {

    val bundleWidget: BundleWidget by bindView(R.id.bundle_widget)
    val changeHotel by lazy { bundleOverviewHeader.toolbar.menu.findItem(R.id.package_change_hotel) }
    val changeHotelRoom by lazy { bundleOverviewHeader.toolbar.menu.findItem(R.id.package_change_hotel_room) }
    val changeFlight by lazy { bundleOverviewHeader.toolbar.menu.findItem(R.id.package_change_flight) }

    val toolbarNavIcon = PublishSubject.create<ArrowXDrawableUtil.ArrowDrawableType>()
    val toolbarNavIconContDescSubject = PublishSubject.create<String>()
    val performMIDCreateTripSubject = PublishSubject.create<Unit>()

    lateinit var webCheckoutViewModel: PackageWebCheckoutViewViewModel
        @Inject set
    val webCheckoutView: WebCheckoutView by lazy {
        val viewStub = findViewById<ViewStub>(R.id.package_web_checkout_stub)
        val webCheckoutView = viewStub.inflate() as WebCheckoutView
        webCheckoutViewModel.packageCreateTripViewModel = getCheckoutPresenter().getCreateTripViewModel()
        webCheckoutViewModel.packageCreateTripViewModel.midCreateTripErrorObservable.subscribe { error ->
            PackagesTracking().trackMidCreateTripError(error)
            if (webCheckoutView.visibility == View.VISIBLE) {
                back()
            }
            midCreateTripErrorDialog.show()
        }

        webCheckoutView.viewModel = webCheckoutViewModel

        webCheckoutViewModel.closeView.subscribe {
            if (checkoutPresenter.visibility == View.GONE) {
                super.back()
            }
        }

        webCheckoutViewModel.backObservable.subscribe {
            webCheckoutView.back()
        }

        webCheckoutViewModel.blankViewObservable.subscribe {
            webCheckoutView.toggleLoading(true)
        }
        webCheckoutViewModel.packageCreateTripViewModel.multiItemResponseSubject.subscribe {
            fireCheckoutOverviewTracking(Db.getPackageResponse().getCurrentOfferPrice()?.packageTotalPrice?.amount?.toDouble())
        }
        webCheckoutView
    }

    override fun inflate() {
        View.inflate(context, R.layout.package_overview, this)
    }

    override fun injectComponents() {}

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
        toolbarNavIconContDescSubject.subscribe(bundleOverviewHeader.toolbar.viewModel.toolbarNavIconContentDesc)
        toolbarNavIcon.subscribe(bundleOverviewHeader.toolbar.viewModel.toolbarNavIcon)
        scrollSpaceView = bundleWidget.scrollSpaceView
        if (PointOfSale.getPointOfSale().pointOfSaleId == PointOfSaleId.JAPAN) {
            totalPriceWidget.bundleTotalText.text = StrUtils.bundleTotalWithTaxesString(context)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        removeView(bundleWidget)
        getCheckoutPresenter().getCreateTripViewModel().createTripResponseObservable.safeSubscribeOptional { _ ->
            bundleWidgetSetup()
            var totalPrice = ""
            bottomCheckoutContainer.viewModel.sliderPurchaseTotalText.onNext(totalPrice)

            setCheckoutHeaderOverviewDates()
        }

        getCheckoutPresenter().getCreateTripViewModel().bundleDatesObservable
                .subscribe(bundleWidget.bundleHotelWidget.viewModel.hotelDatesGuestObservable)

        bundleOverviewHeader.nestedScrollView.addView(bundleWidget)
        bundleOverviewHeader.toolbar.inflateMenu(R.menu.menu_package_checkout)
        bundleWidget.toggleMenuObservable.subscribe(bundleOverviewHeader.toolbar.toggleMenuObserver)

        getCheckoutPresenter().getCheckoutViewModel().slideToBookA11yActivateObservable.subscribe(checkoutSliderSlidObserver)

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

        if (isMidAPIEnabled()) {
            addTransition(overviewToWebCheckoutView)
        } else {
            addTransition(checkoutTransition)
            addTransition(checkoutToCvv)
        }

        if (isMidAPIEnabled()) {
            performMIDCreateTripSubject.subscribe {
                webCheckoutView.clearHistory()
                webCheckoutView.webView.clearHistory()
                webCheckoutView.viewModel.webViewURLObservable.onNext(context.getString(R.string.clear_webview_url))
                (webCheckoutView.viewModel as PackageWebCheckoutViewViewModel).doCreateTrip()
                setupOverviewPresenterForMID()
            }
        }
    }

    private fun cancelMIDCreateTripCall() {
        if (isMidAPIEnabled()) {
            val vm = webCheckoutView.viewModel
            if (vm is PackageWebCheckoutViewViewModel) {
                vm.packageCreateTripViewModel.cancelMultiItemCreateTripSubject.onNext(Unit)
            }
        }
    }

    private fun onFlightChange() {
        resetBundleOverview()
        bundleOverviewHeader.toggleOverviewHeader(false)

        val params = Db.sharedInstance.packageParams
        params.pageType = Constants.PACKAGE_CHANGE_FLIGHT
        params.searchProduct = Constants.PRODUCT_FLIGHT
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
        params.searchProduct = null

        bundleWidget.viewModel.hotelParamsObservable.onNext(params)
        bottomCheckoutContainer.viewModel.sliderPurchaseTotalText.onNext("")
        PackagesTracking().trackBundleEditItemClick("Hotel")
    }

    private fun resetBundleOverview() {
        resetAndShowTotalPriceWidget()
        checkoutPresenter.clearPaymentInfo()
        checkoutPresenter.updateDbTravelers()
        totalPriceWidget.toggleBundleTotalCompoundDrawable(false)
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
            totalPriceWidget.bundleTotalText.text = StrUtils.bundleTotalWithTaxesString(context)
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
            checkoutPresenter.clearPaymentInfo()
            checkoutPresenter.resetTravelers()
            bottomCheckoutContainer.viewModel.sliderPurchaseTotalText.onNext("")
            bundleWidget.viewModel.showSearchObservable.onNext(Unit)
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun getCheckoutPresenter(): PackageCheckoutPresenter {
        return checkoutPresenter as PackageCheckoutPresenter
    }

    override fun trackCheckoutPageLoad() {
        // TODO: needs to be implemented for WebView uCKO
    }

    override fun trackPaymentCIDLoad() {
        PackagesTracking().trackCheckoutPaymentCID()
    }

    override fun setToolbarMenu(forward: Boolean) {
        bundleWidget.toggleMenuObservable.onNext(!forward)
    }

    override fun setToolbarNavIcon(forward: Boolean) {
        if (forward) {
            toolbarNavIconContDescSubject.onNext(resources.getString(R.string.toolbar_nav_icon_cont_desc))
            toolbarNavIcon.onNext(ArrowXDrawableUtil.ArrowDrawableType.BACK)
        } else {
            toolbarNavIconContDescSubject.onNext(resources.getString(R.string.toolbar_nav_icon_close_cont_desc))
            toolbarNavIcon.onNext(ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
        }
    }

    override fun setBundleWidgetAndToolbar(forward: Boolean) {
        setToolbarNavIcon(forward)
        bundleWidget.toggleMenuObservable.onNext(!forward)
    }

    override fun getCostSummaryBreakdownViewModel(): PackageCostSummaryBreakdownViewModel {
        return PackageCostSummaryBreakdownViewModel(context)
    }

    override fun onTripResponse(tripResponse: TripResponse?) {
        if (ProductFlavorFeatureConfiguration.getInstance().shouldShowPackageIncludesView())
            totalPriceWidget.viewModel.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_flights_hotel))
    }

    override fun fireCheckoutOverviewTracking(createTripResponse: TripResponse) {
        // TODO implement with new CT response where we don't get price
    }

    private fun fireCheckoutOverviewTracking(amount: Double?) {
        PackagesPageUsableData.RATE_DETAILS.pageUsableData.markAllViewsLoaded()
        PackagesTracking().trackBundleOverviewPageLoad(amount, PackagesPageUsableData.RATE_DETAILS.pageUsableData)
    }

    override fun getPriceViewModel(context: Context): AbstractUniversalCKOTotalPriceViewModel {
        return PackageTotalPriceViewModel(context)
    }

    private val overviewToWebCheckoutView = object : Transition(BaseTwoScreenOverviewPresenter.BundleDefault::class.java, WebCheckoutView::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            checkoutPresenter.setInverseVisibility(forward)
            bundleOverviewHeader.setInverseVisibility(forward)
            webCheckoutView.setVisibility(forward)
            webCheckoutView.viewModel.showWebViewObservable.onNext(forward)
        }
    }

    override fun showCheckout() {
        if (isMidAPIEnabled()) {
            show(webCheckoutView)
        } else {
            super.showCheckout()
        }
    }

    private fun bundleWidgetSetup() {
        bundleWidget.outboundFlightWidget.toggleFlightWidget(1f, true)
        bundleWidget.inboundFlightWidget.toggleFlightWidget(1f, true)
        bundleWidget.bundleHotelWidget.toggleHotelWidget(1f, true)

        if (currentState == BundleDefault::class.java.name) {
            bundleWidget.toggleMenuObservable.onNext(true)
            setToolbarNavIcon(false)
        }
        bundleWidget.setPadding(0, 0, 0, 0)
        bundleWidget.bundleHotelWidget.collapseSelectedHotel()
        bundleWidget.outboundFlightWidget.collapseFlightDetails()
        bundleWidget.inboundFlightWidget.collapseFlightDetails()
    }

    private fun setupOverviewPresenterForMID() {
        resetCheckoutState()
        bundleWidgetSetup()
        val searchResponse = Db.getPackageResponse() as MultiItemApiSearchResponse
        val hotel = Db.getPackageSelectedHotel()
        bundleWidget.viewModel.getHotelNameAndDaysToSetUpTitle()
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
            totalPriceWidget.viewModel.savings.onNext(tripSavings)
            if (isBetterSavingsOnRDScreenEnabledForPackages(context)) {
                val packageReferenceTotalPrice = it.packageReferenceTotalPrice
                val shouldShowTripSavings = it.showTripSavings
                totalPriceWidget.viewModel.referenceTotalPrice.onNext(packageReferenceTotalPrice)
                totalPriceWidget.viewModel.shouldShowSavings.onNext(shouldShowTripSavings)
                if (shouldShowTripSavings) {
                    totalPriceWidget.bundleSavings.visibility = View.GONE
                    val flightPIID = Db.sharedInstance.packageParams.latestSelectedOfferInfo.flightPIID
                    val standaloneHotelPrice = searchResponse.getSelectedHotelReferenceTotalPriceFromID(hotel.hotelId)?.formattedMoneyFromAmountAndCurrencyCode
                    val standaloneFlightPrice = searchResponse.getSelectedFlightReferenceTotalPriceFromPIID(flightPIID)?.formattedMoneyFromAmountAndCurrencyCode
                    val referenceTotalPrice = packageReferenceTotalPrice?.formattedMoneyFromAmountAndCurrencyCode
                    val totalPrice = it.packageTotalPrice?.formattedMoneyFromAmountAndCurrencyCode
                    val savings = tripSavings?.formattedMoneyFromAmountAndCurrencyCode
                    val costSummaryBreakdown = PackageCostSummaryBreakdownModel(standaloneHotelPrice, standaloneFlightPrice, referenceTotalPrice, savings, totalPrice)
                    val costSummaryViewModel = (totalPriceWidget.breakdown.viewmodel as PackageCostSummaryBreakdownViewModel)
                    costSummaryViewModel.packageCostSummaryObservable.onNext(costSummaryBreakdown)
                }
                totalPriceWidget.toggleBundleTotalCompoundDrawable(shouldShowTripSavings)
            }
        }
    }

    //We have decided to not add mandatory fees in the bundle total for now. This will be worked on again.

    private fun setMandatoryFee(packageResponse: BundleSearchResponse) {
        val packageTotal = packageResponse.getCurrentOfferPrice()?.packageTotalPrice ?: return
/*        val rateInfo = Db.sharedInstance.packageSelectedRoom.rateInfo.chargeableRateInfo
        var mandatoryFee: Float = 0F

        //rateInfo.totalMandatoryFees has either daily or total mandatory fees based upon the display type (See convertMidHotelRoomResponse() in HotelOfferResponse for reference)
        if (rateInfo.mandatoryDisplayCurrency == MandatoryFees.DisplayCurrency.POINT_OF_SALE) {
            if (rateInfo.mandatoryDisplayType == MandatoryFees.DisplayType.DAILY) {
                mandatoryFee = rateInfo.totalMandatoryFees * getNumberOfDaysInHotel(packageResponse)
            } else {
                mandatoryFee = rateInfo.totalMandatoryFees
            }
        }
        val packageTotalWithMandatoryFee = packagetotal?.amount?.plus(BigDecimal(mandatoryFee.toString()))*/
        totalPriceWidget.viewModel.setBundleTotalPrice(packageTotal)
    }

/*    private fun getNumberOfDaysInHotel(packageResponse: BundleSearchResponse): Int {
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd")

        val checkInDate = dtf.parseLocalDate(packageResponse.getHotelCheckInDate())
        val checkoutDate = dtf.parseLocalDate(packageResponse.getHotelCheckOutDate())
        return Days.daysBetween(checkInDate, checkoutDate).days
    }*/

    private fun setHotelBundleWidgetGuestsAndDatesText(packageResponse: BundleSearchResponse) {
        bundleWidget.bundleHotelWidget.viewModel.hotelDatesGuestObservable.onNext(
                PackageUtil.getBundleHotelDatesAndGuestsText(context, packageResponse.getHotelCheckInDate(), packageResponse.getHotelCheckOutDate(), Db.sharedInstance.packageParams.guests))
    }
}
