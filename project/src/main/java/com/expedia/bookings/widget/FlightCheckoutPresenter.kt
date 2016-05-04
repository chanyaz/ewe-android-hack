package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.otto.Events
import com.expedia.bookings.utils.Ui
import com.expedia.vm.FlightCheckoutViewModel
import com.expedia.vm.flights.FlightCreateTripViewModel
import com.expedia.vm.packages.BaseCreateTripViewModel
import com.squareup.otto.Subscribe

class FlightCheckoutPresenter(context: Context, attr: AttributeSet) : BaseCheckoutPresenter(context, attr) {
    override fun setUpCreateTripViewModel(vm : BaseCreateTripViewModel) {
        vm as FlightCreateTripViewModel
        vm.tripParams.subscribe {
            createTripDialog.show()
            userAccountRefresher.ensureAccountIsRefreshed()
        }

        vm.tripResponseObservable.subscribe { response -> response as FlightCreateTripResponse
            loginWidget.updateRewardsText(getLineOfBusiness())
            createTripDialog.hide()
            priceChangeWidget.viewmodel.originalPackagePrice.onNext(response.totalPrice)
            priceChangeWidget.viewmodel.packagePrice.onNext(response.totalPrice)
            totalPriceWidget.viewModel.total.onNext(response.totalPrice)
            totalPriceWidget.viewModel.savings.onNext(response.totalPrice)
            //TODO: Add Breakdown
        }

    }

    @Subscribe fun onUserLoggedIn( @Suppress("UNUSED_PARAMETER") event: Events.LoggedInSuccessful) {
        onLoginSuccess()
    }

    override fun getLineOfBusiness() : LineOfBusiness {
        return LineOfBusiness.FLIGHTS
    }

    override fun updateTravelerPresenter() {
        travelerPresenter.visibility = View.VISIBLE
    }

    override fun trackShowSlideToPurchase() {
    }

    override fun trackShowBundleOverview() {
    }

    override fun setCheckoutViewModel(): FlightCheckoutViewModel {
        return FlightCheckoutViewModel(context, Ui.getApplication(context).flightComponent().flightServices())
    }

    override fun setCreateTripViewModel(): FlightCreateTripViewModel {
        return FlightCreateTripViewModel(Ui.getApplication(context).flightComponent().flightServices())
    }

    override fun getCheckoutViewModel(): FlightCheckoutViewModel {
        return ckoViewModel as FlightCheckoutViewModel
    }

    override fun getCreateTripViewModel(): FlightCreateTripViewModel {
        return tripViewModel as FlightCreateTripViewModel
    }
}
