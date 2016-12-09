package com.expedia.bookings.presenter.lx

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.vm.BaseCheckoutViewModel
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import com.expedia.vm.BaseCreateTripViewModel
import com.expedia.vm.PaymentViewModel

class LxCheckoutPresenterV2(context: Context, attr: AttributeSet?) : BaseCheckoutPresenter(context, attr) {

    override fun injectComponents() {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateDbTravelers() {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun trackShowSlideToPurchase() {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun makeCheckoutViewModel(): BaseCheckoutViewModel {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun makeCreateTripViewModel(): BaseCreateTripViewModel {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCheckoutViewModel(): BaseCheckoutViewModel {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCreateTripViewModel(): BaseCreateTripViewModel {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCostSummaryBreakdownViewModel(): BaseCostSummaryBreakdownViewModel {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setupCreateTripViewModel(vm: BaseCreateTripViewModel) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showMainTravelerMinimumAgeMessaging(): Boolean {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPaymentWidgetViewModel(): PaymentViewModel {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fireCheckoutOverviewTracking(createTripResponse: TripResponse) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}