package com.expedia.bookings.itin.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.itin.vm.FlightItinModifyReservationViewModel
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable

class FlightItinModifyReservationWidget(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val changeReservationButton by bindView<Button>(R.id.change_reservation_button)
    val cancelReservationButton by bindView<Button>(R.id.cancel_reservation_button)

    init {
        View.inflate(context, R.layout.widget_flight_itin_modify_reservation, this)
    }

    var viewModel: FlightItinModifyReservationViewModel by notNullAndObservable { vm ->
        vm.modifyReservationSubject.subscribe { params ->
            setUpWidget(params)
            setUpListeners(params)
        }
    }

    private fun setUpWidget(param: FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams) {
        if (!param.isChangeable || Strings.isEmpty(param.changeReservationURL)) {
            changeReservationButton.alpha = 0.4f
            changeReservationButton.isEnabled = false
        }

        if (!param.isCancellable || Strings.isEmpty(param.cancelReservationURL)) {
            cancelReservationButton.alpha = 0.4f
            cancelReservationButton.isEnabled = false
        }
    }

    private fun setUpListeners(param: FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams) {
        onChangeReservationClick(param)
        onCancelReservationClick(param)
    }

    private fun onChangeReservationClick(param: FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams) = changeReservationButton.setOnClickListener {
        context.startActivity(buildWebViewIntent(R.string.itin_flight_modify_widget_change_reservation_text, param.changeReservationURL).intent)
    }

    private fun onCancelReservationClick(param: FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams) = cancelReservationButton.setOnClickListener {
        context.startActivity(buildWebViewIntent(R.string.itin_flight_modify_widget_cancel_reservation_text, param.cancelReservationURL).intent)
    }

    private fun buildWebViewIntent(title: Int, url: String): WebViewActivity.IntentBuilder {
        val builder: WebViewActivity.IntentBuilder = WebViewActivity.IntentBuilder(context)
        builder.setTitle(title)
        builder.setUrl(url)
        builder.setInjectExpediaCookies(true)
        builder.setAllowMobileRedirects(false)
        return builder
    }
}