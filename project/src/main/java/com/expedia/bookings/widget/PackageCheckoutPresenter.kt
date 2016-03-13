package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.otto.Events
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.PackageCheckoutViewModel
import com.expedia.vm.PackageCreateTripViewModel
import com.squareup.otto.Subscribe
import java.math.BigDecimal

class PackageCheckoutPresenter(context: Context, attr: AttributeSet) : BaseCheckoutPresenter(context, attr) {

    var checkoutViewModel: PackageCheckoutViewModel by notNullAndObservable { vm ->
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
        vm.tripResponseObservable.subscribe { response ->
            loginWidget.updateRewardsText(getLineOfBusiness())
            createTripDialog.hide()
            priceChangeWidget.viewmodel.originalPackagePrice.onNext(response.oldPackageDetails?.pricing?.packageTotal)
            priceChangeWidget.viewmodel.packagePrice.onNext(response.packageDetails.pricing.packageTotal)
            totalPriceWidget.packagebreakdown.viewmodel.newDataObservable.onNext(response.packageDetails)
            var packageTotalPrice = response.packageDetails.pricing
            totalPriceWidget.viewModel.total.onNext(Money(BigDecimal(packageTotalPrice.packageTotal.amount.toDouble()),
                    packageTotalPrice.packageTotal.currencyCode))
            totalPriceWidget.viewModel.savings.onNext(Money(BigDecimal(packageTotalPrice.savings.amount.toDouble()),
                    packageTotalPrice.savings.currencyCode))
            toggleCheckoutButton(true)
        }

    }

    override fun doCreateTrip() {
        createTripViewModel.performCreateTrip.onNext(Unit)
    }

    @Subscribe fun onUserLoggedIn(event: Events.LoggedInSuccessful) {
        onLoginSuccess()
    }

    override fun lineOfBusiness() : LineOfBusiness {
        return LineOfBusiness.PACKAGES
    }
}