package com.expedia.bookings.presenter.rail

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.vm.BaseCheckoutViewModel
import com.expedia.vm.packages.BaseCreateTripViewModel
import com.expedia.vm.rail.RailCheckoutViewModel
import com.expedia.vm.rail.RailCreateTripViewModel
import com.expedia.vm.traveler.CheckoutTravelerViewModel

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
        return LineOfBusiness.RAIL
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

    override fun trackShowSlideToPurchase() {
    }

    override fun trackShowBundleOverview() {
    }

    private fun updatePricing(response: RailCreateTripResponse) {
        totalPriceWidget.viewModel.total.onNext(response.totalPrice)
        totalPriceWidget.viewModel.savings.onNext(response.totalPrice)
        //TODO Cost Breakdown
    }
}


