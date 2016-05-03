package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.flights.FlightCreateTripViewModel
import com.expedia.bookings.otto.Events
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.FlightCheckoutViewModel
import com.squareup.otto.Subscribe

class FlightCheckoutPresenter(context: Context, attr: AttributeSet) : BaseCheckoutPresenter(context, attr) {
    var checkoutViewModel: FlightCheckoutViewModel by notNullAndObservable { vm ->
        viewModel.checkoutInfoCompleted.subscribe(vm.baseParams)
        vm.legalText.subscribeTextAndVisibility(legalInformationText)
        vm.depositPolicyText.subscribeTextAndVisibility(depositPolicyText)
        vm.sliderPurchaseTotalText.subscribeTextAndVisibility(slideTotalText)
    }

    var createTripViewModel: FlightCreateTripViewModel by notNullAndObservable { vm ->

        vm.tripParams.subscribe {
            createTripDialog.show()
            userAccountRefresher.ensureAccountIsRefreshed()
        }

        vm.tripResponseObservable.subscribe { response ->
            loginWidget.updateRewardsText(getLineOfBusiness())
            createTripDialog.hide()
            priceChangeWidget.viewmodel.originalPackagePrice.onNext(response.totalPrice)
            priceChangeWidget.viewmodel.packagePrice.onNext(response.totalPrice)
            totalPriceWidget.viewModel.total.onNext(response.totalPrice)
            totalPriceWidget.viewModel.savings.onNext(response.totalPrice)
            //TODO: Add Breakdown
            toggleCheckoutButton(true)
        }

    }

    override fun doCreateTrip() {
        createTripViewModel.performCreateTrip.onNext(Unit)
    }

    @Subscribe fun onUserLoggedIn( @Suppress("UNUSED_PARAMETER") event: Events.LoggedInSuccessful) {
        onLoginSuccess()
    }

    override fun lineOfBusiness() : LineOfBusiness {
        return LineOfBusiness.FLIGHTS
    }

    override fun updateTravelerPresenter() {
        travelerPresenter.visibility = View.VISIBLE
    }

    override fun trackShowSlideToPurchase() {
    }

    override fun trackShowBundleOverview() {
    }
}
