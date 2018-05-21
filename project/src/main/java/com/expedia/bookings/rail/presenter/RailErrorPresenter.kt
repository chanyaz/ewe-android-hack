package com.expedia.bookings.rail.presenter

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.presenter.BaseErrorPresenter
import com.expedia.vm.rail.RailErrorViewModel

class RailErrorPresenter(context: Context, attr: AttributeSet) : BaseErrorPresenter<RailErrorViewModel>(context, attr) {

    override fun setupStatusBar() {
        // Do nothing
    }
}
