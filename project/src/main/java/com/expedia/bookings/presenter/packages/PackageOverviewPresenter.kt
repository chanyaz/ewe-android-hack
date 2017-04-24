package com.expedia.bookings.presenter.packages

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.presenter.BaseTwoScreenOverviewPresenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.PackageCheckoutPresenter
import com.expedia.ui.PackageHotelActivity
import com.expedia.util.safeSubscribe
import com.expedia.vm.packages.AbstractUniversalCKOTotalPriceViewModel
import com.expedia.vm.packages.PackageTotalPriceViewModel
import com.expedia.vm.packages.PackageCheckoutOverviewViewModel
import com.expedia.vm.packages.PackageCostSummaryBreakdownViewModel
import org.joda.time.format.DateTimeFormat
import rx.subjects.PublishSubject

class PackageOverviewPresenter(context: Context, attrs: AttributeSet) : BaseTwoScreenOverviewPresenter(context, attrs) {
    val bundleWidget: BundleWidget by bindView(R.id.bundle_widget)
    val changeHotel by lazy { bundleOverviewHeader.toolbar.menu.findItem(R.id.package_change_hotel) }
    val changeHotelRoom by lazy { bundleOverviewHeader.toolbar.menu.findItem(R.id.package_change_hotel_room) }
    val changeFlight by lazy { bundleOverviewHeader.toolbar.menu.findItem(R.id.package_change_flight) }

    val toolbarNavIcon = PublishSubject.create<ArrowXDrawableUtil.ArrowDrawableType>()
    val toolbarNavIconContDescSubject = PublishSubject.create<String>()

    override fun inflate() {
        View.inflate(context, R.layout.package_overview, this)
    }

    init {
        bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel = PackageCheckoutOverviewViewModel(context)
        bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel = PackageCheckoutOverviewViewModel(context)
        toolbarNavIconContDescSubject.subscribe(bundleOverviewHeader.toolbar.viewModel.toolbarNavIconContentDesc)
        toolbarNavIcon.subscribe(bundleOverviewHeader.toolbar.viewModel.toolbarNavIcon)
        scrollSpaceView = bundleWidget.scrollSpaceView
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        removeView(bundleWidget)
        getCheckoutPresenter().getCreateTripViewModel().createTripResponseObservable.safeSubscribe { trip ->
            trip as PackageCreateTripResponse
            bundleWidget.outboundFlightWidget.toggleFlightWidget(1f, true)
            bundleWidget.inboundFlightWidget.toggleFlightWidget(1f, true)
            bundleWidget.bundleHotelWidget.toggleHotelWidget(1f, true)

            if (currentState == BundleDefault::class.java.name) {
                bundleWidget.toggleMenuObservable.onNext(true)
                setToolbarNavIcon(false)
            }

            bundleWidget.setPadding(0, 0, 0, 0)
            bundleWidget.viewModel.createTripObservable.onNext(trip)
            (bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel
                    as PackageCheckoutOverviewViewModel).tripResponseSubject.onNext(trip)
            (bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel
                    as PackageCheckoutOverviewViewModel).tripResponseSubject.onNext(trip)
            bundleWidget.bundleHotelWidget.collapseSelectedHotel()
            bundleWidget.outboundFlightWidget.collapseFlightDetails()
            bundleWidget.inboundFlightWidget.collapseFlightDetails()

            setCheckoutHeaderOverviewDates()
        }

        getCheckoutPresenter().getCreateTripViewModel().bundleDatesObservable
                .subscribe(bundleWidget.bundleHotelWidget.viewModel.hotelDatesGuestObservable)

        bundleOverviewHeader.nestedScrollView.addView(bundleWidget)
        bundleOverviewHeader.toolbar.inflateMenu(R.menu.menu_package_checkout)
        bundleWidget.toggleMenuObservable.subscribe(bundleOverviewHeader.toolbar.toggleMenuObserver)

        getCheckoutPresenter().getCheckoutViewModel().slideToBookA11yActivateObservable.subscribe(checkoutSliderSlidObserver)

        changeHotel.setOnMenuItemClickListener({
            bundleOverviewHeader.toggleOverviewHeader(false)
            resetAndShowTotalPriceWidget()
            checkoutPresenter.clearPaymentInfo()
            checkoutPresenter.updateDbTravelers()
            totalPriceWidget.toggleBundleTotalCompoundDrawable(false)
            bundleWidget.collapseBundleWidgets()
            val params = Db.getPackageParams()
            params.pageType = Constants.PACKAGE_CHANGE_HOTEL
            params.searchProduct = null
            bundleWidget.viewModel.hotelParamsObservable.onNext(params)
            PackagesTracking().trackBundleEditItemClick("Hotel")
            true
        })

        changeHotelRoom.setOnMenuItemClickListener({
            resetAndShowTotalPriceWidget()
            checkoutPresenter.clearPaymentInfo()
            checkoutPresenter.updateDbTravelers()
            bundleWidget.collapseBundleWidgets()
            totalPriceWidget.toggleBundleTotalCompoundDrawable(false)
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
            bundleWidget.collapseBundleWidgets()
            val params = Db.getPackageParams()
            params.pageType = Constants.PACKAGE_CHANGE_FLIGHT
            params.searchProduct = Constants.PRODUCT_FLIGHT
            params.selectedLegId = null
            bundleWidget.viewModel.flightParamsObservable.onNext(params)
            PackagesTracking().trackBundleEditItemClick("Flight")

            true
        })

    }

    private fun setCheckoutHeaderOverviewDates() {
        val params = Db.getPackageParams()
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")

        //set the package start and end date
        bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel.checkIn.onNext(params.startDate.toString(formatter))
        bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel.checkOut.onNext(params.endDate?.toString(formatter))

        bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel.checkIn.onNext(params.startDate.toString(formatter))
        bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel.checkOut.onNext(params.endDate?.toString(formatter))
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
        builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, which -> dialog.dismiss() }
        builder.setPositiveButton(context.getString(R.string.start_over)) { dialog, which ->
            checkoutPresenter.clearPaymentInfo()
            checkoutPresenter.resetTravelers()
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
        if(forward) {
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
                else
                    R.string.bundle_total_text
        totalPriceWidget.viewModel.bundleTextLabelObservable.onNext(context.getString(messageString))
        if (ProductFlavorFeatureConfiguration.getInstance().shouldShowPackageIncludesView())
            totalPriceWidget.viewModel.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_flights_hotel))
    }

    override fun fireCheckoutOverviewTracking(createTripResponse: TripResponse) {
        createTripResponse as PackageCreateTripResponse
        PackagesTracking().trackBundleOverviewPageLoad(createTripResponse.packageDetails)
    }

    override fun getPriceViewModel(context: Context): AbstractUniversalCKOTotalPriceViewModel {
        return PackageTotalPriceViewModel(context)
    }
}