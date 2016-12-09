package com.expedia.bookings.presenter.lx

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.presenter.BaseSingleScreenOverviewPresenter

class LXOverviewPresenter(context: Context, attrs: AttributeSet) : BaseSingleScreenOverviewPresenter(context, attrs) {

    override fun inflate() {
        View.inflate(context, R.layout.lx_overview, this)
    }

    override fun onBook(cvv: String?) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun setIsGroundTransport(isGroundTransport: Boolean) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}