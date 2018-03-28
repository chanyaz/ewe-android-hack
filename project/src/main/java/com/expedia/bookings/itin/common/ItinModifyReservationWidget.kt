package com.expedia.bookings.itin.common

import android.content.Context
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.fragment.ItinModifyReservationDialog
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable

class ItinModifyReservationWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val changeReservationButton by bindView<TextView>(R.id.change_reservation_button)
    val cancelReservationButton by bindView<TextView>(R.id.cancel_reservation_button)
    val changeLearnMoreText by bindView<TextView>(R.id.change_reservation_learn_more)
    val cancelLearnMoreText by bindView<TextView>(R.id.cancel_reservation_learn_more)
    private val DIALOG_TAG = "MODIFY_RESERVATION"

    init {
        View.inflate(context, R.layout.widget_flight_itin_modify_reservation, this)
        setUpViews()
    }

    var viewModel: ItinModifyReservationViewModel by notNullAndObservable { vm ->
        vm.changeReservationSubject.subscribe {
            setUpChangeWidget()
        }
        vm.cancelReservationSubject.subscribe {
            setUpCancelWidget()
        }
        vm.webViewIntentSubject.subscribe {
            val animation = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_up_partially, 0).toBundle()
            (context as AppCompatActivity).startActivityForResult(it, Constants.ITIN_WEBVIEW_REFRESH_ON_EXIT_CODE, animation)
        }
    }

    private fun setUpCancelWidget() {
        cancelLearnMoreText.visibility = View.GONE
        cancelReservationButton.isEnabled = true
        cancelReservationButton.alpha = 1f
        cancelReservationButton.setOnClickListener {
            viewModel.cancelTextViewClickSubject.onNext(Unit)
        }
    }

    private fun setUpChangeWidget() {
        changeLearnMoreText.visibility = View.GONE
        changeReservationButton.alpha = 1f
        changeReservationButton.isEnabled = true
        changeReservationButton.setOnClickListener {
            viewModel.changeTextViewClickSubject.onNext(Unit)
        }
    }

    private fun setUpViews() {
        AccessibilityUtil.appendRoleContDesc(cancelLearnMoreText, R.string.accessibility_cont_desc_role_button)
        AccessibilityUtil.appendRoleContDesc(changeLearnMoreText, R.string.accessibility_cont_desc_role_button)
        setUpMoreHelpListeners()
    }

    private fun setUpMoreHelpListeners() {
        val fragmentManager = (context as FragmentActivity).supportFragmentManager
        cancelLearnMoreText.setOnClickListener {
            val dialog = ItinModifyReservationDialog.newInstance(context.getString(viewModel.helpDialogRes)
                    , viewModel.customerSupportNumber, viewModel.itinType)
            dialog.show(fragmentManager, DIALOG_TAG)
            viewModel.cancelLearnMoreClickSubject.onNext(Unit)
        }
        changeLearnMoreText.setOnClickListener {
            val dialog = ItinModifyReservationDialog.newInstance(context.getString(viewModel.helpDialogRes)
                    , viewModel.customerSupportNumber, viewModel.itinType)
            dialog.show(fragmentManager, DIALOG_TAG)
            viewModel.changeLearnMoreClickSubject.onNext(Unit)
        }
    }
}
