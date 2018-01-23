package com.expedia.bookings.rail.presenter

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.presenter.BaseErrorPresenter
import com.expedia.vm.rail.RailErrorViewModel

class RailErrorPresenter(context: Context, attr: AttributeSet) : BaseErrorPresenter(context, attr) {

    override fun getViewModel(): RailErrorViewModel {
        return viewmodel as RailErrorViewModel
    }

    override fun setupStatusBar() {
        // Do nothing
    }
}
