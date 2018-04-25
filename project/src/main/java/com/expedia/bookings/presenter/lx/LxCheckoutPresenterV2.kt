package com.expedia.bookings.presenter.lx

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.otto.Events
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.vm.AbstractCheckoutViewModel
import com.expedia.vm.BaseCreateTripViewModel
import com.expedia.vm.lx.LXCheckoutViewModel
import com.expedia.vm.lx.LXCreateTripViewModel
import com.expedia.vm.traveler.LXTravelersViewModel
import com.expedia.vm.traveler.TravelersViewModel
import com.squareup.otto.Subscribe
import javax.inject.Inject

class LxCheckoutPresenterV2(context: Context, attr: AttributeSet?) : BaseCheckoutPresenter(context, attr) {

    override fun trackCreateTripPriceChange(diffPercentage: Int) {
        //TODO
    }

    override fun trackCheckoutPriceChange(diffPercentage: Int) {
        //TODO
    }

    override fun handleCheckoutPriceChange(tripResponse: TripResponse) {
        //TODO
    }

    override fun onCreateTripResponse(tripResponse: TripResponse?) {
    }

    lateinit var lxCheckoutViewModel: LXCheckoutViewModel
        @Inject set

    lateinit var lxCreateTripViewModel: LXCreateTripViewModel
        @Inject set

    override fun injectComponents() {
        Ui.getApplication(context).lxComponent().inject(this)
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.LX
    }

    override fun updateDbTravelers() {
        //TODO
    }

    override fun trackShowSlideToPurchase() {
        //TODO
    }

    override fun makeCheckoutViewModel(): AbstractCheckoutViewModel {
        return lxCheckoutViewModel
    }

    override fun makeCreateTripViewModel(): BaseCreateTripViewModel {
        return lxCreateTripViewModel
    }

    override fun getCheckoutViewModel(): LXCheckoutViewModel {
        return ckoViewModel as LXCheckoutViewModel
    }

    override fun getCreateTripViewModel(): LXCreateTripViewModel {
        return tripViewModel as LXCreateTripViewModel
    }

    override fun setupCreateTripViewModel(vm: BaseCreateTripViewModel) {
        //TODO
    }

    override fun showMainTravelerMinimumAgeMessaging(): Boolean {
        return false
    }

    @Subscribe fun onUserLoggedIn(@Suppress("UNUSED_PARAMETER") event: Events.LoggedInSuccessful) {
        onLoginSuccess()
    }

    override fun createTravelersViewModel(): TravelersViewModel {
        return LXTravelersViewModel(context, getLineOfBusiness(), showMainTravelerMinimumAgeMessaging())
    }

    override fun getDefaultToTravelerTransition(): DefaultToTraveler {
        return object : DefaultToTraveler(LXTravelersPresenter::class.java) {
            override fun startTransition(forward: Boolean) {
                super.startTransition(forward)
                lxCheckoutViewModel.hideOverviewSummaryObservable.onNext(forward)
            }
        }
    }

    override val defaultToPayment = object : DefaultToPayment(this) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            lxCheckoutViewModel.hideOverviewSummaryObservable.onNext(forward)
        }
    }

    override fun back(): Boolean {
        if (currentState == BaseCheckoutPresenter.CheckoutDefault::class.java.name) {
            lxCheckoutViewModel.backToDetailsObservable.onNext(Unit)
            return false
        }
        return super.back()
    }
}
