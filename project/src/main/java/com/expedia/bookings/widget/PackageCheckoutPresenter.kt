package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import android.view.ViewTreeObserver
import com.expedia.bookings.otto.Events
import com.expedia.bookings.tracking.PackagesTracking
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
            PackagesTracking().trackBundleOverviewPageLoad(response.packageDetails)
        }

    }

    override fun doCreateTrip() {
        createTripViewModel.performCreateTrip.onNext(Unit)
    }

    @Subscribe fun onUserLoggedIn(event: Events.LoggedInSuccessful) {
        onLoginSuccess()
    }

    init {
        globalLayoutListener = (ViewTreeObserver.OnGlobalLayoutListener {
            val decorView = paymentWidgetRootWindow.decorView
            val windowVisibleDisplayFrameRect = Rect()
            decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrameRect)
            var location = IntArray(2)
            scrollView?.getLocationOnScreen(location)
            val lp = scrollView.layoutParams
            val newHeight = windowVisibleDisplayFrameRect.bottom - windowVisibleDisplayFrameRect.top - toolbarHeight

            if (lp.height != newHeight) {
                lp.height = newHeight
                scrollView.layoutParams = lp
            }
        })

        travelerPresenter.expandedSubject.subscribe { expanded ->
            if (expanded) {
                boardingWarning.visibility= View.VISIBLE
                dropShadowView.visibility= View.GONE
            }
            else {
                boardingWarning.visibility= View.GONE
                dropShadowView.visibility= View.VISIBLE
            }
        }
    }

    override fun lineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.PACKAGES
    }

    override fun updateTravelerPresenter() {
        travelerPresenter.refreshAndShow(Db.getPackageParams())
    }
}