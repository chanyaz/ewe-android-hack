package com.expedia.bookings.presenter.rail

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView

class RailConfirmationPresenter(context: Context, attrs: AttributeSet?) : Presenter(context, attrs) {
    val container: ViewGroup by bindView(R.id.rail_confirmation_container)
    val confirmationCodeView: TextView by bindView(R.id.rail_confirmation_code)
    val toolbar: Toolbar by bindView(R.id.toolbar)

    init {
        View.inflate(context, R.layout.rail_confirmation_presenter, this)

        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.rail_primary_color)
            val statusBar = Ui.setUpStatusBar(context, toolbar, container, color)
            addView(statusBar)
        }
    }

    fun update(checkoutResponse: RailCheckoutResponse) {
        val confirmationPlaceHolderText = StringBuilder("Itin #: ").append(checkoutResponse.newTrip.itineraryNumber)
                .append(" recordLocator: " + checkoutResponse.newTrip.travelRecordLocator)
                .append(" tripId: " + checkoutResponse.newTrip.tripId).toString()
        confirmationCodeView.text = confirmationPlaceHolderText
    }

    override fun back(): Boolean {
        (context as AppCompatActivity).finish()
        NavUtils.goToItin(context)
        return true
    }
}