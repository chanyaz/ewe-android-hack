package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.otto.Events
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.PackageCheckoutViewModel
import com.expedia.vm.PackageCreateTripViewModel
import com.squareup.otto.Subscribe
import com.squareup.phrase.Phrase

public class PackageCheckoutPresenter(context: Context, attr: AttributeSet) : BaseCheckoutPresenter(context, attr) {

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
        vm.tripParams.subscribe {
            createTripDialog.show()
            userAccountRefresher.ensureAccountIsRefreshed()
        }
        vm.tripResponseObservable.subscribe {
            loginWidget.updateRewardsText(getLineOfBusiness())
            createTripDialog.hide()
        }
        createTripViewModel.createTripBundleTotalObservable.subscribe { response ->
            var packageSavings = Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                    .put("savings", response.packageDetails.pricing.savings.formattedPrice)
                    .format().toString()
            vm.bundleTotalPrice.onNext(Pair(response.packageDetails.pricing.packageTotal.formattedWholePrice,
                    packageSavings))
        }
    }

    override fun doCreateTrip() {
        createTripViewModel.createTrip.onNext(Unit)
    }

    init {

    }

    @Subscribe fun onUserLoggedIn(event: Events.LoggedInSuccessful) {
        onLoginSuccess()
    }
}