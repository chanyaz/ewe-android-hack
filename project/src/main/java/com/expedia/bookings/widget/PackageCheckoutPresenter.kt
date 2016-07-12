package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.otto.Events
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.Ui
import com.expedia.vm.packages.BaseCreateTripViewModel
import com.expedia.vm.packages.PackageCheckoutViewModel
import com.expedia.vm.packages.PackageCostSummaryBreakdownViewModel
import com.expedia.vm.packages.PackageCreateTripViewModel
import com.expedia.vm.traveler.CheckoutTravelerViewModel
import com.squareup.otto.Subscribe
import java.math.BigDecimal

class PackageCheckoutPresenter(context: Context, attr: AttributeSet) : BaseCheckoutPresenter(context, attr) {

    override fun setupCreateTripViewModel(vm : BaseCreateTripViewModel) {
        vm as PackageCreateTripViewModel
        vm.tripParams.subscribe {
            userAccountRefresher.ensureAccountIsRefreshed()
        }
        vm.tripResponseObservable.subscribe { response -> response as PackageCreateTripResponse
            clearCCNumber()
            loginWidget.updateRewardsText(getLineOfBusiness())
            priceChangeWidget.viewmodel.originalPrice.onNext(response.oldPackageDetails?.pricing?.packageTotal)
            priceChangeWidget.viewmodel.newPrice.onNext(response.packageDetails.pricing.packageTotal)
            (totalPriceWidget.breakdown.viewmodel as PackageCostSummaryBreakdownViewModel).packageCostSummaryObservable.onNext(response.packageDetails)
            var packageTotalPrice = response.packageDetails.pricing
            totalPriceWidget.viewModel.total.onNext(Money(BigDecimal(packageTotalPrice.packageTotal.amount.toDouble()),
                    packageTotalPrice.packageTotal.currencyCode))
            totalPriceWidget.viewModel.savings.onNext(Money(BigDecimal(packageTotalPrice.savings.amount.toDouble()),
                    packageTotalPrice.savings.currencyCode))
            trackShowBundleOverview()
        }

    }

    @Subscribe fun onUserLoggedIn(@Suppress("UNUSED_PARAMETER") event: Events.LoggedInSuccessful) {
        onLoginSuccess()
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.PACKAGES
    }

    override fun updateTravelerPresenter() {
        travelerPresenter.viewModel = CheckoutTravelerViewModel(context)
        travelerPresenter.refreshState()
    }

    override fun updateDbTravelers() {
        val params = Db.getPackageParams()
        travelerManager.updateDbTravelers(params)
    }

    override fun trackShowSlideToPurchase() {
        PackagesTracking().trackCheckoutSlideToPurchase()
    }

    override fun trackShowBundleOverview() {
        PackagesTracking().trackBundleOverviewPageLoad(Db.getTripBucket().`package`.mPackageTripResponse.packageDetails)
    }

    override fun makeCheckoutViewModel() : PackageCheckoutViewModel {
        return PackageCheckoutViewModel(context, Ui.getApplication(context).packageComponent().packageServices())
    }

    override fun makeCreateTripViewModel() : PackageCreateTripViewModel {
        return PackageCreateTripViewModel(Ui.getApplication(context).packageComponent().packageServices(), context)
    }

    override fun getCheckoutViewModel(): PackageCheckoutViewModel {
        return ckoViewModel as PackageCheckoutViewModel
    }

    override fun getCreateTripViewModel(): PackageCreateTripViewModel {
        return tripViewModel as PackageCreateTripViewModel
    }
}
