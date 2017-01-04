package com.expedia.bookings.presenter.lx

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.lx.LXCreateTripResponseV2
import com.expedia.bookings.presenter.BaseSingleScreenOverviewPresenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.LXCheckoutSummaryWidget
import com.expedia.vm.lx.LXCreateTripViewModel

class LXOverviewPresenter(context: Context, attrs: AttributeSet) : BaseSingleScreenOverviewPresenter(context, attrs) {

    override fun trackCheckoutPageLoad() {
        //TODO
    }

    override fun trackPaymentCIDLoad() {
        //TODO
    }

    val lxSummaryWidget by lazy {
        val summaryContainer = findViewById(R.id.summary_container) as FrameLayout
        val summaryView = Ui.inflate<LXCheckoutSummaryWidget>(R.layout.lx_checkout_summary_widget, summaryContainer, false)
        summaryContainer.addView(summaryView)
        summaryView
    }

    override fun inflate() {
        View.inflate(context, R.layout.lx_overview, this)
        checkoutPresenter.getCreateTripViewModel().createTripResponseObservable.subscribe{ response ->
            val createTripResponse = response as LXCreateTripResponseV2
            val tripTotalPrice = if (createTripResponse.hasPriceChange()) createTripResponse.newTotalPrice else (checkoutPresenter.getCreateTripViewModel() as LXCreateTripViewModel).lxState.latestTotalPrice()
            checkoutPresenter.travelersPresenter.viewModel.refresh()
            lxSummaryWidget.bind(createTripResponse.originalPrice, tripTotalPrice, createTripResponse.lxProduct.lxBookableItems.get(0))
        }
    }

    fun makeNewCreateTripCall(){
        checkoutPresenter.getCreateTripViewModel().performCreateTrip.onNext(Unit)
    }
}