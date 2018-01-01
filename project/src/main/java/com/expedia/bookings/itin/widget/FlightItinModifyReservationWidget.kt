package com.expedia.bookings.itin.widget

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.fragment.FlightItinModifyReservationDialog
import com.expedia.bookings.itin.vm.FlightItinModifyReservationViewModel
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable

class FlightItinModifyReservationWidget(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val changeReservationButton by bindView<Button>(R.id.change_reservation_button)
    val cancelReservationButton by bindView<Button>(R.id.cancel_reservation_button)
    val changeLearnMoreText by bindView<TextView>(R.id.change_reservation_learn_more)
    val cancelLearnMoreText by bindView<TextView>(R.id.cancel_reservation_learn_more)
    private val DIALOG_TAG = "MODIFY_RESERVATION"

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
        if (!isValidForChange(param) && !isValidForCancel(param)) {
            setUpDisabledView(changeReservationButton, changeLearnMoreText)
            setUpDisabledView(cancelReservationButton, cancelLearnMoreText)
            setUpChangeOrCancelLearnMoreClick(param.customerSupportNumber)
        } else {
            if (!isValidForChange(param)) {
                setUpDisabledView(changeReservationButton, changeLearnMoreText)
                setUpChangeLearnMoreClick(param.customerSupportNumber)
            } else if (!isValidForCancel(param)) {
                setUpDisabledView(cancelReservationButton, cancelLearnMoreText)
                setUpCancelLearnMoreClick(param.customerSupportNumber)
            }
        }
    }

    private fun isValidForCancel(param: FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams) =
            param.isCancellable || Strings.isNotEmpty(param.cancelReservationURL)

    private fun isValidForChange(param: FlightItinModifyReservationViewModel.FlightItinModifyReservationWidgetParams) =
            param.isChangeable || Strings.isNotEmpty(param.changeReservationURL)

    private fun setUpDisabledView(button: Button, learnMoreView: TextView) {
        button.alpha = 0.4f
        button.isEnabled = false
        learnMoreView.visibility = View.VISIBLE
        AccessibilityUtil.appendRoleContDesc(learnMoreView, learnMoreView.text.toString(), R.string.accessibility_cont_desc_role_button)
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

    private fun setUpCancelLearnMoreClick(customerSupportNumber: String) {
        val cancelContent = context.getString(R.string.itin_flight_modify_widget_cancel_reservation_dialog_text)
        showReservationDialog(cancelLearnMoreText, cancelContent, customerSupportNumber)
    }

    private fun setUpChangeLearnMoreClick(customerSupportNumber: String) {
        val changeContent = context.getString(R.string.itin_flight_modify_widget_change_reservation_dialog_text)
        showReservationDialog(changeLearnMoreText, changeContent, customerSupportNumber)
    }

    private fun setUpChangeOrCancelLearnMoreClick(customerSupportNumber: String) {
        val changeContent = context.getString(R.string.itin_flight_modify_widget_neither_changeable_nor_cancellable_reservation_dialog_text)
        showReservationDialog(cancelLearnMoreText, changeContent, customerSupportNumber)
        showReservationDialog(changeLearnMoreText, changeContent, customerSupportNumber)
    }

    private fun showReservationDialog(view: TextView, content: String, supportNumber: String) {
        val fragmentManager = (context as FragmentActivity).supportFragmentManager
        val dialog = FlightItinModifyReservationDialog.newInstance(content, supportNumber)
        view.setOnClickListener {
            dialog.show(fragmentManager, DIALOG_TAG)
        }
    }
}