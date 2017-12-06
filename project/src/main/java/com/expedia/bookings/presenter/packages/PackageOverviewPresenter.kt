package com.expedia.bookings.presenter.packages

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackagesPageUsableData
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.enums.TwoScreenOverviewState
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.presenter.BaseTwoScreenOverviewPresenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isBackFlowFromOverviewEnabled
import com.expedia.bookings.utils.isMidAPIEnabled
import com.expedia.bookings.widget.PackageCheckoutPresenter
import com.expedia.ui.PackageHotelActivity
import com.squareup.phrase.Phrase
import org.joda.time.format.DateTimeFormat
import rx.subjects.PublishSubject
import com.expedia.bookings.widget.shared.WebCheckoutView
import com.expedia.util.safeSubscribeOptional
import com.expedia.util.setInverseVisibility
import com.expedia.util.updateVisibility
import com.expedia.vm.PackageWebCheckoutViewViewModel
import com.expedia.vm.packages.AbstractUniversalCKOTotalPriceViewModel
import com.expedia.vm.packages.PackageCheckoutOverviewViewModel
import com.expedia.vm.packages.PackageCostSummaryBreakdownViewModel
import com.expedia.vm.packages.PackageTotalPriceViewModel
import com.expedia.vm.packages.OverviewHeaderData

class PackageOverviewPresenter(context: Context, attrs: AttributeSet) : BaseTwoScreenOverviewPresenter(context, attrs) {

    val bundleWidget: BundleWidget by bindView(R.id.bundle_widget)
    val changeHotel by lazy { bundleOverviewHeader.toolbar.menu.findItem(R.id.package_change_hotel) }
    val changeHotelRoom by lazy { bundleOverviewHeader.toolbar.menu.findItem(R.id.package_change_hotel_room) }
    val changeFlight by lazy { bundleOverviewHeader.toolbar.menu.findItem(R.id.package_change_flight) }
    val startOver by lazy { bundleOverviewHeader.toolbar.menu.findItem(R.id.package_start_over) }

    val toolbarNavIcon = PublishSubject.create<ArrowXDrawableUtil.ArrowDrawableType>()
    val toolbarNavIconContDescSubject = PublishSubject.create<String>()
    val performMIDCreateTripSubject = PublishSubject.create<Unit>()

    val webCheckoutView: WebCheckoutView by lazy {
        val viewStub = findViewById<ViewStub>(R.id.package_web_checkout_stub)
        val webCheckoutView = viewStub.inflate() as WebCheckoutView
        val webCheckoutViewModel = PackageWebCheckoutViewViewModel(context)
        webCheckoutViewModel.packageCreateTripViewModel = getCheckoutPresenter().getCreateTripViewModel()
        webCheckoutView.viewModel = webCheckoutViewModel

        webCheckoutViewModel.closeView.subscribe {
            webCheckoutView.clearHistory()
            webCheckoutViewModel.webViewURLObservable.onNext("about:blank")
        }

        webCheckoutViewModel.backObservable.subscribe {
            webCheckoutView.back()
        }

        webCheckoutViewModel.blankViewObservable.subscribe {
            super.back()
        }
        webCheckoutView
    }

    override fun inflate() {
        View.inflate(context, R.layout.package_overview, this)
    }

    override fun injectComponents() {}

    init {
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
        getCheckoutPresenter().getCreateTripViewModel().createTripResponseObservable.safeSubscribeOptional { trip ->
            trip as PackageCreateTripResponse
            bundleWidgetSetup()
            bundleWidget.viewModel.createTripObservable.onNext(trip)

            val hotelTripResponse = trip.packageDetails.hotel
            val headerData = OverviewHeaderData(hotelTripResponse.hotelCity, hotelTripResponse.hotelCountry, hotelTripResponse.hotelStateProvince, hotelTripResponse.checkOutDate,
                    hotelTripResponse.checkInDate, hotelTripResponse.largeThumbnailUrl)
            (bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel
                    as PackageCheckoutOverviewViewModel).tripResponseSubject.onNext(headerData)
            (bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel
                    as PackageCheckoutOverviewViewModel).tripResponseSubject.onNext(headerData)

            var totalPrice = ""
            if (trip.packageDetails.pricing.hasResortFee()) {
                totalPrice = Phrase.from(context, R.string.your_card_will_be_charged_template)
                        .put("dueamount", trip.tripTotalPayableIncludingFeeIfZeroPayableByPoints().formattedMoneyFromAmountAndCurrencyCode)
                        .format().toString()
            }
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
            bundleOverviewHeader.toggleOverviewHeader(false)
            resetAndShowTotalPriceWidget()
            checkoutPresenter.clearPaymentInfo()
            checkoutPresenter.updateDbTravelers()
            totalPriceWidget.toggleBundleTotalCompoundDrawable(false)
            resetBundleTotalTax()
            bundleWidget.collapseBundleWidgets()
            val params = Db.getPackageParams()
            params.pageType = Constants.PACKAGE_CHANGE_HOTEL
            params.searchProduct = null
            bundleWidget.viewModel.hotelParamsObservable.onNext(params)
            bottomCheckoutContainer.viewModel.sliderPurchaseTotalText.onNext("")
            PackagesTracking().trackBundleEditItemClick("Hotel")
            true
        })

        changeHotelRoom.setOnMenuItemClickListener({
            resetAndShowTotalPriceWidget()
            checkoutPresenter.clearPaymentInfo()
            checkoutPresenter.updateDbTravelers()
            bundleWidget.collapseBundleWidgets()
            totalPriceWidget.toggleBundleTotalCompoundDrawable(false)
            resetBundleTotalTax()
            val params = Db.getPackageParams()
            params.pageType = Constants.PACKAGE_CHANGE_HOTEL
            val intent = Intent(context, PackageHotelActivity::class.java)
            intent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, true)
            (context as AppCompatActivity).startActivityForResult(intent, Constants.HOTEL_REQUEST_CODE, null)
            PackagesTracking().trackBundleEditItemClick("Room")
            true
        })

        changeFlight.setOnMenuItemClickListener({
            resetAndShowTotalPriceWidget()
            checkoutPresenter.clearPaymentInfo()
            checkoutPresenter.updateDbTravelers()
            bundleOverviewHeader.toggleOverviewHeader(false)
            totalPriceWidget.toggleBundleTotalCompoundDrawable(false)
            resetBundleTotalTax()
            bundleWidget.collapseBundleWidgets()
            val params = Db.getPackageParams()
            params.pageType = Constants.PACKAGE_CHANGE_FLIGHT
            params.searchProduct = Constants.PRODUCT_FLIGHT
            params.selectedLegId = null
            bundleWidget.viewModel.flightParamsObservable.onNext(params)
            PackagesTracking().trackBundleEditItemClick("Flight")

            true
        })

        if (isMidAPIEnabled(context)) {
            addTransition(overviewToWebCheckoutView)
        } else {
            addTransition(checkoutTransition)
            addTransition(checkoutToCvv)
        }

        //govern start over menu item visibility from AB Test
        if (isBackFlowFromOverviewEnabled(context)) {
            startOver.setOnMenuItemClickListener({
                showBackToSearchDialog()
                PackagesTracking().trackBundleEditItemClick("StartOver")
                true
            })
        } else {
            startOver.setVisible(false)
        }
        if (isMidAPIEnabled(context)) {

            performMIDCreateTripSubject.subscribe {
                (webCheckoutView.viewModel as PackageWebCheckoutViewViewModel).doCreateTrip()
                setupOverviewPresenterForMID()
            }
        }
    }

    private fun resetBundleTotalTax() {
        if (PointOfSale.getPointOfSale().pointOfSaleId == PointOfSaleId.JAPAN) {
            totalPriceWidget.bundleTotalText.text = StrUtils.bundleTotalWithTaxesString(context)
        }
    }

    private fun setCheckoutHeaderOverviewDates() {
        val params = Db.getPackageParams()
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
            //Back press from overview screen

            if (isBackFlowFromOverviewEnabled(context)) {
                checkoutPresenter.getCheckoutViewModel().bottomCheckoutContainerStateObservable.onNext(TwoScreenOverviewState.OTHER)
                bundleOverviewHeader.toggleOverviewHeader(false)
                bundleOverviewHeader.toolbar.menu.setGroupVisible(R.id.package_change_menu, false)
            } else {
                showBackToSearchDialog()
                return true
            }
        }
        bundleWidget.collapseBundleWidgets()
        return super.back()
    }

    private fun showBackToSearchDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.package_checkout_back_dialog_title)
        builder.setMessage(R.string.package_checkout_back_dialog_message)
        builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, which -> dialog.dismiss() }
        builder.setPositiveButton(context.getString(R.string.start_over)) { dialog, which ->
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
        PackagesTracking().trackCheckoutStart(Db.getTripBucket().`package`.mPackageTripResponse.packageDetails, Strings.capitalizeFirstLetter(Db.getPackageSelectedRoom().supplierType))
    }

    override fun trackPaymentCIDLoad() {
        PackagesTracking().trackCheckoutPaymentCID()
    }

    override fun setToolbarMenu(forward: Boolean) {
        bundleWidget.toggleMenuObservable.onNext(!forward)
    }

    override fun setToolbarNavIcon(forward : Boolean) {
        if (forward || isBackFlowFromOverviewEnabled(context)) {
            toolbarNavIconContDescSubject.onNext(resources.getString(R.string.toolbar_nav_icon_cont_desc))
            toolbarNavIcon.onNext(ArrowXDrawableUtil.ArrowDrawableType.BACK)
        }
        else {
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

    override fun onTripResponse(response: TripResponse?) {
        response as PackageCreateTripResponse
        totalPriceWidget.viewModel.total.onNext(response.bundleTotal)
        val packageTotalPrice = response.packageDetails.pricing
        totalPriceWidget.viewModel.savings.onNext(packageTotalPrice.savings)
        val costSummaryViewModel = (totalPriceWidget.breakdown.viewmodel as PackageCostSummaryBreakdownViewModel)
        costSummaryViewModel.packageCostSummaryObservable.onNext(response)

        val messageString =
                if (response.packageDetails.pricing.hasResortFee() && !PointOfSale.getPointOfSale().shouldShowBundleTotalWhenResortFees())
                    R.string.cost_summary_breakdown_total_due_today
                else if (PointOfSale.getPointOfSale().pointOfSaleId == PointOfSaleId.JAPAN)
                    R.string.packages_trip_total
                else
                    R.string.bundle_total_text
        totalPriceWidget.viewModel.bundleTextLabelObservable.onNext(context.getString(messageString))
        if (ProductFlavorFeatureConfiguration.getInstance().shouldShowPackageIncludesView())
            totalPriceWidget.viewModel.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_flights_hotel))
    }

    override fun fireCheckoutOverviewTracking(createTripResponse: TripResponse) {
        createTripResponse as PackageCreateTripResponse
        PackagesPageUsableData.RATE_DETAILS.pageUsableData.markAllViewsLoaded()
        PackagesTracking().trackBundleOverviewPageLoad(createTripResponse.packageDetails, PackagesPageUsableData.RATE_DETAILS.pageUsableData)
    }

    override fun getPriceViewModel(context: Context): AbstractUniversalCKOTotalPriceViewModel {
        return PackageTotalPriceViewModel(context)
    }

    private val overviewToWebCheckoutView = object : Transition(BaseTwoScreenOverviewPresenter.BundleDefault::class.java, WebCheckoutView::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            checkoutPresenter.setInverseVisibility(forward)
            bundleOverviewHeader.setInverseVisibility(forward)
            webCheckoutView.updateVisibility(forward)
        }
    }

    override fun showCheckout() {
        if (isMidAPIEnabled(context)) {
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
        bundleWidget.viewModel.getHotelNameAndDaysToSetUpTitle()
        val hotel = Db.getPackageSelectedHotel()
        val searchResponse = Db.getPackageResponse()

        val headerData = OverviewHeaderData(hotel.city, hotel.countryCode, hotel.stateProvinceCode, searchResponse.getHotelCheckOutDate(),
                searchResponse.getHotelCheckInDate(), hotel.largeThumbnailUrl)
        (bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel
                as PackageCheckoutOverviewViewModel).tripResponseSubject.onNext(headerData)

        (bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel
                as PackageCheckoutOverviewViewModel).tripResponseSubject.onNext(headerData)
        setCheckoutHeaderOverviewDates()
    }
}
