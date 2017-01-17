package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.otto.Events
import com.expedia.bookings.presenter.packages.FlightTravelersPresenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.Ui
import com.expedia.vm.BaseCreateTripViewModel
import com.expedia.vm.packages.PackageCheckoutViewModel
import com.expedia.vm.packages.PackageCostSummaryBreakdownViewModel
import com.expedia.vm.packages.PackageCreateTripViewModel
import com.expedia.vm.traveler.FlightTravelersViewModel
import com.expedia.vm.traveler.TravelersViewModel
import com.squareup.otto.Subscribe

class PackageCheckoutPresenter(context: Context, attr: AttributeSet?) : BaseCheckoutPresenter(context, attr) {

    override fun trackCreateTripPriceChange(diffPercentage: Int) {
        PackagesTracking().trackCreateTripPriceChange(diffPercentage)
    }

    override fun trackCheckoutPriceChange(diffPercentage: Int) {
        PackagesTracking().trackCheckoutPriceChange(diffPercentage)
    }

    override fun shouldShowAlertForCreateTripPriceChange(response: TripResponse?): Boolean {
        return false
    }

    override fun getDefaultToTravelerTransition(): DefaultToTraveler {
        return DefaultToTraveler(FlightTravelersPresenter::class.java)
    }

    override fun injectComponents() {
        Ui.getApplication(context).packageComponent().inject(this)
    }

    override fun setupCreateTripViewModel(vm: BaseCreateTripViewModel) {
        vm as PackageCreateTripViewModel
        vm.tripParams.subscribe {
            userAccountRefresher.ensureAccountIsRefreshed()
        }
        getCheckoutViewModel().checkoutPriceChangeObservable.subscribe(getCreateTripViewModel().createTripResponseObservable)
    }

    @Subscribe fun onUserLoggedIn(@Suppress("UNUSED_PARAMETER") event: Events.LoggedInSuccessful) {
        onLoginSuccess()
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.PACKAGES
    }

    override fun updateDbTravelers() {
        val params = Db.getPackageParams()
        travelerManager.updateDbTravelers(params, context)
        resetTravelers()
    }

    override fun trackShowSlideToPurchase() {
        PackagesTracking().trackCheckoutSlideToPurchase()
    }

    override fun fireCheckoutOverviewTracking(createTripResponse: TripResponse) {
        createTripResponse as PackageCreateTripResponse
        PackagesTracking().trackBundleOverviewPageLoad(createTripResponse.packageDetails)
    }

    override fun makeCheckoutViewModel(): PackageCheckoutViewModel {
        return PackageCheckoutViewModel(context, Ui.getApplication(context).packageComponent().packageServices())
    }

    override fun makeCreateTripViewModel(): PackageCreateTripViewModel {
        return PackageCreateTripViewModel(Ui.getApplication(context).packageComponent().packageServices(), context)
    }

    override fun getCheckoutViewModel(): PackageCheckoutViewModel {
        return ckoViewModel as PackageCheckoutViewModel
    }

    override fun getCreateTripViewModel(): PackageCreateTripViewModel {
        return tripViewModel as PackageCreateTripViewModel
    }


    override fun showMainTravelerMinimumAgeMessaging(): Boolean {
        return true
    }

    override fun createTravelersViewModel(): TravelersViewModel {
        return FlightTravelersViewModel(context, getLineOfBusiness(), showMainTravelerMinimumAgeMessaging())
    }

    override fun handleCheckoutPriceChange(response: TripResponse) {
    }

    override fun onCreateTripResponse(response: TripResponse?) {
        loginWidget.updateRewardsText(getLineOfBusiness())
    }
}
