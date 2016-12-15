package com.expedia.bookings.presenter.lx

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.otto.Events
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.vm.AbstractCheckoutViewModel
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import com.expedia.vm.BaseCreateTripViewModel
import com.expedia.vm.lx.LXCheckoutViewModel
import com.expedia.vm.lx.LXCostSummaryViewModel
import com.expedia.vm.lx.LXCreateTripViewModel
import com.squareup.otto.Subscribe
import javax.inject.Inject

class LxCheckoutPresenterV2(context: Context, attr: AttributeSet?) : BaseCheckoutPresenter(context, attr) {

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

    override fun getCostSummaryBreakdownViewModel(): BaseCostSummaryBreakdownViewModel {
        //TODO
        return LXCostSummaryViewModel(context)
    }

    override fun setupCreateTripViewModel(vm: BaseCreateTripViewModel) {
        //TODO
    }

    override fun showMainTravelerMinimumAgeMessaging(): Boolean {
        return false
    }

    override fun fireCheckoutOverviewTracking(createTripResponse: TripResponse) {
        //TODO
    }

    @Subscribe fun onUserLoggedIn(@Suppress("UNUSED_PARAMETER") event: Events.LoggedInSuccessful) {
        onLoginSuccess()
    }
    
}