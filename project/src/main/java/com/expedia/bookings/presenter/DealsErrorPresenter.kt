package com.expedia.bookings.presenter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.mia.vm.DealsErrorViewModel

class DealsErrorPresenter(context: Context, attr: AttributeSet?) : BaseErrorPresenter<DealsErrorViewModel>(context, attr) {

    init {
        standardToolbarContainer.visibility = View.INVISIBLE
        root.setPadding(0, 0, 0, 0)
    }

    override fun setupStatusBar() {
        // Do nothing
    }
}
