package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.otto.Events
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.Ui
import com.expedia.vm.BaseCreateTripViewModel
import com.expedia.vm.packages.PackageCheckoutViewModel
import com.expedia.vm.packages.PackageCostSummaryBreakdownViewModel
import com.expedia.vm.packages.PackageCreateTripViewModel
import com.squareup.otto.Subscribe
import java.math.BigDecimal

class PackageCheckoutPresenter(context: Context, attr: AttributeSet) : BaseCheckoutPresenter(context, attr) {

    override fun setupCreateTripViewModel(vm: BaseCreateTripViewModel) {
        vm as PackageCreateTripViewModel
        vm.tripParams.subscribe {
            userAccountRefresher.ensureAccountIsRefreshed()
        }
        vm.tripResponseObservable.subscribe { response ->
            response as PackageCreateTripResponse
            clearCCNumber()
            loginWidget.updateRewardsText(getLineOfBusiness())
            priceChangeWidget.viewmodel.originalPrice.onNext(response.oldPackageDetails?.pricing?.packageTotal)
            priceChangeWidget.viewmodel.newPrice.onNext(response.packageDetails.pricing.packageTotal)
            (totalPriceWidget.breakdown.viewmodel as PackageCostSummaryBreakdownViewModel).packageCostSummaryObservable.onNext(response.packageDetails)

            val messageString =
                    if (response.packageDetails.pricing.hasResortFee() && !PointOfSale.getPointOfSale().shouldShowBundleTotalWhenResortFees())
                        R.string.cost_summary_breakdown_total_due_today
                    else
                        R.string.bundle_total_text
            totalPriceWidget.viewModel.bundleTextLabelObservable.onNext(context.getString(messageString))

            val packageTotalPrice = response.packageDetails.pricing
            totalPriceWidget.viewModel.total.onNext(packageTotalPrice.packageTotal)
            totalPriceWidget.viewModel.savings.onNext(packageTotalPrice.savings)
            isPassportRequired(response)
            trackShowBundleOverview()
        }
        getCheckoutViewModel().priceChangeObservable.subscribe {
            getCreateTripViewModel().tripResponseObservable.onNext(it)
        }
    }

    @Subscribe fun onUserLoggedIn(@Suppress("UNUSED_PARAMETER") event: Events.LoggedInSuccessful) {
        onLoginSuccess()
    }

    override fun isPassportRequired(response: TripResponse) {
        val flightOffer = (response as PackageCreateTripResponse).packageDetails.flight.details.offer
        travelerPresenter.viewModel.passportRequired.onNext(flightOffer.isInternational || flightOffer.isPassportNeeded)
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.PACKAGES
    }

    override fun updateDbTravelers() {
        val params = Db.getPackageParams()
        travelerManager.updateDbTravelers(params, context)
    }

    override fun trackShowSlideToPurchase() {
        PackagesTracking().trackCheckoutSlideToPurchase()
    }

    override fun trackShowBundleOverview() {
        PackagesTracking().trackBundleOverviewPageLoad(Db.getTripBucket().`package`.mPackageTripResponse.packageDetails)
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

    override fun clearCCNumber() {
        clearCVV()
        super.clearCCNumber()
    }

    override fun getCostSummaryBreakdownViewModel(): PackageCostSummaryBreakdownViewModel {
        return PackageCostSummaryBreakdownViewModel(context)
    }
}
