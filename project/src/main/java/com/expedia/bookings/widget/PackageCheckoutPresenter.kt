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
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.Ui
import com.expedia.vm.BaseCreateTripViewModel
import com.expedia.vm.PaymentViewModel
import com.expedia.vm.packages.PackageCheckoutViewModel
import com.expedia.vm.packages.PackageCostSummaryBreakdownViewModel
import com.expedia.vm.packages.PackageCreateTripViewModel
import com.squareup.otto.Subscribe
import javax.inject.Inject

class PackageCheckoutPresenter(context: Context, attr: AttributeSet?) : BaseCheckoutPresenter(context, attr) {
    lateinit var paymentViewModel: PaymentViewModel
        @Inject set

    override fun injectComponents() {
        Ui.getApplication(context).packageComponent().inject(this)
    }

    override fun getPaymentWidgetViewModel(): PaymentViewModel {
        return paymentViewModel
    }

    override fun setupCreateTripViewModel(vm: BaseCreateTripViewModel) {
        vm as PackageCreateTripViewModel
        vm.tripParams.subscribe {
            userAccountRefresher.ensureAccountIsRefreshed()
        }
        vm.tripResponseObservable.subscribe { response ->
            response as PackageCreateTripResponse
            loginWidget.updateRewardsText(getLineOfBusiness())
            priceChangeWidget.viewmodel.originalPrice.onNext(response.oldPackageDetails?.pricing?.packageTotal)
            priceChangeWidget.viewmodel.newPrice.onNext(response.tripTotalPayableIncludingFeeIfZeroPayableByPoints())
            totalPriceWidget.viewModel.total.onNext(response.tripTotalPayableIncludingFeeIfZeroPayableByPoints())
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
            isPassportRequired(response)
            trackShowBundleOverview()
        }
        getCheckoutViewModel().priceChangeObservable.subscribe(getCreateTripViewModel().tripResponseObservable)
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
        resetTravelers()
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

    override fun getCostSummaryBreakdownViewModel(): PackageCostSummaryBreakdownViewModel {
        return PackageCostSummaryBreakdownViewModel(context)
    }

    override fun showMainTravelerMinimumAgeMessaging(): Boolean {
        return true
    }
}
