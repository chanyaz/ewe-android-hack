package com.expedia.bookings.presenter.rail

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.vm.BaseCheckoutViewModel
import com.expedia.vm.packages.BaseCreateTripViewModel
import com.expedia.vm.rail.RailCheckoutViewModel
import com.expedia.vm.rail.RailCreateTripViewModel

class RailCheckoutPresenter(context: Context, attrs: AttributeSet) : BaseCheckoutPresenter(context, attrs) {

    override fun lineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.RAIL
    }

    override fun updateTravelerPresenter() {
        travelerPresenter.visibility = View.VISIBLE
    }

    override fun trackShowSlideToPurchase() {
    }

    override fun trackShowBundleOverview() {
    }

    override fun setCheckoutViewModel(): BaseCheckoutViewModel {
        return RailCheckoutViewModel(context)
    }

    override fun setCreateTripViewModel(): RailCreateTripViewModel {
        return RailCreateTripViewModel()
    }

    override fun getCheckoutViewModel(): RailCheckoutViewModel {
        return ckoViewModel as RailCheckoutViewModel
    }

    override fun getCreateTripViewModel(): RailCreateTripViewModel {
        return tripViewModel as RailCreateTripViewModel
    }

    override fun setUpCreateTripViewModel(vm: BaseCreateTripViewModel) {
    }
}


