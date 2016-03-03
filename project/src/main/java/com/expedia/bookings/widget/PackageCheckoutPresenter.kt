package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.otto.Events
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.PackageCheckoutViewModel
import com.expedia.vm.PackageCreateTripViewModel
import com.squareup.otto.Subscribe
import com.squareup.phrase.Phrase

class PackageCheckoutPresenter(context: Context, attr: AttributeSet) : BaseCheckoutPresenter(context, attr) {

    var packageCheckoutViewModel: PackageCheckoutViewModel by notNullAndObservable { vm ->
        viewModel.checkoutInfoCompleted.subscribe(vm.baseParams)
        vm.legalText.subscribeTextAndVisibility(legalInformationText)
        vm.depositPolicyText.subscribeTextAndVisibility(depositPolicyText)
        vm.sliderPurchaseTotalText.subscribeTextAndVisibility(slideTotalText)
        vm.checkoutParams.subscribe {
            checkoutDialog.show()
        }
        vm.checkoutResponse.subscribe {
            checkoutDialog.hide()
        }
    }

    var createTripViewModel: PackageCreateTripViewModel by notNullAndObservable { vm ->
        vm.showCreateTripDialogObservable.subscribe {
            createTripDialog.show()
        }
        vm.tripParams.subscribe {
            userAccountRefresher.ensureAccountIsRefreshed()
        }
        vm.tripResponseObservable.subscribe {
            loginWidget.updateRewardsText(getLineOfBusiness())
            createTripDialog.hide()
        }
        vm.createTripBundleTotalObservable.subscribe { response ->
            priceChangeWidget.viewmodel.originalPackagePrice.onNext(response.oldPackageDetails?.pricing?.packageTotal)
            priceChangeWidget.viewmodel.packagePrice.onNext(response.packageDetails.pricing.packageTotal)
            toggleCheckoutButton(true)
        }
        vm.tripResponseObservable.subscribe(totalPriceWidget.viewModel.createTripObservable)
        vm.createTripBundleTotalObservable.subscribe { trip ->
            totalPriceWidget.packagebreakdown.viewmodel.newDataObservable.onNext(trip.packageDetails)
        }
    }

    override fun doCreateTrip() {
        createTripViewModel.performCreateTrip.onNext(Unit)
    }

    init {

    }

    @Subscribe fun onUserLoggedIn(event: Events.LoggedInSuccessful) {
        onLoginSuccess()
    }
}