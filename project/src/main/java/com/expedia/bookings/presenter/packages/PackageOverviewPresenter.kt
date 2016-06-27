package com.expedia.bookings.presenter.packages

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.presenter.BaseOverviewPresenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.PackageCheckoutPresenter
import com.expedia.ui.PackageHotelActivity
import com.expedia.vm.packages.PackageCheckoutOverviewViewModel
import org.joda.time.format.DateTimeFormat

class PackageOverviewPresenter(context: Context, attrs: AttributeSet) : BaseOverviewPresenter(context, attrs) {
    val bundleWidget: BundleWidget by bindView(R.id.bundle_widget)
    val changeHotel by lazy { bundleOverviewHeader.toolbar.menu.findItem(R.id.package_change_hotel) }
    val changeHotelRoom by lazy { bundleOverviewHeader.toolbar.menu.findItem(R.id.package_change_hotel_room) }
    val changeFlight by lazy { bundleOverviewHeader.toolbar.menu.findItem(R.id.package_change_flight) }

    override fun inflate() {
        View.inflate(context, R.layout.package_overview, this)
    }

    init {
        bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel = PackageCheckoutOverviewViewModel(context)
        bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel = PackageCheckoutOverviewViewModel(context)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        removeView(bundleWidget)
        getCheckoutPresenter().getCreateTripViewModel().tripResponseObservable.subscribe { trip ->
            trip as PackageCreateTripResponse
            bundleOverviewHeader.toolbar.viewModel.showChangePackageMenuObservable.onNext(true)
            bundleWidget.outboundFlightWidget.toggleFlightWidget(1f, true)
            bundleWidget.inboundFlightWidget.toggleFlightWidget(1f, true)
            bundleWidget.bundleHotelWidget.toggleHotelWidget(1f, true)
            bundleWidget.toggleMenuObservable.onNext(true)
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

        changeHotel.setOnMenuItemClickListener({
            bundleOverviewHeader.toggleOverviewHeader(false)
            checkoutPresenter.toggleCheckoutButton(false)
            bundleWidget.collapseBundleWidgets()
            val params = Db.getPackageParams()
            params.pageType = Constants.PACKAGE_CHANGE_HOTEL
            params.searchProduct = null
            bundleWidget.viewModel.hotelParamsObservable.onNext(params)
            PackagesTracking().trackBundleEditItemClick("Hotel")
            true
        })

        changeHotelRoom.setOnMenuItemClickListener({
            checkoutPresenter.toggleCheckoutButton(false)
            bundleWidget.collapseBundleWidgets()
            val params = Db.getPackageParams()
            params.pageType = Constants.PACKAGE_CHANGE_HOTEL
            val intent = Intent(context, PackageHotelActivity::class.java)
            intent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, true)
            (context as AppCompatActivity).startActivityForResult(intent, Constants.HOTEL_REQUEST_CODE, null)
            PackagesTracking().trackBundleEditItemClick("Room")
            true
        })

        changeFlight.setOnMenuItemClickListener({
            bundleOverviewHeader.toggleOverviewHeader(false)
            checkoutPresenter.toggleCheckoutButton(false)
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
        bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel.checkIn.onNext(params.checkIn.toString(formatter))
        bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel.checkOut.onNext(params.checkOut.toString(formatter))

        bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel.checkIn.onNext(params.checkIn.toString(formatter))
        bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel.checkOut.onNext(params.checkOut.toString(formatter))
    }

    override fun back(): Boolean {
        bundleWidget.collapseBundleWidgets()
        return super.back()
    }

    fun getCheckoutPresenter(): PackageCheckoutPresenter {
        return checkoutPresenter as PackageCheckoutPresenter
    }

    override fun getCheckoutTransitionClass(): Class<out Any> {
        return PackageCheckoutPresenter::class.java
    }

    override fun trackCheckoutPageLoad() {
        PackagesTracking().trackCheckoutStart(Db.getTripBucket().`package`.mPackageTripResponse.packageDetails, Strings.capitalizeFirstLetter(Db.getPackageSelectedRoom().supplierType))
    }

    override fun trackPaymentCIDLoad() {
        PackagesTracking().trackCheckoutPaymentCID()
    }

    override fun toggleToolbar(forward: Boolean) {
        bundleWidget.toggleMenuObservable.onNext(!forward)
    }
}