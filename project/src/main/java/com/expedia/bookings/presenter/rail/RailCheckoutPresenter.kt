package com.expedia.bookings.presenter.rail

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.vm.BaseCheckoutViewModel
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import com.expedia.vm.BaseCreateTripViewModel
import com.expedia.vm.rail.RailCheckoutViewModel
import com.expedia.vm.rail.RailCostSummaryBreakdownViewModel
import com.expedia.vm.rail.RailCreateTripViewModel

class RailCheckoutPresenter(context: Context, attrs: AttributeSet) : BaseCheckoutPresenter(context, attrs) {

    override fun setupCreateTripViewModel(vm: BaseCreateTripViewModel) {
        vm as RailCreateTripViewModel
        vm.offerCodeSelectedObservable.subscribe {
            createTripDialog.show()
        }

        vm.tripResponseObservable.subscribe { response -> response as RailCreateTripResponse
            createTripDialog.hide()
            clearCCNumber()
            updatePricing(response)
        }
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.RAILS
    }

    override fun updateDbTravelers() {
    }

    override fun makeCheckoutViewModel(): BaseCheckoutViewModel {
        return RailCheckoutViewModel(context)
    }

    override fun makeCreateTripViewModel(): RailCreateTripViewModel {
        return RailCreateTripViewModel(Ui.getApplication(context).railComponent().railService())
    }

    override fun getCheckoutViewModel(): RailCheckoutViewModel {
        return ckoViewModel as RailCheckoutViewModel
    }

    override fun getCreateTripViewModel(): RailCreateTripViewModel {
        return tripViewModel as RailCreateTripViewModel
    }

    override fun getCostSummaryBreakdownViewModel(): BaseCostSummaryBreakdownViewModel {
        return RailCostSummaryBreakdownViewModel(context)
    }

    override fun trackShowSlideToPurchase() {
    }

    override fun trackShowBundleOverview() {
    }

    override fun isPassportRequired(response: TripResponse) {
    }

    override fun showMainTravelerMinimumAgeMessaging(): Boolean {
        return false
    }

    private fun updatePricing(response: RailCreateTripResponse) {
        totalPriceWidget.viewModel.total.onNext(response.totalPrice)
        totalPriceWidget.viewModel.costBreakdownEnabledObservable.onNext(true)
        (totalPriceWidget.breakdown.viewmodel as RailCostSummaryBreakdownViewModel).railCostSummaryBreakdownObservable.onNext(response.railDomainProduct.railOffer)
    }
}


